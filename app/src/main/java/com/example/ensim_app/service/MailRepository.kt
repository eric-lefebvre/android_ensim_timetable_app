package com.example.ensim_app.service

import android.util.Log
import com.example.ensim_app.service.model.Mail
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.tasks.await
import java.util.Date

object MailRepository {

    private const val TAG = "ENSIM-MAIL"
    private const val MAILS = "mails"

    private val auth get() = Firebase.auth
    private val db get() = Firebase.firestore

    suspend fun ensureSignedIn(): String {
        val current = auth.currentUser
        if (current != null) return current.uid
        val result = auth.signInAnonymously().await()
        val user = result.user ?: error("signInAnonymously a renvoyé un user null")
        // Force le refresh du token pour que le SDK Firestore voie l'auth state
        // avant la première requête (évite une race PERMISSION_DENIED au cold start).
        user.getIdToken(true).await()
        Log.d(TAG, "Signed in anonymously, uid=${user.uid}")
        return user.uid
    }

    suspend fun sendMail(
        fromUid: String,
        fromResourceId: Int,
        fromDisplayName: String,
        toResourceId: Int,
        toDisplayName: String,
        subject: String,
        body: String,
    ) {
        val data = mapOf(
            "fromUid" to fromUid,
            "fromResourceId" to fromResourceId,
            "fromDisplayName" to fromDisplayName,
            "toResourceId" to toResourceId,
            "toDisplayName" to toDisplayName,
            "subject" to subject,
            "body" to body,
            "sentAt" to FieldValue.serverTimestamp(),
            "read" to false,
        )
        db.collection(MAILS).add(data).await()
    }

    fun inboxStream(myResourceId: Int): Flow<List<Mail>> = mailStream(
        tag = "inboxStream",
        field = "toResourceId",
        resourceId = myResourceId,
    )

    fun sentStream(myResourceId: Int): Flow<List<Mail>> = mailStream(
        tag = "sentStream",
        field = "fromResourceId",
        resourceId = myResourceId,
    )

