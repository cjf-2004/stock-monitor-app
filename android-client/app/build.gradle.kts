plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.myapplication"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 30
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
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    // 添加网络请求和 JSON 解析库
    implementation(libs.okhttp) // 👈 改变为版本目录引用
    implementation(libs.gson)   // 👈 改变为版本目录引用
    // OkHttp (你已经有了 libs.okhttp，这里确保日志拦截器也存在)
    // okhttp-logging-interceptor 对于调试网络请求至关重要
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0") // Ensure this version matches your libs.okhttp version

    // 安全加密存储 (EncryptedSharedPreferences)

    // 安全加密存储 (EncryptedSharedPreferences)
    // 请检查Maven Central以获取最新的稳定版本
    // https://mvnrepository.com/artifact/androidx.security/security-crypto
    implementation("androidx.security:security-crypto:1.1.0-alpha06") // 注意：这是一个alpha版本，可能需要更新

    // 生命周期和 ViewModel
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.2")

    // 测试
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    // Retrofit (HTTP 客户端)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0") // Gson converter
    implementation("com.squareup.retrofit2:converter-scalars:2.9.0")
}