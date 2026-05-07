import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.serialization)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    androidLibrary {
        namespace = "zone.ien.taptargetsample.example.lib"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }

        androidResources {
            enable = true
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.material3)
            implementation(libs.compose.preview)
            implementation(projects.taptarget)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.ui.tooling)
}

//android {
//  namespace = "zone.ien.taptargetcmp.sample.lib"
//  compileSdk = Configuration.compileSdk
//
//  defaultConfig {
//    applicationId = "com.psoffritti.taptargetcompose"
//    minSdk = Configuration.minSdkSampleApp
//    targetSdk = Configuration.targetSdk
//    versionCode = Configuration.versionCode
//    versionName = Configuration.versionName
//
//    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
//  }
//
//  buildTypes {
//    release {
//      isMinifyEnabled = false
//      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
//    }
//  }
//  compileOptions {
//    sourceCompatibility = JavaVersion.VERSION_1_8
//    targetCompatibility = JavaVersion.VERSION_1_8
//  }
//  kotlinOptions {
//    jvmTarget = "1.8"
//  }
//  buildFeatures {
//    compose = true
//  }
//}
//
//dependencies {
//  implementation(project(":tap-target-compose"))
//
//  implementation(libs.androidx.core.ktx)
//  implementation(libs.androidx.appcompat)
//  implementation(libs.material)
//
//  val composeBom = platform(libs.androidx.compose.bom)
//  implementation(composeBom)
//  androidTestImplementation(composeBom)
//  implementation(libs.androidx.compose.material3)
//  implementation(libs.androidx.compose.material.icons)
//  implementation(libs.androidx.activity.compose)
//
//  implementation(libs.androidx.compose.ui.tooling.preview)
//  implementation(libs.androidx.compose.ui.tooling)
//}