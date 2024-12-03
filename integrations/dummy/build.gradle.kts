plugins {
    alias(libs.plugins.android.library)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(Versions.JVM_TARGET)
    }
}

android {
    namespace = "app.revanced.bilibili.dummy"
    compileSdkVersion(Versions.COMPILE_SDK)
    ndkVersion = Versions.NDK

    defaultConfig {
        minSdk = Versions.MIN_SDK
        targetSdk = Versions.TARGET_SDK
    }
}

dependencies {
    api(files("libs/grpc_apis.jar"))
    api(libs.fastjson)
    api(libs.protobuf)
    api(libs.androidx.annotation)
    api(libs.androidx.appcompat)
    api(libs.androidx.preference)
    api(libs.androidx.recyclerview)
}
