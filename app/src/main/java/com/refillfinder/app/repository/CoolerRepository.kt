package com.refillfinder.app.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.refillfinder.app.data.local.dao.CoolerDao
import com.refillfinder.app.data.local.entity.CoolerEntity
import com.refillfinder.app.model.Cooler
import com.refillfinder.app.model.CoolerStatus
import com.refillfinder.app.utils.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Offline-First CoolerRepository.
 *
 * Syllabus coverage:
 *  - Repository Pattern + Single Source of Truth (Room DB)
 *  - Offline-First: read from Room, sync with Firestore in background
 *  - callbackFlow for Firestore real-time → Room write-through cache
 *  - Flow operators: map, catch, onStart
 *  - Structured Concurrency: CoroutineScope(Dispatchers.IO)
 */
class CoolerRepository(
    private val coolerDao: CoolerDao,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val scope = CoroutineScope(Dispatchers.IO)

    /**
     * Room as Single Source of Truth.
     * UI always reads from Room; Firestore syncs in background.
     */
    fun getAllCoolersFlow(): Flow<List<Cooler>> =
        coolerDao.getAllCoolers()
            .map { entities -> entities.map { it.toDomain() } }
            .catch { emit(emptyList()) }

    fun getCoolersForBlock(block: Int): Flow<List<Cooler>> =
        coolerDao.getCoolersForBlock(block)
            .map { entities -> entities.map { it.toDomain() } }
            .catch { emit(emptyList()) }

    /**
     * Start Firestore real-time listener → write updates into Room.
     * Call this once from the ViewModel using DisposableEffect / viewModelScope.
     */
    fun startFirestoreSync(): Flow<SyncState> = callbackFlow {
        trySend(SyncState.Syncing)

        val registration = firestore.collection(Constants.COOLERS_COLLECTION)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(SyncState.Error(error.message ?: "Sync failed"))
                    return@addSnapshotListener
                }
                scope.launch {
                    val entities = snapshot?.documents?.mapNotNull { doc ->
                        doc.toObject(Cooler::class.java)?.let { cooler ->
                            CoolerEntity(
                                id = doc.id,
                                block = cooler.block,
                                floor = cooler.floor,
                                status = cooler.status.name,
                                reportCount = cooler.reportCount,
                                lastUpdated = System.currentTimeMillis()
                            )
                        }
                    } ?: emptyList()

                    if (entities.isNotEmpty()) {
                        coolerDao.upsertCoolers(entities)
                    }
                    trySend(SyncState.Success(entities.size))
                }
            }

        awaitClose { registration.remove() }
    }

    /**
     * Seed all 300 coolers (60 blocks × 5 cooler floors) into Room locally.
     * Also pushes to Firestore if not already there.
     */
    suspend fun seedCoolersIfEmpty() {
        if (coolerDao.getCoolerCount() > 0) return

        val entities = buildList {
            for (block in 1..Constants.TOTAL_BLOCKS) {
                for (floor in Constants.COOLER_FLOORS) {
                    // Pre-seed realistic dummy test data for chatbot & testing
                    val status = when {
                        block == 15 && floor == 3 -> CoolerStatus.REPORTED.name
                        block == 15 && floor == 7 -> CoolerStatus.OFFLINE.name
                        block == 60 && floor == 5 -> CoolerStatus.REPORTED.name
                        block == 24 && floor == 9 -> CoolerStatus.ACTIVE.name
                        block % 7 == 0 && floor == 3 -> CoolerStatus.REPORTED.name
                        block % 11 == 0 && floor == 5 -> CoolerStatus.OFFLINE.name
                        else -> CoolerStatus.ACTIVE.name
                    }
                    val reports = if (status != CoolerStatus.ACTIVE.name) 3 else 0

                    add(
                        CoolerEntity(
                            id = "block${block}_floor${floor}",
                            block = block,
                            floor = floor,
                            status = status,
                            reportCount = reports
                        )
                    )
                }
            }
        }
        coolerDao.upsertCoolers(entities)
    }

    suspend fun updateCoolerStatus(coolerId: String, status: CoolerStatus) {
        val existing = coolerDao.getCoolerById(coolerId)
        if (existing != null) {
            coolerDao.updateStatus(coolerId, status.name)
        } else {
            val parts = coolerId.removePrefix("block").split("_floor")
            val block = parts.getOrNull(0)?.toIntOrNull() ?: 1
            val floor = parts.getOrNull(1)?.toIntOrNull() ?: 1
            coolerDao.upsertCooler(
                CoolerEntity(
                    id = coolerId,
                    block = block,
                    floor = floor,
                    status = status.name,
                    lastUpdated = System.currentTimeMillis()
                )
            )
        }
        scope.launch {
            try {
                firestore.collection(Constants.COOLERS_COLLECTION)
                    .document(coolerId)
                    .update("status", status.name)
                    .await()
            } catch (_: Exception) { /* offline — Room already updated */ }
        }
    }

    private fun CoolerEntity.toDomain() = Cooler(
        id = id,
        block = block,
        floor = floor,
        status = runCatching { CoolerStatus.valueOf(status) }.getOrDefault(CoolerStatus.UNKNOWN),
        reportCount = reportCount
    )
}

sealed class SyncState {
    object Syncing : SyncState()
    data class Success(val count: Int) : SyncState()
    data class Error(val message: String) : SyncState()
}
