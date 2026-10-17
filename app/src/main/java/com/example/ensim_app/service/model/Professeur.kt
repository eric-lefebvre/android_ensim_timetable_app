package com.example.ensim_app.service.model

data class Professeur(
    val nom: String,
    val prenom: String
) {
    override fun toString() = "$prenom $nom"
}
