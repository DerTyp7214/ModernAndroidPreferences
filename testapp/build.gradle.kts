plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "de.Maxr1998.modernpreferences.example"
    compileSdk = 31
    defaultConfig {
        minSdk = 21
        targetSdk = 31
        versionCode = 1
        versionName = "1.0"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    buildFeatures {
        viewBinding = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_15
        targetCompatibility = JavaVersion.VERSION_15
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_15.toString()
    }
    lint {
        abortOnError = false
    }
}

dependencies {
    // Core
    implementation(libs.androidx.core)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity)

    // UI
    implementation(libs.androidx.recyclerview)
    implementation(libs.google.material)

    // Preferences library
    implementation(project(":library"))

    // Lifecycle
    implementation(libs.bundles.androidx.lifecycle)

    // Debug
    debugImplementation(libs.leakcanary)
}