package io.github.winterreisender.jkdrcom.gui/*
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
import java.awt.*
import java.net.URI
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import javax.swing.*



object Utils {
    /*
    * TODO: 在窗口中打开校园网之窗
    * 用WebViewJar在新窗口中打开校园窗。废弃,存在内存泄漏问题导致0xC0000409闪退
    * 至今也没有一个跨平台、轻量、稳定的校园网之窗解决方案:
    * 1. Swing JEditPane: 界面不完善,在非MSI包下不显示 界面不正常渲染
    * 2. WebViewJar: 年久失修、内存泄漏
    * 3. JavaFx WebView: 要么体积过大,要么用JavaFx8 WebView和Compose的Java11+不兼容,要么需要用内置JavaFx的的JDK 17
    * 4. jCef: 体积过大
    * 5. 额外用别的语言写个小程序(Tauri): 跨平台困难
    * 6. SWT: 不支持Gradle,文档少
    * 7. 用Jsoup取出图片: 功能不完善,不稳定
    * 8. 用QtJambi Webview: 需要用Qt方式打包,体积过大
    * 9. selenium: 需要手动安装驱动
    * 10.DJ Native Swing,Lobo: 年久失修. DJ Native Swing好像在22年出了个1.0.2preview
    * 11.Desktop.getDesktop().browse(URI(url)): 反而是最靠谱的,但不能调整窗口大小
    * 救一下啊Java,别老整你那后端了,UI都快寄了
    */

    /*
    @Deprecated("Memory Leak!", ReplaceWith("openNetWindow"))
    fun showNetWindow() {
        Thread {
            val url = "http://login.jlu.edu.cn/notice_win.php"
            Thread.currentThread().name = "JKDrcom Net Window"

            WebView().apply {
                size(592, 450) // 这个size也有BUG, 592*450是图片尺寸
                title("Welcome") //有中文支持问题
                resizable(true)
                url(url)
                //addOnBeforeLoad(js :String)
                //addJavascriptCallback(String->Unit) //Handle a message sent via window.external.invoke(message)

                show()
            }
        }.start()
    }
    */

    // 在浏览器中打开校园窗
    fun openNetWindow() {
        val url = "http://login.jlu.edu.cn/notice_win.php"
        Desktop.getDesktop().browse(URI(url))
    }

    // 用JSOUP匹配HTML取背景图.等待其他特性稳定后再添加
    fun getNetWindowPictureURL(){

    }

    fun msgBox(text :String, title :String) :Unit = JOptionPane.showMessageDialog(ComposeWindow(),text,title,JOptionPane.INFORMATION_MESSAGE)
    fun inputBox(text :String, title :String) :String = JOptionPane.showInputDialog(ComposeWindow(),text,title,JOptionPane.INFORMATION_MESSAGE) ?: ""


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

}

