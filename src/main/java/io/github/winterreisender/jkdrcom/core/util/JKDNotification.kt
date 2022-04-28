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
