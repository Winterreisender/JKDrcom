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

package io.github.winterreisender.jkdrcom.gui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.window.*
import kotlin.system.exitProcess


/**
 * 一套Material菜单栏组件,包含MenuBar,Menu,MenuItem三个层次
 *
 * example:
* ```kt
* Scaffold(
*     topBar = {
*         MMenuBar("Jetpack Compose Demo", windowState) {
*             MMenu("文件") {
*                 MMenuItem("新建") {
*             }
*             MMenuItem("打开") {
*                 pagination = Pages.HOME
*             }
*             Divider()
*             MMenuItem("退出") {
*                exitApplication()
*             }
*         }
*     }
* )
```
 *
 * @author Winterreisender
 * @version 0.1.0
 */

// TODO: 用Compose(或Swing)写一份托盘菜单,避免GBK乱码问题, 仿照https://blog.csdn.net/GOGO_912/article/details/115712634

object MTopMenuBar {
    @Composable
    fun WindowScope.MMenuBar(
        title: String,
        windowState: WindowState,
        onIconClicked: ()->Unit = {},
        onExitClicked: ()->Unit = { exitProcess(0)},
        modifier: Modifier = Modifier.height(32.dp),
        menus: @Composable () -> Unit = {}
    ) = TopAppBar(modifier = modifier) {
        //val coroutineScope = rememberCoroutineScope()
        WindowDraggableArea {
            Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Row(horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            onIconClicked()
                        },
                        content = { Icon(Icons.Default.Menu, null) }
                    )
                    menus()
                }

                Text(title, maxLines = 1)

                // TODO: 替换最小化、最大化图标。不要引入图片文件。
                Row(horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { windowState.isMinimized = true },
                        content = { Icon(Icons.Default.ArrowDropDown, null) }
                    )
                    IconButton(
                        onClick = { windowState.placement = if (windowState.placement == WindowPlacement.Maximized) WindowPlacement.Floating else WindowPlacement.Maximized },
                        content = { Icon(Icons.Default.Add, null) }
                    )
                    IconButton(
                        onClick = { onExitClicked() },
                        content = { Icon(Icons.Default.Close, null) }
                    )
                }
            }
        }
    }

    interface MMenuScope : ColumnScope {
        // 关闭Menu
        fun collapseMenu()
    }

    @Composable
    fun MMenu(text: String, dropdownMenuItems: @Composable MMenuScope.() -> Unit) {
        var menuExpanded by remember { mutableStateOf(false) }
        Column {
            Text(text, modifier = Modifier.padding(6.dp, 0.dp).clickable { menuExpanded = true; }, maxLines = 1, fontSize = 1.em)
            DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }, focusable = true,
                //modifier = Modifier.onPointerEvent(PointerEventType.Exit) { menuExpanded = false }
                ) {
                object : MMenuScope, ColumnScope by this { // 委托 GREAT! C++ 可用using
                    override fun collapseMenu() {
                        menuExpanded = false
                    }
                }.dropdownMenuItems()
            }
        }
    }

    @Composable
    fun MMenuScope.MMenuItem(text: String, onClick: () -> Unit) =
        DropdownMenuItem(
            onClick = { onClick(); collapseMenu() },
            modifier = Modifier.height(28.dp)
        ) {
            Text(text, maxLines = 1)
        }

    @Composable
    fun MMenuScope.MMenuToggle(text: String, checked :Boolean, onClick: (Boolean) -> Unit) =
        DropdownMenuItem(
            onClick = {  },
            modifier = Modifier.height(28.dp)
        ) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(text, maxLines = 1)
                Checkbox(checked,onClick, modifier = Modifier.padding(0.dp).size(14.dp))
            }

        }


    interface MSubmenuScope : RowScope {
        // 关闭Menu
        fun collapseMenu()
    }

    //@OptIn(ExperimentalComposeUiApi::class)
    @Composable
    fun MMenuScope.MSubMenu(text: String, dropdownMenuItems: @Composable MSubmenuScope.() -> Unit) {
        var menuExpanded by remember { mutableStateOf(false) }
        DropdownMenuItem(onClick = { menuExpanded = true },modifier = Modifier.height(28.dp)
            //.onPointerEvent(PointerEventType.Enter) { menuExpanded = true }
        ) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(text, maxLines = 1)
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = null)
            }
            Row {
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }, focusable = true,offset = DpOffset(10.dp, (-10).dp)
                    //modifier = Modifier.onPointerEvent(PointerEventType.Exit) { menuExpanded = false }
                    ) {
                    object : MSubmenuScope, RowScope by this@Row {
                        override fun collapseMenu() {
                            this@MSubMenu.collapseMenu()
                        }
                    }.dropdownMenuItems()
                }
            }
        }
    }

    //@OptIn(ExperimentalComposeUiApi::class)
    @Composable
    fun MSubmenuScope.MSubMenu(text: String, dropdownMenuItems: @Composable MSubmenuScope.() -> Unit) {
        var menuExpanded by remember { mutableStateOf(false) }
        DropdownMenuItem(onClick = { menuExpanded = true },modifier = Modifier.height(28.dp)
            //.onPointerEvent(PointerEventType.Enter) { menuExpanded = true }
        ) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(text, maxLines = 1)
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, modifier = Modifier.fillMaxHeight())
            }
            Row {
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }, focusable = true, offset = DpOffset(10.dp, (-10).dp)
                    //modifier = Modifier.onPointerEvent(PointerEventType.Exit) { menuExpanded = false }
                ) {
                    object : MSubmenuScope, RowScope by this@Row {
                        override fun collapseMenu() {
                            this@MSubMenu.collapseMenu()
                        }
                    }.dropdownMenuItems()
                }
            }
        }
    }

    @Composable
    fun MSubmenuScope.MMenuItem(text: String, onClick: () -> Unit) =
        DropdownMenuItem(
            onClick = { onClick(); collapseMenu() },
            modifier = Modifier.height(28.dp)
        ) {
            Text(text, maxLines = 1)
        }
}

