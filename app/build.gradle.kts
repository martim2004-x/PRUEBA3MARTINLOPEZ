import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
    id("jacoco")
}

val keystoreProperties = Properties().apply {
    val file = rootProject.file("keystore.properties")
    if (file.exists()) load(file.inputStream())
}

android {
    namespace = "com.louaraya.macrame"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.louaraya.macrame"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = rootProject.file(keystoreProperties.getProperty("storeFile", "app/keystore/macrame-release.jks"))
            storePassword = keystoreProperties.getProperty("storePassword", "Macrame2026!")
            keyAlias = keystoreProperties.getProperty("keyAlias", "macrame")
            keyPassword = keystoreProperties.getProperty("keyPassword", "Macrame2026!")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Base
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.6")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")

    // Jetpack Compose + Material Design 3
    implementation(platform("androidx.compose:compose-bom:2024.09.02"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // Navegación
    implementation("androidx.navigation:navigation-compose:2.8.1")

    // Persistencia local (SQLite vía Room)
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // Imágenes (fotos tomadas con la cámara)
    implementation("io.coil-kt:coil-compose:2.7.0")

    // Recurso nativo 1: cámara (CameraX) + lectura de QR (ML Kit, modelo incluido en la app)
    implementation("androidx.camera:camera-core:1.3.4")
    implementation("androidx.camera:camera-camera2:1.3.4")
    implementation("androidx.camera:camera-lifecycle:1.3.4")
    implementation("androidx.camera:camera-view:1.3.4")
    implementation("com.google.mlkit:barcode-scanning:17.3.0")

    // Recurso nativo 2: ubicación + mapa (OpenStreetMap, no requiere API key)
    implementation("org.osmdroid:osmdroid-android:6.1.18")

    // Integración con microservicios propios (Retrofit) y API externa (Open-Meteo)
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Pruebas
    testImplementation("junit:junit:4.13.2")
    testImplementation("io.mockk:mockk:1.13.12")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
}

tasks.withType<Test>().configureEach {
    finalizedBy("jacocoTestReport")
}

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
    val mainSourceSet = android.sourceSets["main"]
    sourceDirectories.setFrom(files(mainSourceSet.java.srcDirs))
    val buildDir = layout.buildDirectory.get().asFile
    classDirectories.setFrom(
        files(
            fileTree("${buildDir}/tmp/kotlin-classes/debug") {
                exclude(
                    "**/R.class", "**/R$*.class", "**/BuildConfig.*", "**/Manifest*.*",
                    "**/*Compose*", "**/*Activity*", "**/databinding/**"
                )
            }
        )
    )
    executionData.setFrom(fileTree(buildDir) { include("jacoco/*.exec", "outputs/code_coverage/**/*.ec") })
}
