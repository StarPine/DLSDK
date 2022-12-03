package com.dl.download.utils

import java.security.MessageDigest

/**
 * @author Shuotao Gong
 * @time 2022/11/17
 */
internal object HashKeyUtils {

    fun getSHA256(s: String): String {
        return MessageDigest.getInstance("SHA-256").digest(s.toByteArray()).toHexString() + getExName(s)
    }

    fun getExName(s: String): String {
        val point = s.lastIndexOf('.')
        val ex = if (point > -1) s.substring(point) else ""
        if (ex.length > 5) return ""
        return ex
    }

    fun ByteArray.toHexString():String {
        return buildString {
            this@toHexString.forEach {
                append(String.format("%02x", it))
            }
        }
    }

}