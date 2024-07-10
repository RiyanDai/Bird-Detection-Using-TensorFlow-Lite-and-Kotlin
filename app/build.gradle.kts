plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    //    Library Room

    id("kotlin-parcelize")
    id("com.google.devtools.ksp")

}

android {
    namespace = "com.dicoding.birdie"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.dicoding.birdie"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

    buildFeatures {
        viewBinding = true
        mlModelBinding = true
        buildConfig = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.tensorflow.lite.gpu)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Tensorflow Lite libraries for machine learning tasks
    implementation("org.tensorflow:tensorflow-lite-support:0.4.4")
    implementation("org.tensorflow:tensorflow-lite-metadata:0.4.4")
    implementation("org.tensorflow:tensorflow-lite-task-vision:0.4.4")

// Glide library for image loading and caching
    implementation("com.github.bumptech.glide:glide:4.16.0")

// Lifecycle libraries for ViewModel and LiveData
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")

// Activity KTX library for Kotlin extensions
    implementation("androidx.activity:activity-ktx:1.9.0")

// CircleImageView library for displaying circular images
    implementation ("de.hdodenhof:circleimageview:3.1.0")

// Room libraries for database management
    implementation("androidx.room:room-runtime:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")

// Material Components library for modern Android UI
    implementation ("com.google.android.material:material:1.4.0")

// Dexter library for handling Android runtime permissions
    implementation ("com.karumi:dexter:6.2.3")

// Additional Coil library for image loading (duplicate, consider removing one)
    implementation("io.coil-kt:coil:1.4.0")

// TouchImageView library for pinch-to-zoom functionality
    implementation ("com.github.MikeOrtiz:TouchImageView:1.4.1")

// ImageSlideshow library for image carousel/slideshow
    implementation ("com.github.denzcoskun:ImageSlideshow:0.1.0")

// StickyScrollView library for sticky header in scroll view
    implementation ("com.github.amarjain07:StickyScrollView:1.0.3")


//fragments
    implementation("androidx.vectordrawable:vectordrawable:1.1.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.3.5")
    implementation("androidx.navigation:navigation-ui-ktx:2.3.5")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")

    implementation("com.airbnb.android:lottie:5.2.0")

//retrofit
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

}