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

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.window.*
import kotlin.system.exitProcess


/**
 * 一套Material菜单栏组件,包含MenuBar,Menu,MenuItem三个层次,封装了DropdownMenu
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

// TODO: 用Compose(或Swing)写一份托盘菜单,避免GBK乱码问题

object XTopMenuBar {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun WindowScope.XMenuBar(
        title: String,
        windowState: WindowState,
        onIconClicked: ()->Unit = {},
        onExitClicked: ()->Unit = { exitProcess(0)},
        modifier: Modifier = Modifier,
        menus: @Composable () -> Unit = {}
    ) = WindowDraggableArea {
            CenterAlignedTopAppBar(
                modifier = modifier,
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(),
                title = {
                    Text(title)
                },
                navigationIcon = { Row {
                        menus()
                } },
                actions = {
                    IconButton(
                        onClick = { windowState.isMinimized = true },
                        content = { Icon(MinimizeIcon, null) },
                    )
                    IconButton(
                        onClick = { windowState.placement = if (windowState.placement == WindowPlacement.Maximized) WindowPlacement.Floating else WindowPlacement.Maximized },
                        content = { Icon(MaximizeIcon, null) },
                    )
                    IconButton(
                        onClick = { onExitClicked() },
                        content = { Icon(Icons.Default.Close, null) },
                    )
                }
            )}

    interface XMenuScope : ColumnScope {
        // 关闭Menu
        fun collapseMenu()
    }

    @Composable
    fun XMenu(text: String, dropdownMenuItems: @Composable XMenuScope.() -> Unit) {
        var menuExpanded by remember { mutableStateOf(false) }

        Column {
            TextButton( { menuExpanded = true; }) {
                Text(text)
            }// 13px
            DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }, focusable = true,
                //modifier = Modifier.onPointerEvent(PointerEventType.Exit) { menuExpanded = false }
            ) {
                object : XMenuScope, ColumnScope by this {
                    override fun collapseMenu() {
                        menuExpanded = false
                    }
                }.dropdownMenuItems()
            }
        }
    }

    @Composable
    fun XMenuScope.XMenuItem(text: String, onClick: () -> Unit) =
        DropdownMenuItem(
            text = { Text(text, maxLines = 1, fontSize = 0.8125.em) },
                    onClick = { collapseMenu(); onClick();  },
            modifier = Modifier.height(28.dp)
        )

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun XMenuScope.MMenuToggle(text: String, checked :Boolean, onClick: (Boolean) -> Unit) =
        DropdownMenuItem(
            onClick = {  },
            modifier = Modifier.height(28.dp),
            text = {Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(text, maxLines = 1, fontSize = 0.8125.em)
                Checkbox(checked,onClick, modifier = Modifier.padding(0.dp).size(14.dp))
            }}
        )


    interface XSubmenuScope : RowScope {
        // 关闭Menu
        fun collapseMenu()
    }

    //@OptIn(ExperimentalComposeUiApi::class)
    @Composable
    fun XMenuScope.XSubMenu(text: String, dropdownMenuItems: @Composable XSubmenuScope.() -> Unit) {
        var menuExpanded by remember { mutableStateOf(false) }
        DropdownMenuItem(onClick = { menuExpanded = true },modifier = Modifier.height(28.dp),
            //.onPointerEvent(PointerEventType.Enter) { menuExpanded = true }
            text = {
            Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(text, maxLines = 1, fontSize = 0.8125.em)
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = null)
            }
            Row {
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }, focusable = true,offset = DpOffset(10.dp, (-10).dp)
                    //modifier = Modifier.onPointerEvent(PointerEventType.Exit) { menuExpanded = false }
                ) {
                    object : XSubmenuScope, RowScope by this@Row {
                        override fun collapseMenu() {
                            this@XSubMenu.collapseMenu()
                        }
                    }.dropdownMenuItems()
                }
            }
        }
        )
    }

    //@OptIn(ExperimentalComposeUiApi::class)
    @Composable
    fun XSubmenuScope.XSubMenu(text: String, dropdownMenuItems: @Composable XSubmenuScope.() -> Unit) {
        var menuExpanded by remember { mutableStateOf(false) }
        DropdownMenuItem(onClick = { menuExpanded = true },modifier = Modifier.height(28.dp),
            //.onPointerEvent(PointerEventType.Enter) { menuExpanded = true }
            text = {
                Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text(text, maxLines = 1, fontSize = 0.8125.em)
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, modifier = Modifier.fillMaxHeight())
                }
                Row {
                    DropdownMenu(
                        expanded = menuExpanded, onDismissRequest = { menuExpanded = false }, focusable = true, offset = DpOffset(10.dp, (-10).dp)
                        //modifier = Modifier.onPointerEvent(PointerEventType.Exit) { menuExpanded = false }
                    ) {
                        object : XSubmenuScope, RowScope by this@Row {
                            override fun collapseMenu() {
                                this@XSubMenu.collapseMenu()
                            }
                        }.dropdownMenuItems()
                    }
                }
            }
        )
    }

    @Composable
    fun XSubmenuScope.XMenuItem(text: String, onClick: () -> Unit) =
        DropdownMenuItem(
            onClick = { onClick(); collapseMenu() },
            modifier = Modifier.height(28.dp),
            text = { Text(text, maxLines = 1, fontSize = 0.8125.em) }
    )


    /** 窗口右上角最小化按钮的图标 */
    private val MinimizeIcon by lazy { materialIcon(name = "Minimize") {
        materialPath {
            moveTo(4.0f, 12.0f)
            horizontalLineToRelative(16.0f)
            verticalLineToRelative(-2.0f)
            lineTo(4.0f, 10.0f)
            verticalLineToRelative(2.0f)
            close()
        }
    }}

    /** 窗口右上角最大化按钮的图标 */
    private val MaximizeIcon  by lazy { materialIcon(name = "MaximizeIcon") {
        materialPath {
            moveTo(16.0f, 8.0f)
            verticalLineToRelative(8.0f)
            horizontalLineTo(8.0f)
            verticalLineTo(8.0f)
            horizontalLineToRelative(8.0f)
            moveToRelative(2.0f, -2.0f)
            horizontalLineTo(6.0f)
            verticalLineToRelative(12.0f)
            horizontalLineToRelative(12.0f)
            verticalLineTo(6.0f)
            close()
        }
    }}
}

