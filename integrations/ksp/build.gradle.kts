plugins {
    alias(libs.plugins.kotlin.jvm)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(Versions.JVM_TARGET)
    }
}

dependencies {
    implementation(libs.ksp.api)
    implementation(libs.kotlinpoet.ksp)
}
