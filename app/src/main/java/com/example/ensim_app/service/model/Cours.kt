package com.example.ensim_app.service.model

import java.util.Date

data class Cours(
    val uid: String,
    val matiere: String,
    val salles: List<Salle>,
    val debut: Date,
    val fin: Date,
    val professeurs: List<Professeur>,
    val classe: String,
    val type: TypeSeance
)
