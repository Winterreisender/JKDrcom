/*
 * Copyright (C) 2022  Winterreisender
 *
 * This file is part of JKDrcom.
 *
 * JKDrcom is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, only version 3 of the License.
 *
 * JKDrcom is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * 本文件是 JKDrcom 的一部分。
 * JKDrcom 是自由软件：你可以再分发之和/或依照由自由软件基金会发布的 GNU Affero 通用公共许可证修改之，仅版本 3 许可证。
 * 发布 JKDrcom 是希望它能有用，但是并无保障;甚至连可销售和符合某个特定的目的都不保证。请参看 GNU Affero 通用公共许可证，了解详情。
 * 你应该随程序获得一份 GNU Affero 通用公共许可证的复本。如果没有，请看 <https://www.gnu.org/licenses/>。
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
    //implementation("io.github.vincenzopalazzo:material-ui-swing:1.1.2")
    implementation("com.formdev:flatlaf:2.2")
    implementation("com.formdev:flatlaf-intellij-themes:2.2")

    /*
    废弃,WebViewJar存在内存泄漏问题导致0xC0000409闪退
    implementation(fileTree(mapOf(
        "dir" to "libs",
        "include" to listOf("*.jar")
    )))
    */
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
            packageVersion = "1.0.4" // MAC OS一遇到低于1.0的packageVersion就无法打包
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
                dmgPackageVersion = "1.0.4"
                iconFile.set(project.file("asset/logo.icns"))
            }

        }
    }
}