plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.gms.google.services)
}


android {
    namespace = "com.example.motionlab"
    compileSdk = 35
    ndkVersion = "29.0.14033849"

    defaultConfig {
        applicationId = "com.example.motionlab"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    


    buildTypes {
        debug {
            isDebuggable = true
            isMinifyEnabled = false
            isShrinkResources = false
            // Enable detailed logging for Unity debugging
            buildConfigField("boolean", "ENABLE_UNITY_DEBUG", "true")
            buildConfigField("boolean", "ENABLE_CRASH_DETECTION", "true")
            buildConfigField("boolean", "ENABLE_PERFORMANCE_MONITORING", "true")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("boolean", "ENABLE_UNITY_DEBUG", "false")
            buildConfigField("boolean", "ENABLE_CRASH_DETECTION", "false")
            buildConfigField("boolean", "ENABLE_PERFORMANCE_MONITORING", "false")
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
        compose = true
        buildConfig = true
    }
    composeOptions{
        kotlinCompilerExtensionVersion = "1.5.8"
    }
    
    // Ensure Unity native libraries are extracted for compatibility
    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
    }
    
    lint {
        abortOnError = false
        disable += "MissingClass"
    }
}

dependencies {
    // Compose
    implementation("androidx.compose.material:material-icons-extended")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.androidx.foundation.android)
    implementation(libs.firebase.storage)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    // Hilt
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    ksp(libs.hilt.compiler)

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // Lifecycle + ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.1")

    // Media3 Player & UI
    implementation("androidx.media3:media3-exoplayer:1.3.1")
    implementation("androidx.media3:media3-ui:1.3.1")
    implementation("androidx.media3:media3-common:1.3.1")


    implementation("com.google.code.gson:gson:2.10.1")

    //Firebase for leaderboards and storage
    implementation(platform("com.google.firebase:firebase-bom:33.16.0"))
    implementation(libs.firebase.firestore)
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("com.google.firebase:firebase-appcheck-debug")

    implementation("io.coil-kt:coil-compose:2.5.0")
    
    // Unity Library
    implementation(project(":unityLibrary"))

    implementation("io.sanghun:compose-video:1.2.0")
    implementation("androidx.media3:media3-session:1.1.0")



    implementation(project(":unityLibrary"))
}
