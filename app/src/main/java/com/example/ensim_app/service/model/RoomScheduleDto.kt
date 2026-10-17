package com.example.ensim_app.service.model

import com.google.firebase.Timestamp

data class RoomScheduleDto(
    val name: String = "",
    val resourceId: Int = 0,
    val lastFetchedAt: Timestamp = Timestamp.now(),
    val events: List<EventDto> = emptyList(),
)

data class EventDto(
    val uid: String = "",
    val start: Timestamp = Timestamp.now(),
    val end: Timestamp = Timestamp.now(),
    val matiere: String = "",
    val type: String = "AUTRE",
    val classe: String = "",
    val salles: List<SalleDto> = emptyList(),
    val professeurs: List<ProfesseurDto> = emptyList(),
)

data class SalleDto(
    val nom: String = "",
    val description: String = "",
)

data class ProfesseurDto(
    val nom: String = "",
    val prenom: String = "",
)

fun Cours.toEventDto(): EventDto = EventDto(
    uid = uid,
    start = Timestamp(debut),
    end = Timestamp(fin),
    matiere = matiere,
    type = type.name,
    classe = classe,
    salles = salles.map { SalleDto(nom = it.nom, description = it.description) },
    professeurs = professeurs.map { ProfesseurDto(nom = it.nom, prenom = it.prenom) },
)

fun EventDto.toCours(): Cours = Cours(
    uid = uid,
    matiere = matiere,
    salles = salles.map { Salle(nom = it.nom, description = it.description) },
    debut = start.toDate(),
    fin = end.toDate(),
    professeurs = professeurs.map { Professeur(nom = it.nom, prenom = it.prenom) },
    classe = classe,
    type = runCatching { TypeSeance.valueOf(type) }.getOrDefault(TypeSeance.AUTRE),
)
