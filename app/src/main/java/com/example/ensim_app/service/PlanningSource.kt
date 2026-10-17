package com.example.ensim_app.service

import java.text.Normalizer

enum class Software(val displayName: String) {
    UNITY("Unity"),
    BLENDER("Blender"),
    SOLIDWORKS("Solidworks"),
    COMSOL("ComSol"),
}

data class RoomInfo(
    val name: String,
    val resourceId: Int,
    val hasComputers: Boolean,
    val softwares: Set<Software>,
) {
    init {
        require(hasComputers || softwares.isEmpty()) {
            "Salle $name : impossible d'avoir des logiciels sans ordinateur"
        }
    }
}

data class Student(val resourceId: Int, val displayName: String)

object PlanningSource {

    val rooms: List<RoomInfo> = listOf(
        RoomInfo("I01", 2379, hasComputers = true, softwares = setOf(Software.COMSOL, Software.SOLIDWORKS)),
        RoomInfo("I02", 2378, hasComputers = true, softwares = setOf(Software.COMSOL, Software.SOLIDWORKS)),
        RoomInfo("P26", 2398, hasComputers = true, softwares = setOf(Software.BLENDER, Software.UNITY)),
        RoomInfo("TD2", 2377, hasComputers = true, softwares = setOf(Software.SOLIDWORKS)),
        RoomInfo("TD3", 2362, hasComputers = true, softwares = setOf(Software.SOLIDWORKS)),
        RoomInfo("TD4", 2375, hasComputers = true, softwares = setOf(Software.SOLIDWORKS)),
    )

    // Liste extraite manuellement depuis l'API GWT-RPC de planning.univ-lemans.fr (2026).
    // À mettre à jour à chaque rentrée.
    val students4AInfo: List<Student> = listOf(
        Student(3776, "ACHIBAT Ismail"),
        Student(4625, "BEL HADJ SALAH Mohamed Aziz"),
        Student(9346, "BETCHEM NGON Warren Samir"),
        Student(9354, "BOMBOY FOMBONNE Faustine"),
        Student(9758, "BOUJGHOUAD Aya"),
        Student(9274, "BOURDAS-ROUGEAULT Gaspard"),
        Student(9355, "BRAULT Gabriel"),
        Student(9444, "BRIEAU-ATIENCIA CAMPANA Eric Nicolas"),
        Student(9502, "DIOUF Adja Ndoumbé"),
        Student(4593, "EL-MOUJAHID Omar"),
        Student(9509, "ESSOMBA JABE Michael Marie"),
        Student(9759, "FALL Boubacar"),
        Student(9550, "GOUGET Louise"),
        Student(9596, "HAMDY Maya Tamer Alaaeldine Hassan"),
        Student(4153, "HASSEN Adam"),
        Student(9632, "JERBI Selim"),
        Student(9659, "KONTIEBO Bénéwindé Franck Ange Michael"),
        Student(9681, "KOUAM Nelson"),
        Student(9690, "LABBE Emmy"),
        Student(9716, "LAURENCE Alexiane"),
        Student(9717, "LE COUAZER Cesar"),
        Student(9324, "LEFEBVRE Eric"),
        Student(4624, "MARGHICH Bilal"),
        Student(9760, "MARTY Dylan"),
        Student(4300, "MHADHBI Mohamed Ali"),
        Student(9342, "MOQUEREAU Clément"),
        Student(4735, "MORINE Hanaa"),
        Student(9718, "MOURADIAN Gabriel"),
        Student(9719, "NANA TALOM Franck Jaures"),
        Student(9720, "NGUENANG YOUKAP Arsène Igor"),
        Student(9757, "OLIVIER Matéo"),
        Student(4602, "OUBAHAYBOU Nouhayla"),
        Student(9721, "PASCAL Titouan"),
        Student(4353, "RACHEDI Taha"),
        Student(9722, "ROULET Florentin"),
        Student(9761, "SARKISSIAN Enzo"),
        Student(9723, "SECCI Giulia"),
        Student(9724, "SEGUY HOUEL Melville"),
        Student(9725, "TCHINDA TAPA Joel Dylan"),
        Student(9742, "TRANSON Paul"),
        Student(9726, "VENANCE Eliott"),
        Student(9727, "VILLEDIEU Charline"),
        Student(9728, "VOINSON Maxine"),
        Student(9729, "YONKOUA NGNOBA Ins Nawel"),
        Student(4514, "ZAIME Salma"),
        Student(4521, "ZORMATI Hend"),
    )

    fun roomByName(name: String): RoomInfo? = rooms.find { it.name == name }

    fun searchStudents(query: String, max: Int = 8): List<Student> {
        if (query.isBlank()) return emptyList()
        val q = query.normalizeForSearch()
        return students4AInfo
            .filter { it.displayName.normalizeForSearch().contains(q) }
            .take(max)
    }

    fun findStudentById(resourceId: Int): Student? =
        students4AInfo.firstOrNull { it.resourceId == resourceId }

    fun matchCasIdentity(identity: CasIdentity): Student? {
        val candidates = listOfNotNull(
            identity.displayName,
            listOfNotNull(identity.givenName, identity.surname)
                .joinToString(" ")
                .takeIf { it.isNotBlank() },
            identity.mail?.substringBefore('@')?.replace('.', ' '),
            identity.username.replace('.', ' '),
        )
        for (candidate in candidates) {
            val tokens = candidate.normalizeForSearch().split(' ', '.', '-')
                .filter { it.isNotEmpty() }
                .toSet()
            if (tokens.isEmpty()) continue
            students4AInfo.firstOrNull { student ->
                val studentTokens = student.displayName.normalizeForSearch()
                    .split(' ', '-')
                    .filter { it.isNotEmpty() }
                    .toSet()
                studentTokens == tokens
            }?.let { return it }
        }
        return null
    }

    fun icsUrl(resourceId: Int): String =
        "https://planning.univ-lemans.fr/jsp/custom/modules/plannings/anonymous_cal.jsp" +
            "?resources=$resourceId&projectId=8&calType=ical&nbWeeks=44"

    private fun String.normalizeForSearch(): String =
        Normalizer.normalize(this, Normalizer.Form.NFD)
            .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
            .lowercase()
}
