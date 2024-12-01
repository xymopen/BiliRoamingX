plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "app.revanced.bilibili.extend"
    compileSdkVersion(Versions.COMPILE_SDK)
    ndkVersion = Versions.NDK

    defaultConfig {
        minSdk = Versions.MIN_SDK
        targetSdk = Versions.TARGET_SDK
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        create("dev")
    }
}

dependencies {
    compileOnly(projects.integrations.dummy)
}
