package com.truenorth.citizenshiptest.data

import com.truenorth.citizenshiptest.data.db.CategoryStatEntity
import com.truenorth.citizenshiptest.data.db.QuestionResultEntity
import com.truenorth.citizenshiptest.data.db.SyncStateEntity
import com.truenorth.citizenshiptest.data.db.TestAttemptDao
import com.truenorth.citizenshiptest.data.db.TestAttemptEntity
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlin.time.Clock

/**
 * Mirrors local test history to Firestore and restores it on a new device.
 *
 * Room stays the source of truth for every read in the app. This class only
 * pushes copies up and pulls missing ones down, which is why it can be added
 * and removed without touching a single screen. See
 * `docs/plan-cloud-sync-test-history.md` for why mirroring was chosen over
 * moving history into Firestore outright.
 *
 * Nothing here is allowed to break a practice test. Every method is safe to
 * call repeatedly, and callers run them fire-and-forget: a failure leaves rows
 * unsynced and the next run retries them. That is the entire error-handling
 * strategy and it is sufficient, because the local write already succeeded
 * before any of this is reached.
 */
class TestHistorySyncRepository(
    private val uid: String,
    private val dao: TestAttemptDao
) {
    private val userDoc = Firebase.firestore.collection("users").document(uid)
    private val attemptsCollection = userDoc.collection(ATTEMPTS_COLLECTION)

    /**
     * Whether a restore has completed successfully for this user in this
     * session.
     *
     * This guards [syncMissedQuestionIds], which replaces the cloud array
     * outright. Without the guard: new device, cold start, restore fails on a
     * dead network, user backgrounds and resumes, the resume path pushes the
     * empty local missed-set over the real one and the Smart Review queue is
     * gone from the account for good. Uploading attempts needs no such guard,
     * because it only ever adds documents.
     */
    private var hasRestored = false

    private fun nowMillis(): Long = Clock.System.now().toEpochMilliseconds()

    /**
     * The whole sync for a freshly signed-in user, in the one order that is
     * safe.
     *
     * Ownership first: if the local database belongs to a different account,
     * clear it before anything can upload it into this one. Then restore, so a
     * new device has history before the user looks at it. Then upload, which is
     * what carries an existing user's local-only history into the cloud for the
     * first time. Then the missed-question set, which is derived from whatever
     * the first three steps left in Room.
     */
    suspend fun syncForSignedInUser() {
        claimLocalDatabase()
        restoreFromCloud()
        uploadPending()
        syncMissedQuestionIds()
    }

    /**
     * Wipes local history if it belongs to someone else, then records this
     * account as its owner.
     *
     * Without this, two people sharing a device would merge into one history,
     * and the merge would be permanent because the second person's upload would
     * write the first person's attempts into their cloud account. See
     * [SyncStateEntity].
     *
     * The cost, stated plainly: if the first person had attempts that never
     * reached the cloud, this destroys them. They cannot be rescued first,
     * because by the time this runs the session belongs to the second person
     * and the rules correctly refuse to let them write to the first person's
     * document. The alternative is worse in kind rather than degree: leaving
     * the rows in place pushes one person's history permanently into another
     * person's account. Local loss of data that failed to sync beats silent
     * cross-account contamination.
     */
    private suspend fun claimLocalDatabase() {
        val previousOwner = dao.getOwnerUid()
        if (previousOwner != null && previousOwner != uid) {
            dao.deleteAllCategoryStats()
            dao.deleteAllAttempts()
            dao.deleteAllQuestionResults()
        }
        dao.setSyncState(SyncStateEntity(ownerUid = uid))
    }

    /**
     * Pushes every attempt that has not been mirrored yet, oldest first.
     *
     * Each attempt is one document write, and it is marked synced only once
     * that write returns. An exception part way through leaves the rest
     * unsynced, which is the correct outcome: the next call picks up where this
     * one stopped.
     */
    suspend fun uploadPending() {
        for (attempt in dao.getUnsyncedAttempts()) {
            val stats = dao.getCategoryStatsForAttempt(attempt.id)
            val documentId = attemptDocumentId(
                timestampMillis = attempt.timestampMillis,
                correctCount = attempt.correctCount,
                totalQuestions = attempt.totalQuestions
            )
            attemptsCollection.document(documentId).set(
                mapOf(
                    "timestampMillis" to attempt.timestampMillis,
                    "correctCount" to attempt.correctCount,
                    "totalQuestions" to attempt.totalQuestions,
                    // Category stats are denormalised onto the attempt rather
                    // than given a subcollection of their own: they are only
                    // ever read together with their attempt, they are bounded at
                    // six entries, and this keeps both the read count and the
                    // write count per attempt at exactly one.
                    "categoryNames" to stats.map { it.category },
                    "categoryCorrect" to stats.map { it.correctCount },
                    "categoryTotal" to stats.map { it.totalCount }
                ),
                merge = true
            )
            dao.markAttemptSynced(attempt.id, nowMillis())
        }
    }

    /**
     * Pulls the attempts subcollection and inserts anything missing locally.
     *
     * Restored rows are marked synced immediately, since they came from the
     * cloud and must not be uploaded back.
     */
    suspend fun restoreFromCloud() {
        val remote = attemptsCollection.get().documents.mapNotNull { document ->
            val timestampMillis = document.get<Long?>("timestampMillis") ?: return@mapNotNull null
            val correctCount = document.get<Long?>("correctCount")?.toInt() ?: return@mapNotNull null
            val totalQuestions = document.get<Long?>("totalQuestions")?.toInt() ?: return@mapNotNull null
            RemoteAttempt(
                documentId = document.id,
                timestampMillis = timestampMillis,
                correctCount = correctCount,
                totalQuestions = totalQuestions,
                categoryStats = zipCategoryStats(
                    names = document.get<List<String>?>("categoryNames") ?: emptyList(),
                    correct = (document.get<List<Long>?>("categoryCorrect") ?: emptyList()).map { it.toInt() },
                    total = (document.get<List<Long>?>("categoryTotal") ?: emptyList()).map { it.toInt() }
                )
            )
        }

        // Derived from the local rows rather than stored, so this works on a
        // database that predates sync entirely.
        val localDocumentIds = dao.getAllAttempts()
            .map { attemptDocumentId(it.timestampMillis, it.correctCount, it.totalQuestions) }
            .toSet()

        val syncedAt = nowMillis()
        for (attempt in attemptsMissingLocally(remote, localDocumentIds)) {
            val localAttemptId = dao.insertAttempt(
                TestAttemptEntity(
                    timestampMillis = attempt.timestampMillis,
                    correctCount = attempt.correctCount,
                    totalQuestions = attempt.totalQuestions,
                    syncedAtMillis = syncedAt
                )
            )
            if (attempt.categoryStats.isNotEmpty()) {
                dao.insertCategoryStats(
                    attempt.categoryStats.map { stat ->
                        CategoryStatEntity(
                            attemptId = localAttemptId,
                            category = stat.category,
                            correctCount = stat.correctCount,
                            totalCount = stat.totalCount
                        )
                    }
                )
            }
        }

        restoreMissedQuestions()
        hasRestored = true
    }

    /**
     * Rebuilds the Smart Review queue from the mirrored missed-question set.
     *
     * `question_results` holds only the most recent outcome per question, and
     * attempt documents carry no per-question results, so a fresh device cannot
     * reconstruct this from attempts. The restored rows get the restore time
     * rather than the original answer time, which nothing reads.
     */
    private suspend fun restoreMissedQuestions() {
        val cloudMissedIds = (userDoc.get().get<List<Long>?>(MISSED_QUESTION_IDS_FIELD) ?: emptyList())
            .map { it.toInt() }
        if (cloudMissedIds.isEmpty()) return

        val toRestore = missedQuestionIdsToRestore(cloudMissedIds, dao.getAllQuestionResultIds().toSet())
        if (toRestore.isEmpty()) return

        val now = nowMillis()
        dao.upsertQuestionResults(
            toRestore.map { questionId ->
                QuestionResultEntity(questionId = questionId, wasCorrect = false, lastAttemptMillis = now)
            }
        )
    }

    /**
     * Writes the current missed-question set to the user document.
     *
     * A whole-array replace, deliberately not `arrayUnion`. Answering a
     * previously missed question correctly has to *remove* it, and a union
     * would make the set grow monotonically until Smart Review served
     * questions the user had already mastered.
     *
     * Bounded at 589, so it lives on the user document rather than in a
     * subcollection: it is read on every Home render and wants to arrive with
     * the rest of the user document instead of as a second query.
     */
    suspend fun syncMissedQuestionIds() {
        // See [hasRestored]. Writing before a restore has landed would replace
        // the real set with whatever this device happens to have, which on a
        // new device is nothing.
        if (!hasRestored) return
        userDoc.set(
            mapOf(
                MISSED_QUESTION_IDS_FIELD to dao.getMissedQuestionIds().distinct().sorted().map { it.toLong() },
                HISTORY_SCHEMA_VERSION_FIELD to HISTORY_SCHEMA_VERSION
            ),
            merge = true
        )
    }

    /**
     * Deletes the attempts subcollection.
     *
     * Firestore does not cascade: deleting `users/{uid}` leaves
     * every document under `users/{uid}/attempts` in place, reachable and billable, with no
     * document above it to hint that it exists. Account deletion has to clear
     * this explicitly or the app would keep the test history of someone who
     * asked to be forgotten.
     *
     * Must run while the user is still signed in, because the security rules
     * authorise it by uid.
     */
    suspend fun deleteAllCloudHistory() {
        for (document in attemptsCollection.get().documents) {
            attemptsCollection.document(document.id).delete()
        }
    }
}
