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

import java.io.File

/**
 * Created by lin on 2017-01-09-009.
 * Modified on 2022-4-29
 * 常量
 */
object Constants {
    const val AUTH_SERVER = "auth.jlu.edu.cn" //10.100.61.3
    const val PORT = 61440
    const val TIMEOUT = 10000 //10s
    const val MAX_RETRY = 8 //最大重试次数
}