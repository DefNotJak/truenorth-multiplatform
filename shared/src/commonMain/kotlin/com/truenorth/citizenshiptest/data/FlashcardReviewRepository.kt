package com.truenorth.citizenshiptest.data

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.FieldValue
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FlashcardReviewRepository(uid: String) {
    private val docRef = Firebase.firestore.collection("users").document(uid)

    val reviewedQuestionIds: Flow<Set<Int>> = docRef.snapshots.map { snapshot ->
        (snapshot.get<List<Long>?>("reviewedFlashcardIds") ?: emptyList())
            .map { it.toInt() }
            .toSet()
    }

    suspend fun markReviewed(questionId: Int) {
        docRef.set(mapOf("reviewedFlashcardIds" to FieldValue.arrayUnion(questionId.toLong())), merge = true)
    }
}
