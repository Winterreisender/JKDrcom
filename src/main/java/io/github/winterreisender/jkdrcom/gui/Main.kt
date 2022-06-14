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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.window.*
import com.formdev.flatlaf.intellijthemes.FlatMaterialDesignDarkIJTheme
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMaterialLighterContrastIJTheme
import io.github.winterreisender.jkdrcom.core.JKDrcomTask
import io.github.winterreisender.jkdrcom.core.util.HostInfo
import io.github.winterreisender.jkdrcom.core.util.IPUtil
import io.github.winterreisender.jkdrcom.core.util.JKDNotification
import io.github.winterreisender.jkdrcom.gui.MTopMenuBar.MMenu
import io.github.winterreisender.jkdrcom.gui.MTopMenuBar.MMenuBar
import io.github.winterreisender.jkdrcom.gui.MTopMenuBar.MMenuItem
import kotlinx.coroutines.*
import org.jetbrains.skiko.SystemTheme
import org.jetbrains.skiko.currentSystemTheme
import java.awt.Desktop
import java.net.URI
import javax.swing.UIManager

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import io.github.winterreisender.jkdrcom.core.util.JKDCommunication
import java.util.logging.Logger

val appConfig = AppConfig.getDefault()

lateinit var trayState :TrayState
var setWindowVisible :(Boolean)->Unit = {}

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

            var isPasswordVisible by remember { mutableStateOf(false) }
            OutlinedTextField(password,{password = it}, label = {Text(Constants.UIText.Password)}, isError=password.isEmpty(),keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = if(!isPasswordVisible) PasswordVisualTransformation() else VisualTransformation.None,
                trailingIcon = {
                    IconButton({isPasswordVisible = !isPasswordVisible}) {
                        Icon(Icons.Default.Edit,"Show Password")
                    }
                }
            )

            OutlinedTextField(hostName,{hostName = it}, label = {Text(Constants.UIText.HostName)}, isError = hostName.isEmpty())
            OutlinedTextField(macAddress,{macAddress = it}, label = {Text(Constants.UIText.MacAddress)}, isError = !macAddress.matches(Regex("""([A-E,\d]{2}-?){5}([A-E,\d]{2})""",RegexOption.IGNORE_CASE)),
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
                Row(verticalAlignment = Alignment.CenterVertically)  {
                    Checkbox(rememberPassword, {rememberPassword = it; autoLogin = autoLogin and rememberPassword;})
                    Text(Constants.UIText.SavePassword)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(autoLogin, {autoLogin = it}, enabled = rememberPassword) // 必须记住密码才能自动登录
                    Text(Constants.UIText.AutoLogin)
                }
            }
            Row {
                Button(
                    onClick =  {
                        appConfig.set(username,password,macAddress,hostName,autoLogin,rememberPassword)
                        appConfig.saveToFile()
                        setAppStatus(AppStatus.CONNECTING)
                    },
                    //enabled = macAddress.matches("""([A-E,\d]{2}-?){5}([A-E,\d]{2})""".toRegex(RegexOption.IGNORE_CASE)),
                    content = {
                        Text(Constants.UIText.Login)
                    }
                )
            }
        }
    }
}

