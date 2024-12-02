import com.android.tools.build.apkzlib.zip.ZFile
import org.gradle.internal.os.OperatingSystem
import org.gradle.kotlin.dsl.support.listFilesOrdered
import java.util.Properties

plugins {
    alias(libs.plugins.kotlin.jvm)
}

group = "app.revanced.bilibili"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(Versions.JVM_TARGET_PATCHES)
    }
}

dependencies {
    implementation(libs.revanced.patcher)
    implementation(libs.smali)
    // Used in JsonGenerator.
    implementation(libs.gson)
}

tasks.jar {
    archiveBaseName = "${rootProject.name}-${project.name}"
    exclude("app/revanced/generator")

    manifest {
        attributes["Name"] = "BiliRoamingX Patches"
        attributes["Description"] = "Patches for BiliRoamingX."
        attributes["Version"] = version
        attributes["Timestamp"] = System.currentTimeMillis().toString()
        attributes["Source"] = "git@github.com:BiliRoamingX/BiliRoamingX.git"
        attributes["Author"] = "Kofua"
        attributes["License"] = "GNU General Public License v3.0"
    }
}

tasks.register("buildDexJar") {
    description = "Build and add a DEX to the JAR file"
    group = "build"

    dependsOn(tasks.build)

    doLast {
        val d8Name = OperatingSystem.current().getScriptName("d8")
        val sdkDir = System.getenv("ANDROID_HOME").orEmpty().ifEmpty {
            rootProject.file("local.properties").takeIf { it.exists() }
                ?.inputStream()?.let { Properties().apply { load(it) } }
                ?.getProperty("sdk.dir")
        }.orEmpty().ifEmpty { error("Android sdk not found.") }
        val d8 = File(sdkDir).resolve("build-tools")
            .listFilesOrdered().last().resolve(d8Name).absolutePath

        val patchesJar = configurations.archives.get().allArtifacts.files.files.first().absolutePath
        val workingDirectory = layout.buildDirectory.dir("libs").get().asFile

        exec {
            workingDir = workingDirectory
            commandLine = listOf(d8, "--release", patchesJar)
        }

        ZFile.openReadWrite(File(patchesJar)).use {
            it.add("classes.dex", File(workingDirectory, "classes.dex").inputStream())
        }
    }
}

tasks.register<JavaExec>("generatePatchesFiles") {
    description = "Generate patches files"

    dependsOn(tasks.build)

    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("app.revanced.generator.Main")
}

tasks.register("dist") {
    group = "build"
    dependsOn("buildDexJar")
    dependsOn("generatePatchesFiles")
}
