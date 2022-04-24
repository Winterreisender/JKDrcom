package io.github.winterreisender.jkdrcom

import io.github.winterreisender.jkdrcom.util.*
import java.io.IOException
import java.net.*
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Created by lin on 2017-01-11-011.
 * Modified by Winterreisender on 2022-04-23
 * challenge, login, keep_alive, logout
 */
class JKDrcomTask(
    private val username: String,
    private val password: String,
    private val hostInfo: HostInfo,
    val onSignalEmit :(String)->(Unit) = {}
) : Runnable {
    /**
     * 在 challenge 的回复报文中获得 [4:8]
     */
    private val salt = ByteArray(4)

    /**
     * 在 challenge 的回复报文中获得 [20:24], 当是有线网时，就是网络中心分配的 IP 地址，当时无线网时，是局域网地址
     */
    private val clientIp = ByteArray(4)

    /**
     * 在 login 的回复报文中获得[23:39]
     */
    private val tail1 = ByteArray(16)

    /**
     * 在 login 报文计算，计算时需要用到 salt,password
     */
    private val md5a = ByteArray(16)

    /**
     * 初始为 {0，0，0，0} , 在 keep40_1 的回复报文更新[16:20]
     */
    private val tail2 = ByteArray(4)

    /**
     * 在 keep alive 中计数.
     * 初始在 keep40_extra : 0x00, 之后每次 keep40 都加一
     */
    private var count = 0
    private var keep38Count = 0 //仅用于日志计数
    private var notifyLogout = false
    private lateinit var client: DatagramSocket
    private lateinit var serverAddress: InetAddress

    // Modified
    override fun run() {
        log.level = Level.ALL

        onSignalEmit("INITIALIZING")
        init()

        Retry.retry(1, cleanup = {client.close();onSignalEmit("RETRYING");Thread.sleep(1000L)}) {
            onSignalEmit("CHALLENGING")
            if (!challenge(challengeTimes++)) {
                log.warning("challenge failed...")
                throw DrcomException("Server refused the request.{0}" + DrcomException.CODE.ex_challenge)
            }

            onSignalEmit("LONGING")
            if (!login()) {
                log.warning("login failed...")
                throw DrcomException("Failed to send authentication information.{0}" + DrcomException.CODE.ex_login)
            }
            log.info("登录成功!")
            //showWebPage(Constants.NOTICE_URL, Constants.NOTICE_W, Constants.NOTICE_H)

            //keep alive
            onSignalEmit("KEEPING_ALIVE")
            count = 0
            while (!notifyLogout && alive()) { //收到注销通知则停止
                Thread.sleep(20000) //每 20s 一次
            }
        }.fold(
            { log.info("正常停止") },
            {
                log.severe(when(it) {
                    is SocketTimeoutException -> "通信超时: $it"
                    is DrcomException -> "登录异常: $it"
                    is InterruptedException -> "线程异常: $it"
                    else -> "其他异常: $it"
                })
            }
        )

        if (!client.isClosed) {
            client.close()
        }

    }
    /**
     * 初始化套接字、设置超时时间、设置服务器地址
     */
    @Throws(DrcomException::class)
    private fun init() {

        //每次使用同一个端口 若有多个客户端运行这里会抛出异常
        client = DatagramSocket(Constants.PORT)
        client.soTimeout = Constants.TIMEOUT
        serverAddress = InetAddress.getByName(Constants.AUTH_SERVER)
        /* catch (e: SocketException) {
            throw DrcomException("The port ${Constants.PORT} may be occupied, do you have any other clients not exited?", e, DrcomException.CODE.ex_init)
        } catch (e: UnknownHostException) {
            throw DrcomException("The server could not be found. (check DNS settings)", DrcomException.CODE.ex_init)
        }*/
    }

    /**
     * 在回复报文中取得 salt 和 clientIp
     */
    @Throws(DrcomException::class)
    private fun challenge(tryTimes: Int): Boolean {
        return try {
            var buf = byteArrayOf(
                0x01, (0x02 + tryTimes).toByte(), ByteUtil.randByte(), ByteUtil.randByte(), 0x6a,
                0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00
            )
            var packet = DatagramPacket(buf, buf.size, serverAddress, Constants.PORT)
            client.send(packet)
            log.fine("send challenge data.【{}】" + ByteUtil.toHexString(buf))
            buf = ByteArray(76)
            packet = DatagramPacket(buf, buf.size)
            client.receive(packet)
            if (buf[0].toInt() == 0x02) {
                log.fine("recv challenge data.【{}】" + ByteUtil.toHexString(buf))
                // 保存 salt 和 clientIP
                System.arraycopy(buf, 4, salt, 0, 4)
                System.arraycopy(buf, 20, clientIp, 0, 4)
                return true
            }
            log.warning("challenge fail, unrecognized response.【{}】" + ByteUtil.toHexString(buf))
            false
        } catch (e: SocketTimeoutException) {
            throw DrcomException("Challenge server failed, time out. {0}" + DrcomException.CODE.ex_challenge)
        } catch (e: IOException) {
            throw DrcomException("Failed to send authentication information. {0}" + DrcomException.CODE.ex_challenge)
        }
    }

    @Throws(IOException::class, DrcomException::class)
    private fun login(): Boolean {
        val buf = makeLoginPacket()
        val packet = DatagramPacket(buf, buf.size, serverAddress, Constants.PORT)
        client.send(packet)
        log.fine("send login packet.【{}】" + ByteUtil.toHexString(buf))
        val recv = ByteArray(128) //45
        client.receive(DatagramPacket(recv, recv.size))
        log.fine("recv login packet.【{}】" + ByteUtil.toHexString(recv))
        if (recv[0].toInt() != 0x04) {
            if (recv[0].toInt() == 0x05) {
                if (recv[4].toInt() == 0x0B) {
                    throw DrcomException(
                        "Invalid Mac Address, please select the address registered in ip.jlu.edu.cn" + "MAC 地址错误, 请选择在网络中心注册的地址.",
                        DrcomException.CODE.ex_login
                    )
                }
                throw DrcomException("Invalid username or password.", DrcomException.CODE.ex_login)
            } else {
                throw DrcomException("Failed to login, unknown error.", DrcomException.CODE.ex_login)
            }
        }
        // 保存 tail1. 构造 keep38 要用 md5a(在mkptk中保存) 和 tail1
        // 注销也要用 tail1
        System.arraycopy(recv, 23, tail1, 0, 16)
        return true
    }

    /**
     * 需要用来自 challenge 回复报文中的 salt, 构造报文时会保存 md5a keep38 要用
     */
    private fun makeLoginPacket(): ByteArray {
        val code: Byte = 0x03
        val type: Byte = 0x01
        val EOF: Byte = 0x00
        val controlCheck: Byte = 0x20
        val adapterNum: Byte = 0x05
        val ipDog: Byte = 0x01
        val primaryDNS = byteArrayOf(10, 10, 10, 10)
        val dhcp = byteArrayOf(0, 0, 0, 0)
        var passLen = password.length
        if (passLen > 16) {
            passLen = 16
        }
        val dataLen = 334 + (passLen - 1) / 4 * 4
        val data = ByteArray(dataLen)
        data[0] = code
        data[1] = type
        data[2] = EOF
        data[3] = (username.length + 20).toByte()

        System.arraycopy(
            MD5.md5(byteArrayOf(code, type), salt, password.toByteArray()),
            0, md5a, 0, 16
        ) //md5a保存起来
        System.arraycopy(md5a, 0, data, 4, md5a.size) //md5a 4+16=20
        val user = ByteUtil.ljust(username.toByteArray(), 36)
        System.arraycopy(user, 0, data, 20, user.size) //username 20+36=56
        data[56] = controlCheck //0x20
        data[57] = adapterNum //0x05

        //md5a[0:6] xor mac
        System.arraycopy(md5a, 0, data, 58, 6)
        val macBytes = hostInfo.macBytes
        for (i in 0..5) {
            data[i + 58] = (data[i + 58].toInt() xor macBytes[i].toInt()).toByte() //md5a oxr mac
        } // xor 58+6=64
        val md5b: ByteArray = MD5.md5(byteArrayOf(0x01), password.toByteArray(), salt, byteArrayOf(0x00, 0x00, 0x00, 0x00))
        System.arraycopy(md5b, 0, data, 64, md5b.size) //md5b 64+16=80
        data[80] = 0x01 //number of ip
        System.arraycopy(clientIp, 0, data, 81, clientIp.size) //ip1 81+4=85
        System.arraycopy(
            byteArrayOf(
                0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00
            ), 0, data, 85, 12
        ) //ip2/3/4 85+12=97
        data[97] = 0x14 //临时放，97 ~ 97+8 是 md5c[0:8]
        data[98] = 0x00
        data[99] = 0x07
        data[100] = 0x0b
        var tmp = ByteArray(101)
        System.arraycopy(data, 0, tmp, 0, tmp.size) //前 97 位 和 0x14_00_07_0b
        val md5c = MD5.md5(tmp)
        System.arraycopy(md5c, 0, data, 97, 8) //md5c 97+8=105
        data[105] = ipDog //0x01
        //0 106+4=110
        val hostname = ByteUtil.ljust(hostInfo.hostname.toByteArray(), 32)
        System.arraycopy(hostname, 0, data, 110, hostname.size) //hostname 110+32=142
        System.arraycopy(primaryDNS, 0, data, 142, 4) //primaryDNS 142+4=146
        System.arraycopy(dhcp, 0, data, 146, 4) //dhcp 146+4=150

        //second dns 150+4=154
        //delimiter 154+8=162
        data[162] = 0x94.toByte() //unknown 162+4=166
        data[166] = 0x06 //os major 166+4=170
        data[170] = 0x02 //os minor 170+4=174
        data[174] = 0xf0.toByte() //os build
        data[175] = 0x23 //os build 174+4=178
        data[178] = 0x02 //os unknown 178+4=182

        //DRCOM CHECK
        data[182] = 0x44 //\x44\x72\x43\x4f\x4d\x00\xcf\x07
        data[183] = 0x72
        data[184] = 0x43
        data[185] = 0x4f
        data[186] = 0x4d
        data[187] = 0x00
        data[188] = 0xcf.toByte()
        data[189] = 0x07
        data[190] = 0x6a

        //0 191+55=246
        System.arraycopy(
            "1c210c99585fd22ad03d35c956911aeec1eb449b".toByteArray(),
            0, data, 246, 40
        ) //246+40=286
        //0 286+24=310
        data[310] = 0x6a //0x6a 0x00 0x00 310+3=313
        data[313] = passLen.toByte() //password length
        val ror = ByteUtil.ror(md5a, password.toByteArray())
        System.arraycopy(ror, 0, data, 314, passLen) //314+passlen
        data[314 + passLen] = 0x02
        data[315 + passLen] = 0x0c

        //checksum(data+'\x01\x26\x07\x11\x00\x00'+dump(mac))
        //\x01\x26\x07\x11\x00\x00
        data[316 + passLen] = 0x01 //临时放, 稍后被 checksum 覆盖
        data[317 + passLen] = 0x26
        data[318 + passLen] = 0x07
        data[319 + passLen] = 0x11
        data[320 + passLen] = 0x00
        data[321 + passLen] = 0x00
        System.arraycopy(macBytes, 0, data, 322 + passLen, 4)
        tmp = ByteArray(326 + passLen) //data+'\x01\x26\x07\x11\x00\x00'+dump(mac)
        System.arraycopy(data, 0, tmp, 0, tmp.size)
        tmp = ByteUtil.checksum(tmp)
        System.arraycopy(tmp, 0, data, 316 + passLen, 4) //checksum 316+passlen+4=320+passLen
        data[320 + passLen] = 0x00
        data[321 + passLen] = 0x00 //分割
        System.arraycopy(macBytes, 0, data, 322 + passLen, macBytes.size)
        //mac 322+passLen+6=328+passLen

        // passLen % 4=mod 补0个数  4-mod  (4-mod)%4
        //             0    0        4
        //             1    3        3
        //             2    2        2
        //             3    1        1
        val zeroCount = (4 - passLen % 4) % 4
        for (i in 0 until zeroCount) {
            data[328 + passLen + i] = 0x00
        }
        data[328 + passLen + zeroCount] = ByteUtil.randByte()
        data[329 + passLen + zeroCount] = ByteUtil.randByte()
        return data
    }

    @Throws(IOException::class)
    private fun alive(): Boolean {
        var needExtra = false
        ++keep38Count
        log.fine("count = {}, keep38count = {}$count$keep38Count")
        if (count % 21 == 0) { //第一个 keep38 后有 keep40_extra, 十个 keep38 后 count 就加了21
            needExtra = true
        } //每10个keep38

        //-------------- keep38 ----------------------------------------------------
        val packet38 = makeKeepPacket38()
        var packet = DatagramPacket(packet38, packet38.size, serverAddress, Constants.PORT)
        client.send(packet)
        log.fine(
            "[rand={}|{}]send keep38. 【{}】" +
                    ByteUtil.toHexString(packet38[36]) + ByteUtil.toHexString(packet38[37]) +
                    ByteUtil.toHexString(packet38)
        )
        var recv = ByteArray(128)
        client.receive(DatagramPacket(recv, recv.size))
        log.fine(
            "[rand={}|{}]recv Keep38. [{}.{}.{}.{}] 【{}】" +
                    ByteUtil.toHexString(recv[6]) + ByteUtil.toHexString(recv[7]) +
                    ByteUtil.toInt(recv[12]) + ByteUtil.toInt(recv[13]) + ByteUtil.toInt(recv[14]) + ByteUtil.toInt(recv[15]) +
                    ByteUtil.toHexString(recv)
        )
        keepAliveVer[0] = recv[28] //收到keepAliveVer//通常不会变
        keepAliveVer[1] = recv[29]
        if (needExtra) { //每十次keep38都要发一个 keep40_extra
            log.info("Keep40_extra...")
            //--------------keep40_extra--------------------------------------------
            //先发 keep40_extra 包
            val packet40extra = makeKeepPacket40(1, true)
            packet = DatagramPacket(packet40extra, packet40extra.size, serverAddress, Constants.PORT)
            client.send(packet)
            log.fine(
                "[seq={}|type={}][rand={}|{}]send Keep40_extra. 【{}】" + packet40extra[1] + packet40extra[5] +
                        ByteUtil.toHexString(packet40extra[8]) + ByteUtil.toHexString(packet40extra[9]) +
                        ByteUtil.toHexString(packet40extra)
            )
            recv = ByteArray(512)
            client.receive(DatagramPacket(recv, recv.size))
            log.fine(
                "[seq={}|type={}][rand={}|{}]recv Keep40_extra. 【{}】" + recv[1] + recv[5] +
                        ByteUtil.toHexString(recv[8]) + ByteUtil.toHexString(recv[9]) + ByteUtil.toHexString(recv)
            )
            //不理会回复
        }

        //--------------keep40_1----------------------------------------------------
        val packet40_1 = makeKeepPacket40(1, false)
        packet = DatagramPacket(packet40_1, packet40_1.size, serverAddress, Constants.PORT)
        client.send(packet)
        log.fine(
            "[seq={}|type={}][rand={}|{}]send Keep40_1. 【{}】" + packet40_1[1] + packet40_1[5] +
                    ByteUtil.toHexString(packet40_1[8]) + ByteUtil.toHexString(packet40_1[9]) +
                    ByteUtil.toHexString(packet40_1)
        )
        recv = ByteArray(64) //40
        client.receive(DatagramPacket(recv, recv.size))
        log.fine(
            "[seq={}|type={}][rand={}|{}]recv Keep40_1. 【{}】" + recv[1] + recv[5] +
                    ByteUtil.toHexString(recv[8]) + ByteUtil.toHexString(recv[9]) + ByteUtil.toHexString(recv)
        )
        //保存 tail2 , 待会儿构造 packet 要用
        System.arraycopy(recv, 16, tail2, 0, 4)

        //--------------keep40_2----------------------------------------------------
        val packet40_2 = makeKeepPacket40(2, false)
        packet = DatagramPacket(packet40_2, packet40_2.size, serverAddress, Constants.PORT)
        client.send(packet)
        log.fine(
            "[seq={}|type={}][rand={}|{}]send Keep40_2. 【{}】" + packet40_2[1] + packet40_2[5] +
                    ByteUtil.toHexString(packet40_2[8]) + ByteUtil.toHexString(packet40_2[9]) +
                    ByteUtil.toHexString(packet40_2)
        )
        client.receive(DatagramPacket(recv, recv.size))
        log.fine(
            "[seq={}|type={}][rand={}|{}]recv Keep40_2. 【{}】" + recv[1] + recv[5] +
                    ByteUtil.toHexString(recv[8]) + ByteUtil.toHexString(recv[9]) + ByteUtil.toHexString(recv)
        )
        //keep40_2 的回复也不用理会
        return true
    }

    /**
     * 0xff md5a:16位 0x00 0x00 0x00 tail1:16位 rand rand
     */
    private fun makeKeepPacket38(): ByteArray {
        val data = ByteArray(38)
        data[0] = 0xff.toByte()
        System.arraycopy(md5a, 0, data, 1, md5a.size) //1+16=17
        //17 18 19
        System.arraycopy(tail1, 0, data, 20, tail1.size) //20+16=36
        data[36] = ByteUtil.randByte()
        data[37] = ByteUtil.randByte()
        return data
    }

    /**
     * keep40_额外的 就是刚登录时, keep38 后发的那个会收到 272 This Program can not run in dos mode
     * keep40_1     每 秒发送
     * keep40_2
     */
    private fun makeKeepPacket40(firstOrSecond: Int, extra: Boolean): ByteArray {
        val data = ByteArray(40)
        data[0] = 0x07
        data[1] = count++.toByte() //到了 0xff 会回到 0x00
        data[2] = 0x28
        data[3] = 0x00
        data[4] = 0x0b
        //   keep40_1   keep40_2
        //  发送  接收  发送  接收
        //  0x01 0x02 0x03 0xx04
        if (firstOrSecond == 1 || extra) { //keep40_1 keep40_extra 是 0x01
            data[5] = 0x01
        } else {
            data[5] = 0x03
        }
        if (extra) {
            data[6] = 0x0f
            data[7] = 0x27
        } else {
            data[6] = keepAliveVer[0]
            data[7] = keepAliveVer[1]
        }
        data[8] = ByteUtil.randByte()
        data[9] = ByteUtil.randByte()

        //[10-15]:0
        System.arraycopy(tail2, 0, data, 16, 4) //16+4=20

        //20 21 22 23 : 0
        if (firstOrSecond == 2) {
            System.arraycopy(clientIp, 0, data, 24, 4)
            var tmp = ByteArray(28)
            System.arraycopy(data, 0, tmp, 0, tmp.size)
            tmp = ByteUtil.crc(tmp)
            System.arraycopy(tmp, 0, data, 24, 4) //crc 24+4=28
            System.arraycopy(clientIp, 0, data, 28, 4) //28+4=32
            //之后 8 个 0
        }
        return data
    }

    fun notifyLogout() {
        notifyLogout = true //终止 keep 线程
        //logout
        log.info("收到注销指令")
        if (true) { //已登录才注销
            var succ = true
            try {
                challenge(challengeTimes++)
                logout()
            } catch (t: Throwable) {
                succ = false
                log.severe("注销异常$t")
            } finally {
                //不管怎样重新登录
                if (succ) {
                    log.info("Logout success.")
                }
                if (!client.isClosed) {
                    client.close()
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun logout(): Boolean {
        val buf = makeLogoutPacket()
        val packet = DatagramPacket(buf, buf.size, serverAddress, Constants.PORT)
        client.send(packet)
        log.fine("send logout packet.【{}】" + ByteUtil.toHexString(buf))
        val recv = ByteArray(512) //25
        client.receive(DatagramPacket(recv, recv.size))
        log.fine("recv logout packet response.【{}】" + ByteUtil.toHexString(recv))
        if (recv[0].toInt() == 0x04) {
            log.info("注销成功")
        } else {
            log.info("注销...失败?")
        }
        return true
    }

    private fun makeLogoutPacket(): ByteArray {
        val data = ByteArray(80)
        data[0] = 0x06 //code
        data[1] = 0x01 //type
        data[2] = 0x00 //EOF
        data[3] = (username.length + 20).toByte()
        val md5 = MD5.md5(byteArrayOf(0x06, 0x01), salt, password.toByteArray())
        System.arraycopy(md5, 0, data, 4, md5.size) //md5 4+16=20
        System.arraycopy(
            ByteUtil.ljust(username.toByteArray(), 36),
            0, data, 20, 36
        ) //username 20+36=56
        data[56] = 0x20
        data[57] = 0x05
        val macBytes = hostInfo.macBytes
        for (i in 0..5) {
            data[58 + i] = (data[4 + i].toInt() xor macBytes[i].toInt()).toByte()
        } // mac xor md5 58+6=64
        System.arraycopy(tail1, 0, data, 64, tail1.size) //64+16=80
        return data
    }

    companion object {
        private val log = Logger.getLogger("DrcomTask")
        //region //Drcom 协议若干字段信息
        /**
         * 在 keep38 的回复报文中获得[28:30]
         */
        private val keepAliveVer = byteArrayOf(0xdc.toByte(), 0x02)

        /**
         * 官方客户端在密码错误后重新登录时，challenge 包的这个数会递增，因此这里设为静态的
         */
        private var challengeTimes = 0
    }
}