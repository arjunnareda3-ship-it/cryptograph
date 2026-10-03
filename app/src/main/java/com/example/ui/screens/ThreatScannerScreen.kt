package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.scanner.PhishingScanner
import com.example.ui.CyberViewModel
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.CyberRed

@Composable
fun ThreatScannerScreen(
    viewModel: CyberViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 8.dp)
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = CyberCyan
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Phishing URL Scanner", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Password Entropy Lab", fontWeight = FontWeight.Bold) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            if (selectedTab == 0) {
                PhishingUrlSection(viewModel)
            } else {
                PasswordEntropySection(viewModel)
            }
        }
    }
}

// ----------------- PHISHING SCANNER -----------------
@Composable
fun PhishingUrlSection(viewModel: CyberViewModel) {
    val targetUrl by viewModel.targetUrl.collectAsState()
    val report by viewModel.urlReport.collectAsState()
    val isScanning by viewModel.isScanningUrl.collectAsState()

    LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "🛡️ Multi-Vector URL Heuristic Analyzer",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = CyberCyan
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Audits URLs against homograph attacks, suspicious TLDs, IP-based destinations, and Shannon character entropy.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            OutlinedTextField(
                value = targetUrl,
                onValueChange = { viewModel.targetUrl.value = it },
                label = { Text("Enter Web URL to Inspect") },
                modifier = Modifier.fillMaxWidth().testTag("phishing_url_input"),
                shape = RoundedCornerShape(10.dp),
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Link, contentDescription = "URL", tint = CyberCyan)
                }
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Preset samples for fast demo
                PresetChip("Bank Scam Sample") {
                    viewModel.targetUrl.value = "http://verify-sbi-banking-update.xyz/login-kyc"
                    viewModel.scanUrl()
                }
                PresetChip("IP Host Sample") {
                    viewModel.targetUrl.value = "http://192.168.1.100@phish-login.com"
                    viewModel.scanUrl()
                }
                PresetChip("Safe Google") {
                    viewModel.targetUrl.value = "https://www.google.com"
                    viewModel.scanUrl()
                }
            }
        }

        item {
            Button(
                onClick = { viewModel.scanUrl() },
                modifier = Modifier.fillMaxWidth().testTag("scan_url_button"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color(0xFF00363D)),
                enabled = !isScanning
            ) {
                if (isScanning) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Executing Heuristic Engine...")
                } else {
                    Icon(imageVector = Icons.Default.Shield, contentDescription = "Scan")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Scan URL for Phishing Markers")
                }
            }
        }

        if (report != null) {
            item {
                ThreatScoreCard(report = report!!)
            }

            item {
                Text(
                    text = "Detailed Heuristics & Findings (${report!!.findings.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            items(report!!.findings) { finding ->
                FindingItemCard(finding = finding)
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
fun PresetChip(label: String, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = CardDefaults.outlinedCardBorder(),
        modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { onClick() }
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = CyberCyan,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun ThreatScoreCard(report: PhishingScanner.ScanReport) {
    val (color, label) = when (report.verdict) {
        "SAFE" -> CyberGreen to "SAFE DOMAIN"
        "LOW_RISK" -> CyberCyan to "LOW RISK"
        "SUSPICIOUS" -> CyberAmber to "SUSPICIOUS SITE"
        else -> CyberRed to "DANGEROUS PHISHING"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Threat Risk Score",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${report.threatScore} / 100",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = color.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, color)
                ) {
                    Text(
                        text = label,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium,
                        color = color,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { report.threatScore / 100f },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = color,
                trackColor = MaterialTheme.colorScheme.surface
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Host Domain: ${report.domain}",
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Domain Shannon Entropy: ${report.entropy} bits",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Recommendation: ${report.recommendation}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        }
    }
}

@Composable
fun FindingItemCard(finding: PhishingScanner.Finding) {
    val iconColor = when (finding.severity) {
        PhishingScanner.Severity.CRITICAL -> CyberRed
        PhishingScanner.Severity.HIGH -> CyberRed
        PhishingScanner.Severity.MEDIUM -> CyberAmber
        PhishingScanner.Severity.LOW -> CyberCyan
        PhishingScanner.Severity.INFO -> CyberGreen
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = when (finding.severity) {
                    PhishingScanner.Severity.CRITICAL, PhishingScanner.Severity.HIGH -> Icons.Default.Error
                    PhishingScanner.Severity.MEDIUM -> Icons.Default.Warning
                    else -> Icons.Default.Check
                },
                contentDescription = "Severity",
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = finding.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = finding.severity.name,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = iconColor
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = finding.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ----------------- PASSWORD ENTROPY -----------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PasswordEntropySection(viewModel: CyberViewModel) {
    val password by viewModel.passwordInput.collectAsState()
    val audit by viewModel.passwordAudit.collectAsState()

    LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "🔐 Password Entropy & GPU Cracking Lab",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = CyberCyan
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Calculates pool Shannon Entropy in bits ($ E = L \\times \\log_2 R $) and estimates real-world cracking times against NVIDIA RTX 4090 Hashcat rigs.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            OutlinedTextField(
                value = password,
                onValueChange = { viewModel.updatePassword(it) },
                label = { Text("Type Password to Audit") },
                modifier = Modifier.fillMaxWidth().testTag("password_entropy_input"),
                shape = RoundedCornerShape(10.dp),
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = "Pass", tint = CyberCyan)
                }
            )
        }

        item {
            Button(
                onClick = { viewModel.generateNewPassword() },
                modifier = Modifier.fillMaxWidth().testTag("generate_secure_pass_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color(0xFF00363D)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.Autorenew, contentDescription = "Generate")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generate 16-Char Cryptographic Password")
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Entropy Pool Value", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${audit.entropyBits} bits", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = CyberCyan)
                        }
                        val ratingColor = when (audit.strengthRating) {
                            "VERY_STRONG" -> CyberGreen
                            "STRONG" -> CyberCyan
                            "MODERATE" -> CyberAmber
                            else -> CyberRed
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = ratingColor.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, ratingColor)
                        ) {
                            Text(
                                text = audit.strengthRating,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = ratingColor,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text("Crack Time Benchmark Matrix:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(6.dp))

                    BenchmarkRow("🌐 Online Attack (100 req/s):", audit.crackTimeOnline)
                    BenchmarkRow("💻 Fast CPU (10 MH/s):", audit.crackTimeCpu)
                    BenchmarkRow("🚀 8x RTX 4090 GPU Rig (100 GH/s):", audit.crackTimeGpuCluster)

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Character Complexity:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        ComplexityBadge("Lowercase (a-z)", audit.hasLower)
                        ComplexityBadge("Uppercase (A-Z)", audit.hasUpper)
                        ComplexityBadge("Numbers (0-9)", audit.hasDigit)
                        ComplexityBadge("Symbols (!@#$)", audit.hasSymbol)
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("💡 Security Hardening Advice:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = CyberCyan)
                    Spacer(modifier = Modifier.height(6.dp))
                    audit.tips.forEach { tip ->
                        Row(modifier = Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.Top) {
                            Text("• ", fontWeight = FontWeight.Bold, color = CyberCyan)
                            Text(tip, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
fun BenchmarkRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = CyberCyan)
    }
}

@Composable
fun ComplexityBadge(label: String, active: Boolean) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = if (active) CyberGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (active) Icons.Default.Check else Icons.Default.Close,
                contentDescription = null,
                tint = if (active) CyberGreen else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (active) CyberGreen else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun BorderStroke(width: androidx.compose.ui.unit.Dp, color: Color) =
    androidx.compose.foundation.BorderStroke(width, color)
