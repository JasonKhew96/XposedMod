import org.gradle.internal.os.OperatingSystem
import java.nio.file.Paths


fun findInPath(executable: String): String? {
    val pathEnv = System.getenv("PATH")
    return pathEnv.split(File.pathSeparator).map { folder ->
        Paths.get("${folder}${File.separator}${executable}${if (OperatingSystem.current().isWindows) ".exe" else ""}")
            .toFile()
    }.firstOrNull { path ->
        path.exists()
    }?.absolutePath
}

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.jasonkhew96.xposedmod"
    compileSdk = 33
    buildToolsVersion = "33.0.0"
    defaultConfig {
        applicationId = "com.jasonkhew96.xposedmod"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"
        externalNativeBuild {
            cmake {
                abiFilters("arm64-v8a")
                findInPath("ccache")?.let {
                    println("Using ccache $it")
                    arguments += listOf(
                        "-DANDROID_CCACHE=$it",
                        "-DCMAKE_C_COMPILER_LAUNCHER=ccache",
                        "-DCMAKE_CXX_COMPILER_LAUNCHER=ccache",
                        "-DNDK_CCACHE=ccache"
                    )
                }
            }
        }
    }
    androidResources {
        noCompress("libxposedmod.so")
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility(JavaVersion.VERSION_11)
        targetCompatibility(JavaVersion.VERSION_11)
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    dependenciesInfo {
        includeInApk = false
    }
    externalNativeBuild {
        cmake {
            path("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
//    androidResources {
//        additionalParameters += arrayOf("--allow-reserved-package-id", "--package-id", "0x23")
//    }
}

dependencies {
    implementation("androidx.annotation:annotation:1.3.0")
    // Xposed Framework
    compileOnly("de.robv.android.xposed:api:82")
}
