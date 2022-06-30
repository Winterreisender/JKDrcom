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

// 匹配合法MAC地址 AA-AA-AA-AA-AA-AA, `-`是可选的

@kotlinx.serialization.Serializable
data class AppConfig(
    var username: String,
    var password: String,
    var macAddress: String,
    var hostName: String = "",
    var maxRetry: Int = 8,
    var autoLogin :Boolean,
    var rememberPassword :Boolean
) {

    fun getHostInfo() = HostInfo(hostName, macAddress)
    fun set(username: String, password: String, macAddress: String, hostName: String, autoLogin: Boolean, rememberPassword: Boolean){
        this.username = username
        this.password = password
        this.macAddress = macAddress
        this.hostName = hostName
        this.autoLogin = autoLogin
        this.rememberPassword = rememberPassword
    }

    // DONE: 密码加密存储。 由于协议中传输的是随机加盐md5,不可能只存储密码的md5,程序必须能够获取明文密码,所以只能用双向加密。DrcomJava也是用的DES对称加密。
    fun saveToFile() {
        val userHome = System.getProperty("user.home")
        val configDirectory = "$userHome/.drcom/"
        val configFilename = "jkdrcom.json"

        // JSON序列化
        val jsonText = Json.encodeToString(
            this.copy(
                password = if(rememberPassword) Utils.pwdEncrypt(password) else ""            //判断是否需要存储密码 TODO: 可选明文/密文保存密码
            )
        )

        // 写入路径
        Files.createDirectories(Paths.get(configDirectory)) // `路径`已存在也不会报错
        with(File("$configDirectory/$configFilename")) {
            writeText(jsonText)
        }
        Logger.getLogger("AppConfig").info("Saved config: $jsonText")
    }
    fun readFromFile() {
        val userHome = System.getProperty("user.home")
        lateinit var savedObj :AppConfig
        try {
            with(File("$userHome/.drcom/jkdrcom.json")) {
                savedObj = Json.decodeFromString(readText())
            }

            // This IS Engineering!
            username = savedObj.username
            password = if(savedObj.password.isEmpty()) "" else kotlin.runCatching { Utils.pwdDecrypt(savedObj.password) }.getOrDefault("") // TODO: 可选明文/密文保存密码
            macAddress = savedObj.macAddress
            hostName = savedObj.hostName
            maxRetry = savedObj.maxRetry
            autoLogin = savedObj.autoLogin
            rememberPassword = savedObj.rememberPassword
        }catch (e :Exception){
            Logger.getLogger("AppConfig").warning("Error occurs in readFromFile: $e. Using default one")
        }

    }

    companion object {
        // 返回一个默认值
        fun getDefault() = AppConfig("", "", "","",5,false,true)
    }
}