    private fun mailStream(tag: String, field: String, resourceId: Int): Flow<List<Mail>> =
        callbackFlow {
            val registration = db.collection(MAILS)
                .whereEqualTo(field, resourceId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "$tag snapshot error", error)
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    val mails = snapshot?.documents.orEmpty().mapNotNull { doc ->
                        doc.toObject(Mail::class.java)?.copy(id = doc.id)
                    }.sortedByDescending { it.sentAt?.toDate()?.time ?: 0L }
                    trySend(mails)
                }
            awaitClose { registration.remove() }
        }.catch { e ->
            Log.e(TAG, "$tag flow error", e)
            emit(emptyList())
        }

    suspend fun markRead(mailId: String) {
        db.collection(MAILS).document(mailId).update("read", true).await()
    }

    private data class SeedMail(
        val fromResourceId: Int,
        val fromDisplayName: String,
        val subject: String,
        val body: String,
        val hoursAgo: Long,
        val read: Boolean,
    )

    private val demoSeeds: List<SeedMail> = listOf(
        SeedMail(9354, "BOMBOY FOMBONNE Faustine",
            "TD info-com de demain",
            "Salut Eric, t'as fait l'exo 3 du TD ? Je galère un peu sur la récursion, " +
                "si t'as 5 min pour m'expliquer demain matin avant le cours ça m'aiderait grave.",
            hoursAgo = 1, read = false),
        SeedMail(9659, "KONTIEBO Bénéwindé Franck Ange Michael",
            "Soirée de promo vendredi",
            "Yo, on se retrouve au bar du centre vers 21h, comme d'habitude. " +
                "Confirme-moi rapidement pour que je réserve la bonne taille de table.",
            hoursAgo = 4, read = false),
        SeedMail(9716, "LAURENCE Alexiane",
            "Notes du cours de système",
            "Hello ! Voici mes notes du dernier cours, j'ai rajouté les schémas du tableau " +
                "à la fin. N'hésite pas si t'as des questions sur la partie ordonnancement.",
            hoursAgo = 9, read = false),
        SeedMail(9550, "GOUGET Louise",
            "Re: projet Android",
            "OK super, je m'occupe de la partie Firebase de mon côté. Tu peux me passer " +
                "ton displayName CAS exact pour qu'on harmonise les tests d'intégration ?",
            hoursAgo = 22, read = true),
        SeedMail(4153, "HASSEN Adam",
            "Question sur le DM de réseaux",
            "Salut, dans la question 4 du DM, il faut donner la trace complète des paquets " +
                "ou juste la séquence d'arrivée côté récepteur ? Le sujet est pas clair.",
            hoursAgo = 27, read = true),
        SeedMail(9355, "BRAULT Gabriel",
            "Covoit lundi matin",
            "Hey, je peux te déposer à 7h30 lundi si ça te dit, je pars du centre-ville. " +
                "Réponds-moi avant dimanche soir histoire que je m'organise.",
            hoursAgo = 31, read = true),
        SeedMail(9758, "BOUJGHOUAD Aya",
            "Ressources crypto",
            "Tiens, j'ai trouvé un cours de Stanford super clair sur la crypto symétrique, " +
                "je te file le lien et les slides en TD demain.",
            hoursAgo = 46, read = true),
        SeedMail(9342, "MOQUEREAU Clément",
            "Tournoi de baby-foot",
            "On organise un mini-tournoi à la cafet vendredi midi entre les 4A. " +
                "Inscris-toi avant jeudi soir si tu veux jouer, on monte les équipes par paires.",
            hoursAgo = 53, read = true),
        SeedMail(9719, "NANA TALOM Franck Jaures",
            "Stage été",
            "Hey, t'as eu des nouvelles de ta candidature chez Capgemini ? " +
                "Moi toujours rien depuis trois semaines, je commence à perdre espoir.",
            hoursAgo = 68, read = true),
        SeedMail(9722, "ROULET Florentin",
            "Repas asso info",
            "Cotisation de 5€ pour le repas de fin de semestre. " +
                "Faut récupérer ça avant mercredi pour que je réserve le resto à temps.",
            hoursAgo = 74, read = true),
        SeedMail(9742, "TRANSON Paul",
            "Bug rendu OpenGL",
            "T'as eu l'erreur d'allocation OpenGL toi aussi sur le projet graphique ? " +
                "Sur ma machine ça plante systématiquement au 3e frame, j'ai essayé sur 2 GPU différents.",
            hoursAgo = 91, read = true),
        SeedMail(9728, "VOINSON Maxine",
            "Polycopié de probas",
            "Le prof a déposé le poly à jour sur Moodle ce matin. " +
                "Fais-le passer aux autres si tu en croises, il y a 2-3 corrections importantes au chapitre 4.",
            hoursAgo = 109, read = true),
    )

    suspend fun seedDemoInbox(
        targetResourceId: Int,
        targetDisplayName: String,
        sentByUid: String,
    ): Int {
        val now = System.currentTimeMillis()
        val batch = db.batch()
        demoSeeds.forEach { seed ->
            val sentAt = Timestamp(Date(now - seed.hoursAgo * 3_600_000L))
            val docRef = db.collection(MAILS).document()
            val data = mapOf(
                "fromUid" to sentByUid,
                "fromResourceId" to seed.fromResourceId,
                "fromDisplayName" to seed.fromDisplayName,
                "toResourceId" to targetResourceId,
                "toDisplayName" to targetDisplayName,
                "subject" to seed.subject,
                "body" to seed.body,
                "sentAt" to sentAt,
                "read" to seed.read,
            )
            batch.set(docRef, data)
        }
        batch.commit().await()
        Log.d(TAG, "Seeded ${demoSeeds.size} demo mails for resourceId=$targetResourceId")
        return demoSeeds.size
    }
}
