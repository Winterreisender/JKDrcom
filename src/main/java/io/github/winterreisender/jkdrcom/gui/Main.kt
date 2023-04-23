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

import androidx.compose.animation.animateContentSize
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material.MaterialTheme as Material2Theme
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.window.*
import com.formdev.flatlaf.FlatDarkLaf
import com.formdev.flatlaf.FlatLightLaf
import io.github.winterreisender.jkdrcom.core.JKDrcomTask
import io.github.winterreisender.jkdrcom.core.util.HostInfo
import io.github.winterreisender.jkdrcom.core.util.IPUtil
import io.github.winterreisender.jkdrcom.core.util.JKDCommunication
import io.github.winterreisender.jkdrcom.core.util.JKDNotification
import io.github.winterreisender.jkdrcom.gui.XTopMenuBar.XMenu
import io.github.winterreisender.jkdrcom.gui.XTopMenuBar.XMenuBar
import io.github.winterreisender.jkdrcom.gui.XTopMenuBar.XMenuItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.skiko.SystemTheme
import org.jetbrains.skiko.currentSystemTheme
import java.awt.Desktop
import java.awt.Dimension
import java.net.URI
import java.util.logging.Logger
import javax.swing.JColorChooser
import javax.swing.JOptionPane

/** 用户配置类 */
var appConfig = AppConfig.loadFromFile()

/** 托盘状态,用以发送通知 */
lateinit var trayState :TrayState

/** 设置窗口可见,用于菜单栏中的隐藏窗口 */
var setWindowVisible :(Boolean)->Unit = {}

/** App状态,用以切换页面 */
enum class AppStatus {
    /** 空闲,属于用户名和密码等等的状态 */
    IDLE,
    /** 连接中 */
    CONNECTING
}

/** 空闲状态的界面 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdlePage(setAppStatus :(status :AppStatus)->Unit = {}) {

    var username   by remember { mutableStateOf(appConfig.username)   }
    var password   by remember { mutableStateOf(appConfig.password)   }
    var macAddress by remember { mutableStateOf(appConfig.macAddress) }
    var hostName   by remember { mutableStateOf(appConfig.hostName)   }

    var autoLogin        by remember { mutableStateOf(appConfig.autoLogin)        }
    var rememberPassword by remember { mutableStateOf(appConfig.rememberPassword) }

    var hostMenuExpand by remember { mutableStateOf(false) }

    // 自动检测到的MAC地址和主机名
    var hostInfos = remember { listOf<HostInfo>() }

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
                trailingIcon = { // 自动检测MAC地址和主机名的按钮
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
            Material2Theme(colors = if(isSystemInDarkTheme()) darkColors() else lightColors()) {
            // 检测到的MAC地址和主机名
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
                        appConfig.let {
                            it.username = username
                            it.password = password
                            it.macAddress = macAddress
                            it.hostName = hostName
                            it.autoLogin = autoLogin
                            it.rememberPassword = rememberPassword
                            it.saveToFile()
                        }
                        setAppStatus(AppStatus.CONNECTING)
                    },
                    enabled = macAddress.matches("""([A-E,\d]{2}-?){5}([A-E,\d]{2})""".toRegex(RegexOption.IGNORE_CASE)) && password.isNotEmpty(),
                    content = {
                        Text(Constants.UIText.Login)
                    }
                )
            }
        }

}

/** 连接状态的页面 */
@OptIn(ExperimentalMaterial3Api::class)
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

                if(timesRemain == null)
                    // 打开校园网之窗
                    when(appConfig.netWindow) {
                        AppConfig.NetWindowType.NONE -> {}
                        AppConfig.NetWindowType.WINDOWED -> { Utils.showNetWindow(closeAfterSecs = appConfig.closeAfterSecs) }
                        AppConfig.NetWindowType.BROWSER -> { Utils.openNetWindow() }
                    }
                // 自动隐藏窗口
                delay(appConfig.closeAfterSecs * 1000L)
                setWindowVisible(false)
            }
            else -> {}
        }

    }

    // 页面首次渲染时启动网络线程
    LaunchedEffect(Unit) {
        println("Creating thread")
        println(appConfig)
        val task = JKDrcomTask(appConfig.username, appConfig.password, appConfig.getHostInfo(), maxRetry = appConfig.maxRetry ,jkdCommunication)
        val thread = Thread(task)
        thread.start()
    }

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


