/*
 * Copyright (C) 2022 Winterreisender.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>
 */

package io.github.winterreisender.jkdrcom.core.util

// ADT
// JKDrcom线程向主线程返回的信号
sealed class JKDNotification {
    object NOTHING :JKDNotification()
    object INITIALIZING : JKDNotification()
    object CHALLENGING : JKDNotification()
    object LOGGING : JKDNotification()
    object KEEPING_ALIVE : JKDNotification()
    object LOGOUT : JKDNotification()
    data class  RETRYING(val timesRemain :Int, val exception: Throwable?) : JKDNotification()
    object EXITED : JKDNotification()

    override fun toString() = when(this) {
        NOTHING -> ""
        INITIALIZING -> "初始化线程"
        CHALLENGING -> "握手"
        LOGGING ->  "登录中"
        LOGOUT ->  "已注销"
        KEEPING_ALIVE -> "保持连接中"
        is RETRYING -> "重试中 剩余${this.timesRemain}次 原因:$exception"
        EXITED -> "已退出"
    }
}

// 线程共享对象(内存)
open class JKDCommunication {
    /** 注销通知,由GUI向Core通知 */
    @set:Synchronized
    @get:Synchronized
    var notifyLogout = false

    /** 回调函数,由Core向GUI发送JKDNotification */
    @Synchronized
    open fun emitNotification(notification :JKDNotification) {}
}
