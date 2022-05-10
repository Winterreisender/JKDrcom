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


package io.github.winterreisender.jkdrcom.cli
/*
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
 */