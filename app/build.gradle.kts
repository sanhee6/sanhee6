plugins {
    alias(libs.plugins.android.application)
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("kotlin-parcelize")
}

android {
    namespace = "com.example.time"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.time"
        minSdk = 24
        targetSdk = 35
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
}

dependencies {
    // 基础依赖
    implementation("androidx.core:core-ktx:1.13.1")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    
    // Kotlin 协程
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    
    // Jetpack 组件
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.6")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.6")
    
    // Room 数据库
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    
    // Gson
    implementation("com.google.code.gson:gson:2.10.1")
    
    // 图片加载
    implementation("com.github.bumptech.glide:glide:4.16.0")
    
    // 测试依赖
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}