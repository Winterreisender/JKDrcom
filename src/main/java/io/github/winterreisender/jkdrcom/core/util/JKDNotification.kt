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
    class  RETRYING(val timesRemain :Int) : JKDNotification()
    object EXITED : JKDNotification()

    override fun toString() = when(this) {
        NOTHING -> ""
        INITIALIZING -> "初始化线程"
        CHALLENGING -> "握手"
        LOGGING ->  "登录中"
        LOGOUT ->  "已注销"
        KEEPING_ALIVE -> "保持连接中"
        is RETRYING -> "重试中 剩余${this.timesRemain}"
        EXITED -> "已退出"
    }
}
