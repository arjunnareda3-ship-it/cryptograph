package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.crypto.CryptoEngine
import com.example.data.local.AppDatabase
import com.example.data.model.CyberProjectIdea
import com.example.data.model.ProjectGuideData
import com.example.data.model.SecurityLogEntity
import com.example.data.repository.SecurityRepository
import com.example.network.NetworkInspector
import com.example.scanner.AppAuditHelper
import com.example.scanner.PhishingScanner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.security.KeyPair

enum class CyberTab(val label: String, val iconName: String) {
    PROJECTS("Project Ideas", "School"),
    CRYPTO("Crypto Lab", "Lock"),
    SCANNER("Threat Scan", "Shield"),
    NETWORK("Net & Apps", "Router"),
    VIVA_QUIZ("Viva & Quiz", "Quiz")
}

class CyberViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SecurityRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = SecurityRepository(db.securityLogDao())
    }

    val securityLogs: StateFlow<List<SecurityLogEntity>> = repository.allLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Navigation
    private val _currentTab = MutableStateFlow(CyberTab.PROJECTS)
    val currentTab = _currentTab.asStateFlow()

    fun selectTab(tab: CyberTab) {
        _currentTab.value = tab
    }

    // Selected project detail
    private val _selectedProject = MutableStateFlow<CyberProjectIdea?>(null)
    val selectedProject = _selectedProject.asStateFlow()

    fun selectProject(project: CyberProjectIdea?) {
        _selectedProject.value = project
    }

    // ==================== CRYPTO LAB STATE ====================
    // AES (GCM & CBC Modes + Educational Visual Matrix)
    var aesPlainText = MutableStateFlow("Confidential BCA Cyber Security Project Data")
    var aesPassphrase = MutableStateFlow("CyberSecretKey2026")
    var aesMode = MutableStateFlow("GCM") // "GCM" (256-bit) or "CBC" (128-bit)
    var aesResult = MutableStateFlow<CryptoEngine.AesResult?>(null)
    var aesCbcResult = MutableStateFlow<CryptoEngine.AesCbcResult?>(null)
    var aesDecryptedText = MutableStateFlow<String?>(null)
    var aesMatrixStages = MutableStateFlow(CryptoEngine.getAesEducationalStages("CYBER2026"))

    fun updateAesVisualSample(sample: String) {
        val safeText = if (sample.isBlank()) "CRYPTO" else sample
        aesMatrixStages.value = CryptoEngine.getAesEducationalStages(safeText)
    }

    fun encryptAes() {
        try {
            if (aesMode.value == "CBC") {
                val res = CryptoEngine.encryptAesCbc(aesPlainText.value, aesPassphrase.value)
                aesCbcResult.value = res
                aesDecryptedText.value = null
                logAction("CRYPTO", "AES-128-CBC Encryption", "Encrypted ${aesPlainText.value.length} chars with CBC Mode")
            } else {
                val res = CryptoEngine.encryptAesGcm(aesPlainText.value, aesPassphrase.value)
                aesResult.value = res
                aesDecryptedText.value = null
                logAction("CRYPTO", "AES-256-GCM Encryption", "Encrypted ${aesPlainText.value.length} chars with GCM AEAD Mode")
            }
        } catch (_: Exception) {}
    }

    fun decryptAes() {
        try {
            if (aesMode.value == "CBC") {
                val res = aesCbcResult.value ?: return
                val decrypted = CryptoEngine.decryptAesCbc(res.cipherTextBase64, res.ivBase64, res.keyUsedBase64)
                aesDecryptedText.value = decrypted
            } else {
                val res = aesResult.value ?: return
                val decrypted = CryptoEngine.decryptAesGcm(res.cipherTextBase64, res.ivBase64, res.keyUsedBase64)
                aesDecryptedText.value = decrypted
            }
        } catch (e: Exception) {
            aesDecryptedText.value = "Decryption Failed: ${e.message}"
        }
    }

    // RSA (2048-bit + Digital Signatures + Step-by-Step Educational Totient Math)
    var rsaKeyPair = MutableStateFlow<KeyPair?>(null)
    var rsaPlainText = MutableStateFlow("RSA Key Exchange Payload")
    var rsaEncryptedBase64 = MutableStateFlow<String?>(null)
    var rsaDecryptedText = MutableStateFlow<String?>(null)
    var rsaSignature = MutableStateFlow<String?>(null)
    var rsaVerifyResult = MutableStateFlow<Boolean?>(null)

    // Toy RSA Educational Math Sandbox (Small Primes)
    var toyRsaP = MutableStateFlow(61L)
    var toyRsaQ = MutableStateFlow(53L)
    var toyRsaM = MutableStateFlow(42L)
    var toyRsaE = MutableStateFlow(17L)
    var toyRsaResult = MutableStateFlow(CryptoEngine.computeToyRsa(61L, 53L, 42L, 17L))

    fun updateToyRsa() {
        toyRsaResult.value = CryptoEngine.computeToyRsa(
            p = toyRsaP.value,
            q = toyRsaQ.value,
            m = toyRsaM.value,
            eCandidate = toyRsaE.value
        )
    }

    fun generateRsaKeys() {
        viewModelScope.launch {
            val pair = CryptoEngine.generateRsaKeyPair()
            rsaKeyPair.value = pair
            rsaEncryptedBase64.value = null
            rsaDecryptedText.value = null
            rsaSignature.value = null
            rsaVerifyResult.value = null
            logAction("CRYPTO", "RSA-2048 Generation", "Generated fresh 2048-bit Asymmetric Keypair")
        }
    }

    fun encryptRsa() {
        val pair = rsaKeyPair.value ?: return
        try {
            val cipher = CryptoEngine.encryptRsa(rsaPlainText.value, pair.public)
            rsaEncryptedBase64.value = cipher
            rsaDecryptedText.value = null
        } catch (e: Exception) {
            rsaEncryptedBase64.value = "RSA Encryption Error: ${e.message}"
        }
    }

    fun decryptRsa() {
        val pair = rsaKeyPair.value ?: return
        val cipher = rsaEncryptedBase64.value ?: return
        try {
            val plain = CryptoEngine.decryptRsa(cipher, pair.private)
            rsaDecryptedText.value = plain
        } catch (e: Exception) {
            rsaDecryptedText.value = "RSA Decryption Error: ${e.message}"
        }
    }

    fun signRsa() {
        val pair = rsaKeyPair.value ?: return
        try {
            val sig = CryptoEngine.signDataRsa(rsaPlainText.value, pair.private)
            rsaSignature.value = sig
            rsaVerifyResult.value = null
        } catch (_: Exception) {}
    }

    fun verifyRsa() {
        val pair = rsaKeyPair.value ?: return
        val sig = rsaSignature.value ?: return
        val verified = CryptoEngine.verifySignatureRsa(rsaPlainText.value, sig, pair.public)
        rsaVerifyResult.value = verified
    }

    // Diffie-Hellman Key Exchange Simulation
    var dhP = MutableStateFlow(23L)
    var dhG = MutableStateFlow(5L)
    var dhAlicePrivate = MutableStateFlow(6L)
    var dhBobPrivate = MutableStateFlow(15L)
    var dhResult = MutableStateFlow(CryptoEngine.simulateDiffieHellman(23L, 5L, 6L, 15L))

    fun runDiffieHellman() {
        dhResult.value = CryptoEngine.simulateDiffieHellman(
            p = dhP.value,
            g = dhG.value,
            a = dhAlicePrivate.value,
            b = dhBobPrivate.value
        )
    }

    // Avalanche Effect Lab
    var avalancheText1 = MutableStateFlow("CyberSecurityLab2026")
    var avalancheText2 = MutableStateFlow("CyberSecurityLab2027")
    var avalancheAlgo = MutableStateFlow("SHA-256")
    var avalancheResult = MutableStateFlow(CryptoEngine.calculateAvalanche("CyberSecurityLab2026", "CyberSecurityLab2027", "SHA-256"))

    fun updateAvalanche() {
        avalancheResult.value = CryptoEngine.calculateAvalanche(
            text1 = avalancheText1.value,
            text2 = avalancheText2.value,
            algorithm = avalancheAlgo.value
        )
    }

    // Hashes
    var hashInput = MutableStateFlow("IntegrityCheckString123")
    var hashAlgorithm = MutableStateFlow("SHA-256")
    var hashResult = MutableStateFlow(CryptoEngine.hashString("IntegrityCheckString123", "SHA-256"))
    var verifyHashInput = MutableStateFlow("")
    var hashMatchResult = MutableStateFlow<Boolean?>(null)

    fun computeHash() {
        val res = CryptoEngine.hashString(hashInput.value, hashAlgorithm.value)
        hashResult.value = res
        checkHashMatch()
    }

    fun checkHashMatch() {
        if (verifyHashInput.value.isBlank()) {
            hashMatchResult.value = null
        } else {
            hashMatchResult.value = hashResult.value.equals(verifyHashInput.value.trim(), ignoreCase = true)
        }
    }

    // Caesar & Vigenère
    var caesarInput = MutableStateFlow("CYBER DEFENSE INITIATIVE")
    var caesarShift = MutableStateFlow(3)
    var caesarOutput = MutableStateFlow(CryptoEngine.caesarCipher("CYBER DEFENSE INITIATIVE", 3, true))
    var bruteForceResults = MutableStateFlow<List<Pair<Int, String>>>(emptyList())

    fun updateCaesar(text: String, shift: Int) {
        caesarInput.value = text
        caesarShift.value = shift
        caesarOutput.value = CryptoEngine.caesarCipher(text, shift, true)
    }

    fun runCaesarBruteForce() {
        bruteForceResults.value = CryptoEngine.bruteForceCaesar(caesarOutput.value)
    }

    // Zero-width stego
    var stegoCover = MutableStateFlow("Hello Professor, please check the project report attached.")
    var stegoSecret = MutableStateFlow("FLAG{bca_cyber_stego_success}")
    var stegoEncoded = MutableStateFlow("")
    var stegoDecoded = MutableStateFlow<String?>(null)

    fun encodeStego() {
        val encoded = CryptoEngine.encodeZeroWidthStego(stegoCover.value, stegoSecret.value)
        stegoEncoded.value = encoded
        stegoDecoded.value = null
    }

    fun decodeStego(text: String = stegoEncoded.value) {
        val decoded = CryptoEngine.decodeZeroWidthStego(text)
        stegoDecoded.value = decoded ?: "No hidden stego payload detected in text."
    }

    // ==================== THREAT & URL SCANNER ====================
    var targetUrl = MutableStateFlow("http://secure-login.bank-verify.top/account-kyc")
    var urlReport = MutableStateFlow<PhishingScanner.ScanReport?>(null)
    var isScanningUrl = MutableStateFlow(false)

    fun scanUrl() {
        isScanningUrl.value = true
        viewModelScope.launch {
            val report = PhishingScanner.analyzeUrl(targetUrl.value)
            urlReport.value = report
            isScanningUrl.value = false
            logAction(
                category = "PHISHING_SCAN",
                title = "Scanned ${report.domain}",
                details = "Threat Score: ${report.threatScore}/100 - ${report.verdict}",
                riskLevel = if (report.threatScore > 50) "HIGH" else "SAFE"
            )
        }
    }

    // Password Entropy
    var passwordInput = MutableStateFlow("CyberPass#2026!")
    var passwordAudit = MutableStateFlow(CryptoEngine.auditPassword("CyberPass#2026!"))

    fun updatePassword(newPass: String) {
        passwordInput.value = newPass
        passwordAudit.value = CryptoEngine.auditPassword(newPass)
    }

    fun generateNewPassword() {
        val generated = CryptoEngine.generateSecurePassword(16)
        passwordInput.value = generated
        passwordAudit.value = CryptoEngine.auditPassword(generated)
    }

    // ==================== NETWORK & APP AUDIT ====================
    var networkDetails = MutableStateFlow(NetworkInspector.getNetworkDetails(application))
    var portTargetHost = MutableStateFlow("127.0.0.1")
    var isScanningPorts = MutableStateFlow(false)
    var portScanResults = MutableStateFlow<List<NetworkInspector.PortScanResult>>(emptyList())
    var pingResult = MutableStateFlow<NetworkInspector.PingResult?>(null)
    var isPinging = MutableStateFlow(false)

    fun refreshNetworkInfo() {
        networkDetails.value = NetworkInspector.getNetworkDetails(getApplication())
    }

    fun runPortScan() {
        isScanningPorts.value = true
        viewModelScope.launch {
            val results = NetworkInspector.scanTargetPorts(portTargetHost.value)
            portScanResults.value = results
            isScanningPorts.value = false
            val openCount = results.count { it.isOpen }
            logAction(
                category = "NETWORK_AUDIT",
                title = "Port Scan: ${portTargetHost.value}",
                details = "Scanned ${results.size} common ports. Found $openCount open ports.",
                riskLevel = if (openCount > 0) "MEDIUM" else "SAFE"
            )
        }
    }

    fun runPing() {
        isPinging.value = true
        viewModelScope.launch {
            val result = NetworkInspector.pingHost(portTargetHost.value)
            pingResult.value = result
            isPinging.value = false
        }
    }

    // App Permissions Audit
    var isAuditingApps = MutableStateFlow(false)
    var auditedApps = MutableStateFlow<List<AppAuditHelper.AppSecurityReport>>(emptyList())
    var filterHighRiskOnly = MutableStateFlow(false)

    fun auditDeviceApps() {
        isAuditingApps.value = true
        viewModelScope.launch {
            val apps = AppAuditHelper.auditInstalledApps(getApplication())
            auditedApps.value = apps
            isAuditingApps.value = false
            logAction(
                category = "APP_AUDIT",
                title = "Installed Apps Audit",
                details = "Analyzed ${apps.size} applications for dangerous permissions.",
                riskLevel = "INFO"
            )
        }
    }

    // ==================== VIVA QUIZ ====================
    var currentQuizIndex = MutableStateFlow(0)
    var selectedOption = MutableStateFlow<Int?>(null)
    var isAnswerSubmitted = MutableStateFlow(false)
    var quizScore = MutableStateFlow(0)
    var isQuizCompleted = MutableStateFlow(false)

    fun selectQuizOption(index: Int) {
        if (!isAnswerSubmitted.value) {
            selectedOption.value = index
        }
    }

    fun submitQuizAnswer() {
        val curIndex = currentQuizIndex.value
        val questions = ProjectGuideData.quizQuestions
        if (curIndex in questions.indices && selectedOption.value != null) {
            isAnswerSubmitted.value = true
            if (selectedOption.value == questions[curIndex].correctIndex) {
                quizScore.value += 1
            }
        }
    }

    fun nextQuizQuestion() {
        val nextIdx = currentQuizIndex.value + 1
        if (nextIdx < ProjectGuideData.quizQuestions.size) {
            currentQuizIndex.value = nextIdx
            selectedOption.value = null
            isAnswerSubmitted.value = false
        } else {
            isQuizCompleted.value = true
        }
    }

    fun restartQuiz() {
        currentQuizIndex.value = 0
        selectedOption.value = null
        isAnswerSubmitted.value = false
        quizScore.value = 0
        isQuizCompleted.value = false
    }

    // Logs
    private fun logAction(category: String, title: String, details: String, riskLevel: String = "INFO") {
        viewModelScope.launch {
            repository.insertLog(category, title, details, riskLevel)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearLogs()
        }
    }
}
