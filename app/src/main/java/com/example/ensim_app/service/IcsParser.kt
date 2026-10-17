package com.example.ensim_app.service

import com.example.ensim_app.service.model.Cours
import com.example.ensim_app.service.model.Professeur
import com.example.ensim_app.service.model.Salle
import com.example.ensim_app.service.model.TypeSeance
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object IcsParser {

    fun parse(icsContent: String): List<Cours> {
        val result = mutableListOf<Cours>()
        val lines = unfoldLines(icsContent)

        var inEvent = false
        val props = mutableMapOf<String, String>()

        for (line in lines) {
            when {
                line == "BEGIN:VEVENT" -> {
                    inEvent = true
                    props.clear()
                }
                line == "END:VEVENT" && inEvent -> {
                    parseCours(props)?.let { result.add(it) }
                    inEvent = false
                }
                inEvent -> {
                    val colonIdx = line.indexOf(':')
                    if (colonIdx > 0) {
                        val key = line.substring(0, colonIdx).split(';')[0].uppercase()
                        val value = line.substring(colonIdx + 1)
                        props[key] = value
                    }
                }
            }
        }

        return result
    }

    // Les lignes ICS peuvent être "foldées" (continuation avec espace/tab en début de ligne)
    private fun unfoldLines(content: String): List<String> =
        content
            .replace("\r\n ", "")
            .replace("\r\n\t", "")
            .replace("\n ", "")
            .replace("\n\t", "")
            .lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }

    private fun parseCours(props: Map<String, String>): Cours? {
        val uid = props["UID"] ?: return null
        val debut = parseIcsDate(props["DTSTART"] ?: return null) ?: return null
        val fin = parseIcsDate(props["DTEND"] ?: return null) ?: return null
        val summary = props["SUMMARY"]?.unescapeIcs() ?: ""
        val location = props["LOCATION"]?.unescapeIcs() ?: ""
        val description = props["DESCRIPTION"]?.unescapeIcs() ?: ""

        val (classe, professeursRaw) = parseDescription(description)

        return Cours(
            uid = uid,
            matiere = parseMatiere(summary),
            salles = parseSalles(location),
            debut = debut,
            fin = fin,
            professeurs = professeursRaw.map { parseProfesseur(it) },
            classe = classe,
            type = parseType(summary)
        )
    }

    private fun parseIcsDate(value: String): Date? {
        return try {
            val isUtc = value.endsWith("Z")
            val clean = value.trimEnd('Z')
            val format = SimpleDateFormat("yyyyMMdd'T'HHmmss", Locale.FRENCH).apply {
                timeZone = if (isUtc) TimeZone.getTimeZone("UTC")
                           else TimeZone.getTimeZone("Europe/Paris")
            }
            format.parse(clean)
        } catch (e: Exception) {
            null
        }
    }

    // Retourne (classe, listeProfesseursRaw)
    // - classe : ligne commençant par un chiffre (ex: "4IPS", "4TP IPS 2")
    // - professeurs : lignes entièrement en majuscules (ex: "CORDON PAUL", "HASSAN KAIS")
    // - élèves : lignes avec casse mixte → ignorées
    private fun parseDescription(description: String): Pair<String, List<String>> {
        val lines = description.split("\n")
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("(Exported") }
        val classe = lines.firstOrNull { it.firstOrNull()?.isDigit() == true } ?: ""
        val professeurs = lines.filter { isProfessorLine(it) }
        return Pair(classe, professeurs)
    }

    // Tout en majuscules (espaces et tirets exclus) et ne commence pas par un chiffre
    private fun isProfessorLine(line: String): Boolean {
        if (line.isBlank() || line.first().isDigit()) return false
        val clean = line.replace(" ", "").replace("-", "")
        return clean.isNotEmpty() && clean.all { it.isUpperCase() }
    }

    // "Interopérabilité TP IPS 2" → "Interopérabilité"
    // Prend tout ce qui est avant le premier token CM/TD/TP
    private fun parseMatiere(summary: String): String {
        val tokens = summary.split(" ")
        val typeIndex = tokens.indexOfFirst { it.uppercase() == "TP" || it.uppercase() == "TD" || it.uppercase() == "CM" }
        return if (typeIndex > 0) tokens.subList(0, typeIndex).joinToString(" ").trim()
               else summary.trim()
    }

    // "CORDON PAUL" → Professeur(nom="CORDON", prenom="PAUL")
    private fun parseProfesseur(raw: String): Professeur {
        val parts = raw.trim().split(" ", limit = 2)
        return Professeur(
            nom = parts[0],
            prenom = parts.getOrElse(1) { "" }
        )
    }

    // "C07 - labo de langues,C06 - labo de langues" → List<Salle>
    private fun parseSalles(location: String): List<Salle> {
        if (location.isBlank()) return emptyList()
        return location.split(",").map { it.trim() }.filter { it.isNotEmpty() }.map { parseSalle(it) }
    }

    private fun parseSalle(raw: String): Salle {
        return when {
            raw.contains(" - ") -> {
                val parts = raw.split(" - ", limit = 2)
                Salle(nom = parts[0].trim(), description = parts[1].trim())
            }
            raw.contains("(") -> {
                val nom = raw.substringBefore("(").trim()
                val desc = raw.substringAfter("(").trimEnd(')', ' ')
                Salle(nom = nom, description = desc)
            }
            else -> Salle(nom = raw.trim(), description = "")
        }
    }

    // Cherche CM / TD / TP / EXAM dans le SUMMARY
    private fun parseType(summary: String): TypeSeance {
        val tokens = summary.uppercase().split(" ")
        return when {
            tokens.contains("TP") -> TypeSeance.TP
            tokens.contains("TD") -> TypeSeance.TD
            tokens.contains("CM") -> TypeSeance.CM
            tokens.any { it.contains("EXAM") || it == "DS" || it == "CC" } -> TypeSeance.EXAM
            else -> TypeSeance.AUTRE
        }
    }

    private fun String.unescapeIcs(): String =
        replace("\\n", "\n")
            .replace("\\,", ",")
            .replace("\\;", ";")
            .replace("\\\\", "\\")
}
