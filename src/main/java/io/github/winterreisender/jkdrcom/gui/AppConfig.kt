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

/**
 * 整个App的配置由AppConfig类管理
 *
 * 负责 1.存储、读取配置文件 2. 对配置文件中的内容进行转换供GUI使用
 */
@kotlinx.serialization.Serializable
data class AppConfig(
    /** 用户名 */
    var username         :String  = "",
    /** 密码,以RFC 2397 格式加密存储 */
    var password         :String  = "",
    /** MAC地址,如AA-BB-CC-DD-EE-FF */
    var macAddress       :String  = "",
    /** 主机名称,这个其实无所谓 */
    var hostName         :String  = "",
    /** 最大重试次数 */
    var maxRetry         :Int     = 2,
    /** 自动登录 */
    var autoLogin        :Boolean = false,
    /** 记住密码 */
    var rememberPassword :Boolean = false,
    /** 在登录成功后几秒自动关闭窗口, <=0 表示不自动关闭 */
    var closeAfterSecs   :Int     = -1,
    /** 校园网之窗的打开方式 */
    var netWindow        :NetWindowType  = NetWindowType.BROWSER,
    /** 主色调,以CSS颜色格式存储,如"#0066cc" */
    var mainColor        :String  = Constants.DefaultPrimaryColor.toString()
) {

    /**
     * 校园网之窗的打开方式
     */
    enum class NetWindowType {
        /** 在登录成功后不自动打开,手动点击菜单选项时在浏览器打开 */
        NONE,
        /** 在webview窗口中打开 */
        WINDOWED,
        /** 在浏览器打开  */
        BROWSER;
        override fun toString() =
            when(this) {
                NONE     -> "无"
                WINDOWED -> "窗口"
                BROWSER  -> "浏览器"
            }
    }

    /**
     * 将存储的颜色(字符串"#3F3F3F")转换为 [Utils.WebColor]
     */
    fun getPrimaryColor() = kotlin.runCatching { Utils.WebColor.from(mainColor) }.getOrDefault(Constants.DefaultPrimaryColor) // 出现异常则用Constants.DefaultPrimaryColor

    /**
     * 获取主机信息(主机名+MAC地址),用于core模块的[io.github.winterreisender.jkdrcom.core.JKDrcomTask]的构造参数
     */
    fun getHostInfo() = HostInfo(hostName, macAddress)

    /**
     * 保存到配置文件中
     */
    fun saveToFile() {
        val jsonText = Json.encodeToString(
            this.copy(
                //判断是否需要存储密码,如果需要则调用pwdEncrypt
                password = if(rememberPassword) Utils.pwdEncrypt(password) else ""
            )
        )
        configFile.writeText(jsonText)
        Logger.getLogger("AppConfig").info("Saved config: $jsonText")
    }

    companion object {
        /** 配置文件类 */
        val configFile :File by lazy {
            val configDirectory = System.getProperty("user.home", ".") + File.separator + ".drcom"
            val configFilename = "jkdrcom.json"
            Files.createDirectories(Paths.get(configDirectory))
            File(configDirectory + File.separator + configFilename)
        }

        /** 从配置文件中加载配置 */
        fun loadFromFile() :AppConfig = kotlin.runCatching {
            Json.decodeFromString<AppConfig>(configFile.readText()).apply {
                password = if(password.isEmpty()) "" else kotlin.runCatching { Utils.pwdDecrypt(password) }.getOrDefault("")
            }}.getOrDefault(AppConfig().also{Logger.getLogger("AppConfig").warning("Error occurs in loadFromFile: $it. Using default one")})
    }
}

