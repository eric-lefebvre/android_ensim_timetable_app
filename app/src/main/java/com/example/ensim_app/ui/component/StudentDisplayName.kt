package com.example.ensim_app.ui.component

// "LEFEBVRE Eric" → ("Eric", "LEFEBVRE")
// "BEL HADJ SALAH Mohamed Aziz" → ("Mohamed Aziz", "BEL HADJ SALAH")
// Les tokens en tête entièrement en MAJUSCULES (tirets autorisés) forment le nom ;
// le reste est le prénom.
fun splitStudentName(displayName: String): Pair<String, String> {
    val tokens = displayName.split(" ")
    val nomEnd = tokens.indexOfFirst { token ->
        val clean = token.replace("-", "")
        clean.isEmpty() || !clean.all { it.isUpperCase() || !it.isLetter() } ||
            clean.none { it.isLetter() }
    }.let { if (it == -1) tokens.size else it }
    val nom = tokens.subList(0, nomEnd).joinToString(" ")
    val prenom = tokens.subList(nomEnd, tokens.size).joinToString(" ")
    return prenom to nom
}

// "LEFEBVRE Eric" → "Eric LEFEBVRE"
fun formatStudentName(displayName: String): String {
    val (prenom, nom) = splitStudentName(displayName)
    return "$prenom $nom".trim()
}
