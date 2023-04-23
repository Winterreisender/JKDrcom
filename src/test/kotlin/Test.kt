/*
 * Copyright (C) 2022 Winterreisender.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>
 */

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.github.winterreisender.jkdrcom.core.util.IPUtil
import io.github.winterreisender.jkdrcom.gui.Constants
import io.github.winterreisender.jkdrcom.gui.Utils
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.*
import kotlin.test.Test


internal class Test {

    // 一个Tab实现
    @Composable
    fun XTabView(tabs: Map<String, @Composable () -> Unit>) {
        var selectedTabIndex by remember { mutableStateOf(0) }

        Column {
            TabRow(
                selectedTabIndex = selectedTabIndex,
            ) {
                tabs.keys.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(text = title)
                        }
                    )
                }
            }

            tabs.values.toList()[selectedTabIndex]()
        }
    }

    // 新的关于页面,很遗憾没有来得及实装
    @Test fun newAboutWindow() {
        application {
            Window({}) {
            XTabView(mapOf(
                "关于" to {SelectionContainer {
                    Text(Constants.AppAbout.trimIndent())
                }},
                "许可" to {

                }
            ))
            }
        }
    }

    @Test fun netWindowSwing() {
        Utils.showNetWindow("http://login.jlu.edu.cn/notice.php")
        Thread.sleep(50000L)
    }


    @Test fun hostInfo() {
        IPUtil.getHostInfo().forEach(::println)
    }

}