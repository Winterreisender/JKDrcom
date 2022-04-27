package io.github.winterreisender.jkdrcom.core.util

import java.util.Timer

/**
 * Created by lin on 2017-01-10-010.
 * Modified by winterreisender 2022-04-27
 * 机器的IP、HostName、MAC等
 */
class HostInfo(var hostname: String, macHex: String,displayName :String = "") {
    val macBytes = ByteArray(6)
    var macHexDash: String = ""
        private set
    var macNoDash: String = ""
        private set
    var address4 = "0.0.0.0" //仅用于显示

    init {
        checkHexToDashMac(macHex)
    }

    private fun checkHexToDashMac(mac: String) {
        var mac = mac
        if (mac.contains("-")) {
            mac = mac.replace("-".toRegex(), "")
        }
        if (mac.length != 12) {
            throw RuntimeException("MAC 地址格式错误。应为 xx-xx-xx-xx-xx-xx 或 xxxxxxxxxxxx 格式的 16 进制: $mac")
        }
        try {
            mac.toLong(16)
        } catch (e: NumberFormatException) {
            throw RuntimeException("MAC 地址格式错误。应为 xx-xx-xx-xx-xx-xx 或 xxxxxxxxxxxx 格式的 16 进制: $mac")
        }
        val sb = StringBuilder(18)
        run {
            var i = 0
            while (i < 12) {
                sb.append(mac[i++]).append(mac[i]).append("-")
                i++
            }
        }
        macHexDash = sb.substring(0, 17)
        macNoDash = mac
        val split = macHexDash.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (i in split.indices) {
            macBytes[i] = split[i].toInt(16).toByte()
        }
    }

    override fun toString(): String {
        return "$macHexDash/$hostname/$address4"
    }

    fun setMacHexDash(macHexDash: String) {
        checkHexToDashMac(macHexDash)
    }

    fun setMacNoDash(macNoDash: String) {
        checkHexToDashMac(macNoDash)
    }
}

fun main() {
    IPUtil.getHostInfo().forEach(::println)
}