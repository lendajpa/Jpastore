package com.jpastore.box

import java.security.MessageDigest
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object PinSecurity {
    fun valid(pin: String) = pin.length in 6..12 && pin.all { it in '0'..'9' }
    fun hash(pin: String, salt: ByteArray): ByteArray = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
        .generateSecret(PBEKeySpec(pin.toCharArray(), salt, 120000, 256)).encoded
    fun matches(pin: String, salt: ByteArray, expected: ByteArray) = MessageDigest.isEqual(hash(pin, salt), expected)
}
