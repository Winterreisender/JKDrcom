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

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

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

}