import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.serialization") version "1.6.10"
    id("org.jetbrains.compose") version "1.1.1"
}

group = "me.guest_3slo32w"
version = "1.0"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines:0.19.2")
}

tasks.withType<KotlinCompile>().all {
    kotlinOptions.freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
    // "Kotlin source files are always UTF-8 by design."  THAT'S GOOD!
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}


compose.desktop {
    application {
        mainClass = "io.github.winterreisender.jkdrcom.gui.MainKt"
        jvmArgs += listOf()//"-Xmx512m","-Xms32m","-XX:+UseZGC","-Dfile.encoding=gbk")
        args += listOf()
        description = "JKDrcom client"
        nativeDistributions {
            //includeAllModules = true
            targetFormats(TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Dmg)
            packageName = "JKDrcom"
            packageVersion = "0.1.0"
            copyright = "©Copyright 2022 Winterreisender. License under AGPL-3.0-only."
            vendor = "Winterreisender"
            licenseFile.set(project.file("LICENSE"))
            windows {
                upgradeUuid = "f7dac7eb-0136-48db-8103-85c56d4bf3f5"
                dirChooser = true
                shortcut = true
                msiPackageVersion = "0.1.0"
                //iconFile.set(project.file("icon.ico"))
            }
            linux {
                shortcut = true
                packageName = "jkdrcom"
                //iconFile.set(project.file("icon.png"))
            }
            macOS {
                dmgPackageVersion = "1.0.0"
                //iconFile.set(project.file("icon.icns"))
            }


        }
    }
}