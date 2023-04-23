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

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.graphics.Color
import com.formdev.flatlaf.ui.FlatRoundBorder
import java.awt.*
import java.awt.event.*
import java.net.URI
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import javax.swing.*
import javax.swing.Timer
import java.io.BufferedInputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import java.util.logging.Logger
import javax.swing.event.HyperlinkEvent
import javax.swing.text.html.HTMLEditorKit
import javax.swing.text.html.StyleSheet

object Utils {
    /**
     * 在窗口中显示校园网之窗
     *
     * @param url 要显示的URL
     * @param closeAfterSecs 在显示后多少秒自动退出
     * @author Winterreisender
     * */
    fun showNetWindow(url :String = Constants.SchoolNetWindowURL, closeAfterSecs :Int = 0) {

        fun getRedirectedURL(url :URL): URL {
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 5000
                addRequestProperty("Accept-Charset", "UTF-8;")
            }
            val redirectedURL = BufferedInputStream(conn.inputStream)
                .readBytes().let{String(it)}
                .let{
                    """<meta http-equiv="Refresh" content="0;URL=(\S+)">""".toRegex(RegexOption.IGNORE_CASE)
                        .find(it)
                        ?.groupValues?.get(1)!!
                }
            return URL(redirectedURL)
        }
        val redirectedURL = getRedirectedURL(URL(url))
        Logger.getLogger("showNetWindow").info("redirectedURL: $redirectedURL")

        val minSize = Dimension(Constants.MinWindowSizeX,Constants.MinWindowSizeY)
        JFrame(Constants.MenuText.Function_SchoolNetWindow).apply {
            title = Constants.MenuText.Function_SchoolNetWindow
            minimumSize = minSize

            iconImage = toolkit.getImage(object {}.javaClass.getResource("/logo.png")!!)
            defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE

            addWindowListener(object : WindowAdapter() {
                override fun windowClosing(e : WindowEvent) {
                    super.windowClosed(e)
                }
                override fun windowOpened(e: WindowEvent?) {
                    // 定时关闭
                    if(closeAfterSecs > 0)
                        Timer(closeAfterSecs*1000) {
                            dispose()
                            (it.source as Timer).stop()
                        }.start()
                }
            })

            JEditorPane().apply {
                page = redirectedURL
                isEditable = false
                minimumSize = minSize

                // 处理点击事件
                addHyperlinkListener {
                    when(it.eventType) {
                        HyperlinkEvent.EventType.ACTIVATED -> Desktop.getDesktop().browse(it.url.toURI())
                        HyperlinkEvent.EventType.ENTERED   -> Unit
                        HyperlinkEvent.EventType.EXITED    -> Unit
                    }
                }

                // 调整样式
                val htmlKit = editorKit as HTMLEditorKit
                htmlKit.styleSheet.addRule("a {color: white; text-decoration: none; font-size: 10px}")

            }.let(contentPane::add)

            pack()
            validate()
            isVisible = true
        }
    }

    /** 在浏览器中打开校园窗 */
    fun openNetWindow(url :String = Constants.SchoolNetWindowURL) =
        Desktop.getDesktop().browse(URI(url))

    /** JOptionPane.showOptionDialog的简单封装,用于提供选项供用户选择 */
    fun <R> optionBox(options :Map<Any,()->R>, message :String, title :String, messageType :Int = JOptionPane.WARNING_MESSAGE) :R =
        JOptionPane.showOptionDialog(null,
            message,title,
            JOptionPane.DEFAULT_OPTION,messageType,null,
            options.keys.toTypedArray(),options.keys.last()
        ).let {
            options.values.toTypedArray()[it]()
        }

    /** JOptionPane.showMessageDialog的简单封装 */
    fun msgBox(text :String, title :String, type :Int = JOptionPane.INFORMATION_MESSAGE) :Unit = JOptionPane.showMessageDialog(ComposeWindow(),text,title,type)

    /** JOptionPane.showInputDialog的简单封装 */
    fun inputBox(text :String, default :String) :String = JOptionPane.showInputDialog(ComposeWindow(),text,default) ?: ""

    /** JOptionPane.showInputDialog的简单封装 */
    fun <T> chooseBox(text: String, choices :Array<T>, default :T, title: String = text) :T =
        JOptionPane.showInputDialog(ComposeWindow(),text,title,JOptionPane.INFORMATION_MESSAGE,null, choices,default)  as T? ?: default

    /**
     * 用户密码加密
     *
     * @param password 用户明文密码
     * @param key 加密密钥
     * @return RFC 2397 格式的byteArray, 如`data:application/octet-stream;base64,XXXXXXXXXX`
     * @author Winterreisender
     */
    fun pwdEncrypt(password :String, key :ByteArray = Constants.PwdCryptoKey) :String {
        val cipher :ByteArray = with(Cipher.getInstance("AES")) {
            init(
                Cipher.ENCRYPT_MODE,
                SecretKeySpec(key, "AES")
            )
            doFinal(password.toByteArray())
        }
        val b64 = Base64.getEncoder().encodeToString(cipher)
        return "data:application/octet-stream;base64,$b64" // use RFC 2397: Data URL Scheme
    }

    /**
     * 用户密码解密
     *
     * 全局变量依赖: Constants.PwdCryptoKey
     *
     * @param cipherURI RFC 2397 格式的byteArray (data:application/octet-stream;base64,)
     * @return 明文密码
     *
     */
    fun pwdDecrypt(cipherURI :String, key :ByteArray = Constants.PwdCryptoKey) :String {

        val pattern = Regex("""^data:application/octet-stream;base64,([A-Za-z0-9+/=]+)${'$'}""")
        val b64 = pattern.find(cipherURI)?.groupValues?.get(1)!!
        val cipher = Base64.getDecoder().decode(b64)!!

        return with(Cipher.getInstance("AES")) {
            init(
                Cipher.DECRYPT_MODE,
                SecretKeySpec(key, "AES")
            )
            String(doFinal(cipher))
        }

    }

    /** Color的工具类
     * 采用Web标准存储,即 #RRGGBB(AA), 主要用于在JSON存储格式,AWT/Swing颜色类,Compose颜色类三者之间转换
     * */
    class WebColor(val red :Int, val green :Int, val blue  :Int, val alpha :Int = 255) {
        init {
            assert(red   in 0..255)
            assert(green in 0..255)
            assert(blue  in 0..255)
            assert(alpha in 0..255)
        }

        /** 转为Web标准格式字符串 #RRGGBB 或 #RRGGBBAA (alpha!=255时) */
        override fun toString() = if(alpha==255) String.format("#%02x%02x%02x",red,green,blue) else String.format("#%02x%02x%02x%02x",red,green,blue,alpha)
        /** 转为Swing/AWT格式 */
        fun toAwt() = java.awt.Color(red,green,blue,alpha)

        /** 转为 Jetpack Compose 格式 */
        fun toCompose() = Color(alpha=alpha,red=red,green=green,blue=blue)
        companion object {
            /** 从JSON中的Web标准格式字符串加载 */
            @Throws(NumberFormatException::class)
            fun from(s :String) :WebColor = from(java.awt.Color.decode(s))

            /** 从Swing/AWT颜色对象加载 */
            fun from(jColor :java.awt.Color) = WebColor(jColor.red,jColor.green,jColor.blue,jColor.alpha)

            /** 从Compose颜色对象加载 */
            fun from(color :Color) = WebColor(color.red.toInt(),color.green.toInt(),color.blue.toInt(),color.alpha.toInt())
        }
    }

}

