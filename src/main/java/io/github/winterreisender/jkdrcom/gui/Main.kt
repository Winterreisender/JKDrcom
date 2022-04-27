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

import androidx.compose.animation.animateContentSize
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.layout.VerticalAlignmentLine
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.max
import androidx.compose.ui.window.*
import io.github.winterreisender.jkdrcom.core.JKDrcomTask
import io.github.winterreisender.jkdrcom.core.util.HostInfo
import io.github.winterreisender.jkdrcom.core.util.IPUtil
import io.github.winterreisender.jkdrcom.core.util.JKDNotification
import isValidMacAddress
import kotlinx.coroutines.*
import showNetWindow
import java.awt.Desktop
import java.net.URI
import javax.swing.JOptionPane
import javax.swing.UIManager

val appConfig = AppConfig.getDummyAppConfig()

enum class AppStatus {
    IDLE,
    CONNECTING
}

@Composable
fun IdlePage(setAppStatus :(status :AppStatus)->Unit = {}) {
    val scope = rememberCoroutineScope()

    var username by remember { mutableStateOf(appConfig.username) }
    var password by remember { mutableStateOf(appConfig.password) }
    var macAddress by remember { mutableStateOf(appConfig.macAddress) }
    var hostName by remember { mutableStateOf(appConfig.hostName) }

    var autoLogin by remember { mutableStateOf(appConfig.autoLogin) }
    var rememberPassword by remember { mutableStateOf(appConfig.rememberPassword) }

    var hostMenuExpand by remember { mutableStateOf(false) }

    var hostInfos = remember { listOf<HostInfo>() }

    Card(Modifier.fillMaxSize().padding(16.dp)) {
        Column(Modifier.fillMaxSize().padding(16.dp).animateContentSize(), verticalArrangement = Arrangement.SpaceAround, horizontalAlignment = Alignment.CenterHorizontally) {
            OutlinedTextField(username,{username = it}, label = {Text("用户名")})
            OutlinedTextField(password,{password = it}, label = {Text("密码")},visualTransformation = PasswordVisualTransformation('*'))
            OutlinedTextField(hostName,{hostName = it}, label = {Text("计算机名称")}, isError = hostName.isEmpty())
            OutlinedTextField(macAddress,{macAddress = it}, label = {Text("MAC")}, isError = !macAddress.isValidMacAddress(),
                trailingIcon = {
                    var isLoading by remember { mutableStateOf(false) }
                    Button(onClick = {
                        isLoading = true
                        Thread {
                            hostInfos = IPUtil.getHostInfo() //CPU密集
                            hostMenuExpand=true
                            isLoading = false
                        }.start()
                        },
                        modifier = Modifier.padding(5.dp),
                        enabled = !isLoading,
                        content = {
                            if (isLoading) {
                                CircularProgressIndicator(Modifier.size(18.dp))
                            }else{
                                Text("检测")
                            }
                        }
                    )
                })

            DropdownMenu(hostMenuExpand, {hostMenuExpand=false}, modifier = Modifier.fillMaxWidth(0.62f)) {
                hostInfos.forEach {
                    DropdownMenuItem(
                        onClick = {
                            macAddress = it.macHexDash
                            hostName = it.hostname
                            hostMenuExpand=false
                        },
                        content = {
                            Text(it.toString(), maxLines = 1, fontSize = 0.8.em, overflow = TextOverflow.Ellipsis)
                        }
                    )
                }
            }


            Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(autoLogin,{autoLogin = it})
                    Text("自动登录")
                }
                Row(verticalAlignment = Alignment.CenterVertically)  {
                    Checkbox(rememberPassword,{rememberPassword = it})
                    Text("记住密码")
                }
            }
            Row {
                Button(
                    onClick =  {
                        appConfig.set(username,password,macAddress,hostName,autoLogin,rememberPassword, 8)
                        setAppStatus(AppStatus.CONNECTING)
                    },
                    enabled = macAddress.isValidMacAddress(),
                    content = {
                        Text("登录")
                    }
                )
            }
        }
    }
}

@Composable
fun ConnectingPage(appConfig: AppConfig, setStatus: (AppStatus) -> Unit) {
    var task :JKDrcomTask? = null
    val scope = rememberCoroutineScope()

    // 处理线程的副作用
    var threadNotification :JKDNotification by remember { mutableStateOf(JKDNotification.NOTHING) } // 线程返回的通知,如CHALLENGING
    var guiText by remember { mutableStateOf("") }
    LaunchedEffect(threadNotification) {
        guiText = threadNotification.toString()

        when(threadNotification) {
            JKDNotification.EXITED -> {
                //TrayState().sendNotification(Notification("JKDrcom","已断开",Notification.Type.Info))
            }
            JKDNotification.KEEPING_ALIVE -> {
                showNetWindow();
                //TrayState().sendNotification(Notification("JKDrcom","已连接",Notification.Type.Info))
            }
            else -> {}
        }

    }

    // 页面首次渲染时启动网络线程
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            task = JKDrcomTask(appConfig.username, appConfig.password, appConfig.toHostInfo(), maxRetry = appConfig.maxRetry ,{threadNotification = it})
            val thread = Thread(task)
            thread.start()
            thread.join()
            threadNotification = JKDNotification.EXITED
            delay(2000L)
            setStatus(AppStatus.IDLE)
        }
    }

    Card(Modifier.fillMaxSize().padding(16.dp)) {
        Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.SpaceAround, horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Text(guiText)
            Button(
                onClick = {
                    scope.launch{
                        task?.notifyLogout()
                    }
                    setStatus(AppStatus.IDLE)
                },
                content = {
                    Text("注销")
                }
            )
        }
    }


}
@Preview
@Composable
fun AppPage() {
    val (status,setStatus) = remember { mutableStateOf(if (appConfig.autoLogin) AppStatus.CONNECTING else AppStatus.IDLE) }

    when(status) {
        AppStatus.IDLE -> IdlePage(setStatus)
        AppStatus.CONNECTING -> ConnectingPage(appConfig, setStatus)
    }

}

fun main(args :Array<String>) {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
    appConfig.readFromFile()
    println(appConfig)

    application {
        var windowVisible by remember { mutableStateOf(true) }
        MaterialTheme {
            val trayState = rememberTrayState()
            val notification = rememberNotification("测试","信息",Notification.Type.Warning)
            Tray(painterResource("logo.png"), onAction = {windowVisible=true}) {

                if(!windowVisible)
                    Item("显示 Show") {
                        windowVisible = true
                    }
                else
                    Item("隐藏 Hide") {
                        windowVisible = false
                    }

                Item("退出 Exit") {
                    exitApplication()
                }
                Item("测试 Test") {
                    trayState.sendNotification(notification)
                }
            }

            Window({ appConfig.saveToFile(); exitApplication()}, rememberWindowState(size = DpSize(600.dp,500.dp)),windowVisible, title = "JKDrcom",icon = painterResource("logo.png")) {
                MenuBar {
                    Menu("功能") {
                        Item("校园网之窗") {
                            showNetWindow()
                        }
                    }
                    Menu("帮助") {
                        Item("网址") {
                            Desktop.getDesktop().browse(URI("https://github.com/Winterreisender/JKDrcom"))
                        }
                        Item("讨论/报告Bug") {
                            Desktop.getDesktop().browse(URI("https://github.com/Winterreisender/JKDrcom/discussions"))
                        }
                        Item("关于") {
                            JOptionPane.showMessageDialog(ComposeWindow(),"JKDrcom v0.2.0. \n Inspired and Powered by DrcomJava")
                        }
                    }
                }

                AppPage()
            }
        }
    }
}