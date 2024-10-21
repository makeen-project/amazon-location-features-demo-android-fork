package com.aws.amazonlocation.utils

import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

internal const val HASHING_ALGORITHM = "SHA-256"
internal const val MAC_ALGORITHM = "HmacSHA256"

internal fun hash(value: String): String {
    val bytes = value.toByteArray()
    val md = MessageDigest.getInstance(HASHING_ALGORITHM)
    val digest = md.digest(bytes)
    return digest.toHexString()
}

@Throws(Exception::class)
internal fun hmacSha256(key: ByteArray, data: String): ByteArray {
    val sha256Hmac = Mac.getInstance(MAC_ALGORITHM)
    val secretKey = SecretKeySpec(key, MAC_ALGORITHM)
    sha256Hmac.init(secretKey)

    return sha256Hmac.doFinal(data.toByteArray(StandardCharsets.UTF_8))
}

@Throws(Exception::class)
internal fun hmacSha256(key: String, data: String) =
    hmacSha256(key.toByteArray(Charset.forName("utf-8")), data)

internal fun ByteArray.toHexString(): String{
    return joinToString("") { "%02x".format(it) }
}