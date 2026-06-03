plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.lsparanoid)
}

lsparanoid {
    seed = if (RAND_SEED != 0) RAND_SEED else null
    includeDependencies = true
    classFilter = { true }
}

android {
    namespace = "com.topjohnwu.magisk"
    enableKotlin = false

    val base = "https://api.github.com/repos/DaDevMikey/MagiskNext/releases/latest"
    val url = base

    defaultConfig {
        applicationId = "com.topjohnwu.magisk"
        versionCode = 1
        versionName = "1.0"
        buildConfigField("String", "APK_URL", "\"$url\"")
        buildConfigField("int", "STUB_VERSION", Config.stubVersion)
    }

    buildTypes {
        release {
            proguardFiles("proguard-rules.pro")
            isMinifyEnabled = true
        }
    }

    buildFeatures {
        buildConfig = true
    }
}

setupStubApk()

dependencies {
    implementation(project(":shared"))
}
