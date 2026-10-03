package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crypto.CryptoEngine
import com.example.ui.CyberViewModel
import com.example.ui.router.HashRouteBar
import com.example.ui.router.HashRouter
import com.example.ui.router.HashRouterHost
import com.example.ui.router.rememberHashRouter
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.CyberRed

/**
 * Cryptography Lab Screen utilizing a HashRouter to handle view switching.
 * Supports educational modules for AES, RSA, Diffie-Hellman, Hash/Avalanche, Caesar, and Steganography.
 */
@Composable
fun CryptoLabScreen(
    viewModel: CyberViewModel,
    modifier: Modifier = Modifier,
    initialRoute: String = "#/aes"
) {
    val router = rememberHashRouter(initialRoute = initialRoute)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // HashRouter Address Bar & Route Switcher Pills
        HashRouteBar(
            router = router,
            availableRoutes = HashRouter.DEFAULT_ROUTES,
            onRouteSelected = { route -> router.navigate(route) }
        )

        // View Switching managed by HashRouterHost
        HashRouterHost(
            router = router,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) { activeHash ->
            when (activeHash) {
                "#/aes", "#/aes-256", "#/aes-cbc" -> AesLabView(viewModel = viewModel, router = router)
                "#/rsa", "#/rsa-2048", "#/rsa-math" -> RsaLabView(viewModel = viewModel, router = router)
                "#/diffie-hellman", "#/dh" -> DiffieHellmanLabView(viewModel = viewModel, router = router)
                "#/hash-integrity", "#/hash", "#/avalanche" -> HashIntegrityLabView(viewModel = viewModel)
                "#/caesar-vigenere", "#/caesar" -> ClassicalCipherLabView(viewModel = viewModel)
                "#/steganography", "#/stego" -> SteganographyLabView(viewModel = viewModel)
                else -> AesLabView(viewModel = viewModel, router = router)
            }
        }
    }
}

