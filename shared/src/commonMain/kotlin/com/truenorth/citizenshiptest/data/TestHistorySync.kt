package com.truenorth.citizenshiptest.data

/**
 * Bumped if the Firestore shape of test history ever changes, so a future
 * version can detect old data and migrate it rather than guess.
 */
const val HISTORY_SCHEMA_VERSION = 1

/** Subcollection under `users/{uid}` holding one document per test attempt. */
const val ATTEMPTS_COLLECTION = "attempts"

/** Field on the `users/{uid}` document holding the derived missed-question set. */
const val MISSED_QUESTION_IDS_FIELD = "missedQuestionIds"

/** Field on the `users/{uid}` document holding [HISTORY_SCHEMA_VERSION]. */
const val HISTORY_SCHEMA_VERSION_FIELD = "historySchemaVersion"

/**
 * Stable Firestore document id for one attempt, derived from the attempt's own
 * contents rather than allocated by Firestore.
 *
 * This is what makes mirroring safe to repeat. A device can upload an attempt,
 * lose connectivity before it records the success locally, and retry; with a
 * deterministic id the retry overwrites one document instead of creating a
 * second one that inflates the user's test count forever. It is also what makes
 * the two-device union merge work, since both devices derive the same id for
 * the same attempt.
 *
 * The local autoincrement row id is deliberately not part of this. Row ids are
 * assigned per device, so including one would give the same attempt a different
 * id on each device and defeat the merge.
 *
 * Accepted collision: two attempts finishing in the same millisecond with
 * identical scores collapse into one document. A practice test runs for
 * minutes, so this cannot realistically happen on one device, and the
 * alternative (a random id) would duplicate on every retry, which is a
 * certainty rather than a near-impossibility.
 */
fun attemptDocumentId(timestampMillis: Long, correctCount: Int, totalQuestions: Int): String =
    "$timestampMillis-$correctCount-$totalQuestions"

/** One attempt as it is stored in, and read back from, Firestore. */
data class RemoteAttempt(
    val documentId: String,
    val timestampMillis: Long,
    val correctCount: Int,
    val totalQuestions: Int,
    val categoryStats: List<RemoteCategoryStat>
)

data class RemoteCategoryStat(
    val category: String,
    val correctCount: Int,
    val totalCount: Int
)

/**
 * Cloud attempts that are not already present locally.
 *
 * Merge, never replace. Someone who has taken tests on two devices should end
 * up with the union of both, not with whichever device happened to sync last.
 */
fun attemptsMissingLocally(
    remote: List<RemoteAttempt>,
    localDocumentIds: Set<String>
): List<RemoteAttempt> = remote.filter { it.documentId !in localDocumentIds }

/**
 * Pairs the three parallel category lists from an attempt document back into
 * records, stopping at the shortest.
 *
 * Category stats are stored as three primitive lists rather than one list of
 * maps because `List<String>` and `List<Long>` are the only collection shapes
 * this codebase has actually proven against GitLive's Firestore encoder. A
 * nested-map shape would be tidier to read and would fail silently inside a
 * background sync if the encoder disagreed, which is the worst place to find
 * out.
 *
 * Truncating to the shortest list means a partially written document degrades
 * to fewer categories instead of throwing and blocking the whole restore.
 */
fun zipCategoryStats(
    names: List<String>,
    correct: List<Int>,
    total: List<Int>
): List<RemoteCategoryStat> {
    val count = minOf(names.size, correct.size, total.size)
    return (0 until count).map { i ->
        RemoteCategoryStat(category = names[i], correctCount = correct[i], totalCount = total[i])
    }
}

/**
 * Question ids from the cloud missed-set that should be written into
 * `question_results` on restore.
 *
 * Only ids with no local row at all. `question_results` holds the most recent
 * outcome per question, and attempt documents do not carry per-question
 * results, so a fresh device cannot rebuild the table from attempts alone; the
 * derived missed-set is mirrored separately for exactly this reason. But a
 * device that already has local results knows something the cloud snapshot does
 * not, so its rows win.
 */
fun missedQuestionIdsToRestore(
    cloudMissedIds: Collection<Int>,
    localQuestionIds: Set<Int>
): List<Int> = cloudMissedIds.distinct().filter { it !in localQuestionIds }
