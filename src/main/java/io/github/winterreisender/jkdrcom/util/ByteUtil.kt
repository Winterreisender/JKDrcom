package io.github.winterreisender.jkdrcom.util

import java.math.BigInteger
import java.util.*

/**
 * Created by lin on 2017-01-11-011.
 * 字节数组工具, byte 数字当作无符号数处理. 因此 toInt(0xff) 将得到 255.
 */
object ByteUtil {
    private val random = Random(System.currentTimeMillis())
    fun randByte(): Byte {
        return random.nextInt().toByte()
    }

    fun toInt(b: Byte): Int {
        return b.toInt() and 0xff //0xff -> 255
    }

    /*得到两位长度的 16 进制表示*/
    fun toHexString(b: Byte): String {
        var tmp = Integer.toHexString(toInt(b))
        if (tmp.length == 1) {
            tmp = "0$tmp"
        }
        return tmp
    }

    /*使用 split 字符分割的十六进制表示字符串*/ /*使用空格分割的十六进制表示字符串*/
    @JvmStatic
    @JvmOverloads
    fun toHexString(bytes: ByteArray?, split: Char = ' '): String {
        if (bytes == null || bytes.isEmpty()) {
            return ""
        }
        val len = bytes.size
        val sb = StringBuilder(len * 3) //两位数字+split
        for (b in bytes) {
            sb.append(toHexString(b)).append(split)
        }
        return sb.toString().substring(0, len * 3 - 1).uppercase(Locale.getDefault())
    }

    @JvmStatic
    @JvmOverloads
    fun fromHex(hexStr: String, split: Char = ' '): ByteArray {
        // hexStr = 00 01 AB str len = 8 length = (8+1)/3=3
        val length = (hexStr.length + 1) / 3
        val ret = ByteArray(length)
        for (i in 0 until length) {
            ret[i] = hexStr.substring(i * 3, i * 3 + 2).toInt(16).toByte()
        }
        return ret
    }

    @JvmOverloads
    fun ljust(src: ByteArray, count: Int, fill: Byte = 0x00.toByte()): ByteArray {
        val srcLen = src.size
        val ret = ByteArray(count)
        if (srcLen >= count) { //只返回前 count 位
            System.arraycopy(src, 0, ret, 0, count)
            return ret
        }
        System.arraycopy(src, 0, ret, 0, srcLen)
        for (i in srcLen until count) {
            ret[i] = fill
        }
        return ret
    }

    fun ror(md5a: ByteArray, password: ByteArray): ByteArray {
        val len = password.size
        val ret = ByteArray(len)
        var x: Int
        for (i in 0 until len) {
            x = toInt(md5a[i]) xor toInt(password[i])
            //e.g. x =   0xff      1111_1111
            // x<<3  = 0x07f8 0111_1111_1000
            // x>>>5 = 0x0007           0111
            // +       0x07ff 0111_1111_1111
            //(byte)截断=0xff
            ret[i] = ((x shl 3) + (x ushr 5)).toByte()
        }
        return ret
    }

    /**
     * 每四个数倒过来后与sum相与, 最后*1968取后4个数.
     *
     *
     * Python版用`'....'`匹配四个字节，但是这个正则会忽略换行符 0x0a, 因此计算出来的是错误的.
     * 但是python版代码是可用的，因此应该是服务器没有检验
     * (Python 版协议与本工程有些许不一样, 所以这里需要返回正确的校验码)
     * <pre>
     * import struct
     * import re
     * def checksum(s):
     * ret = 1234
     * for i in re.findall('....', s):
     * tmp = int(i[::-1].encode('hex'), 16)
     * print(i, i[::-1], ret, tmp, ret ^ tmp)
     * ret ^= tmp
     * ret = (1968 * ret) & 0xffffffff
     * return struct.pack('<I></I>', ret)
    </pre> *
     */
    fun checksum(data: ByteArray): ByteArray {
        // 1234 = 0x_00_00_04_d2
        val sum = byteArrayOf(0x00, 0x00, 0x04, 0xd2.toByte())
        var len = data.size
        var i = 0
        //0123_4567_8901_23
        while (i + 3 < len) {

            //abcd ^ 3210
            //abcd ^ 7654
            //abcd ^ 1098
            sum[0] = (sum[0].toInt() xor data[i + 3].toInt()).toByte()
            sum[1] = (sum[1].toInt() xor data[i + 2].toInt()).toByte()
            sum[2] = (sum[2].toInt() xor data[i + 1].toInt()).toByte()
            sum[3] = (sum[3].toInt() xor data[i].toInt()).toByte()
            i += 4
        }
        if (i < len) {
            //剩下_23
            //i=12,len=14
            val tmp = ByteArray(4)
            run {
                var j = 3
                while (j >= 0 && i < len) {

                    //j=3 tmp = 0 0 0 2  i=12  13
                    //j=2 tmp = 0 0 3 2  i=13  14
                    tmp[j] = data[i++]
                    j--
                }
            }
            for (j in 0..3) {
                sum[j] = (sum[j].toInt() xor tmp[j].toInt()).toByte()
            }
        }
        var bigInteger = BigInteger(1, sum) //无符号数即正数
        bigInteger = bigInteger.multiply(BigInteger.valueOf(1968))
        bigInteger = bigInteger.and(BigInteger.valueOf(0xffffffffL))
        val bytes = bigInteger.toByteArray()
        //System.out.println(ByteUtil.toHexString(bytes));
        len = bytes.size
        i = 0
        val ret = ByteArray(4)
        var j = len - 1
        while (j >= 0 && i < 4) {
            ret[i++] = bytes[j]
            j--
        }
        return ret
    }

    //参考了 Python 版
    fun crc(data: ByteArray): ByteArray {
        val sum = ByteArray(2)
        var len = data.size
        run {
            var i = 0
            while (i + 1 < len) {
                sum[0] = (sum[0].toInt() xor data[i + 1].toInt()).toByte()
                sum[1] = (sum[1].toInt() xor data[i].toInt()).toByte()
                i += 2
            }
        }
        var b = BigInteger(1, sum)
        b = b.multiply(BigInteger.valueOf(711))
        val bytes = b.toByteArray()
        len = bytes.size
        //System.out.println(toHexString(bytes));
        val ret = ByteArray(4)
        var i = 0
        while (i < 4 && len > 0) {
            ret[i] = bytes[--len]
            i++
        }
        return ret
    }
}