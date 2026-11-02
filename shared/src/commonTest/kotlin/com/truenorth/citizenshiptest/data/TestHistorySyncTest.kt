package com.truenorth.citizenshiptest.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestHistorySyncTest {

    // --- attemptDocumentId ---

    @Test
    fun documentIdIsStableForTheSameAttempt() {
        assertEquals(
            attemptDocumentId(1_700_000_000_000L, 15, 20),
            attemptDocumentId(1_700_000_000_000L, 15, 20)
        )
    }

    @Test
    fun documentIdDiffersWhenAnyFieldDiffers() {
        val base = attemptDocumentId(1_700_000_000_000L, 15, 20)
        val ids = setOf(
            base,
            attemptDocumentId(1_700_000_000_001L, 15, 20),
            attemptDocumentId(1_700_000_000_000L, 16, 20),
            attemptDocumentId(1_700_000_000_000L, 15, 25)
        )
        assertEquals(4, ids.size, "each field must contribute to the id")
    }

    /**
     * The accepted collision, pinned by a test so it stays a decision rather
     * than becoming a surprise. Two attempts finishing in the same millisecond
     * with identical scores share an id and collapse to one document. A
     * practice test runs for minutes, so this cannot realistically happen on
     * one device, and the alternative of a random id would duplicate on every
     * retry.
     */
    @Test
    fun sameMillisecondAndSameScoreCollide() {
        assertEquals(
            attemptDocumentId(1_700_000_000_000L, 15, 20),
            attemptDocumentId(1_700_000_000_000L, 15, 20)
        )
    }

    @Test
    fun documentIdContainsNoCharactersFirestoreRejects() {
        val id = attemptDocumentId(1_700_000_000_000L, 15, 20)
        // Firestore document ids cannot contain a forward slash, cannot be "."
        // or "..", and cannot be empty.
        assertTrue(id.isNotEmpty())
        assertTrue(!id.contains("/"))
        assertTrue(id != "." && id != "..")
    }

    // --- attemptsMissingLocally ---

    private fun remote(timestampMillis: Long, correct: Int, total: Int) = RemoteAttempt(
        documentId = attemptDocumentId(timestampMillis, correct, total),
        timestampMillis = timestampMillis,
        correctCount = correct,
        totalQuestions = total,
        categoryStats = emptyList()
    )

    @Test
    fun cloudOnlyAttemptsAreAllRestored() {
        val cloud = listOf(remote(1L, 10, 20), remote(2L, 15, 20))
        assertEquals(cloud, attemptsMissingLocally(cloud, emptySet()))
    }

    @Test
    fun attemptsAlreadyLocalAreSkipped() {
        val shared = remote(1L, 10, 20)
        val cloudOnly = remote(2L, 15, 20)
        val result = attemptsMissingLocally(listOf(shared, cloudOnly), setOf(shared.documentId))
        assertEquals(listOf(cloudOnly), result)
    }

    @Test
    fun localOnlyAttemptsAreLeftAlone() {
        // Nothing to restore, and critically nothing that would delete the
        // local row. Merge, never replace.
        val result = attemptsMissingLocally(emptyList(), setOf("1-10-20"))
        assertTrue(result.isEmpty())
    }

    @Test
    fun overlappingSetsProduceTheUnion() {
        val onlyLocal = remote(1L, 10, 20)
        val both = remote(2L, 12, 20)
        val onlyCloud = remote(3L, 18, 20)

        val toInsert = attemptsMissingLocally(
            remote = listOf(both, onlyCloud),
            localDocumentIds = setOf(onlyLocal.documentId, both.documentId)
        )

        val finalIds = setOf(onlyLocal.documentId, both.documentId) + toInsert.map { it.documentId }
        assertEquals(
            setOf(onlyLocal.documentId, both.documentId, onlyCloud.documentId),
            finalIds
        )
        assertEquals(listOf(onlyCloud), toInsert, "only the cloud-only attempt is inserted")
    }

    @Test
    fun restoreIsIdempotent() {
        val cloud = listOf(remote(1L, 10, 20), remote(2L, 15, 20))

        val firstPass = attemptsMissingLocally(cloud, emptySet())
        assertEquals(2, firstPass.size)

        // After the first pass those rows exist locally, so their derived ids
        // are present and the second pass inserts nothing.
        val afterFirstPass = firstPass.map { it.documentId }.toSet()
        assertTrue(attemptsMissingLocally(cloud, afterFirstPass).isEmpty())
    }

    // --- zipCategoryStats ---

    @Test
    fun categoryStatsPairUpByPosition() {
        val stats = zipCategoryStats(
            names = listOf("History", "Rights"),
            correct = listOf(3, 4),
            total = listOf(5, 5)
        )
        assertEquals(
            listOf(
                RemoteCategoryStat("History", 3, 5),
                RemoteCategoryStat("Rights", 4, 5)
            ),
            stats
        )
    }

    @Test
    fun categoryStatsTruncateToTheShortestList() {
        // A half-written document degrades to fewer categories rather than
        // throwing and blocking the whole restore.
        val stats = zipCategoryStats(
            names = listOf("History", "Rights", "Geography"),
            correct = listOf(3, 4),
            total = listOf(5)
        )
        assertEquals(listOf(RemoteCategoryStat("History", 3, 5)), stats)
    }

    @Test
    fun categoryStatsHandleEmptyLists() {
        assertTrue(zipCategoryStats(emptyList(), emptyList(), emptyList()).isEmpty())
    }

    // --- missedQuestionIdsToRestore ---

    @Test
    fun missedQuestionsRestoreOntoAFreshDevice() {
        assertEquals(
            listOf(4, 17, 92),
            missedQuestionIdsToRestore(listOf(4, 17, 92), emptySet())
        )
    }

    @Test
    fun localQuestionResultsWinOverTheCloudSet() {
        // The device knows the user answered 17 since that snapshot was taken.
        // Restoring it as missed would push a mastered question back into Smart
        // Review, which is the opposite of the feature's job.
        assertEquals(
            listOf(4, 92),
            missedQuestionIdsToRestore(listOf(4, 17, 92), localQuestionIds = setOf(17))
        )
    }

    @Test
    fun missedQuestionRestoreDeduplicates() {
        assertEquals(
            listOf(4, 17),
            missedQuestionIdsToRestore(listOf(4, 17, 4, 17, 4), emptySet())
        )
    }

    @Test
    fun missedQuestionRestoreHandlesAnEmptyCloudSet() {
        assertTrue(missedQuestionIdsToRestore(emptyList(), setOf(1, 2, 3)).isEmpty())
    }

    @Test
    fun missedQuestionRestoreIsIdempotent() {
        val cloud = listOf(4, 17, 92)
        val first = missedQuestionIdsToRestore(cloud, emptySet())
        assertEquals(3, first.size)
        // Those rows now exist locally, so a second restore writes nothing.
        assertTrue(missedQuestionIdsToRestore(cloud, first.toSet()).isEmpty())
    }
}
