import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
    alias(libs.plugins.koin.compiler)
}

val secrets = Properties().apply {
    val secretsFile = rootProject.file("secrets.properties")
    if (secretsFile.exists()) secretsFile.inputStream().use(::load)
}
val omdbApiKey = providers.gradleProperty("omdb.apiKey")
    .orElse(providers.environmentVariable("OMDB_API_KEY"))
    .orElse(secrets.getProperty("omdb.apiKey", ""))

fun String.asBuildConfigString(): String =
    "\"${replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r")}\""

android {
    namespace = "com.jacks.moviet"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.jacks.moviet"
        minSdk = 28
        targetSdk = 37
        versionCode = 1
        versionName = "0.1.0"
        buildConfigField("String", "OMDB_BASE_URL", "https://www.omdbapi.com/".asBuildConfigString())
        buildConfigField("String", "OMDB_API_KEY", omdbApiKey.get().asBuildConfigString())
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    implementation(project(":SDK"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.paging.runtime)
    implementation(libs.androidx.paging.compose)
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)
    implementation(libs.koin.compose)
    implementation(libs.koin.compose.viewmodel)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    testImplementation(libs.junit4)
    testImplementation(libs.kotlinx.coroutines.test)
}
