/*
 * Copyright (C) 2022 Winterreisender.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>
 */

import androidx.compose.ui.awt.ComposeWindow
import io.github.winterreisender.jkdrcom.core.util.IPUtil
import io.github.winterreisender.jkdrcom.gui.Constants
import io.github.winterreisender.jkdrcom.gui.Utils
import java.awt.Cursor
import java.awt.Desktop
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.IOException
import java.net.URI
import javax.swing.*
import kotlin.test.Test

internal class Test {
    @Test fun netWindow() {
        Utils.showNetWindow(closeAfterSecs = 5).join()
    }

    @Test fun hostInfo() {
        IPUtil.getHostInfo().forEach(::println)
    }

    @Test fun somefun() {
        val html = """
            <html>
            <p>本项目使用了以下开源软件:</p>
            <table>
            <thead>
            <tr>
            <th>软件</th>
            <th>许可</th>
            </tr>
            </thead>
            <tbody><tr>
            <td><a href="https://github.com/YouthLin/jlu-drcom-client/tree/master/jlu-drcom-java">jlu-drcom-java</a></td>
            <td><a href="https://github.com/youthlin/jlu-drcom-client/blob/master/jlu-drcom-java/LICENSE">AGPL-3.0-only</a></td>
            </tr>
            <tr>
            <td><a href="https://kotlinlang.org/">Kotlin</a></td>
            <td><a href="https://www.apache.org/licenses/LICENSE-2.0.html">Apache-2.0</a></td>
            </tr>
            <tr>
            <td>Java (<a href="https://www.graalvm.org/">GraalVM</a>,<a href="https://adoptium.net/">Eclipse Adoptium</a>)</td>
            <td><a href="https://openjdk.java.net/legal/gplv2+ce.html">GPL-2.0-only WITH Classpath-exception-2.0</a></td>
            </tr>
            <tr>
            <td><a href="https://github.com/JetBrains/compose-jb/">Jetpack Compose Desktop</a></td>
            <td><a href="https://www.apache.org/licenses/LICENSE-2.0.html">Apache-2.0</a></td>
            </tr>
            <tr>
            <td><a href="https://github.com/JetBrains/intellij-community">IntelliJ IDEA Community</a></td>
            <td><a href="https://www.apache.org/licenses/LICENSE-2.0.html">Apache-2.0</a></td>
            </tr>
            <tr>
            <td><a href="https://www.formdev.com/flatlaf/">flatlaf</a></td>
            <td><a href="https://www.apache.org/licenses/LICENSE-2.0.html">Apache-2.0</a></td>
            </tr>
            <tr>
            <td><a href="https://github.com/Winterreisender/webviewko">WebviewKo</a></td>
            <td><a href="https://github.com/Winterreisender/webviewko/blob/master/LICENSE">Apache-2.0</a></td>
            </tr>
            </tbody></table>
            </html>
        """.trimIndent()
        val linkLabel = JLabel(html).apply {
            cursor = Cursor.getPredefinedCursor( Cursor.HAND_CURSOR )
            addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent?) {
                    try {
                        Desktop.getDesktop().browse( URI( "https://github.com/Winterreisender/JKDrcom#%E5%BC%95%E7%94%A8%E4%B8%8E%E5%8F%82%E8%80%83" ) )
                    }catch (_:Throwable) {}
                }
            })
        }
        JOptionPane.showMessageDialog(null,linkLabel, Constants.MenuText.Help_About,JOptionPane.INFORMATION_MESSAGE)

    }

}