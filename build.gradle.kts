plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

// val compose_version = "1.4.0" // BOM을 사용하고 libs.versions.toml에서 관리되므로 필요 없습니다.

android {
    namespace = "com.example.myapplication"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 26
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
    buildFeatures {
        compose = true
    }
    composeOptions {
        // libs.versions.toml에서 컴파일러 확장 버전이 관리되지 않는다면 유지.
        // 하지만 BOM을 사용한다면 이 설정도 대부분 불필요해질 수 있습니다.
        kotlinCompilerExtensionVersion = "1.4.0"
    }
}

dependencies {

    // **기존 libs. 의존성 (Compose BOM으로 버전 일관성 유지)**
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // **테스트 의존성**
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // 추가한 의존성들
    // 헬스커넥트 SDK (안정화 버전)
    implementation("androidx.health.connect:connect-client:1.1.0")
    // Retrofit (2.9.0은 최신 안정 버전. OK)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    // 비동기 실행 (1.7.3은 OK)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    // 로그 확인용 (5.0.1은 OK)
    implementation("com.jakewharton.timber:timber:5.0.1")

    // **<<< 중복된 Compose 및 Lifecycle 의존성 삭제됨 >>>**
}