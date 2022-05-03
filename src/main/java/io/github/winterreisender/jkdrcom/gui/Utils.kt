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

import java.awt.*
import java.net.URI
import javax.swing.*

fun String.isValidMacAddress() = this.matches(Regex("""([A-E,\d]{2}-?){5}([A-E,\d]{2})""",RegexOption.IGNORE_CASE))


// Bug: not work after jpackage
// 在新窗口中打开校园窗
@Deprecated("JEditorPane is pretty buggy", ReplaceWith("openNetWindow"))
fun showNetWindow() {
    Thread {
        val url = "http://login.jlu.edu.cn/notice_win.php"

        JFrame("校园网之窗").apply {
            size = Dimension(600, 540)
            location = GraphicsEnvironment.getLocalGraphicsEnvironment().centerPoint

            JPanel().apply {
                layout = FlowLayout()
                alignmentX = Component.CENTER_ALIGNMENT
                size = Dimension(640, 510)

                JEditorPane(url).apply {
                    size = Dimension(640, 465)
                    isEditable = false
                }.also(::add)

                JButton("在浏览器中打开").apply {
                    addActionListener {
                        openNetWindow()
                    }
                }.also(::add)

            }.also(::add)

            isVisible = true
        }
    }.start()

}

// 在浏览器中打开校园窗
fun openNetWindow() {
    val url = "http://login.jlu.edu.cn/notice_win.php"
    Desktop.getDesktop().browse(URI(url))
}