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



package io.github.winterreisender.jkdrcom.gui

import io.github.winterreisender.jkdrcom.core.util.HostInfo
import isValidMacAddress
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
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

    fun inValid() :Boolean = username.isEmpty() || password.isEmpty() || !macAddress.isValidMacAddress() || maxRetry <= 0 || hostName.isEmpty()
    fun toHostInfo() = HostInfo(hostName, macAddress)
    fun set(username: String, password: String, macAddress: String, hostName: String, autoLogin: Boolean, rememberPassword: Boolean, maxRetry: Int){
        this.username = username
        this.password = password
        this.macAddress = macAddress
        this.hostName = hostName
        this.autoLogin = autoLogin
        this.rememberPassword = rememberPassword
        this.maxRetry = maxRetry
    }
    fun saveToFile() {
        val userHome = System.getProperty("user.home")
        val jsonText = Json.encodeToString(this)
        with(File("$userHome/.drcom/jkdrcom.json")) {
            writeText(jsonText)
        }
        println("Save config $jsonText")
    }
    fun readFromFile() {
        val userHome = System.getProperty("user.home")
        lateinit var savedObj :AppConfig
        try {
            with(File("$userHome/.drcom/jkdrcom.json")) {
                savedObj = Json.decodeFromString(readText())
            }

            // This is Engineering!
            username = savedObj.username
            password = savedObj.password
            macAddress = savedObj.macAddress
            hostName = savedObj.hostName
            maxRetry = savedObj.maxRetry
            autoLogin = savedObj.autoLogin
            rememberPassword = savedObj.rememberPassword
        }catch (e :Exception){
            Logger.getLogger("AppConfig").warning("Error occurs in readFromFile: $e. Use dummy one")
        }

    }

    companion object {
        // 返回一个不能用的默认值
        fun getDummyAppConfig() = AppConfig("", "", "","",1,false,false)
    }
}

