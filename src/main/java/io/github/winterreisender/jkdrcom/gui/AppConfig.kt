/*
 * Copyright (C) 2022 Winterreisender.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>
 */



package io.github.winterreisender.jkdrcom.gui

import androidx.compose.ui.graphics.Color
import io.github.winterreisender.jkdrcom.core.util.HostInfo
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.logging.Logger


@kotlinx.serialization.Serializable
data class AppConfig(
    var username         :String  = "",
    var password         :String  = "",
    var macAddress       :String  = "",
    var hostName         :String  = "",
    var maxRetry         :Int     = 2,
    var autoLogin        :Boolean = false,
    var rememberPassword :Boolean = false,
    var closeAfterSecs   :Int     = -1,
    var netWindow        :NetWindowType  = NetWindowType.NONE,  // Use @EncodeDefault to write to json when it's default value
    var mainColor        :String  = "0xFF39c5bb"
) {

    enum class NetWindowType {
        NONE,     // DO NOT open school net window
        WINDOWED, // Open in webview
        BROWSER;  // Open in browser

        override fun toString() =
            when(this) {
                NONE     -> "无"
                WINDOWED -> "窗口"
                BROWSER  -> "浏览器"
            }
    }

    fun getPrimaryColor() = Color(mainColor.removePrefix("0x").toLong(radix = 16))

    fun getHostInfo() = HostInfo(hostName, macAddress)

    fun saveToFile() {
        val jsonText = Json.encodeToString(
            this.copy(
                password = if(rememberPassword) Utils.pwdEncrypt(password) else ""            //判断是否需要存储密码 TODO: 可选明文/密文保存密码
            )
        )
        configFile.writeText(jsonText)
        Logger.getLogger("AppConfig").info("Saved config: $jsonText")
    }


    companion object {
        // 配置文件
        val configFile :File by lazy {
            val configDirectory = "${System.getProperty("user.home")}/.drcom/"
            val configFilename = "jkdrcom.json"
            Files.createDirectories(Paths.get(configDirectory))
            File(configDirectory + configFilename)
        }

        // 从配置文件中加载配置
        fun loadFromFile() :AppConfig = kotlin.runCatching {
            Json.decodeFromString<AppConfig>(configFile.readText()).apply {
                password = if(password.isEmpty()) "" else kotlin.runCatching { Utils.pwdDecrypt(password) }.getOrDefault("") // TODO: 可选明文/密文保存密码
            }}.getOrDefault(AppConfig().also{Logger.getLogger("AppConfig").warning("Error occurs in loadFromFile: $it. Using default one")})
    }
}

