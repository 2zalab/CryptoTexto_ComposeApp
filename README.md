# CryptoTexto

## 📱 Description
CryptoTexto est une application Android moderne conçue pour le cryptage et le décryptage sécurisé de messages. Développée avec Kotlin et Jetpack Compose, elle offre une interface utilisateur élégante et une expérience utilisateur fluide tout en maintenant un niveau élevé de sécurité.

## 🔑 Caractéristiques Principales

### 🔒 Cryptage de Messages
- **Algorithmes Supportés:**
  - César (décalage simple)
  - Vigenère (substitution polyalphabétique)
  - Affine (fonction mathématique)
  - Transposition (réarrangement des caractères)
- **Validation des Clés:**
  - Vérification de la validité des clés pour chaque algorithme
  - Gestion des cas spéciaux (clé Affine avec validation de coprimarité)
  - Formatage automatique des entrées

### 🔓 Décryptage de Messages
- Interface intuitive pour le décryptage
- Support de tous les algorithmes de cryptage
- Validation automatique des entrées
- Gestion des erreurs et feedback utilisateur

### 🗝️ Gestion des Clés Secrètes
- **Stockage Sécurisé:**
  - Utilisation de DataStore pour la persistance
  - Chiffrement des données stockées
  - Protection par authentification biométrique
- **Fonctionnalités:**
  - Ajout, modification et suppression de clés
  - Description personnalisée pour chaque clé
  - Catégorisation par algorithme
  - Export sécurisé (PDF/TXT)

### 🔐 Sécurité
- **Authentification Biométrique:**
  - Protection de l'accès aux clés secrètes
  - Support des empreintes digitales
  - Gestion des erreurs d'authentification
- **Sécurité des Données:**
  - Fonctionnement 100% hors ligne
  - Pas de stockage cloud
  - Effacement sécurisé des données sensibles

### 🎨 Interface Utilisateur
- **Design Material 3:**
  - Thème dynamique avec Material You
  - Support du mode sombre
  - Animations fluides
  - Composants Material Design modernes
- **Navigation:**
  - Menu latéral (Drawer)
  - Navigation entre les écrans
  - Gestion de l'état de l'application
- **Composants Personnalisés:**
  - Cartes pour les clés secrètes
  - Dialogues d'édition
  - Champs de saisie adaptés
  - Boutons d'action contextuels

## 💻 Technologies Utilisées

### 📚 Framework et Langage
- **Kotlin** 1.9.0
  - Coroutines pour l'asynchrone
  - Flow pour la gestion des états
  - Extensions Kotlin

### 🎯 Jetpack Compose
- **Version:** 1.5.0
- **Composants:**
  - Material 3
  - Navigation Compose
  - ViewModel Compose
  - Accompanist libraries

### 🏗️ Architecture
- **MVVM (Model-View-ViewModel)**
  - ViewModels pour la logique métier
  - États UI immutables
  - Gestion des événements UI

### 📱 Composants Android
- **Biometric:** Pour l'authentification
- **DataStore:** Pour le stockage sécurisé
- **WorkManager:** Pour les tâches en arrière-plan
- **Room Database:** Pour la persistence locale (optionnel)

## 🛠️ Installation et Configuration

### 📋 Prérequis
- Android Studio Hedgehog (2023.1.1) ou plus récent
- JDK 17
- Android SDK 34
- Gradle 8.0

### 🔧 Configuration
```gradle
android {
    compileSdk 34
    
    defaultConfig {
        minSdk 24
        targetSdk 34
        versionCode 1
        versionName "1.0"
    }

    buildFeatures {
        compose true
    }

    composeOptions {
        kotlinCompilerExtensionVersion "1.5.4"
    }
}
```

### 📦 Dépendances Principales
```gradle
dependencies {
    // Compose
    implementation "androidx.compose.ui:ui:$compose_version"
    implementation "androidx.compose.material3:material3:$material3_version"
    
    // Navigation
    implementation "androidx.navigation:navigation-compose:2.7.6"
    
    // Biometric
    implementation "androidx.biometric:biometric:1.2.0-alpha05"
    
    // DataStore
    implementation "androidx.datastore:datastore-preferences:1.0.0"
    
    // PDF Generation
    implementation "com.itextpdf:itextpdf:5.5.13.3"
}
```

## 🚀 Fonctionnalités à Venir

### Version 1.1
- [ ] Support multi-langues
- [ ] Sauvegarde/Restauration des clés
- [ ] Mode hors ligne amélioré
- [ ] Nouvelles animations

### Version 1.2
- [ ] Nouveaux algorithmes de cryptage
- [ ] Interface tablette optimisée
- [ ] Widgets personnalisables
- [ ] Statistiques d'utilisation

## 👥 Contribution
1. Fork du projet
2. Création d'une branche (`git checkout -b feature/AmazingFeature`)
3. Commit des changements (`git commit -m 'Add some AmazingFeature'`)
4. Push vers la branche (`git push origin feature/AmazingFeature`)
5. Création d'une Pull Request

## 📄 Licence
Ce projet est sous licence MIT. Voir le fichier [LICENSE](LICENSE) pour plus de détails.

## 📞 Contact et Support
- **Site Web:** https://2zalab.com
- **Email:** contact@2zalab.com
- **Issues:** https://github.com/2zalab/CryptoTexto_ComposeApp/issues

## 🙏 Remerciements
- L'équipe Android et Jetpack Compose
- La communauté open source
- Tous les contributeurs

---

📱 **Développé avec ❤️ par [2zalab](https://github.com/2zalab)**
