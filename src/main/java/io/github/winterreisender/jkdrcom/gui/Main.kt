package io.github.winterreisender.jkdrcom.gui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import io.github.winterreisender.jkdrcom.core.JKDrcomTask
import io.github.winterreisender.jkdrcom.core.util.HostInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.Desktop
import java.net.URI
import javax.swing.JOptionPane
import javax.swing.UIManager

object ComposeUtils {
}



val coreConfig = CoreConfig("", "", "")
val guiConfig = GUIConfig(false, false)


enum class AppStatus {
    IDLE,
    CONNECTING
}

@Composable
fun IdlePage(setAppStatus :(status :AppStatus)->Unit = {}) {
    var username by remember { mutableStateOf(coreConfig.username) }
    var password by remember { mutableStateOf(coreConfig.password) }
    var macAddress by remember { mutableStateOf(coreConfig.macAddress) }

    var autoLogin by remember { mutableStateOf(false) }
    var rememberPassword by remember { mutableStateOf(false) }


    Card(Modifier.fillMaxSize().padding(16.dp)) {
        Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.SpaceAround, horizontalAlignment = Alignment.CenterHorizontally) {
            OutlinedTextField(username,{username = it}, label = {Text("用户名")})
            OutlinedTextField(password,{password = it}, label = {Text("密码")},visualTransformation = PasswordVisualTransformation('*'))
            OutlinedTextField(macAddress,{macAddress = it}, label = {Text("MAC")}, isError = !macAddress.isValidMacAddress())

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
                        coreConfig.username = username
                        coreConfig.password = password
                        coreConfig.macAddress = macAddress
                        setAppStatus(AppStatus.CONNECTING)
                    },
                    enabled = !coreConfig.inValid(),
                    content = {
                        Text("登录")
                    }
                )
            }
        }
    }
}

@Composable
fun ConnectingPage(coreConfig: CoreConfig, guiConfig: GUIConfig, setStatus: (AppStatus) -> Unit) {
    var threadNotification by remember { mutableStateOf("") }
    var task :JKDrcomTask? = null

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            task = JKDrcomTask(coreConfig.username, coreConfig.password, coreConfig.toHostInfo(), maxRetry = coreConfig.maxRetry ,{threadNotification = it})
            val thread = Thread(task)
            thread.start()
            thread.join()
            setStatus(AppStatus.IDLE)
        }
    }

    Card(Modifier.fillMaxSize().padding(16.dp)) {
        Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.SpaceAround, horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()

            Text(when(threadNotification) {
                "INITIALIZING" -> "初始化线程"
                "CHALLENGING" -> "握手"
                "LOGGING" -> "登录中"
                "KEEPING_ALIVE" -> "保持连接中"
                else -> threadNotification
            })

            Button(
                onClick = {
                    task?.notifyLogout()
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
    val (status,setStatus) = remember { mutableStateOf(if (guiConfig.autoLogin) AppStatus.CONNECTING else AppStatus.IDLE) }

    when(status) {
        AppStatus.IDLE -> IdlePage(setStatus)
        AppStatus.CONNECTING -> ConnectingPage(coreConfig, guiConfig,setStatus)
    }

}

fun main(args :Array<String>) {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
    coreConfig.username = args.getOrElse(0,{""})
    coreConfig.password = args.getOrElse(1,{""})
    coreConfig.macAddress = args.getOrElse(2,{""})
    coreConfig.hostName = "HWAWEI-XCKK"
    coreConfig.networkInterfaceDisplayName = "HWAWEI-XCKK"


    application {
        var windowVisible by remember { mutableStateOf(true) }
        MaterialTheme {
            val trayState = rememberTrayState()
            val notification = rememberNotification("测试","信息",Notification.Type.Warning)
            Tray(rememberVectorPainter(Icons.Default.Send),trayState) {

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

            Window({exitApplication()}, rememberWindowState(size = DpSize(600.dp,450.dp)),windowVisible, title = "JKDrcom") {
                MenuBar {
                    Menu("文件") {

                    }
                    Menu("帮助") {
                        Item("网址") {
                            Desktop.getDesktop().browse(URI("https://github.com/Winterreisender/JKDrcom"))
                        }
                        Item("关于") {
                            JOptionPane.showMessageDialog(ComposeWindow(),"JKDrcom v0.1.0")
                        }
                    }
                }

                AppPage()
            }
        }
    }
}