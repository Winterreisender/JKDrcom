package io.github.winterreisender.jkdrcom.cli

import io.github.winterreisender.jkdrcom.core.JKDrcomTask
import io.github.winterreisender.jkdrcom.core.util.HostInfo
import io.github.winterreisender.jkdrcom.core.util.JKDNotification

fun onThreadMsgRecv(msg :JKDNotification) {
    println(msg)
}

fun main(args :Array<String>) {
    // 辅助小函数,从args数组中取出
    val argOrInput = { arg :()->String, prompt :String ->
        runCatching { arg().also { println("$prompt $it") } }.getOrElse {
            print(prompt)
            readln()
        }
    }

    println("原作: YouthLin https://github.com/YouthLin/jlu-drcom-client")
    val username = argOrInput({args[0]}, "用户名:")
    val macAddress = argOrInput({args[1]}, "MAC地址:")
    val hostname = argOrInput({args[2]}, "主机名:")
    val password = argOrInput({args[3]}, "密码:")

    val jkDrcomThread = Thread(JKDrcomTask(username,password, HostInfo(hostname,macAddress),1,::onThreadMsgRecv))
    jkDrcomThread.start()
    jkDrcomThread.join()

}