// =========================================================================
// 1. AES LAB (Advanced Encryption Standard - GCM & CBC + 4-Step Matrix)
// =========================================================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AesLabView(viewModel: CyberViewModel, router: HashRouter) {
    val plainText by viewModel.aesPlainText.collectAsState()
    val passphrase by viewModel.aesPassphrase.collectAsState()
    val mode by viewModel.aesMode.collectAsState()
    val gcmResult by viewModel.aesResult.collectAsState()
    val cbcResult by viewModel.aesCbcResult.collectAsState()
    val decrypted by viewModel.aesDecryptedText.collectAsState()
    val matrixStages by viewModel.aesMatrixStages.collectAsState()
    val context = LocalContext.current

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Live Workbench, 1: 4-Step Round Visualizer, 2: Theory
    var activeMatrixStep by remember { mutableIntStateOf(0) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            CryptoHeaderCard(
                title = "AES (Advanced Encryption Standard - Rijndael)",
                subtitle = "NIST FIPS 197 standard symmetric block cipher. Encrypts data in 128-bit blocks using substitution-permutation networks.",
                badge = "Symmetric Block Cipher"
            )
        }

        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = CyberCyan,
                divider = {}
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Workbench", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("aes_tab_workbench")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("4-Step Visualizer", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("aes_tab_visualizer")
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Theory & Security", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("aes_tab_theory")
                )
            }
        }

        if (selectedTab == 0) {
            // Cipher Mode Selector
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Operational Mode:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    FilterChip(
                        selected = mode == "GCM",
                        onClick = { viewModel.aesMode.value = "GCM" },
                        label = { Text("AES-256 GCM (AEAD)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyberCyan.copy(alpha = 0.2f),
                            selectedLabelColor = CyberCyan
                        ),
                        modifier = Modifier.testTag("aes_mode_gcm")
                    )
                    FilterChip(
                        selected = mode == "CBC",
                        onClick = { viewModel.aesMode.value = "CBC" },
                        label = { Text("AES-128 CBC") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyberCyan.copy(alpha = 0.2f),
                            selectedLabelColor = CyberCyan
                        ),
                        modifier = Modifier.testTag("aes_mode_cbc")
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = plainText,
                    onValueChange = {
                        viewModel.aesPlainText.value = it
                        viewModel.updateAesVisualSample(it)
                    },
                    label = { Text("Plaintext Data to Encrypt") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("aes_plain_input"),
                    shape = RoundedCornerShape(10.dp)
                )
            }

            item {
                OutlinedTextField(
                    value = passphrase,
                    onValueChange = { viewModel.aesPassphrase.value = it },
                    label = { Text("Secret Passphrase / Key (Derives 256/128-bit key)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("aes_key_input"),
                    shape = RoundedCornerShape(10.dp)
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { viewModel.encryptAes() },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("aes_encrypt_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyberCyan,
                            contentColor = Color(0xFF00363D)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = "Encrypt", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Encrypt ($mode)")
                    }

                    OutlinedButton(
                        onClick = { viewModel.decryptAes() },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("aes_decrypt_btn"),
                        shape = RoundedCornerShape(10.dp),
                        enabled = if (mode == "CBC") cbcResult != null else gcmResult != null
                    ) {
                        Icon(imageVector = Icons.Default.LockOpen, contentDescription = "Decrypt", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Decrypt")
                    }
                }
            }

            // Encrypted Cipher Result Card
            val activeResult = if (mode == "CBC") {
                cbcResult?.let { it.cipherTextBase64 to it.ivBase64 }
            } else {
                gcmResult?.let { it.cipherTextBase64 to it.ivBase64 }
            }

            if (activeResult != null) {
                val (cipherB64, ivB64) = activeResult
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🔐 $mode Ciphertext (Base64)",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = CyberCyan
                                )
                                IconButton(
                                    onClick = { copyToClipboard(context, "Ciphertext", cipherB64) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                                }
                            }
                            Text(
                                text = cipherB64,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("🎲 Initialization Vector (IV):", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(ivB64, fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = CyberGreen)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Mode Guarantee:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        if (mode == "GCM") "Confidentiality + Authenticity" else "Confidentiality (CBC)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CyberCyan
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (decrypted != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("🔓 Verified Decrypted Plaintext:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = CyberGreen)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(decrypted!!, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }

            // Quick navigation to RSA
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { router.navigate("#/rsa") },
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Next Educational Module: RSA", fontWeight = FontWeight.Bold, color = CyberCyan, style = MaterialTheme.typography.labelMedium)
                            Text("Explore Asymmetric Public/Private Key Cryptography", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Navigate", tint = CyberCyan)
                    }
                }
            }
        } else if (selectedTab == 1) {
            // 4-Step Round Visualizer
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("AES Rijndael 4-Transformation Matrix", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = CyberCyan)
                        Text("Each AES round transforms a 4×4 byte matrix through four mathematical operations.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Step Selector Chips
            item {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    matrixStages.forEachIndexed { index, step ->
                        FilterChip(
                            selected = activeMatrixStep == index,
                            onClick = { activeMatrixStep = index },
                            label = { Text(step.stepName) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyberCyan.copy(alpha = 0.2f),
                                selectedLabelColor = CyberCyan
                            )
                        )
                    }
                }
            }

            // Active Stage 4x4 Matrix Display
            if (activeMatrixStep in matrixStages.indices) {
                val currentStage = matrixStages[activeMatrixStep]
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(currentStage.stepName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = CyberCyan)
                            Text(currentStage.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                            Text("4×4 State Matrix (Hex Values):", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)

                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                currentStage.matrix.forEachIndexed { rowIndex, row ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        row.forEachIndexed { colIndex, byteHex ->
                                            Box(
                                                modifier = Modifier
                                                    .size(46.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(
                                                        if (rowIndex == activeMatrixStep % 4) CyberCyan.copy(alpha = 0.15f)
                                                        else MaterialTheme.colorScheme.surface
                                                    )
                                                    .border(
                                                        1.dp,
                                                        if (rowIndex == activeMatrixStep % 4) CyberCyan else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                                        RoundedCornerShape(8.dp)
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = byteHex,
                                                    fontFamily = FontFamily.Monospace,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = if (rowIndex == activeMatrixStep % 4) CyberCyan else MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Theory Tab
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("💡 AES Cryptographic Properties for BCA Viva", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = CyberCyan)
                        Text(
                            "• Confusion & Diffusion: SubBytes creates non-linear confusion (shattering statistical relationships between key and ciphertext). ShiftRows and MixColumns provide diffusion (each plaintext bit affects multiple ciphertext bytes).\n\n" +
                                    "• Why ECB Mode is Flawed: Electronic Codebook encrypts identical 16-byte blocks identically, leaking patterns (e.g. the famous ECB Penguin experiment).\n\n" +
                                    "• CBC vs GCM: CBC mode uses an IV and chains previous blocks. GCM (Galois/Counter Mode) adds an authentication tag (AEAD) to prevent padding oracle and bit-flipping attacks.\n\n" +
                                    "• Key Schedule: 128-bit (10 rounds), 192-bit (12 rounds), 256-bit (14 rounds). Quantum-resistant against Grover's algorithm due to 256-bit key search space.",
                            style = MaterialTheme.typography.bodySmall,
                            lineHeight = 20.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

// =========================================================================
// 2. RSA LAB (Toy RSA Totient Math + 2048-bit Keystore Production Lab)
// =========================================================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RsaLabView(viewModel: CyberViewModel, router: HashRouter) {
    val keyPair by viewModel.rsaKeyPair.collectAsState()
    val plainText by viewModel.rsaPlainText.collectAsState()
    val encrypted by viewModel.rsaEncryptedBase64.collectAsState()
    val decrypted by viewModel.rsaDecryptedText.collectAsState()
    val signature by viewModel.rsaSignature.collectAsState()
    val verifyResult by viewModel.rsaVerifyResult.collectAsState()

    val toyP by viewModel.toyRsaP.collectAsState()
    val toyQ by viewModel.toyRsaQ.collectAsState()
    val toyM by viewModel.toyRsaM.collectAsState()
    val toyE by viewModel.toyRsaE.collectAsState()
    val toyResult by viewModel.toyRsaResult.collectAsState()
    val context = LocalContext.current

    var selectedSubTab by remember { mutableIntStateOf(0) } // 0: Toy RSA Math Sandbox, 1: 2048-bit Keystore & Signatures

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            CryptoHeaderCard(
                title = "RSA (Rivest–Shamir–Adleman) Asymmetric Cryptography",
                subtitle = "Uses a public key to encrypt and a mathematically linked private key to decrypt, based on the factoring hardness of large prime products.",
                badge = "Asymmetric / Public Key"
            )
        }

        item {
            TabRow(
                selectedTabIndex = selectedSubTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = CyberCyan,
                divider = {}
            ) {
                Tab(
                    selected = selectedSubTab == 0,
                    onClick = { selectedSubTab = 0 },
                    text = { Text("Educational Math Sandbox", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("rsa_tab_math")
                )
                Tab(
                    selected = selectedSubTab == 1,
                    onClick = { selectedSubTab = 1 },
                    text = { Text("2048-bit Lab & Signatures", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("rsa_tab_2048")
                )
            }
        }

        if (selectedSubTab == 0) {
            // Toy RSA Mathematical Sandbox
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("📐 Step-by-Step RSA Prime Walkthrough", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = CyberCyan)
                        Text("Select small prime numbers to watch Euler's Totient φ(n), the public key (e, n), and private key (d, n) calculated in real time.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        // Prime Preset Buttons
                        Text("Preset Prime Pairs (p, q):", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                Triple(61L, 53L, "p=61, q=53"),
                                Triple(11L, 13L, "p=11, q=13"),
                                Triple(17L, 19L, "p=17, q=19"),
                                Triple(31L, 37L, "p=31, q=37")
                            ).forEach { (pVal, qVal, label) ->
                                FilterChip(
                                    selected = toyP == pVal && toyQ == qVal,
                                    onClick = {
                                        viewModel.toyRsaP.value = pVal
                                        viewModel.toyRsaQ.value = qVal
                                        viewModel.updateToyRsa()
                                    },
                                    label = { Text(label, fontSize = 11.sp) }
                                )
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = toyP.toString(),
                                onValueChange = {
                                    viewModel.toyRsaP.value = it.toLongOrNull() ?: 61L
                                    viewModel.updateToyRsa()
                                },
                                label = { Text("Prime p") },
                                modifier = Modifier.weight(1f).testTag("rsa_p_input")
                            )
                            OutlinedTextField(
                                value = toyQ.toString(),
                                onValueChange = {
                                    viewModel.toyRsaQ.value = it.toLongOrNull() ?: 53L
                                    viewModel.updateToyRsa()
                                },
                                label = { Text("Prime q") },
                                modifier = Modifier.weight(1f).testTag("rsa_q_input")
                            )
                        }

                        OutlinedTextField(
                            value = toyM.toString(),
                            onValueChange = {
                                viewModel.toyRsaM.value = it.toLongOrNull() ?: 42L
                                viewModel.updateToyRsa()
                            },
                            label = { Text("Message Integer m (where m < n)") },
                            modifier = Modifier.fillMaxWidth().testTag("rsa_m_input")
                        )
                    }
                }
            }

            // Calculation Results & Mathematical Proof Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🧮 Mathematical Computation", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = CyberCyan)
                            Text(
                                if (toyResult.isValid) "✅ Decryption Verified" else "⚠️ Math Error",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (toyResult.isValid) CyberGreen else CyberRed
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                        toyResult.steps.forEach { stepText ->
                            Text(
                                text = stepText,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 16.sp
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Ciphertext c:", style = MaterialTheme.typography.labelSmall, color = CyberCyan)
                                Text("${toyResult.cipher}", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Decrypted m:", style = MaterialTheme.typography.labelSmall, color = CyberGreen)
                                Text("${toyResult.decrypted}", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        } else {
            // 2048-bit Production Lab & Digital Signature
            item {
                Button(
                    onClick = { viewModel.generateRsaKeys() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("rsa_gen_keys_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color(0xFF00363D)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.VpnKey, contentDescription = "Keys", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (keyPair == null) "Generate 2048-Bit RSA Keypair" else "Regenerate Fresh Keypair")
                }
            }

            if (keyPair != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            val pubStr = android.util.Base64.encodeToString(keyPair!!.public.encoded, android.util.Base64.NO_WRAP)
                            val privStr = android.util.Base64.encodeToString(keyPair!!.private.encoded, android.util.Base64.NO_WRAP)

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Public Key (Shared Publicly):", style = MaterialTheme.typography.labelSmall, color = CyberCyan, fontWeight = FontWeight.Bold)
                                IconButton(onClick = { copyToClipboard(context, "Public Key", pubStr) }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                                }
                            }
                            Text(pubStr.take(70) + "...", fontFamily = FontFamily.Monospace, fontSize = 11.sp)

                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Private Key (Kept Secret):", style = MaterialTheme.typography.labelSmall, color = CyberRed, fontWeight = FontWeight.Bold)
                                IconButton(onClick = { copyToClipboard(context, "Private Key", privStr) }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                                }
                            }
                            Text(privStr.take(70) + "...", fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = plainText,
                        onValueChange = { viewModel.rsaPlainText.value = it },
                        label = { Text("Message to Encrypt or Sign") },
                        modifier = Modifier.fillMaxWidth().testTag("rsa_plain_input"),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { viewModel.encryptRsa() },
                            modifier = Modifier.weight(1f).height(48.dp).testTag("rsa_encrypt_btn"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Encrypt (Pub Key)")
                        }

                        OutlinedButton(
                            onClick = { viewModel.decryptRsa() },
                            modifier = Modifier.weight(1f).height(48.dp).testTag("rsa_decrypt_btn"),
                            shape = RoundedCornerShape(10.dp),
                            enabled = encrypted != null
                        ) {
                            Text("Decrypt (Priv Key)")
                        }
                    }
                }

                if (encrypted != null) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = CardDefaults.outlinedCardBorder(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("🔒 RSA-2048 Ciphertext:", style = MaterialTheme.typography.labelSmall, color = CyberCyan, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(encrypted!!, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                            }
                        }
                    }
                }

                if (decrypted != null) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = CardDefaults.outlinedCardBorder(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("🔓 Decrypted with Private Key:", style = MaterialTheme.typography.labelSmall, color = CyberGreen, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(decrypted!!, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }

                // Digital Signature Section
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("✍️ RSA Digital Signature (SHA256withRSA)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = CyberCyan)
                            Text("Signatures prove authenticity: Signed with Private Key, verified by anyone with Public Key.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { viewModel.signRsa() },
                                    modifier = Modifier.weight(1f).height(44.dp).testTag("rsa_sign_btn"),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color(0xFF00363D))
                                ) {
                                    Text("Sign Message")
                                }

                                OutlinedButton(
                                    onClick = { viewModel.verifyRsa() },
                                    modifier = Modifier.weight(1f).height(44.dp).testTag("rsa_verify_btn"),
                                    shape = RoundedCornerShape(8.dp),
                                    enabled = signature != null
                                ) {
                                    Text("Verify Signature")
                                }
                            }

                            if (signature != null) {
                                Text("Signature (Base64):", style = MaterialTheme.typography.labelSmall, color = CyberCyan)
                                Text(signature!!, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                            }

                            if (verifyResult != null) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (verifyResult == true) CyberGreen.copy(alpha = 0.15f) else CyberRed.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = if (verifyResult == true) "✅ Signature Valid: Signed by authentic private key holder!" else "❌ Signature Invalid: Message or key does not match!",
                                        modifier = Modifier.padding(8.dp),
                                        fontWeight = FontWeight.Bold,
                                        color = if (verifyResult == true) CyberGreen else CyberRed,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Quick Link to Diffie-Hellman
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { router.navigate("#/diffie-hellman") },
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Next Module: Diffie-Hellman Key Exchange", fontWeight = FontWeight.Bold, color = CyberCyan, style = MaterialTheme.typography.labelMedium)
                        Text("Learn how symmetric keys are established over insecure channels", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Navigate", tint = CyberCyan)
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

// =========================================================================
// 3. DIFFIE-HELLMAN KEY EXCHANGE LAB
// =========================================================================
@Composable
fun DiffieHellmanLabView(viewModel: CyberViewModel, router: HashRouter) {
    val pVal by viewModel.dhP.collectAsState()
    val gVal by viewModel.dhG.collectAsState()
    val aliceSecret by viewModel.dhAlicePrivate.collectAsState()
    val bobSecret by viewModel.dhBobPrivate.collectAsState()
    val dhResult by viewModel.dhResult.collectAsState()

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            CryptoHeaderCard(
                title = "Diffie-Hellman Key Exchange Protocol",
                subtitle = "Enables two parties (Alice & Bob) who have no prior knowledge of each other to jointly establish a shared secret key over an unsecure channel.",
                badge = "Key Agreement"
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("⚙️ Public Domain Parameters (Agreed Publicly)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = CyberCyan)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = pVal.toString(),
                            onValueChange = {
                                viewModel.dhP.value = it.toLongOrNull() ?: 23L
                                viewModel.runDiffieHellman()
                            },
                            label = { Text("Prime Modulus (p)") },
                            modifier = Modifier.weight(1f).testTag("dh_p_input")
                        )
                        OutlinedTextField(
                            value = gVal.toString(),
                            onValueChange = {
                                viewModel.dhG.value = it.toLongOrNull() ?: 5L
                                viewModel.runDiffieHellman()
                            },
                            label = { Text("Generator Base (g)") },
                            modifier = Modifier.weight(1f).testTag("dh_g_input")
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                    Text("🔒 Private Secrets (Kept strictly private)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = CyberAmber)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = aliceSecret.toString(),
                            onValueChange = {
                                viewModel.dhAlicePrivate.value = it.toLongOrNull() ?: 6L
                                viewModel.runDiffieHellman()
                            },
                            label = { Text("Alice's Secret (a)") },
                            modifier = Modifier.weight(1f).testTag("dh_alice_input")
                        )
                        OutlinedTextField(
                            value = bobSecret.toString(),
                            onValueChange = {
                                viewModel.dhBobPrivate.value = it.toLongOrNull() ?: 15L
                                viewModel.runDiffieHellman()
                            },
                            label = { Text("Bob's Secret (b)") },
                            modifier = Modifier.weight(1f).testTag("dh_bob_input")
                        )
                    }
                }
            }
        }

        // Shared Secret Verification Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🔑 Key Exchange Mathematical Steps", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = CyberCyan)
                        Text(
                            if (dhResult.sharedSecretMatches) "✅ Shared Secret: K=${dhResult.aliceSecret}" else "❌ Error",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (dhResult.sharedSecretMatches) CyberGreen else CyberRed
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                    dhResult.steps.forEach { stepText ->
                        Text(
                            text = stepText,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 17.sp
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Alice Computes:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("K = Bᵃ mod p = ${dhResult.aliceSecret}", fontWeight = FontWeight.Bold, color = CyberGreen)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Bob Computes:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("K = Aᵇ mod p = ${dhResult.bobSecret}", fontWeight = FontWeight.Bold, color = CyberGreen)
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

// =========================================================================
// 4. HASH & AVALANCHE EFFECT LAB
// =========================================================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HashIntegrityLabView(viewModel: CyberViewModel) {
    val hashInput by viewModel.hashInput.collectAsState()
    val hashAlgo by viewModel.hashAlgorithm.collectAsState()
    val hashResult by viewModel.hashResult.collectAsState()
    val verifyInput by viewModel.verifyHashInput.collectAsState()
    val matchResult by viewModel.hashMatchResult.collectAsState()

    val text1 by viewModel.avalancheText1.collectAsState()
    val text2 by viewModel.avalancheText2.collectAsState()
    val avalancheResult by viewModel.avalancheResult.collectAsState()
    val context = LocalContext.current

    val algos = listOf("SHA-256", "SHA-512", "MD5", "SHA-1")

    LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            CryptoHeaderCard(
                title = "Cryptographic Hash & Avalanche Effect Lab",
                subtitle = "One-way deterministic digests. Changing a single bit in the input radically alters the output hash.",
                badge = "Integrity & Digests"
            )
        }

        item {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                algos.forEach { algo ->
                    FilterChip(
                        selected = hashAlgo == algo,
                        onClick = {
                            viewModel.hashAlgorithm.value = algo
                            viewModel.computeHash()
                        },
                        label = { Text(algo) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyberCyan.copy(alpha = 0.2f),
                            selectedLabelColor = CyberCyan
                        )
                    )
                }
            }
        }

        item {
            OutlinedTextField(
                value = hashInput,
                onValueChange = {
                    viewModel.hashInput.value = it
                    viewModel.computeHash()
                },
                label = { Text("Input String / Data") },
                modifier = Modifier.fillMaxWidth().testTag("hash_input_field"),
                shape = RoundedCornerShape(10.dp)
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("$hashAlgo Digest (${hashResult.length * 4} bits):", style = MaterialTheme.typography.labelSmall, color = CyberCyan, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { copyToClipboard(context, "$hashAlgo Hash", hashResult) }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = hashResult,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Avalanche Effect Visualizer Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("⚡ Avalanche Effect Demonstration", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = CyberCyan)
                    Text("A good hash function exhibits the Strict Avalanche Criterion (SAC): flipping 1 input bit causes ~50% of the output bits to flip.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    OutlinedTextField(
                        value = text1,
                        onValueChange = {
                            viewModel.avalancheText1.value = it
                            viewModel.updateAvalanche()
                        },
                        label = { Text("Text 1") },
                        modifier = Modifier.fillMaxWidth().testTag("avalanche_text1")
                    )

                    OutlinedTextField(
                        value = text2,
                        onValueChange = {
                            viewModel.avalancheText2.value = it
                            viewModel.updateAvalanche()
                        },
                        label = { Text("Text 2 (Change just 1 character)") },
                        modifier = Modifier.fillMaxWidth().testTag("avalanche_text2")
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = CyberCyan.copy(alpha = 0.12f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                "💥 Bit Flips: ${avalancheResult.bitDifferences} of ${avalancheResult.totalBits} bits (${avalancheResult.percentageChanged}% change)",
                                fontWeight = FontWeight.Bold,
                                color = CyberCyan,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                if (avalancheResult.percentageChanged in 40.0..60.0)
                                    "✅ Excellent Avalanche Effect! Indistinguishable from random noise."
                                else
                                    "Comparison completed across digest bit spaces.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Integrity Tampering Check
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🔍 Integrity Tamper Detection", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = CyberCyan)
                    OutlinedTextField(
                        value = verifyInput,
                        onValueChange = {
                            viewModel.verifyHashInput.value = it
                            viewModel.checkHashMatch()
                        },
                        label = { Text("Paste Expected Hash to Verify Integrity") },
                        modifier = Modifier.fillMaxWidth().testTag("hash_verify_field")
                    )

                    if (matchResult != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (matchResult == true) CyberGreen.copy(alpha = 0.15f) else CyberRed.copy(alpha = 0.15f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (matchResult == true) "✅ MATCH: Data integrity intact! No tampering detected." else "❌ MISMATCH: Hash differs! File or data modified.",
                                fontWeight = FontWeight.Bold,
                                color = if (matchResult == true) CyberGreen else CyberRed,
                                modifier = Modifier.padding(10.dp),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

// =========================================================================
// 5. CLASSICAL CIPHERS (Caesar & Brute-Force)
// =========================================================================
@Composable
fun ClassicalCipherLabView(viewModel: CyberViewModel) {
    val input by viewModel.caesarInput.collectAsState()
    val shift by viewModel.caesarShift.collectAsState()
    val output by viewModel.caesarOutput.collectAsState()
    val bruteForceList by viewModel.bruteForceResults.collectAsState()

    LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            CryptoHeaderCard(
                title = "Caesar Cipher & Brute-Force Cracker",
                subtitle = "Classical monoalphabetic substitution cipher. Key space is only 25, making it vulnerable to instant cryptanalysis.",
                badge = "Historical Ciphers"
            )
        }

        item {
            OutlinedTextField(
                value = input,
                onValueChange = { viewModel.updateCaesar(it, shift) },
                label = { Text("Text to Encipher") },
                modifier = Modifier.fillMaxWidth().testTag("caesar_input"),
                shape = RoundedCornerShape(10.dp)
            )
        }

        item {
            Text("Shift Key (K = $shift):", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Slider(
                value = shift.toFloat(),
                onValueChange = { viewModel.updateCaesar(input, it.toInt()) },
                valueRange = 0f..25f,
                steps = 24
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Encrypted Ciphertext:", style = MaterialTheme.typography.labelSmall, color = CyberCyan, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(output, style = MaterialTheme.typography.titleMedium, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            Button(
                onClick = { viewModel.runCaesarBruteForce() },
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("caesar_bruteforce_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color(0xFF00363D)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Run Brute-Force Cryptanalysis (All 25 Shifts)")
            }
        }

        if (bruteForceList.isNotEmpty()) {
            items(bruteForceList) { (s, text) ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (s == shift) CyberCyan.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                    ),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Shift -$s:", fontWeight = FontWeight.Bold, color = CyberCyan, style = MaterialTheme.typography.labelMedium)
                        Text(text, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

// =========================================================================
// 6. STEGANOGRAPHY LAB
// =========================================================================
@Composable
fun SteganographyLabView(viewModel: CyberViewModel) {
    val cover by viewModel.stegoCover.collectAsState()
    val secret by viewModel.stegoSecret.collectAsState()
    val encoded by viewModel.stegoEncoded.collectAsState()
    val decoded by viewModel.stegoDecoded.collectAsState()
    val context = LocalContext.current

    LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            CryptoHeaderCard(
                title = "Invisible Unicode Steganography",
                subtitle = "Conceals a secret binary message inside innocent cover text using Zero-Width Unicode characters (U+200B and U+200C). Completely invisible to the human eye!",
                badge = "Data Hiding"
            )
        }

        item {
            OutlinedTextField(
                value = cover,
                onValueChange = { viewModel.stegoCover.value = it },
                label = { Text("Normal Cover Text (Visible Message)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )
        }

        item {
            OutlinedTextField(
                value = secret,
                onValueChange = { viewModel.stegoSecret.value = it },
                label = { Text("Confidential Secret to Hide") },
                modifier = Modifier.fillMaxWidth().testTag("stego_secret_input"),
                shape = RoundedCornerShape(10.dp)
            )
        }

        item {
            Button(
                onClick = { viewModel.encodeStego() },
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("stego_encode_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color(0xFF00363D)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Embed Secret into Cover Text (Stego Encode)")
            }
        }

        if (encoded.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Carrier Text (Secret Embedded Inaudibly):", style = MaterialTheme.typography.labelSmall, color = CyberCyan, fontWeight = FontWeight.Bold)
                            IconButton(onClick = { copyToClipboard(context, "Stego Text", encoded) }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(encoded, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Notice: The secret characters have 0-pixel width! You can copy and paste this text anywhere.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                OutlinedButton(
                    onClick = { viewModel.decodeStego() },
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("stego_decode_btn"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Visibility, contentDescription = "Extract", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Extract Hidden Secret from Carrier Text")
                }
            }
        }

        if (decoded != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("🎯 Extracted Secret Payload:", style = MaterialTheme.typography.labelSmall, color = CyberGreen, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(decoded!!, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = CyberGreen)
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

// =========================================================================
// Helper UI Components
// =========================================================================
@Composable
fun CryptoHeaderCard(
    title: String,
    subtitle: String,
    badge: String? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = CyberCyan,
                    modifier = Modifier.weight(1f)
                )
                if (badge != null) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = CyberCyan.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = badge,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberCyan,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
        }
    }
}

private fun copyToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
    Toast.makeText(context, "Copied $label to clipboard", Toast.LENGTH_SHORT).show()
}
