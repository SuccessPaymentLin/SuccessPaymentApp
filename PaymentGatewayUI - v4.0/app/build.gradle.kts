plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
}

android {
    namespace = "com.example.paymentgatewayui"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.paymentgatewayui"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        viewBinding = true
    }

    // ✅ Fix for META-INF/DEPENDENCIES conflicts
    packaging {
        resources {
            excludes += setOf(
                "META-INF/DEPENDENCIES",
                "META-INF/DEPENDENCIES.txt",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt"
            )
        }
    }
}

dependencies {
    // Wallee SDK (if available via Maven Central)
    implementation("com.wallee:wallee-java-sdk:8.7.0")

    // AndroidX Core & AppCompat
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation ("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation ("com.google.android.material:material:1.12.0")

    // Android Lifecycle
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")

    // WorkManager for background tasks (fixes missing WorkManager & ConstraintProxy errors)
    implementation("androidx.work:work-runtime:2.9.0")

    // Room DB for MultiInstanceInvalidationService
    implementation("androidx.room:room-runtime:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    // Profile Installer (fixes ProfileInstallReceiver)
    implementation("androidx.profileinstaller:profileinstaller:1.3.1")

    // EmojiCompat if needed
    implementation("androidx.emoji2:emoji2:1.4.0")

    // Optional Logging
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    implementation ("com.android.volley:volley:1.2.1")
}