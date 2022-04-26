package io.github.winterreisender.jkdrcom.gui

import io.github.winterreisender.jkdrcom.core.util.HostInfo
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File


fun String.isValidMacAddress() = this.matches(Regex("""([A-E,0-9]{2}-*){5}([A-E,0-9]{2})"""))

@kotlinx.serialization.Serializable
data class CoreConfig(
    var username: String,
    var password: String,
    var macAddress: String,
    var hostName: String = "",
    var networkInterfaceDisplayName: String = "",
    val maxRetry: Int = 8
) {
    fun inValid() :Boolean = username.isEmpty() || password.isEmpty() || !macAddress.isValidMacAddress() || maxRetry <= 0 || hostName.isEmpty()
    fun toHostInfo() = HostInfo(hostName, macAddress, networkInterfaceDisplayName)
}

data class GUIConfig(var autoLogin :Boolean,var rememberPassword :Boolean)


fun main() {
    val userHome = System.getProperty("user.home")
    println(userHome)
    val json = Json.encodeToString(CoreConfig("sda","sdsd","dsdsd","dssdd","sdsd",8)).also { println(it) }
    with(File("$userHome/.drcom/jkdrcom.json")) {
        writeText(json)
    }

}
