package com.jpastore.box
import org.junit.Assert.*
import org.junit.Test
class PinSecurityTest {
    @Test fun rejectsWeakAndMalformedPins() { assertFalse(PinSecurity.valid("1234")); assertFalse(PinSecurity.valid("12345a")); assertTrue(PinSecurity.valid("382910")) }
    @Test fun verifiesSaltedPin() { val salt = ByteArray(16) { it.toByte() }; val hash = PinSecurity.hash("382910", salt); assertTrue(PinSecurity.matches("382910", salt, hash)); assertFalse(PinSecurity.matches("382911", salt, hash)); assertFalse(hash.contentEquals(PinSecurity.hash("382910", ByteArray(16) { 7 }))) }
}
