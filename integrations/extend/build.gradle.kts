plugins {
    alias(libs.plugins.android.library)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(Versions.JVM_TARGET)
    }
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
}

dependencies {
    compileOnly(projects.integrations.dummy)
}
