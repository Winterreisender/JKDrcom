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

import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.toLowerCase
import java.awt.*
import java.net.URI
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import javax.swing.*

import com.github.winterreisender.webviewko.WebviewKo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.coroutines.CoroutineContext

object Utils {
    /*
    * TODO: NEED TESTING 在窗口中打开校园网之窗
    */

    fun showNetWindow(url :String = Constants.SchoolNetWindowURL, closeAfterSecs :Int = 0) = Thread {
             try {
                 WebviewKo(1).run {
                     val scales = GraphicsEnvironment.getLocalGraphicsEnvironment().defaultScreenDevice.defaultConfiguration.defaultTransform
                     title("JKDrcom Net Window")
                     size((592 * scales.scaleX).toInt(), (455 * scales.scaleY).toInt())

                     if (closeAfterSecs > 0) {
                         init(
                             """
                        setTimeout( () => { window.closeWebview() }, ${closeAfterSecs * 1000}  ) 
                     """.trimIndent()
                         )
                         bind("closeWebview") {
                             terminate()
                             ""
                         }
                     }

                     navigate(url)
                     show()
                 }
             } catch (e :Exception) {
                 e.printStackTrace()
                 SwingUtilities.invokeLater {
                     if(e.message == "Failed to create webview" && System.getProperty("os.name").lowercase(Locale.getDefault()).contains("windows")) {
                         optionBox(mapOf(
                             "前往下载" to {Desktop.getDesktop().browse(URI("https://developer.microsoft.com/zh-cn/microsoft-edge/webview2"))},
                             "在浏览器中打开" to {openNetWindow()},
                             "取消" to {}
                         ),"创建WebviewKo失败,无法在窗口中显示校园网之窗\r\n 一种可能是您需要安装 Microsoft Edge WebView2, 请到 https://developer.microsoft.com/zh-cn/microsoft-edge/webview2 下载","创建WebviewKo失败")
                     }else {
                         msgBox("${e.message}","Error",JOptionPane.WARNING_MESSAGE)
                     }
                 }
             }
        }.apply {
             uncaughtExceptionHandler = Thread.UncaughtExceptionHandler {t,e -> println("$t ${e.stackTrace}") }
             start()
         }

    // 在浏览器中打开校园窗
    fun openNetWindow(url :String = Constants.SchoolNetWindowURL) =
        Desktop.getDesktop().browse(URI(url))

    fun <R> optionBox(options :Map<Any,()->R>, message :String, title :String, messageType :Int = JOptionPane.WARNING_MESSAGE) :R =
        JOptionPane.showOptionDialog(null,
            message,title,
            JOptionPane.DEFAULT_OPTION,messageType,null,
            options.keys.toTypedArray(),options.keys.last()
        ).let {
            options.values.toTypedArray()[it]()
        }

    fun msgBox(text :String, title :String, type :Int = JOptionPane.INFORMATION_MESSAGE) :Unit = JOptionPane.showMessageDialog(ComposeWindow(),text,title,JOptionPane.INFORMATION_MESSAGE)
    fun inputBox(text :String, default :String) :String = JOptionPane.showInputDialog(ComposeWindow(),text,default) ?: ""

    fun <T> chooseBox(text: String, choices :Array<T>, default :T, title: String = text) :T =
        JOptionPane.showInputDialog(ComposeWindow(),text,title,JOptionPane.INFORMATION_MESSAGE,null, choices,default)  as T? ?: default

    /**
     * 用户密码加密
     *
     * @param password 用户明文密码
     * @return cipherURI RFC 2397 格式的byteArray (data:application/octet-stream;base64,)
     *
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

    // Color的工具
    // 采用Web标准存储,即 #RRGGBB(AA), 在JSON,AWT,Compose三者之间转换
    class WebColor(val red :Int, val green :Int, val blue  :Int, val alpha :Int = 255) {
        init {
            assert(red   in 0..255)
            assert(green in 0..255)
            assert(blue  in 0..255)
            assert(alpha in 0..255)
        }

        /**
         * 转为Web标准格式字符串 #RRGGBB 或 #RRGGBBAA (alpha!=255时)
         */
        override fun toString() = if(alpha==255) String.format("#%02x%02x%02x",red,green,blue) else String.format("#%02x%02x%02x%02x",red,green,blue,alpha)
        fun toAwt() = java.awt.Color(red,green,blue,alpha)
        fun toCompose() = Color(alpha=alpha,red=red,green=green,blue=blue)
        companion object {
            @Throws(NumberFormatException::class)
            fun from(s :String) :WebColor = from(java.awt.Color.decode(s))
            fun from(jColor :java.awt.Color) = WebColor(jColor.red,jColor.green,jColor.blue,jColor.alpha)
            fun from(color :Color) = WebColor(color.red.toInt(),color.green.toInt(),color.blue.toInt(),color.alpha.toInt())
        }
    }

}

