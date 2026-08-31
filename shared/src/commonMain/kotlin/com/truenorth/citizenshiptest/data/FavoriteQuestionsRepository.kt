package com.truenorth.citizenshiptest.data

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.FieldValue
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FavoriteQuestionsRepository(uid: String) {
    private val docRef = Firebase.firestore.collection("users").document(uid)

    val favoriteQuestionIds: Flow<Set<Int>> = docRef.snapshots.map { snapshot ->
        (snapshot.get<List<Long>?>("favoriteQuestionIds") ?: emptyList())
            .map { it.toInt() }
            .toSet()
    }

    suspend fun setFavorite(questionId: Int, isFavorite: Boolean) {
        val update = if (isFavorite) {
            FieldValue.arrayUnion(questionId.toLong())
        } else {
            FieldValue.arrayRemove(questionId.toLong())
        }
        docRef.set(mapOf("favoriteQuestionIds" to update), merge = true)
    }
}
