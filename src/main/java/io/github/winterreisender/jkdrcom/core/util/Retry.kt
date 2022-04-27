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

import java.util.logging.Logger

// 重试maxTimes次
object Retry {
    /**
     * 重试maxTimes次body,每次遇到异常后运行cleanup :剩余次数,异常->Unit
     *
     * @param maxTimes max retry times
     * @param cleanup actions on failed, (timesRemain :Int, exception :Throwable?)->Unit
     * @param body main body of retry
     * @return Result(body()) if success finally and Result(last exception) if all failed
     */
    fun<R> retry(maxTimes: Int, cleanup :(Int,Throwable?)->Unit ,body :()->R) :Result<R> {

        var timesRemain = maxTimes-1;
        var r = runCatching { body() }

        while (timesRemain != 0) {
            if(r.isSuccess) {
                break
            }else{
                Logger.getLogger("Retry").warning("Failed with Exception(${r.exceptionOrNull()}). Times remain $timesRemain")
                cleanup(timesRemain,r.exceptionOrNull())
                r = runCatching { body() }
                timesRemain--
            }
        }

        return r;
    }

}