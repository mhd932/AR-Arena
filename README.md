# AR Arena V1

Prototype Android Kotlin / ARCore SceneView / Nearby Connections.

## Compilation via GitHub Actions
Décompresser et envoyer **le contenu** de ce ZIP à la racine du dépôt GitHub, en conservant `.github/workflows/android.yml`.
Ouvrir **Actions > Build Android APK > Run workflow** puis télécharger l’artifact `AR-Arena-debug-apk` après réussite.

## Important
Projet non compilé ni testé dans cet environnement. Le repère partagé AR n’est pas réellement synchronisé entre téléphones : la précision des tirs n’est pas garantie. La gestion réseau, des PV et des permissions reste expérimentale.

## Installation et limites

Télécharger et décompresser l’artifact, puis installer `app-debug.apk` sur un téléphone compatible ARCore (Android 9 ou supérieur). Autoriser la caméra, la localisation et les appareils à proximité. Tester les connexions et les tirs sur deux appareils réels ; une compilation réussie ne valide pas le jeu multijoueur. Les connexions Nearby sont acceptées automatiquement : utiliser uniquement avec des appareils de confiance.

Source récupérée : `AR_Arena_GitHub_Actions.zip` dans Téléchargements, dont les copies numérotées sont identiques. Le ZIP original `ar-arena(1).zip` mentionné dans la demande n’était pas accessible dans le contexte transmis.
