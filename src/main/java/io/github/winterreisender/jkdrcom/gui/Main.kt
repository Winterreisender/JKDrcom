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
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.window.*
import io.github.winterreisender.jkdrcom.core.JKDrcomTask
import io.github.winterreisender.jkdrcom.core.util.HostInfo
import io.github.winterreisender.jkdrcom.core.util.IPUtil
import io.github.winterreisender.jkdrcom.core.util.JKDNotification
import io.github.winterreisender.jkdrcom.gui.MTopMenuBar.MMenu
import io.github.winterreisender.jkdrcom.gui.MTopMenuBar.MMenuBar
import io.github.winterreisender.jkdrcom.gui.MTopMenuBar.MMenuItem
import isValidMacAddress
import kotlinx.coroutines.*
import openNetWindow
import java.awt.Desktop
import java.net.URI
import javax.swing.JOptionPane
import javax.swing.UIManager

val appConfig = AppConfig.getDefault()

lateinit var trayState :TrayState

enum class AppStatus {
    IDLE,
    CONNECTING
}

@Composable
fun IdlePage(setAppStatus :(status :AppStatus)->Unit = {}) {

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
            OutlinedTextField(username,{username = it}, label = {Text(Constants.UIText.Username)}, isError = !username.matches(Regex("""^\S+${'$'}""")))
            OutlinedTextField(password,{password = it}, label = {Text(Constants.UIText.Password)},visualTransformation = PasswordVisualTransformation('*'))
            OutlinedTextField(hostName,{hostName = it}, label = {Text(Constants.UIText.HostName)}, isError = hostName.isEmpty())
            OutlinedTextField(macAddress,{macAddress = it}, label = {Text(Constants.UIText.MacAddress)}, isError = !macAddress.isValidMacAddress(),
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
                                CircularProgressIndicator(Modifier.size(20.dp))
                            }else{
                                Text(Constants.UIText.DetectMac)
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
                    Text(Constants.UIText.AutoLogin)
                }
                Row(verticalAlignment = Alignment.CenterVertically)  {
                    Checkbox(rememberPassword,{rememberPassword = it})
                    Text(Constants.UIText.SavePassword)
                }
            }
            Row {
                Button(
                    onClick =  {
                        appConfig.set(username,password,macAddress,hostName,autoLogin,rememberPassword)
                        appConfig.saveToFile()
                        setAppStatus(AppStatus.CONNECTING)
                    },
                    enabled = macAddress.isValidMacAddress(),
                    content = {
                        Text(Constants.UIText.Login)
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
            is JKDNotification.RETRYING -> {
                val (timesRemain, _) = (threadNotification as JKDNotification.RETRYING)
                trayState.sendNotification(Notification(Constants.AppName,Constants.UIText.Retrying(timesRemain),Notification.Type.Warning))
            }
            JKDNotification.EXITED -> {
                trayState.sendNotification(Notification(Constants.AppName,Constants.UIText.Disconnected,Notification.Type.Error))
            }
            JKDNotification.KEEPING_ALIVE -> {
                trayState.sendNotification(Notification(Constants.AppName,Constants.UIText.Connected,Notification.Type.Info))
                openNetWindow()
            }
            else -> {}
        }

    }

    // 页面首次渲染时启动网络线程
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            task = JKDrcomTask(appConfig.username, appConfig.password, appConfig.getHostInfo(), maxRetry = appConfig.maxRetry ,{threadNotification = it})
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
            CircularProgressIndicator() //  LinearProgressIndicator() and CircularProgressIndicator() have high cpu cost. Wait for further Compose version.

            Text(guiText)
            Button(
                onClick = {
                    scope.launch{
                        task?.notifyLogout()
                    }
                    setStatus(AppStatus.IDLE)
                },
                content = {
                    Text(Constants.UIText.Logout)
                }
            )
        }
    }


}
@Preview
@Composable
// AppPage层往下面的都要保持跨平台
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
        val windowState = rememberWindowState(size = DpSize(600.dp,500.dp))
        var windowVisible by remember { mutableStateOf(true) }

        trayState = rememberTrayState()
        Tray(painterResource("logo.png"), trayState, onAction = {windowVisible=true}) {
            if(!windowVisible)
                Item(Constants.MenuText.Tray_Show) {
                    windowVisible = true
                }
            else
                Item(Constants.MenuText.Tray_Hide) {
                    windowVisible = false
                }

            Item(Constants.MenuText.Tray_Exit) {
                exitApplication()
            }
        }

        MaterialTheme(colors = if (isSystemInDarkTheme()) darkColors() else lightColors()) {
            Window({exitApplication()}, windowState,windowVisible, title = Constants.AppName,icon = painterResource("logo.png"),undecorated = true) {
                Scaffold(
                    //modifier = Modifier.clip(RoundedCornerShape(5.dp)),
                    topBar = {
                        MMenuBar(Constants.AppName,windowState, onExitClicked = { appConfig.saveToFile(); exitApplication() }) {
                            val msgBox = { text :String, title :String -> JOptionPane.showMessageDialog(ComposeWindow(),text,title,JOptionPane.INFORMATION_MESSAGE) }
                            val inputBox = {text :String, title :String -> JOptionPane.showInputDialog(ComposeWindow(),text,title,JOptionPane.INFORMATION_MESSAGE)}

                            MMenu(Constants.MenuText.Function) {
                                MMenuItem(Constants.MenuText.Function_SchoolNetWindow) {
                                    openNetWindow()
                                }

                                MMenuItem(Constants.MenuText.Function_SetMaxRetry) {
                                    when(val r :Int? = inputBox(Constants.MenuText.Function_SetMaxRetry,Constants.MenuText.Function_SetMaxRetry)?.toIntOrNull()) {
                                        null -> {msgBox(Constants.MenuText.Function_SetMaxRetry_NeedNum,Constants.MenuText.Function_SetMaxRetry)}
                                        in 1..128 -> {appConfig.maxRetry = r}
                                        else -> {msgBox(Constants.MenuText.Function_SetMaxRetry_NeedNum,Constants.MenuText.Function_SetMaxRetry)}
                                    }
                                }

                                MMenuItem(Constants.MenuText.Function_ResetConfig) {
                                    with(AppConfig.getDefault()) {
                                        appConfig.set(username, password, macAddress, hostName, autoLogin, rememberPassword)
                                        appConfig.maxRetry = 1
                                    }
                                    msgBox(Constants.MenuText.Function_ResetConfig_Done(appConfig.toString()),Constants.MenuText.Function_ResetConfig)
                                }

                                MMenuItem(Constants.MenuText.Function_SaveConfig) {
                                    val r = runCatching {appConfig.saveToFile()}.fold({Constants.MenuText.Function_SaveConfig_Done},{"${Constants.MenuText.Function_SaveConfig_Failed} $it"})
                                    msgBox(r,Constants.MenuText.Function_SaveConfig)
                                }

                                MMenuItem(Constants.MenuText.Function_HideWindow) {
                                    windowVisible = false
                                }
                            }
                            MMenu(Constants.MenuText.Help) {
                                MMenuItem(Constants.MenuText.Help_HomePage) {
                                    Desktop.getDesktop().browse(URI(Constants.AppHomepage))
                                }
                                MMenuItem(Constants.MenuText.Help_Feedback) {
                                    Desktop.getDesktop().browse(URI(Constants.AppFeedback))
                                }
                                Divider()
                                MMenuItem(Constants.MenuText.Help_About) {
                                    JOptionPane.showMessageDialog(ComposeWindow(),Constants.AppAbout.trimIndent())
                                }
                            }
                        }
                    },
                    bottomBar = {
                        BottomAppBar(modifier = Modifier.height(18.dp)) {
                            Text("${Constants.AppName} ${Constants.AppVersion}")
                        }
                    },
                    content = {
                        AppPage()
                    }
                )
            }
        }
    }
}