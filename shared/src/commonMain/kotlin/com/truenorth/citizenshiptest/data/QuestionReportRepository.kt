package com.truenorth.citizenshiptest.data

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.FieldValue
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class QuestionReportRepository(private val uid: String) {
    private val firestore = Firebase.firestore
    private val userDocRef = firestore.collection("users").document(uid)
    private val reportsCollection = firestore.collection("questionReports")

    val reportedQuestionIds: Flow<Set<Int>> = userDocRef.snapshots.map { snapshot ->
        (snapshot.get<List<Long>?>("reportedQuestionIds") ?: emptyList())
            .map { it.toInt() }
            .toSet()
    }

    // Batched so the new report doc and the user's reportedQuestionIds flag land
    // atomically - two independent writes here could otherwise race the same way
    // the onboarding test-date save once did.
    suspend fun submitReport(questionId: Int, reason: String, note: String?) {
        val batch = firestore.batch()
        val reportDocRef = reportsCollection.document
        batch.set(
            reportDocRef,
            mapOf(
                "questionId" to questionId,
                "userId" to uid,
                "reason" to reason,
                "note" to note?.takeIf { it.isNotBlank() },
                "createdAt" to FieldValue.serverTimestamp
            )
        )
        batch.set(
            userDocRef,
            mapOf("reportedQuestionIds" to FieldValue.arrayUnion(questionId.toLong())),
            merge = true
        )
        batch.commit()
    }
}
