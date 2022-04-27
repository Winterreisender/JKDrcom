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

