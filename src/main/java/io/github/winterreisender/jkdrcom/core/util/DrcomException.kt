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
 * 异常
 */
class DrcomException : Exception {
    constructor(msg: String) : super('['.toString() + CODE.ex_unknown.name + "] " + msg) {}
    constructor(msg: String, code: CODE) : super('['.toString() + code.name + "] " + msg) {}
    constructor(msg: String, cause: Throwable?) : super('['.toString() + CODE.ex_unknown.name + "] " + msg, cause) {}
    constructor(msg: String, cause: Throwable?, code: CODE) : super('['.toString() + code.name + "] " + msg, cause) {}

    enum class CODE {
        ex_unknown, ex_init, ex_challenge, ex_login, ex_timeout, ex_io, ex_thread
    }
}