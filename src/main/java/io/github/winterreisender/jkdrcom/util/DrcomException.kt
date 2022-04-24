package io.github.winterreisender.jkdrcom.util

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