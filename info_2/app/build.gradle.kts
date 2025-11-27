plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.pack.info_2"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.pack.info_2"
        minSdk = 24
        targetSdk = 36
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
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation("androidx.core:core-ktx:1.9.0") // 버전은 다를 수 있습니다.
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0") // 버전은 다를 수 있습니다.
    implementation("androidx.constraintlayout:constraintlayout:2.1.4") // 버전은 다를 수 있습니다.
    implementation("androidx.cardview:cardview:1.0.0")

        // Retrofit 코어 라이브러리
    implementation("com.squareup.retrofit2:retrofit:2.9.0")

        // JSON 데이터를 Kotlin 객체로 변환하기 위한 컨버터 (Gson 사용 가정)
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

        // Coroutines 관련 의존성 (비동기 처리를 위해 필요)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1")
        // ... 다른 의존성들
    implementation("com.google.code.gson:gson:2.10.1")


// 최신 버전을 사용하세요.
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}