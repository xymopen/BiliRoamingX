@file:Suppress("UnstableApiUsage")

import com.android.build.gradle.internal.tasks.R8Task

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(Versions.JVM_TARGET)
    }
}

android {
    namespace = "app.revanced.bilibili.integrations"
    compileSdkVersion(Versions.COMPILE_SDK)
    ndkVersion = Versions.NDK

    defaultConfig {
        applicationId = "app.revanced.bilibili.integrations"
        multiDexEnabled = false
        minSdk = Versions.MIN_SDK
        targetSdk = Versions.TARGET_SDK

        val verName = version as String
        versionName = verName
        versionCode = verName.split('.').let { (m, s, f) ->
            m.toInt() * 1000000 + s.toInt() * 1000 + f.toInt()
        }

        externalNativeBuild {
            cmake {
                version = Versions.CMAKE
                val flags = arrayOf(
                    "-Qunused-arguments",
                    "-Wno-gnu-string-literal-operator-template",
                    "-fno-rtti",
                    "-fvisibility=hidden",
                    "-fvisibility-inlines-hidden",
                    "-fno-exceptions",
                    "-fno-stack-protector",
                    "-fomit-frame-pointer",
                    "-Wno-builtin-macro-redefined",
                    "-ffunction-sections",
                    "-fdata-sections",
                    "-Wno-unused-value",
                    "-D__FILE__=__FILE_NAME__",
                    "-Wl,--exclude-libs,ALL",
                )
                cFlags("-std=c18", *flags)
                cppFlags("-std=c++20", *flags)
                targets("biliroamingx")
            }
        }
    }

    buildTypes {
        all {
            val flags = arrayOf(
                "-Wl,--gc-sections",
                "-flto",
                "-fno-unwind-tables",
                "-fno-asynchronous-unwind-tables",
            )
            val configFlags = arrayOf(
                "-Oz",
                "-DNDEBUG"
            ).joinToString(" ")
            val args = arrayOf(
                "-DANDROID_STL=c++_shared",
                "-DCMAKE_BUILD_TYPE=Release",
                "-DCMAKE_CXX_FLAGS_RELEASE=$configFlags",
                "-DCMAKE_C_FLAGS_RELEASE=$configFlags",
            )
            externalNativeBuild.cmake {
                cFlags += flags
                cppFlags += flags
                arguments += args
            }
        }
        debug {
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("debug")
            proguardFiles(
                getDefaultProguardFile("proguard-android.txt"),
                "proguard-rules-debug.pro"
            )
        }
        release {
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("debug")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        applicationVariants.all {
            outputs.all {
                this as com.android.build.gradle.internal.api.ApkVariantOutputImpl

                outputFileName = "${rootProject.name}-${parent!!.name}-$versionName.apk"
            }
        }
    }

    buildFeatures {
        buildConfig = true
        resValues = false
    }

    packaging {
        // since it's already packaged in host client
        jniLibs.excludes += "**/libc++_shared.so"
        resources.excludes += setOf(
            "kotlin/**",
            "META-INF/**",
            "kotlin-tooling-metadata.json",
        )
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/jni/CMakeLists.txt")
        }
    }
}

gradle.taskGraph.whenReady {
    if (gradle.taskGraph.allTasks.any { it.name == "distDebug" }) {
        tasks.withType<R8Task> {
            val state = useFullR8.javaClass.superclass.declaredFields.first { it.name == "state" }
                .also { it.isAccessible = true }.get(this)
            state.javaClass.declaredFields.first { it.name == "disallowChanges" }
                .also { it.isAccessible = true }.setBoolean(state, false)
            useFullR8 = false
        }
    }
}

dependencies {
    implementation(projects.integrations.extend)
    implementation(libs.hiddenapibypass)
    implementation(libs.truetypeparser)
    implementation(libs.androidx.documentfile) {
        exclude(libs.androidx.annotation.get().group)
    }
    implementation(libs.kotlinx.serialization.json)
    ksp(projects.integrations.ksp)
    compileOnly(projects.integrations.dummy)
}

tasks.named<Delete>("clean") {
    delete(layout.projectDirectory.dir(".cxx"))
}
