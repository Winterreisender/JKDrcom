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

/**
 * Created by lin on 2017-01-09-009.
 * Modified on 2022-4-29
 * 常量
 */
object Constants {
    const val AUTH_SERVER = "auth.jlu.edu.cn" //10.100.61.3
    const val PORT = 61440
    const val TIMEOUT = 10000 //10s
    const val DEFAULT_MAX_RETRY = 8 //最大重试次数
    const val RETRY_INTERVAL = 5000L
}