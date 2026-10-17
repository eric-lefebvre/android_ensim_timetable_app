package com.example.ensim_app.service

import android.util.Log
import com.example.ensim_app.service.model.Cours
import com.example.ensim_app.service.model.RoomScheduleDto
import com.example.ensim_app.service.model.toCours
import com.example.ensim_app.service.model.toEventDto
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

object RoomScheduleRepository {

    private const val TAG = "RoomScheduleRepo"
    private const val COLLECTION = "rooms"
    private const val CACHE_TTL_MS = 60L * 60L * 1000L

    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val auth by lazy { FirebaseAuth.getInstance() }

    suspend fun getSchedule(roomName: String): List<Cours> = withContext(Dispatchers.IO) {
        val resourceId = PlanningSource.roomByName(roomName)?.resourceId
            ?: error("Salle inconnue: $roomName")

        ensureSignedIn()

        val docRef = firestore.collection(COLLECTION).document(roomName)
        val snapshot = docRef.get().await()
        val cached = snapshot.toObject(RoomScheduleDto::class.java)

        val fresh = cached?.let {
            System.currentTimeMillis() - it.lastFetchedAt.toDate().time < CACHE_TTL_MS
        } == true

        if (fresh && cached != null) {
            Log.d(TAG, "Cache HIT pour $roomName (${cached.events.size} events)")
            return@withContext cached.events.map { it.toCours() }
        }

        Log.d(TAG, "Cache MISS pour $roomName, refetch depuis l'API univ")
        val ics = IcsService.fetchIcs(PlanningSource.icsUrl(resourceId))
        val cours = IcsParser.parse(ics)

        val dto = RoomScheduleDto(
            name = roomName,
            resourceId = resourceId,
            lastFetchedAt = Timestamp.now(),
            events = cours.map { it.toEventDto() },
        )
        docRef.set(dto).await()
        Log.d(TAG, "Cache écrit pour $roomName (${dto.events.size} events)")

        cours
    }

    private suspend fun ensureSignedIn() {
        if (auth.currentUser != null) return
        auth.signInAnonymously().await()
        Log.d(TAG, "Auth anonyme OK (uid=${auth.currentUser?.uid})")
    }
}
