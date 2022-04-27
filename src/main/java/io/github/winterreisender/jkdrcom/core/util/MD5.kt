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

import io.github.winterreisender.jkdrcom.core.util.ByteUtil.fromHex
import io.github.winterreisender.jkdrcom.core.util.ByteUtil.ljust
import io.github.winterreisender.jkdrcom.core.util.ByteUtil.toHexString
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import javax.crypto.Cipher
import javax.crypto.NoSuchPaddingException
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

/**
 * Created by lin on 2017-01-10-010.
 * MD5
 */
object MD5 {
    private val zero16 = ByteArray(16)

    fun md5(bytes: ByteArray): ByteArray {
        try {
            val instance = MessageDigest.getInstance("MD5")
            instance.update(bytes)
            return instance.digest()
        } catch (ignore: NoSuchAlgorithmException) {
        }
        return zero16 //容错
    }

    fun md5(vararg bytes: ByteArray): ByteArray {
        var len = 0
        for (bs in bytes) {
            len += bs.size //数据总长度
        }
        val data = ByteArray(len)
        len = 0 //记录已拷贝索引
        for (bs in bytes) {
            System.arraycopy(bs, 0, data, len, bs.size)
            len += bs.size
        }
        return md5(data)
    }



    /*//http://www.tuicool.com/articles/QJ7bYr
     * 字符串 DESede(3DES) 加密
     * ECB模式/使用PKCS7方式填充不足位,目前给的密钥是192位
     * 3DES（即Triple DES）是DES向AES过渡的加密算法（1999年，NIST将3-DES指定为过渡的
     * 加密标准），是DES的一个更安全的变形。它以DES为基本模块，通过组合分组方法设计出分组加
     * 密算法，其具体实现如下：设Ek()和Dk()代表DES算法的加密和解密过程，K代表DES算法使用的
     * 密钥，P代表明文，C代表密表，这样，
     * 3DES加密过程为：C=Ek3(Dk2(Ek1(P)))
     * 3DES解密过程为：P=Dk1((EK2(Dk3(C)))
     * <p>
     * args在java中调用sun公司提供的3DES加密解密算法时，需要使
     * 用到$JAVA_HOME/jre/lib/目录下如下的4个jar包：
     * jce.jar
     * security/US_export_policy.jar
     * security/local_policy.jar
     * ext/sunjce_provider.jar
     */
    private const val Algorithm = "DESede" //定义加密算法,可用 DES,DESede,Blowfish
    private val none = byteArrayOf()
    const val DES_KEY_LEN = 24
    private fun des(mode: Int, key: ByteArray, data: ByteArray): ByteArray {
        try {
            val deskey: SecretKey = SecretKeySpec(key, Algorithm) //生成密钥
            val c1 = Cipher.getInstance(Algorithm) //加密或解密
            c1.init(mode, deskey)
            return c1.doFinal(data) //在单一方面的加密或解密
        } catch (ignore: NoSuchAlgorithmException) {
        } catch (ignore: NoSuchPaddingException) {
        } catch (e3: Exception) {
            e3.printStackTrace()
        }
        return none
    }

    fun encrypt3DES(keybyte: ByteArray, src: ByteArray): ByteArray {
        return des(Cipher.ENCRYPT_MODE, keybyte, src)
    }

    fun decrypt3DES(keybyte: ByteArray, secret: ByteArray): ByteArray {
        return des(Cipher.DECRYPT_MODE, keybyte, secret)
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val enk = ljust("1".toByteArray(), DES_KEY_LEN) //用于加密的密码，必须 24 长度
        val password = "a" //要加密的字符串
        println("加密前的字符串:" + password + " | " + toHexString(password.toByteArray()))
        val encoded = encrypt3DES(enk, password.toByteArray())
        println("加密后:" + toHexString(encoded))
        val srcBytes = decrypt3DES(enk, fromHex(toHexString(encoded), ' '))
        println("解密后的字符串:" + String(srcBytes) + " | " + toHexString(srcBytes))
    } /*
     * 加密前的字符串:123456 | 31 32 33 34 35 36
     * 加密后的字符串:ǋ�Y�_l� | C7 8B C1 59 94 5F 6C AE
     * 解密后的字符串:123456 | 31 32 33 34 35 36
     */
}