@Composable
fun ConnectingPage(setStatus: (AppStatus) -> Unit) {
    val scope = rememberCoroutineScope()

    // 处理线程的副作用
    var threadNotification :JKDNotification by remember { mutableStateOf(JKDNotification.NOTHING) } // 线程返回的通知,如CHALLENGING
    var guiText by remember { mutableStateOf("") }
    var timesRemain :Int? by remember { mutableStateOf(null) } // 剩余重试次数

    val jkdCommunication = remember {
        object :JKDCommunication() {
            @Synchronized
            override fun emitNotification(notification: JKDNotification) {
                //super.emitNotification(notification)
                threadNotification = notification
            }
        }
    }

    // 响应线程传出的通知
    LaunchedEffect(threadNotification) {
        guiText = threadNotification.toString()

        when(threadNotification) {
            is JKDNotification.RETRYING -> {
                timesRemain = (threadNotification as JKDNotification.RETRYING).timesRemain
                trayState.sendNotification(Notification(Constants.AppName,Constants.UIText.Retrying(timesRemain!!),Notification.Type.Warning))
            }
            JKDNotification.EXITED -> {
                trayState.sendNotification(Notification(Constants.AppName,Constants.UIText.Disconnected,Notification.Type.Error))
                setStatus(AppStatus.IDLE)
            }
            JKDNotification.KEEPING_ALIVE -> {
                trayState.sendNotification(Notification(Constants.AppName,Constants.UIText.Connected,Notification.Type.Info))
                timesRemain ?: Utils.openNetWindow() // TESTING：如果是在Retry中重试成功则不打开校园网窗
                scope.launch {
                    // 三秒后自动隐藏窗口
                    delay(3000L) // TODO: 自定义等待时间
                    setWindowVisible(false)
                }
            }
            else -> {}
        }

    }

    // 页面首次渲染时启动网络线程
    LaunchedEffect(Unit) {
        println("Creating thread")
        val task = JKDrcomTask(appConfig.username, appConfig.password, appConfig.getHostInfo(), maxRetry = appConfig.maxRetry ,jkdCommunication)
        val thread = Thread(task)
        thread.start()
    }

    Card(Modifier.fillMaxSize().padding(16.dp)) {
        Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.SpaceAround, horizontalAlignment = Alignment.CenterHorizontally) {

            // 进度提示
            when(threadNotification) {
                JKDNotification.KEEPING_ALIVE -> {Icon(Icons.Default.CheckCircle, null, Modifier.size(50.dp))}
                else -> {CircularProgressIndicator(Modifier.size(50.dp))}
            }

            Text(guiText)

            var buttonEnabled by remember { mutableStateOf(true) }
            Button(
                onClick = {
                    scope.launch{
                        jkdCommunication.notifyLogout = true
                        buttonEnabled = false
                        guiText = Constants.UIText.LoggingOut
                    }
                },
                content = {
                    Text(Constants.UIText.Logout)
                },
                enabled = buttonEnabled
            )
        }
    }


}
@Preview
@Composable
// AppPage层往下面的都要保持跨平台
fun AppPage() {
    val (status,setStatus) = remember { mutableStateOf(if (appConfig.autoLogin) AppStatus.CONNECTING else AppStatus.IDLE) }
    //AnimatedContent(status) { // 使用动画会导致LaunchedEffect(Unit)执行两次,貌似是BUG
        when(status) {
            AppStatus.IDLE -> IdlePage(setStatus)
            AppStatus.CONNECTING -> ConnectingPage(setStatus)
        }
    //}
}

fun main(args :Array<String>) {
    appConfig.readFromFile()

    application {
        LaunchedEffect(Unit) {
            Logger.getLogger("Main").info(appConfig.toString())
            UIManager.setLookAndFeel(
                when(currentSystemTheme){ // currentSystemTheme要在Application内运行
                    SystemTheme.LIGHT -> FlatMaterialLighterContrastIJTheme()
                    SystemTheme.DARK -> FlatMaterialDesignDarkIJTheme()
                    else -> FlatMaterialLighterContrastIJTheme()
                }
            )
        }

        val windowState = rememberWindowState(size = DpSize(600.dp,500.dp))
        var windowVisible by remember { mutableStateOf(true) }
        setWindowVisible = {windowVisible = it}  //状态提升到全局

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
                            MMenu(Constants.MenuText.Function) {
                                MMenuItem(Constants.MenuText.Function_SchoolNetWindow) {
                                    Utils.openNetWindow()
                                }

                                MMenuItem(Constants.MenuText.Function_SetMaxRetry) {
                                    when(val r :Int? = Utils.inputBox(Constants.MenuText.Function_SetMaxRetry,Constants.MenuText.Function_SetMaxRetry).toIntOrNull()) {
                                        null -> {
                                            Utils.msgBox(Constants.MenuText.Function_SetMaxRetry_NeedNum,Constants.MenuText.Function_SetMaxRetry)}
                                        in 1..128 -> {appConfig.maxRetry = r}
                                        else -> {
                                            Utils.msgBox(Constants.MenuText.Function_SetMaxRetry_NeedNum,Constants.MenuText.Function_SetMaxRetry)}
                                    }
                                }

                                MMenuItem(Constants.MenuText.Function_ResetConfig) {
                                    with(AppConfig.getDefault()) {
                                        appConfig.set(username, password, macAddress, hostName, autoLogin, rememberPassword)
                                        appConfig.maxRetry = 1 // TODO: maxRetry也用getDefault的值
                                    }
                                    Utils.msgBox(Constants.MenuText.Function_ResetConfig_Done(appConfig.toString()),Constants.MenuText.Function_ResetConfig)
                                }

                                MMenuItem(Constants.MenuText.Function_SaveConfig) {
                                    val r = runCatching {appConfig.saveToFile()}.fold({Constants.MenuText.Function_SaveConfig_Done},{"${Constants.MenuText.Function_SaveConfig_Failed} $it"})
                                    Utils.msgBox(r,Constants.MenuText.Function_SaveConfig)
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
                                    Utils.msgBox(Constants.AppAbout.trimIndent(), Constants.MenuText.Help_About)
                                }
                            }
                        }
                    },
                    /*bottomBar = {
                        BottomAppBar(modifier = Modifier.height(18.dp)) {
                            Text("${Constants.AppName} ${Constants.AppVersion}")
                        }
                    },*/
                    content = {
                        AppPage()
                    }
                )
            }
        }
    }
}
