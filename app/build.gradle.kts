plugins {
    id("com.android.application")
}

android {
    namespace = "com.george.backgammon"
    compileSdk = 35

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    defaultConfig {
        applicationId = "com.george.backgammon"
        minSdk = 24
        targetSdk = 35
        versionCode = 7
        versionName = "0.7.0"
    }
}
