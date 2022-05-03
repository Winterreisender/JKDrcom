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

import io.github.winterreisender.jkdrcom.core.util.ByteUtil.toHexString
import java.net.NetworkInterface
import java.net.SocketException
import java.util.*
import java.util.logging.Logger

/**
 * Created by lin on 2017-01-09-009.
 * 工具类
 */
object IPUtil {
    private val log = Logger.getLogger("IPUtil")
    fun <T> asList(enumeration: Enumeration<T>): List<T> {
        val ret: MutableList<T> = ArrayList()
        while (enumeration.hasMoreElements()) {
            ret.add(enumeration.nextElement())
        }
        return ret
    }

    fun getHostInfo(): List<HostInfo> {
        val hostInfoList: MutableList<HostInfo> = ArrayList()
        try {
            val networkInterfaces = NetworkInterface.getNetworkInterfaces()
                ?: return hostInfoList //空结果 -> 补救:使用配置文件手动指定
            val networkInterfacesList = asList(networkInterfaces)
            val size = networkInterfacesList.size
            var index = 0
            for (networkInterface in networkInterfacesList) {
                index++
                //callback.update(index, size)
                if (!networkInterface.isUp) {
                    continue
                }
                var addr: String? = null
                var hostname: String? = null
                val interfaceAddresses = networkInterface.interfaceAddresses
                for (interfaceAddress in interfaceAddresses) {
                    val address = interfaceAddress.address
                    //log.trace("{}/{}. address = {}", index, size, address);
                    val hostAddress = address.hostAddress //to耗时do
                    if (hostAddress.contains(".")) { // not ':' -> IPv6
                        addr = hostAddress
                        hostname = address.hostName
                        break
                    }
                }
                val hardwareAddress = networkInterface.hardwareAddress //to耗时do
                val dashMAC = getDashMAC(hardwareAddress)
                //log.trace("Dash Mac = {}", dashMAC);
                if (dashMAC != null && dashMAC.length == 17 && addr != null && hostname != null) { // 00-00-00-00-00-00
                    val hostInfo = HostInfo(hostname, dashMAC, networkInterface.displayName)
                    hostInfo.address4 = addr
                    hostInfoList.add(hostInfo)
                }
            }
        } catch (e: SocketException) {
            log.severe("Socket Exception: $e")
        }
        //callback.done(hostInfoList)
        return hostInfoList
    }

    fun getDashMAC(hardwareAddress: ByteArray?): String? {
        return if (hardwareAddress == null) {
            //throw new NullPointerException("Hardware address should not be null.");
            null
        } else toHexString(hardwareAddress, '-')
    }

    fun isPublicIP(dotIP: String): Boolean {
        try {
            val split = dotIP.trim { it <= ' ' }.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val a = split[0].toInt()
            val b = split[1].toInt()
            //int c = Integer.parseInt(split[2]);
            //int d = Integer.parseInt(split[3]);
            //A 类：1.0.0.0 到 127.255.255.255  //A 类：10.0.0.0 到 10.255.255.255
            //                              127.0.0.0 到 127.255.255.255 为系统回环地址
            if (a in 1..127) {
                if (!(a == 10 || a == 127)) {
                    return true
                }
            } else if (a in 128..191) {
                // 169.254.X.X 是保留地址。
                // 如果你的 IP 地址是自动获取 IP 地址，
                // 而你在网络上又没有找到可用的 DHCP 服务器。就会得到其中一个 IP。
                // UPDATE at 2017-01-23: http://baike.baidu.com/subview/8370/15816170.htm#5
                if (!(a == 172 && b >= 16 && b < 31) && !(a == 169 && b == 254)) {
                    return true
                }
            } else if (a in 192..223) {
                if (!(a == 192 && b == 168)) {
                    return true
                }
            }
            //D 类：224.0.0.0 到 239.255.255.255
            //E 类：240.0.0.0 到 255.255.255.255
        } catch (e: Exception) {
            log.warning("判断是否为公网 IP 时发生异常: {}$dotIP$e")
            return false
        }
        return false
    }

}