/*
 * Copyright (C) 2022 Winterreisender.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>
 */

import com.formdev.flatlaf.FlatLightLaf
import io.github.winterreisender.jkdrcom.core.util.IPUtil
import io.github.winterreisender.jkdrcom.gui.Constants
import io.github.winterreisender.jkdrcom.gui.Utils
import java.awt.Cursor
import java.awt.Desktop
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.net.URI
import javax.swing.*
import kotlin.test.Test

internal class Test {
    @Test fun netWindow() {
        Utils.showNetWindow(closeAfterSecs = 5)
    }

    @Test fun hostInfo() {
        IPUtil.getHostInfo().forEach(::println)
    }

    @Test fun somefun() {
        FlatLightLaf.setup()
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
            <td>jlu-drcom-java</td>
            <td>AGPL-3.0-only</td>
            </tr>
            <tr>
            <td>Kotlin</td>
            <td>Apache-2.0</td>
            </tr>
            <tr>
            <td>Java (GraalVM,Eclipse Adoptium)</td>
            <td>GPL-2.0-only WITH Classpath-exception-2.0</td>
            </tr>
            <tr>
            <td>Jetpack Compose Desktop</td>
            <td>Apache-2.0</td>
            </tr>
            <tr>
            <td>IntelliJ IDEA Community</td>
            <td>Apache-2.0</td>
            </tr>
            <tr>
            <td>FlatLaf</td>
            <td>Apache-2.0</td>
            </tr>
            <tr>
            <td>webviewko</td>
            <td>Apache-2.0</td>
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