/*
 * Copyright (C) 2022 Winterreisender.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>
 */

import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.serialization") version "1.6.10"
    id("org.jetbrains.compose") version "1.1.1"
}

group = "io.github.winterreisender.jkdrcom"
version = "1.1.0"

repositories {
    google()
    mavenCentral()
    mavenLocal()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://gitlab.com/api/v4/projects/38224197/packages/maven")
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines:0.19.2")
    implementation("com.formdev:flatlaf:2.3")
    implementation("com.formdev:flatlaf-intellij-themes:2.3")

    implementation("com.github.winterreisender:webviewko:0.3.0")
    implementation("com.github.winterreisender:webviewko-jvm:0.3.0")


    /*
    implementation(fileTree(mapOf(
        "dir" to "src/libs",
        "include" to listOf("*.jar")
    )))
    */
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
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
            packageVersion = "1.0.6" // MAC OS一遇到低于1.0的packageVersion就无法打包
            copyright = "Copyright 2022 Winterreisender. License under AGPL-3.0-only."
            vendor = "Winterreisender"
            licenseFile.set(project.file("LICENSE"))
            windows {
                upgradeUuid = "f7dac7eb-0136-48db-8103-85c56d4bf3f5"
                dirChooser = true
                shortcut = true
                iconFile.set(project.file("asset/logo.ico"))
            }
            linux {
                shortcut = true
                packageName = "jkdrcom"
                iconFile.set(project.file("asset/logo.png"))
            }
            macOS {
                dmgPackageVersion = "1.0.6"
                iconFile.set(project.file("asset/logo.icns"))
            }

        }
    }
}
