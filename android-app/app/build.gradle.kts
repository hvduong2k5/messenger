plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.midterm.team12345"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.midterm.team12345"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    viewBinding {
        enable = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Retrofit & OkHttp
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp.logging)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Lifecycle
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.lifecycle.runtime.ktx)

    // Security
    implementation(libs.security.crypto)

    // Image loading
    implementation("com.github.bumptech.glide:glide:4.15.1")

    // CircleImageView
    implementation("de.hdodenhof:circleimageview:3.1.0")

    // SwipeRefresh
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.2.0")

    // MQTT
    implementation(libs.mqtt.paho)
    implementation("androidx.localbroadcastmanager:localbroadcastmanager:1.1.0")

    // Bean Validation API (provides javax.validation annotations at compile time)
    implementation("javax.validation:validation-api:2.0.1.Final")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
