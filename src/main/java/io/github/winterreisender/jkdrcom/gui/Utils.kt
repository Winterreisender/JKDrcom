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
import java.awt.*
import java.net.URI
import javax.swing.*


object Utils {
    // 用WebViewJar在新窗口中打开校园窗。废弃,存在内存泄漏问题导致0xC0000409闪退
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

    // 用JSOUP匹配HTML取背景图

    fun msgBox(text :String, title :String) :Unit = JOptionPane.showMessageDialog(ComposeWindow(),text,title,JOptionPane.INFORMATION_MESSAGE)
    fun inputBox(text :String, title :String) :String = JOptionPane.showInputDialog(ComposeWindow(),text,title,JOptionPane.INFORMATION_MESSAGE) ?: ""
}

