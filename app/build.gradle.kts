plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}
android {
    namespace = "com.example.ararena"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.example.ararena"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "0.1"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true }
}
dependencies {
    implementation("io.github.sceneview:arsceneview:2.2.1")
    implementation("com.google.android.gms:play-services-nearby:19.3.0")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.material3:material3:1.3.1")
}
