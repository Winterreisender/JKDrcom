/*
 * Copyright (C) 2022 Winterreisender.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>
 */


package io.github.winterreisender.jkdrcom.cli

import io.github.winterreisender.jkdrcom.core.JKDrcomTask
import io.github.winterreisender.jkdrcom.core.util.HostInfo
import io.github.winterreisender.jkdrcom.core.util.JKDCommunication
import io.github.winterreisender.jkdrcom.core.util.JKDNotification

// 命令行版
fun main(args :Array<String>) {
    // 辅助小函数,从args数组中取出
    val argOrInput = { arg :()->String, prompt :String ->
        runCatching { arg().also { println("$prompt $it") } }.getOrElse {
            print(prompt)
            readln()
        }
    }

    val username = argOrInput({args[0]}, "用户名:")
    val macAddress = argOrInput({args[1]}, "MAC地址:")
    val hostname = argOrInput({args[2]}, "主机名:")
    val password = argOrInput({args[3]}, "密码:")

    // 共享内存对象
    val communication = object : JKDCommunication() {
        @Synchronized
        override fun emitNotification(notification: JKDNotification) {
            super.emitNotification(notification)
            println(notification)
        }
    }
    val jkDrcomThread = Thread(JKDrcomTask(username,password, HostInfo(hostname,macAddress),1,communication))
    jkDrcomThread.start()
    jkDrcomThread.join()

}