/** 主页面 */
@Preview
@Composable
// AppPage层往下面的都要保持跨平台
fun AppPage() {
    val (status,setStatus) = remember { mutableStateOf(if (appConfig.autoLogin && appConfig.password.isNotEmpty()) AppStatus.CONNECTING else AppStatus.IDLE) }
    //AnimatedContent(status) { // 使用动画会导致LaunchedEffect(Unit)执行两次,貌似是BUG
        when(status) {
            AppStatus.IDLE -> IdlePage(setStatus)
            AppStatus.CONNECTING -> ConnectingPage(setStatus)
        }
    //}
}

@OptIn(ExperimentalMaterial3Api::class)
fun main() {
    application {
        LaunchedEffect(Unit) {
            Logger.getLogger("Main").info(appConfig.toString())
            // 设置Swing的夜间模式
            when(currentSystemTheme){       // currentSystemTheme要在Application内运行
                SystemTheme.LIGHT -> FlatLightLaf.setup()
                SystemTheme.DARK  -> FlatDarkLaf.setup()
                else              -> FlatLightLaf.setup()
            }
        }

        val windowState = rememberWindowState(size = DpSize(600.dp,500.dp))
        var windowVisible by remember { mutableStateOf(true) }
        setWindowVisible = {windowVisible = it}  //状态提升到全局
        trayState = rememberTrayState()

        Tray(painterResource("logo.svg"), trayState, onAction = {windowVisible=true}) {
            if(!windowVisible)
                Item(Constants.MenuText.Tray_Show) {
                    windowVisible = true
                    windowState.isMinimized = false
                }
            else
                Item(Constants.MenuText.Tray_Hide) {
                    windowVisible = false
                }

            Item(Constants.MenuText.Tray_Exit) {
                exitApplication()
            }
        }

        var primaryColorState by remember { mutableStateOf(appConfig.getPrimaryColor().toCompose()) }

        MaterialTheme(colorScheme = if (isSystemInDarkTheme()) darkColorScheme(primaryColorState) else lightColorScheme(primaryColorState)) {
            Window({exitApplication()}, windowState,windowVisible, title = Constants.AppName,icon = painterResource("logo.svg"),undecorated = true) {
                window.minimumSize = Dimension(Constants.MinWindowSizeX,Constants.MinWindowSizeY)
                Scaffold(
                    //modifier = Modifier.clip(RoundedCornerShape(5.dp)),
                    modifier = Modifier.border(1.dp, color = MaterialTheme.colorScheme.primary),
                    topBar = {
                        XMenuBar(Constants.AppName,windowState, onExitClicked = { appConfig.saveToFile(); exitApplication() }) { // modifier = Modifier.height(32.dp).background(Brush.horizontalGradient(listOf(Color(0xFF00B4DB), Color(0xFF0083b0))))
                            XMenu(Constants.MenuText.Function) {
                                XMenuItem(Constants.MenuText.Function_SchoolNetWindow) {
                                    when(appConfig.netWindow) {
                                        AppConfig.NetWindowType.WINDOWED -> {Utils.showNetWindow()}
                                        AppConfig.NetWindowType.NONE -> {Utils.showNetWindow()}
                                        AppConfig.NetWindowType.BROWSER -> {Utils.openNetWindow()}
                                    }
                                }

                                XMenuItem(Constants.MenuText.Function_SchoolNetInfo) {
                                    Utils.openNetWindow(Constants.SchoolNetInfoURL)
                                }

                                XMenuItem(Constants.MenuText.Function_ServiceHall) {
                                    Utils.openNetWindow(Constants.ServiceHallURL)
                                }

                                XMenuItem(Constants.MenuText.Function_NetGuide) {
                                    Utils.openNetWindow(Constants.NetGuideURL)
                                }

                                XMenuItem(Constants.MenuText.Function_HideWindow) {
                                    windowVisible = false
                                }
                            }

                            XMenu(Constants.MenuText.Settings) {
                                XMenuItem(Constants.MenuText.Function_CloseAfterSecs) {
                                    when(val r :Int? = Utils.inputBox(Constants.MenuText.Function_CloseAfterSecs_Text,appConfig.closeAfterSecs.toString()).toIntOrNull()) {
                                        in -1..3600 -> {appConfig.closeAfterSecs = r!!}
                                        else -> {
                                            Utils.msgBox(Constants.MenuText.Function_CloseAfterSecs_NeedNum,Constants.MenuText.Function_CloseAfterSecs)}
                                    }
                                }

                                XMenuItem(Constants.MenuText.Function_NetWindowType) {
                                    val windowTypes = AppConfig.NetWindowType.values()
                                    val chosen = Utils.chooseBox(Constants.MenuText.Function_NetWindowType,windowTypes, appConfig.netWindow, title = Constants.MenuText.Function_NetWindowType)
                                    appConfig.netWindow = chosen
                                }

                                XMenuItem(Constants.MenuText.Function_SetThemeColor) {
                                    val jColor = JColorChooser.showDialog(ComposeWindow(),Constants.MenuText.Function_SetThemeColor,Constants.DefaultPrimaryColor.toAwt()) ?: return@XMenuItem
                                    appConfig.mainColor = Utils.WebColor.from(jColor).toString()
                                    primaryColorState = appConfig.getPrimaryColor().toCompose()
                                }

                                XMenuItem(Constants.MenuText.Function_SetMaxRetry) {
                                    when(val r :Int? = Utils.inputBox(Constants.MenuText.Function_SetMaxRetry, appConfig.maxRetry.toString()).toIntOrNull()) {
                                        null -> {
                                            Utils.msgBox(Constants.MenuText.Function_SetMaxRetry_NeedNum,Constants.MenuText.Function_SetMaxRetry)}
                                        in 1..128 -> {appConfig.maxRetry = r}
                                        else -> {
                                            Utils.msgBox(Constants.MenuText.Function_SetMaxRetry_NeedNum,Constants.MenuText.Function_SetMaxRetry)}
                                    }
                                }

                                Divider()

                                XMenuItem(Constants.MenuText.Function_EditConfig) {
                                    try {
                                        Desktop.getDesktop().open(AppConfig.configFile)
                                    } catch (e :Exception) {
                                        Utils.msgBox("""
                                            ${e.localizedMessage}
                                            ${AppConfig.configFile.absolutePath}
                                            """.trimIndent(),
                                            "Warning"
                                        )
                                    }
                                }

                                XMenuItem(Constants.MenuText.Function_ResetConfig) {
                                    appConfig = AppConfig()
                                    Utils.msgBox(Constants.MenuText.Function_ResetConfig_Done + ":\n" + appConfig.toString(),Constants.MenuText.Function_ResetConfig)
                                }

                                XMenuItem(Constants.MenuText.Function_SaveConfig) {
                                    val r = runCatching {appConfig.saveToFile()}.fold({Constants.MenuText.Function_SaveConfig_Done},{"${Constants.MenuText.Function_SaveConfig_Failed} $it"})
                                    Utils.msgBox(r,Constants.MenuText.Function_SaveConfig)
                                }
                            }

                            XMenu(Constants.MenuText.Help) {
                                XMenuItem(Constants.MenuText.Help_HomePage) {
                                    Desktop.getDesktop().browse(URI(Constants.AppHomepage))
                                }
                                XMenuItem(Constants.MenuText.Help_Feedback) {
                                    Desktop.getDesktop().browse(URI(Constants.AppFeedback))
                                }
                                Divider()
                                XMenuItem(Constants.MenuText.Help_Credits) {
                                    Desktop.getDesktop().browse(URI(Constants.MenuText.Help_Credits_URL))
                                }
                                XMenuItem(Constants.MenuText.Help_About) {
                                    Utils.msgBox(Constants.AppAbout.trimIndent(), Constants.MenuText.Help_About)
                                }
                            }
                        }
                    },
                    content = {
                        Column(Modifier.fillMaxSize()) {
                            Spacer(Modifier.padding(20.dp))
                            AppPage()
                        }
                    }
                )
            }
        }
    }
}
