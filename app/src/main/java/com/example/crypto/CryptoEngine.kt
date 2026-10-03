package com.example.crypto

import android.util.Base64
import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom
import java.security.Signature
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.math.log2
import kotlin.math.pow

object CryptoEngine {

    // ==================== AES-256 GCM ====================
    data class AesResult(
        val cipherTextBase64: String,
        val ivBase64: String,
        val keyUsedBase64: String,
        val mode: String = "AES-256-GCM"
    )

    fun encryptAesGcm(plainText: String, secretKeyPass: String? = null): AesResult {
        val keyBytes = deriveKey(secretKeyPass, 32)
        val secretKey = SecretKeySpec(keyBytes, "AES")

        val iv = ByteArray(12)
        SecureRandom().nextBytes(iv)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec)

        val cipherBytes = cipher.doFinal(plainText.toByteArray(StandardCharsets.UTF_8))

        return AesResult(
            cipherTextBase64 = Base64.encodeToString(cipherBytes, Base64.NO_WRAP),
            ivBase64 = Base64.encodeToString(iv, Base64.NO_WRAP),
            keyUsedBase64 = Base64.encodeToString(keyBytes, Base64.NO_WRAP)
        )
    }

    fun decryptAesGcm(cipherTextBase64: String, ivBase64: String, keyBase64: String): String {
        val keyBytes = try {
            Base64.decode(keyBase64, Base64.NO_WRAP)
        } catch (_: Exception) {
            deriveKey(keyBase64, 32)
        }
        val ivBytes = Base64.decode(ivBase64, Base64.NO_WRAP)
        val cipherBytes = Base64.decode(cipherTextBase64, Base64.NO_WRAP)

        val secretKey = SecretKeySpec(keyBytes, "AES")
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(128, ivBytes)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

        val plainBytes = cipher.doFinal(cipherBytes)
        return String(plainBytes, StandardCharsets.UTF_8)
    }

    // ==================== AES-128 CBC ====================
    data class AesCbcResult(
        val cipherTextBase64: String,
        val ivBase64: String,
        val keyUsedBase64: String,
        val mode: String = "AES-128-CBC"
    )

    fun encryptAesCbc(plainText: String, secretKeyPass: String? = null): AesCbcResult {
        val keyBytes = deriveKey(secretKeyPass, 16)
        val secretKey = SecretKeySpec(keyBytes, "AES")
        val iv = ByteArray(16)
        SecureRandom().nextBytes(iv)

        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val spec = IvParameterSpec(iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec)

        val cipherBytes = cipher.doFinal(plainText.toByteArray(StandardCharsets.UTF_8))
        return AesCbcResult(
            cipherTextBase64 = Base64.encodeToString(cipherBytes, Base64.NO_WRAP),
            ivBase64 = Base64.encodeToString(iv, Base64.NO_WRAP),
            keyUsedBase64 = Base64.encodeToString(keyBytes, Base64.NO_WRAP)
        )
    }

    fun decryptAesCbc(cipherTextBase64: String, ivBase64: String, keyBase64: String): String {
        val keyBytes = try {
            Base64.decode(keyBase64, Base64.NO_WRAP)
        } catch (_: Exception) {
            deriveKey(keyBase64, 16)
        }
        val ivBytes = Base64.decode(ivBase64, Base64.NO_WRAP)
        val cipherBytes = Base64.decode(cipherTextBase64, Base64.NO_WRAP)

        val secretKey = SecretKeySpec(keyBytes, "AES")
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val spec = IvParameterSpec(ivBytes)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

        val plainBytes = cipher.doFinal(cipherBytes)
        return String(plainBytes, StandardCharsets.UTF_8)
    }

    // ==================== AES 4-STEP VISUAL ROUND STEPS ====================
    data class AesMatrixStep(
        val stepName: String,
        val description: String,
        val matrix: List<List<String>> // 4 rows x 4 cols (hex strings)
    )

    // Standard Rijndael S-Box sample lookup for visual education
    private val sBoxLookup = intArrayOf(
        0x63, 0x7c, 0x77, 0x7b, 0xf2, 0x6b, 0x6f, 0xc5, 0x30, 0x01, 0x67, 0x2b, 0xfe, 0xd7, 0xab, 0x76,
        0xca, 0x82, 0xc9, 0x7d, 0xfa, 0x59, 0x47, 0xf0, 0xad, 0xd4, 0xa2, 0xaf, 0x9c, 0xa4, 0x72, 0xc0,
        0xb7, 0xfd, 0x93, 0x26, 0x36, 0x3f, 0xf7, 0xcc, 0x34, 0xa5, 0xe5, 0xf1, 0x71, 0xd8, 0x31, 0x15,
        0x04, 0xc7, 0x23, 0xc3, 0x18, 0x96, 0x05, 0x9a, 0x07, 0x12, 0x80, 0xe2, 0xeb, 0x27, 0xb2, 0x75
    )

    fun getAesEducationalStages(sampleText: String = "CRYPTO"): List<AesMatrixStep> {
        // Pad sample to 16 bytes (4x4 state matrix in column-major order)
        val bytes = ByteArray(16)
        val raw = sampleText.toByteArray(StandardCharsets.UTF_8)
        System.arraycopy(raw, 0, bytes, 0, minOf(raw.size, 16))

        // 1. Initial State Matrix (4x4)
        val initialMatrix = List(4) { r ->
            List(4) { c ->
                val byteVal = bytes[c * 4 + r].toInt() and 0xFF
                "%02X".format(byteVal)
            }
        }

        // 2. SubBytes (S-Box non-linear substitution)
        val subBytesMatrix = List(4) { r ->
            List(4) { c ->
                val byteVal = bytes[c * 4 + r].toInt() and 0xFF
                val sVal = sBoxLookup[byteVal % sBoxLookup.size]
                "%02X".format(sVal)
            }
        }

        // 3. ShiftRows (Row 0: 0 shift, Row 1: 1 shift left, Row 2: 2 shift left, Row 3: 3 shift left)
        val shiftRowsMatrix = List(4) { r ->
            List(4) { c ->
                val originalCol = (c + r) % 4
                subBytesMatrix[r][originalCol]
            }
        }

        // 4. MixColumns (Galois Field matrix multiplication)
        val mixColsMatrix = List(4) { r ->
            List(4) { c ->
                val v1 = shiftRowsMatrix[0][c].toInt(16)
                val v2 = shiftRowsMatrix[1][c].toInt(16)
                val v3 = shiftRowsMatrix[2][c].toInt(16)
                val v4 = shiftRowsMatrix[3][c].toInt(16)
                val mixed = (v1 xor (v2 shl 1) xor v3 xor v4) and 0xFF
                "%02X".format(mixed)
            }
        }

        // 5. AddRoundKey (XOR with round subkey)
        val addKeyMatrix = List(4) { r ->
            List(4) { c ->
                val mixed = mixColsMatrix[r][c].toInt(16)
                val roundKeyByte = (0x5A xor (r * 11 + c * 17)) and 0xFF
                "%02X".format(mixed xor roundKeyByte)
            }
        }

        return listOf(
            AesMatrixStep(
                "1. State Matrix",
                "128-bit block arranged as 4×4 byte matrix in column-major order.",
                initialMatrix
            ),
            AesMatrixStep(
                "2. SubBytes",
                "Non-linear S-Box substitution in GF(2⁸) providing high confusion against linear cryptanalysis.",
                subBytesMatrix
            ),
            AesMatrixStep(
                "3. ShiftRows",
                "Cyclic left byte rotation of each row (Row 1 shifts 1, Row 2 shifts 2, Row 3 shifts 3) providing diffusion.",
                shiftRowsMatrix
            ),
            AesMatrixStep(
                "4. MixColumns",
                "Matrix multiplication in Galois Field GF(2⁸) mixing columns to spread byte influence across the entire state.",
                mixColsMatrix
            ),
            AesMatrixStep(
                "5. AddRoundKey",
                "Bitwise XOR (⊕) of each byte with the round subkey extracted from the Rijndael Key Schedule.",
                addKeyMatrix
            )
        )
    }

    // ==================== RSA-2048 ====================
    data class RsaKeyPairResult(
        val publicKeyBase64: String,
        val privateKeyBase64: String
    )

    fun generateRsaKeyPair(): KeyPair {
        val keyGen = KeyPairGenerator.getInstance("RSA")
        keyGen.initialize(2048, SecureRandom())
        return keyGen.generateKeyPair()
    }

    fun encryptRsa(plainText: String, publicKey: java.security.PublicKey): String {
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        val cipherBytes = cipher.doFinal(plainText.toByteArray(StandardCharsets.UTF_8))
        return Base64.encodeToString(cipherBytes, Base64.NO_WRAP)
    }

    fun decryptRsa(cipherTextBase64: String, privateKey: java.security.PrivateKey): String {
        val cipherBytes = Base64.decode(cipherTextBase64, Base64.NO_WRAP)
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.DECRYPT_MODE, privateKey)
        val plainBytes = cipher.doFinal(cipherBytes)
        return String(plainBytes, StandardCharsets.UTF_8)
    }

    fun signDataRsa(plainText: String, privateKey: PrivateKey): String {
        val signer = Signature.getInstance("SHA256withRSA")
        signer.initSign(privateKey)
        signer.update(plainText.toByteArray(StandardCharsets.UTF_8))
        return Base64.encodeToString(signer.sign(), Base64.NO_WRAP)
    }

    fun verifySignatureRsa(plainText: String, signatureBase64: String, publicKey: PublicKey): Boolean {
        return try {
            val verifier = Signature.getInstance("SHA256withRSA")
            verifier.initVerify(publicKey)
            verifier.update(plainText.toByteArray(StandardCharsets.UTF_8))
            val sigBytes = Base64.decode(signatureBase64, Base64.NO_WRAP)
            verifier.verify(sigBytes)
        } catch (_: Exception) {
            false
        }
    }

    // ==================== TOY RSA EDUCATIONAL MATH ====================
    data class ToyRsaResult(
        val p: Long,
        val q: Long,
        val n: Long,
        val phi: Long,
        val e: Long,
        val d: Long,
        val message: Long,
        val cipher: Long,
        val decrypted: Long,
        val isValid: Boolean,
        val steps: List<String>
    )

    fun computeToyRsa(p: Long, q: Long, m: Long, eCandidate: Long = 17L): ToyRsaResult {
        val bigP = BigInteger.valueOf(p)
        val bigQ = BigInteger.valueOf(q)
        val bigN = bigP.multiply(bigQ)
        val phi = bigP.subtract(BigInteger.ONE).multiply(bigQ.subtract(BigInteger.ONE))

        var e = BigInteger.valueOf(eCandidate)
        if (e.compareTo(phi) >= 0 || !e.gcd(phi).equals(BigInteger.ONE)) {
            val candidates = listOf(3L, 5L, 7L, 11L, 13L, 17L, 19L, 23L, 65537L)
            val found = candidates.firstOrNull { BigInteger.valueOf(it).gcd(phi) == BigInteger.ONE && BigInteger.valueOf(it) < phi }
            e = BigInteger.valueOf(found ?: 3L)
        }

        val d = try {
            e.modInverse(phi)
        } catch (_: Exception) {
            BigInteger.ONE
        }

        val bigM = BigInteger.valueOf(m.coerceAtLeast(1) % bigN.toLong().coerceAtLeast(2))
        val cipher = bigM.modPow(e, bigN)
        val decrypted = cipher.modPow(d, bigN)

        val steps = listOf(
            "1. Primes chosen: p = $p, q = $q",
            "2. Modulus n = p × q = $p × $q = $bigN (Public key modulus)",
            "3. Euler's Totient φ(n) = (p-1) × (q-1) = (${p - 1}) × (${q - 1}) = $phi",
            "4. Public Exponent e = $e, coprime with φ(n) [gcd($e, $phi) = 1]",
            "5. Private Key d = e⁻¹ mod φ(n) = $d (calculated via Extended Euclidean)",
            "6. Encryption: c = mᵉ mod n = $bigM^$e mod $bigN = $cipher",
            "7. Decryption: m = cᵈ mod n = $cipher^$d mod $bigN = $decrypted"
        )

        return ToyRsaResult(
            p = p,
            q = q,
            n = bigN.toLong(),
            phi = phi.toLong(),
            e = e.toLong(),
            d = d.toLong(),
            message = bigM.toLong(),
            cipher = cipher.toLong(),
            decrypted = decrypted.toLong(),
            isValid = bigM == decrypted,
            steps = steps
        )
    }

    // ==================== DIFFIE-HELLMAN KEY EXCHANGE ====================
    data class DiffieHellmanResult(
        val p: Long,
        val g: Long,
        val alicePrivate: Long,
        val bobPrivate: Long,
        val alicePublic: Long,
        val bobPublic: Long,
        val aliceSecret: Long,
        val bobSecret: Long,
        val sharedSecretMatches: Boolean,
        val steps: List<String>
    )

    fun simulateDiffieHellman(p: Long, g: Long, a: Long, b: Long): DiffieHellmanResult {
        val bigP = BigInteger.valueOf(p)
        val bigG = BigInteger.valueOf(g)
        val bigA = BigInteger.valueOf(a)
        val bigB = BigInteger.valueOf(b)

        val alicePublic = bigG.modPow(bigA, bigP)
        val bobPublic = bigG.modPow(bigB, bigP)

        val aliceSecret = bobPublic.modPow(bigA, bigP)
        val bobSecret = alicePublic.modPow(bigB, bigP)

        val matches = aliceSecret == bobSecret
        val steps = listOf(
            "1. Public Domain Parameters: Prime modulus p = $p, Generator base g = $g",
            "2. Alice picks private secret a = $a (kept secret by Alice)",
            "3. Bob picks private secret b = $b (kept secret by Bob)",
            "4. Alice computes public key A = gᵃ mod p = $g^$a mod $p = $alicePublic and sends it to Bob",
            "5. Bob computes public key B = gᵇ mod p = $g^$b mod $p = $bobPublic and sends it to Alice",
            "6. Insecure Channel: Eavesdropper Eve sees p=$p, g=$g, A=$alicePublic, B=$bobPublic but cannot find a or b (Discrete Logarithm Problem)",
            "7. Alice computes Shared Secret K = Bᵃ mod p = $bobPublic^$a mod $p = $aliceSecret",
            "8. Bob computes Shared Secret K = Aᵇ mod p = $alicePublic^$b mod $p = $bobSecret",
            "9. Perfect Agreement: Alice and Bob now independently share secret K = $aliceSecret for AES symmetric encryption!"
        )

        return DiffieHellmanResult(
            p = p,
            g = g,
            alicePrivate = a,
            bobPrivate = b,
            alicePublic = alicePublic.toLong(),
            bobPublic = bobPublic.toLong(),
            aliceSecret = aliceSecret.toLong(),
            bobSecret = bobSecret.toLong(),
            sharedSecretMatches = matches,
            steps = steps
        )
    }

    // ==================== AVALANCHE EFFECT DEMO ====================
    data class AvalancheResult(
        val input1: String,
        val input2: String,
        val hash1Hex: String,
        val hash2Hex: String,
        val bitDifferences: Int,
        val totalBits: Int,
        val percentageChanged: Double
    )

    fun calculateAvalanche(text1: String, text2: String, algorithm: String = "SHA-256"): AvalancheResult {
        val digest = MessageDigest.getInstance(algorithm)
        val hash1Bytes = digest.digest(text1.toByteArray(StandardCharsets.UTF_8))
        digest.reset()
        val hash2Bytes = digest.digest(text2.toByteArray(StandardCharsets.UTF_8))

        var diffCount = 0
        val totalBits = hash1Bytes.size * 8
        for (i in hash1Bytes.indices) {
            val xor = (hash1Bytes[i].toInt() xor hash2Bytes[i].toInt()) and 0xFF
            diffCount += Integer.bitCount(xor)
        }

        val pct = if (totalBits > 0) (diffCount.toDouble() / totalBits) * 100.0 else 0.0
        return AvalancheResult(
            input1 = text1,
            input2 = text2,
            hash1Hex = hash1Bytes.joinToString("") { "%02x".format(it) },
            hash2Hex = hash2Bytes.joinToString("") { "%02x".format(it) },
            bitDifferences = diffCount,
            totalBits = totalBits,
            percentageChanged = (pct * 10).toInt() / 10.0
        )
    }

    // ==================== HASH FUNCTIONS ====================
    fun hashString(input: String, algorithm: String): String {
        val digest = MessageDigest.getInstance(algorithm)
        val hashBytes = digest.digest(input.toByteArray(StandardCharsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    fun hmacSha256(input: String, key: String): String {
        val secretKey = SecretKeySpec(key.toByteArray(StandardCharsets.UTF_8), "HmacSHA256")
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(secretKey)
        val bytes = mac.doFinal(input.toByteArray(StandardCharsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    // ==================== CLASSICAL CIPHERS ====================
    fun caesarCipher(text: String, shift: Int, encrypt: Boolean): String {
        val actualShift = if (encrypt) (shift % 26 + 26) % 26 else (26 - (shift % 26 + 26) % 26) % 26
        return text.map { char ->
            when {
                char.isUpperCase() -> ('A'.code + (char.code - 'A'.code + actualShift) % 26).toChar()
                char.isLowerCase() -> ('a'.code + (char.code - 'a'.code + actualShift) % 26).toChar()
                else -> char
            }
        }.joinToString("")
    }

    fun bruteForceCaesar(cipherText: String): List<Pair<Int, String>> {
        return (1..25).map { shift ->
            shift to caesarCipher(cipherText, shift, encrypt = false)
        }
    }

    fun vigenereCipher(text: String, key: String, encrypt: Boolean): String {
        if (key.isBlank()) return text
        val cleanedKey = key.filter { it.isLetter() }.uppercase()
        if (cleanedKey.isEmpty()) return text

        var keyIndex = 0
        return text.map { char ->
            if (char.isLetter()) {
                val base = if (char.isUpperCase()) 'A'.code else 'a'.code
                val shift = cleanedKey[keyIndex % cleanedKey.length].code - 'A'.code
                val effectiveShift = if (encrypt) shift else (26 - shift) % 26
                keyIndex++
                (base + (char.code - base + effectiveShift) % 26).toChar()
            } else {
                char
            }
        }.joinToString("")
    }

    // ==================== ZERO-WIDTH STEGANOGRAPHY ====================
    // Zero-width characters for invisible message hiding in plain text
    private const val ZW_ZERO = '\u200B' // Zero-width space
    private const val ZW_ONE = '\u200C'  // Zero-width non-joiner
    private const val ZW_START = '\u200D' // Zero-width joiner
    private const val ZW_END = '\uFEFF'   // Zero-width no-break space

    fun encodeZeroWidthStego(coverText: String, secretMessage: String): String {
        if (secretMessage.isEmpty()) return coverText
        val binary = secretMessage.toByteArray(StandardCharsets.UTF_8).joinToString("") { byte ->
            String.format("%8s", Integer.toBinaryString(byte.toInt() and 0xFF)).replace(' ', '0')
        }

        val hiddenBuilder = StringBuilder()
        hiddenBuilder.append(ZW_START)
        for (bit in binary) {
            hiddenBuilder.append(if (bit == '1') ZW_ONE else ZW_ZERO)
        }
        hiddenBuilder.append(ZW_END)

        // Inject after first space or at end
        val spaceIndex = coverText.indexOf(' ')
        return if (spaceIndex != -1) {
            coverText.substring(0, spaceIndex + 1) + hiddenBuilder.toString() + coverText.substring(spaceIndex + 1)
        } else {
            coverText + hiddenBuilder.toString()
        }
    }

    fun decodeZeroWidthStego(textWithSecret: String): String? {
        val startIdx = textWithSecret.indexOf(ZW_START)
        val endIdx = textWithSecret.indexOf(ZW_END)
        if (startIdx == -1 || endIdx == -1 || endIdx <= startIdx) return null

        val payload = textWithSecret.substring(startIdx + 1, endIdx)
        val bitString = StringBuilder()
        for (c in payload) {
            when (c) {
                ZW_ZERO -> bitString.append('0')
                ZW_ONE -> bitString.append('1')
            }
        }

        if (bitString.length % 8 != 0 || bitString.isEmpty()) return null

        val bytes = ByteArray(bitString.length / 8)
        for (i in bytes.indices) {
            val chunk = bitString.substring(i * 8, (i + 1) * 8)
            bytes[i] = chunk.toInt(2).toByte()
        }
        return String(bytes, StandardCharsets.UTF_8)
    }

    // ==================== PASSWORD ENTROPY & CRACK TIME ====================
    data class PasswordAuditResult(
        val entropyBits: Double,
        val charsetSize: Int,
        val strengthRating: String, // VERY_WEAK, WEAK, MODERATE, STRONG, VERY_STRONG
        val crackTimeOnline: String,
        val crackTimeCpu: String,
        val crackTimeGpuCluster: String,
        val hasLower: Boolean,
        val hasUpper: Boolean,
        val hasDigit: Boolean,
        val hasSymbol: Boolean,
        val tips: List<String>
    )

    fun auditPassword(password: String): PasswordAuditResult {
        if (password.isEmpty()) {
            return PasswordAuditResult(
                0.0, 0, "VERY_WEAK", "Instant", "Instant", "Instant",
                false, false, false, false, listOf("Enter a password to analyze.")
            )
        }

        var charsetSize = 0
        val hasLower = password.any { it.isLowerCase() }
        val hasUpper = password.any { it.isUpperCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSymbol = password.any { !it.isLetterOrDigit() }

        if (hasLower) charsetSize += 26
        if (hasUpper) charsetSize += 26
        if (hasDigit) charsetSize += 10
        if (hasSymbol) charsetSize += 33

        // Shannon / pool entropy in bits = length * log2(charsetSize)
        val entropy = if (charsetSize > 0) password.length * log2(charsetSize.toDouble()) else 0.0

        val rating = when {
            entropy < 28 -> "VERY_WEAK"
            entropy < 45 -> "WEAK"
            entropy < 65 -> "MODERATE"
            entropy < 85 -> "STRONG"
            else -> "VERY_STRONG"
        }

        // Combinations = charsetSize ^ length
        val combinations = charsetSize.toDouble().pow(password.length.toDouble())

        // Assumptions:
        // Online attack: 100 attempts / sec
        // Single CPU: 10,000,000 hashes / sec (10 MH/s)
        // 8x RTX 4090 GPU cluster: 100,000,000,000 hashes / sec (100 GH/s)
        val secondsOnline = combinations / 100.0
        val secondsCpu = combinations / 1e7
        val secondsGpu = combinations / 1e11

        val tips = mutableListOf<String>()
        if (password.length < 12) tips.add("Increase length to 12+ characters (length beats complexity).")
        if (!hasUpper) tips.add("Add uppercase letters (A-Z).")
        if (!hasDigit) tips.add("Add numerical digits (0-9).")
        if (!hasSymbol) tips.add("Add special symbols (!@#$%^&*).")
        if (password.lowercase().contains("password") || password.contains("12345")) {
            tips.add("Avoid dictionary words and common keyboard sequences.")
        }
        if (tips.isEmpty()) tips.add("Excellent password! High entropy protects against brute-force attacks.")

        return PasswordAuditResult(
            entropyBits = (entropy * 10).toInt() / 10.0,
            charsetSize = charsetSize,
            strengthRating = rating,
            crackTimeOnline = formatDuration(secondsOnline),
            crackTimeCpu = formatDuration(secondsCpu),
            crackTimeGpuCluster = formatDuration(secondsGpu),
            hasLower = hasLower,
            hasUpper = hasUpper,
            hasDigit = hasDigit,
            hasSymbol = hasSymbol,
            tips = tips
        )
    }

    private fun formatDuration(seconds: Double): String {
        return when {
            seconds < 1.0 -> "Instant (< 1 sec)"
            seconds < 60.0 -> "${seconds.toInt()} seconds"
            seconds < 3600.0 -> "${(seconds / 60).toInt()} minutes"
            seconds < 86400.0 -> "${(seconds / 3600).toInt()} hours"
            seconds < 31536000.0 -> "${(seconds / 86400).toInt()} days"
            seconds < 3153600000.0 -> "${(seconds / 31536000.0).toInt()} years"
            seconds < 3153600000000.0 -> "${(seconds / 31536000000.0).toInt()} thousand years"
            seconds < 3153600000000000.0 -> "${(seconds / 31536000000000.0).toInt()} billion years"
            else -> "Centuries (Astronomical)"
        }
    }

    fun generateSecurePassword(
        length: Int = 16,
        includeUpper: Boolean = true,
        includeLower: Boolean = true,
        includeDigits: Boolean = true,
        includeSymbols: Boolean = true
    ): String {
        val uppers = "ABCDEFGHJKLMNPQRSTUVWXYZ"
        val lowers = "abcdefghijkmnopqrstuvwxyz"
        val digits = "23456789"
        val symbols = "!@#$%^&*()-_=+[]{}<>?"

        val pool = StringBuilder()
        if (includeUpper) pool.append(uppers)
        if (includeLower) pool.append(lowers)
        if (includeDigits) pool.append(digits)
        if (includeSymbols) pool.append(symbols)

        if (pool.isEmpty()) return ""

        val random = SecureRandom()
        val chars = CharArray(length)
        for (i in 0 until length) {
            chars[i] = pool[random.nextInt(pool.length)]
        }
        return String(chars)
    }

    private fun deriveKey(passOrBase64: String?, keyLengthBytes: Int): ByteArray {
        if (passOrBase64.isNullOrEmpty()) {
            val key = ByteArray(keyLengthBytes)
            SecureRandom().nextBytes(key)
            return key
        }
        val md = MessageDigest.getInstance("SHA-256")
        val hash = md.digest(passOrBase64.toByteArray(StandardCharsets.UTF_8))
        return hash.copyOf(keyLengthBytes)
    }
}
