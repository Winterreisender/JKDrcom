package me.winterreisender.jkdrcom

import me.winterreisender.jkdrcom.util.HostInfo

fun onThreadMsgRecv(msg :String) {

}


fun main(args :Array<String>) {

    val username = args[1]
    val password = args[2]
    val macAddress = args[3]
    val hostname = args[4]

    val jDrcomThread = Thread(JKDrcomTask(username,password, HostInfo(hostname,macAddress,"")))
    jDrcomThread.start()
    jDrcomThread.join()

}