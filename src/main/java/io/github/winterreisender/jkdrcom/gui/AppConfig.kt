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
    var netWindow        :NetWindowType  = NetWindowType.NONE  // Use @EncodeDefault to write to json when it's default value
) {

    enum class NetWindowType {
        NONE, // DO NOT open school net window
        WINDOWED, // Open in webview
        BROWSER // Open in browser
    }

    fun getHostInfo() = HostInfo(hostName, macAddress)

    @Deprecated("Use T.apply")
    fun set(username: String, password: String, macAddress: String, hostName: String, autoLogin: Boolean, rememberPassword: Boolean){
        this.username = username
        this.password = password
        this.macAddress = macAddress
        this.hostName = hostName
        this.autoLogin = autoLogin
        this.rememberPassword = rememberPassword
    }

    fun saveToFile() {
        val jsonText = Json.encodeToString(
            this.copy(
                password = if(rememberPassword) Utils.pwdEncrypt(password) else ""            //判断是否需要存储密码 TODO: 可选明文/密文保存密码
            )
        )
        File(getConfigFile()).run {
            writeText(jsonText)
        }
        Logger.getLogger("AppConfig").info("Saved config: $jsonText")
    }


    companion object {
        // 返回配置文件路径
        fun getConfigFile() :String {
            val configDirectory = "${System.getProperty("user.home")}/.drcom/"
            val configFilename = "jkdrcom.json"

            Files.createDirectories(Paths.get(configDirectory))
            return configDirectory + configFilename
        }

        fun loadFromFile() :AppConfig {
            var cfg :AppConfig? = null
            try {
                File(getConfigFile()).run {
                    cfg = Json.decodeFromString<AppConfig>(readText())
                }

                cfg!!.password = if(cfg!!.password.isEmpty()) "" else kotlin.runCatching { Utils.pwdDecrypt(cfg!!.password) }.getOrDefault("") // TODO: 可选明文/密文保存密码

            }catch (e :Exception){
                Logger.getLogger("AppConfig").warning("Error occurs in readFromFile: $e. Using default one")
            }

            return cfg ?: AppConfig()
        }
    }
}

