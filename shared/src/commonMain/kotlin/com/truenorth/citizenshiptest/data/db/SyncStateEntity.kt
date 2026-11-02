package com.truenorth.citizenshiptest.data.db

import androidx.room3.Entity
import androidx.room3.PrimaryKey

/**
 * Single-row table recording which account the local history belongs to.
 *
 * The Room database is per-device, not per-account, and signing out does not
 * clear it. Before cloud sync existed that was only a display bug: a second
 * person signing in on the same device saw the first person's stats. With sync
 * it becomes much worse, because [com.truenorth.citizenshiptest.data.TestHistorySyncRepository.uploadPending]
 * would push the first person's attempts into the second person's account
 * permanently. Comparing the signed-in uid against [ownerUid] is what stops
 * that.
 *
 * Known gap: a device that already had history when the app updated to schema 2
 * has [ownerUid] null, so whoever signs in first claims it. Closing that would
 * need ownership information the old schema never recorded. It requires a shared
 * device and a single app update to line up, so it is accepted rather than
 * worked around.
 */
@Entity(tableName = "sync_state")
data class SyncStateEntity(
    @PrimaryKey val id: Int = SINGLETON_ID,
    val ownerUid: String?
) {
    companion object {
        const val SINGLETON_ID = 0
    }
}
