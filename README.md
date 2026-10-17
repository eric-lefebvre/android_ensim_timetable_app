# ENSIM App — Emploi du temps ENSIM sur Android

Application Android native (Kotlin / Jetpack Compose) qui affiche l'emploi du temps **à jour** des étudiants de l'ENSIM (Le Mans Université), directement depuis le planning officiel de l'université.

## Fonctionnalités

- **Emploi du temps** : vue semaine (grille horaire), navigation par swipe d'une semaine à l'autre, sélecteur de date, détail d'un cours (matière, type de séance, salle(s), enseignant(s), groupe).
- **Connexion individuelle** : authentification via le **CAS de l'université** ; l'identité renvoyée est associée automatiquement à l'emploi du temps de l'étudiant.
- **Consultation d'autres calendriers** : possibilité d'afficher l'emploi du temps d'un autre étudiant de la promo.
- **Salles disponibles** : en quelques clics, savoir quelles salles sont libres maintenant ou à une date/heure choisie, avec filtres (salle informatique, logiciels installés : Unity, Blender, Solidworks, Comsol).
- **Messagerie interne** : boîte de réception / envoyés entre étudiants, en temps réel.
- **Bilingue** : français / anglais, changement de langue depuis le profil.

## Points techniques

### Stack

| Domaine | Choix |
|---|---|
| Langage / UI | Kotlin, Jetpack Compose, Material 3 |
| Navigation | Navigation Compose (`NavHost`, un `enum AppTab` par onglet) |
| Réseau | OkHttp |
| Backend | Firebase (Auth anonyme + Cloud Firestore) |
| Asynchrone | Coroutines / `Flow` |
| SDK | minSdk 24, targetSdk 36 |

### Source des données : le flux iCal de l'université

Chaque ressource du planning (étudiant ou salle) possède un identifiant. L'app récupère le flux **ICS** public :

```
https://planning.univ-lemans.fr/jsp/custom/modules/plannings/anonymous_cal.jsp
    ?resources=<resourceId>&projectId=8&calType=ical&nbWeeks=44
```

puis le parse avec un parser maison ([IcsParser.kt](app/src/main/java/com/example/ensim_app/service/IcsParser.kt)) : dépliage des lignes, conversion des dates UTC/locales, extraction de la matière, du type de séance, des salles et des enseignants.

La liste des étudiants et des salles (avec leurs `resourceId`) est définie dans [PlanningSource.kt](app/src/main/java/com/example/ensim_app/service/PlanningSource.kt) ; elle est à mettre à jour à chaque rentrée.

### Authentification CAS

[CasLoginActivity](app/src/main/java/com/example/ensim_app/ui/cas/CasLoginActivity.kt) ouvre la page de login CAS dans une `WebView`, intercepte la redirection contenant le ticket (`ST-...`), puis [CasService](app/src/main/java/com/example/ensim_app/service/CasService.kt) le valide via `/serviceValidate` et lit les attributs (nom, prénom, mail). L'identité est ensuite rapprochée d'un étudiant de la liste (comparaison normalisée, sans accents).

### Cache des salles dans Firestore

Pour la recherche de salles libres, interroger le planning de chaque salle à chaque recherche serait lent. [RoomScheduleRepository](app/src/main/java/com/example/ensim_app/service/RoomScheduleRepository.kt) utilise donc Firestore comme **cache partagé** (collection `rooms`, TTL d'une heure) : si le cache est frais on le lit, sinon on refetch l'ICS et on réécrit le document. Les salles sont interrogées en parallèle (`async` / `awaitAll`).

### Persistance locale

`SharedPreferences` ([UserProfileStore](app/src/main/java/com/example/ensim_app/service/UserProfileStore.kt)) mémorise le profil connecté et le dernier emploi du temps affiché, rouverts au lancement de l'app.

### Réseau et certificats

Les certificats racines HARICA et ISRG sont embarqués ([network_security_config.xml](app/src/main/res/xml/network_security_config.xml)) pour que les connexions vers `univ-lemans.fr` fonctionnent aussi sur les anciennes versions d'Android.

## Structure

```
app/src/main/java/com/example/ensim_app/
├── MainActivity.kt        # Point d'entrée, navigation, état global
├── domain/                # Resource (Loading / Success / Error)
├── service/               # CAS, ICS (fetch + parse), Firestore, préférences
│   └── model/             # Cours, Salle, Professeur, Mail, DTO...
├── ui/
│   ├── cas/               # Écran de connexion CAS (WebView)
│   ├── screen/            # Calendrier, Recherche, Messagerie, Profil
│   ├── component/         # Composants réutilisables (date picker, sélecteur d'étudiant)
│   ├── state/             # États Compose (chargement des cours, auth Firebase)
│   └── theme/             # Couleurs, typo, thème
└── util/                  # Dates, gestion de la langue
```

## Lancer le projet

1. Ouvrir le dossier dans Android Studio.
2. Ajouter un fichier `app/google-services.json` (projet Firebase avec Auth anonyme et Firestore activés) — il n'est pas versionné.
3. Déployer les règles [firestore.rules](firestore.rules) sur le projet Firebase.
4. Lancer sur un émulateur ou un appareil (Android 7.0+).
