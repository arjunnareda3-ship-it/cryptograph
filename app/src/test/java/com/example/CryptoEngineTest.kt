package com.example

import com.example.crypto.CryptoEngine
import com.example.scanner.PhishingScanner
import com.example.ui.router.HashRouter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CryptoEngineTest {

    @Test
    fun testHashRouterNavigationAndHistory() {
        val router = HashRouter("#/aes")
        assertEquals("#/aes", router.currentRoute.value)
        assertFalse(router.canGoBack)
        assertFalse(router.canGoForward)

        // Navigate to RSA
        router.navigate("#/rsa")
        assertEquals("#/rsa", router.currentRoute.value)
        assertTrue(router.canGoBack)
        assertEquals(1, router.backStack.size)

        // Navigate to Diffie-Hellman
        router.navigate("diffie-hellman") // test normalization
        assertEquals("#/diffie-hellman", router.currentRoute.value)
        assertEquals(2, router.backStack.size)

        // Go Back
        assertTrue(router.goBack())
        assertEquals("#/rsa", router.currentRoute.value)
        assertTrue(router.canGoForward)

        // Go Forward
        assertTrue(router.goForward())
        assertEquals("#/diffie-hellman", router.currentRoute.value)

        // Go Back to AES
        assertTrue(router.goBack())
        assertTrue(router.goBack())
        assertEquals("#/aes", router.currentRoute.value)
        assertFalse(router.canGoBack)
    }

    @Test
    fun testAesEncryptionDecryption() {
        val original = "BCA Final Year Cyber Security Project 2026"
        val password = "StrongSecretKey#99"

        val encResult = CryptoEngine.encryptAesGcm(original, password)
        assertNotNull(encResult.cipherTextBase64)
        assertNotNull(encResult.ivBase64)
        assertNotEquals(original, encResult.cipherTextBase64)

        val decrypted = CryptoEngine.decryptAesGcm(
            encResult.cipherTextBase64,
            encResult.ivBase64,
            encResult.keyUsedBase64
        )
        assertEquals(original, decrypted)
    }

    @Test
    fun testAesCbcEncryptionDecryption() {
        val original = "Confidential AES CBC Demonstration Payload"
        val password = "PasswordCbc#2026"

        val cbcResult = CryptoEngine.encryptAesCbc(original, password)
        assertNotNull(cbcResult.cipherTextBase64)
        assertNotNull(cbcResult.ivBase64)
        assertEquals("AES-128-CBC", cbcResult.mode)

        val decrypted = CryptoEngine.decryptAesCbc(
            cbcResult.cipherTextBase64,
            cbcResult.ivBase64,
            cbcResult.keyUsedBase64
        )
        assertEquals(original, decrypted)
    }

    @Test
    fun testAesVisualMatrixStages() {
        val stages = CryptoEngine.getAesEducationalStages("CRYPTO_TEST")
        assertEquals(5, stages.size)
        assertEquals("1. State Matrix", stages[0].stepName)
        assertEquals("2. SubBytes", stages[1].stepName)
        assertEquals("3. ShiftRows", stages[2].stepName)
        assertEquals("4. MixColumns", stages[3].stepName)
        assertEquals("5. AddRoundKey", stages[4].stepName)
        assertEquals(4, stages[0].matrix.size)
        assertEquals(4, stages[0].matrix[0].size)
    }

    @Test
    fun testToyRsaMathematicalModel() {
        // Primes p=61, q=53 -> n=3233, phi=3120, e=17
        val m = 65L
        val rsaResult = CryptoEngine.computeToyRsa(p = 61L, q = 53L, m = m, eCandidate = 17L)

        assertEquals(3233L, rsaResult.n)
        assertEquals(3120L, rsaResult.phi)
        assertTrue(rsaResult.isValid)
        assertEquals(m, rsaResult.decrypted)
        assertEquals(7, rsaResult.steps.size)
    }

    @Test
    fun testDiffieHellmanKeyExchange() {
        // p=23, g=5, a=6, b=15
        val dh = CryptoEngine.simulateDiffieHellman(p = 23L, g = 5L, a = 6L, b = 15L)
        assertTrue(dh.sharedSecretMatches)
        assertEquals(dh.aliceSecret, dh.bobSecret)
        assertTrue(dh.steps.isNotEmpty())
    }

    @Test
    fun testAvalancheEffectCalculation() {
        val avalanche = CryptoEngine.calculateAvalanche("CyberSecurity2026", "CyberSecurity2027")
        assertEquals(256, avalanche.totalBits)
        assertTrue(avalanche.bitDifferences > 80) // SAC says ~50%
        assertTrue(avalanche.percentageChanged > 30.0)
    }

    @Test
    fun testRsaDigitalSignatures() {
        val keyPair = CryptoEngine.generateRsaKeyPair()
        val message = "Digital Signature Authenticity Verification"

        val signature = CryptoEngine.signDataRsa(message, keyPair.private)
        assertNotNull(signature)

        val isValid = CryptoEngine.verifySignatureRsa(message, signature, keyPair.public)
        assertTrue(isValid)

        val isTampered = CryptoEngine.verifySignatureRsa("Tampered Message", signature, keyPair.public)
        assertFalse(isTampered)
    }

    @Test
    fun testCaesarCipherAndBruteForce() {
        val plain = "SECURITY"
        val shift = 5
        val cipher = CryptoEngine.caesarCipher(plain, shift, encrypt = true)
        assertEquals("XJHZWNYD", cipher)

        val decrypted = CryptoEngine.caesarCipher(cipher, shift, encrypt = false)
        assertEquals(plain, decrypted)

        val bruteForce = CryptoEngine.bruteForceCaesar(cipher)
        assertEquals(25, bruteForce.size)
        assertTrue(bruteForce.any { it.first == shift && it.second == plain })
    }

    @Test
    fun testZeroWidthSteganography() {
        val cover = "Hello Examiner, please review the synopsis."
        val secret = "SECRET_VIVA_TOKEN_123"

        val encoded = CryptoEngine.encodeZeroWidthStego(cover, secret)
        assertNotNull(encoded)

        val decoded = CryptoEngine.decodeZeroWidthStego(encoded)
        assertEquals(secret, decoded)
    }

    @Test
    fun testPhishingScannerHeuristics() {
        val phishingUrl = "http://verify-bank-account-login.xyz/kyc-update"
        val report = PhishingScanner.analyzeUrl(phishingUrl)

        assertTrue(report.threatScore > 50)
        assertTrue(report.verdict == "SUSPICIOUS" || report.verdict == "DANGEROUS_PHISHING")
    }

    @Test
    fun testPasswordEntropyCalculation() {
        val weakAudit = CryptoEngine.auditPassword("123456")
        assertTrue(weakAudit.entropyBits < 30)

        val strongAudit = CryptoEngine.auditPassword("Kx#99!vL@021aZ*&")
        assertTrue(strongAudit.entropyBits > 70)
        assertTrue(strongAudit.hasUpper && strongAudit.hasLower && strongAudit.hasDigit && strongAudit.hasSymbol)
    }
}
