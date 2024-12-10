@file:Suppress("UnstableApiUsage")

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
}

android {
    namespace = "com.shoots.shoots_ui"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.shoots.shoots_ui"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            buildConfigField("String", "API_URL", "\"http://10.0.0.164:3000/\"")
            buildConfigField("String", "GOOGLE_CLIENT_ID", "\"697607574955-h622i8vloo6ldr0r15vahvu4iandgvve.apps.googleusercontent.com\"")
        }
        release {
            isMinifyEnabled = false
            buildConfigField("String", "API_URL", "\"https://shoots-api.onrender.com/\"")
            buildConfigField("String", "GOOGLE_CLIENT_ID", "\"697607574955-h622i8vloo6ldr0r15vahvu4iandgvve.apps.googleusercontent.com\"")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }

        debug {}
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions { jvmTarget = "1.8" }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions { kotlinCompilerExtensionVersion = "1.5.2" }
    packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }
    buildToolsVersion = "34.0.0"
    buildFeatures { viewBinding = true }
}

dependencies {
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.camera:camera-camera2:1.4.0-rc01")
    implementation("androidx.camera:camera-lifecycle:1.4.0-rc01")
    implementation("androidx.camera:camera-view:1.4.0-rc01")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    implementation("com.github.bumptech.glide:compose:1.0.0-beta01")
    implementation(libs.androidx.lifecycle.runtime.compose.android)
    implementation(libs.androidx.material3.android)

    // Compose
    val composeBom = platform("androidx.compose:compose-bom:2024.02.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.compose.material:material")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.runtime:runtime-livedata")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.material3:material3-window-size-class")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Room
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    implementation("androidx.room:room-paging:$roomVersion")
    testImplementation("androidx.room:room-testing:$roomVersion")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.hamcrest:hamcrest-all:1.3")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")

    androidTestImplementation("junit:junit:4.13.2")
    androidTestImplementation("org.mockito:mockito-core:3.4.6")
    androidTestImplementation("androidx.navigation:navigation-testing:2.7.7")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    // Google Sign In
    implementation("com.google.android.gms:play-services-auth:20.7.0")

    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
}