package com.example.scanner

import java.net.URI
import java.util.regex.Pattern
import kotlin.math.log2

object PhishingScanner {

    data class ScanReport(
        val originalUrl: String,
        val domain: String,
        val isHttps: Boolean,
        val threatScore: Int, // 0 to 100
        val verdict: String,  // SAFE, LOW_RISK, SUSPICIOUS, DANGEROUS_PHISHING
        val findings: List<Finding>,
        val entropy: Double,
        val recommendation: String
    )

    data class Finding(
        val title: String,
        val description: String,
        val severity: Severity // INFO, LOW, MEDIUM, HIGH, CRITICAL
    )

    enum class Severity { INFO, LOW, MEDIUM, HIGH, CRITICAL }

    private val SUSPICIOUS_TLDS = setOf(
        "xyz", "top", "tk", "ml", "ga", "cf", "gq", "work", "click", "buzz",
        "fit", "rest", "cam", "country", "stream", "live", "link"
    )

    private val PHISHING_KEYWORDS = listOf(
        "login", "verify", "secure", "update", "banking", "account",
        "signin", "auth", "kyc", "wallet", "support", "confirm",
        "security", "recovery", "password", "paypal", "google", "apple", "sbi"
    )

    private val IP_PATTERN = Pattern.compile("^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$")

    fun analyzeUrl(rawUrl: String): ScanReport {
        var url = rawUrl.trim()
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://$url"
        }

        val findings = mutableListOf<Finding>()
        var penalty = 0

        val uri = try {
            URI(url)
        } catch (_: Exception) {
            findings.add(
                Finding("Malformed URL Syntax", "The URL has an invalid RFC-compliant structure.", Severity.HIGH)
            )
            return ScanReport(
                originalUrl = rawUrl,
                domain = "Unknown",
                isHttps = false,
                threatScore = 80,
                verdict = "DANGEROUS_PHISHING",
                findings = findings,
                entropy = 0.0,
                recommendation = "Do not open this URL; structure is malformed."
            )
        }

        val host = uri.host ?: ""
        val scheme = uri.scheme ?: ""
        val isHttps = scheme.equals("https", ignoreCase = true)
        val path = uri.path ?: ""
        val query = uri.query ?: ""

        // 1. Protocol check
        if (!isHttps) {
            penalty += 20
            findings.add(
                Finding("Insecure Protocol (HTTP)", "Traffic is unencrypted and vulnerable to Man-in-the-Middle (MitM) packet sniffing.", Severity.HIGH)
            )
        } else {
            findings.add(
                Finding("Encrypted Transport (HTTPS)", "Uses SSL/TLS encryption for transit.", Severity.INFO)
            )
        }

        // 2. Direct IP Address in host
        if (IP_PATTERN.matcher(host).matches()) {
            penalty += 35
            findings.add(
                Finding("Raw IP Address Hostname", "URL points directly to an IP address instead of a reputable domain name.", Severity.CRITICAL)
            )
        }

        // 3. '@' Symbol in URL
        if (rawUrl.contains("@")) {
            penalty += 30
            findings.add(
                Finding("Credential Trap (@ Symbol)", "Contains an '@' character, often used to obscure actual destination host.", Severity.CRITICAL)
            )
        }

        // 4. Excessive Subdomains or Dots
        val dotCount = host.count { it == '.' }
        if (dotCount > 3) {
            penalty += 15
            findings.add(
                Finding("Excessive Subdomains ($dotCount dots)", "Deep subdomain nesting is frequently used to impersonate established brands.", Severity.MEDIUM)
            )
        }

        // 5. Excessive Hyphens in Domain
        val hyphenCount = host.count { it == '-' }
        if (hyphenCount >= 2) {
            penalty += 15
            findings.add(
                Finding("Hyphenated Domain ($hyphenCount hyphens)", "Multiple hyphens are commonly used to craft lookalike brand domains (e.g. secure-login-bank).", Severity.MEDIUM)
            )
        }

        // 6. Suspicious TLD
        val parts = host.split(".")
        val tld = if (parts.size > 1) parts.last().lowercase() else ""
        if (SUSPICIOUS_TLDS.contains(tld)) {
            penalty += 20
            findings.add(
                Finding("High-Risk TLD (.$tld)", "Top-level domain '.$tld' has high correlation with disposable phishing/spam hosting.", Severity.HIGH)
            )
        }

        // 7. Brand / Phishing Keywords in subdomain or path
        val fullPathLower = (host + path + query).lowercase()
        val matchedKeywords = PHISHING_KEYWORDS.filter { fullPathLower.contains(it) }
        if (matchedKeywords.isNotEmpty()) {
            val kwCount = matchedKeywords.size
            penalty += (kwCount * 8).coerceAtMost(25)
            findings.add(
                Finding("Sensitive Phishing Keywords", "Found credential harvesting keywords: ${matchedKeywords.joinToString(", ")}", Severity.HIGH)
            )
        }

        // 8. Homograph / Punycode check
        if (host.startsWith("xn--") || host.any { it.code > 127 }) {
            penalty += 35
            findings.add(
                Finding("IDN Homograph / Unicode Character", "Contains Punycode or non-ASCII characters, used in homoglyph attack impersonation.", Severity.CRITICAL)
            )
        }

        // 9. Host Entropy Calculation
        val entropy = calculateShannonEntropy(host)
        if (entropy > 3.6 && host.length > 10) {
            penalty += 15
            findings.add(
                Finding("High Domain Entropy (${(entropy * 100).toInt() / 100.0})", "Randomized character sequences suggest Domain Generation Algorithm (DGA) or disposable host.", Severity.MEDIUM)
            )
        }

        // 10. URL Length Check
        if (url.length > 75) {
            penalty += 10
            findings.add(
                Finding("Abnormally Long URL (${url.length} chars)", "Excessive length can conceal malicious payloads or phishing parameters.", Severity.LOW)
            )
        }

        val threatScore = penalty.coerceIn(0, 100)
        val verdict = when {
            threatScore < 20 -> "SAFE"
            threatScore < 45 -> "LOW_RISK"
            threatScore < 70 -> "SUSPICIOUS"
            else -> "DANGEROUS_PHISHING"
        }

        val recommendation = when (verdict) {
            "SAFE" -> "The URL passes standard safety heuristics. Ensure the SSL certificate matches when opened."
            "LOW_RISK" -> "Minor flags detected. Verify the sender and confirm the legitimate official domain before entering credentials."
            "SUSPICIOUS" -> "Warning: Multiple phishing indicators observed. Do NOT enter passwords, OTPs, or financial information."
            else -> "HIGH ALERT: Strong phishing / scam signature detected. Do not click or submit any sensitive information."
        }

        return ScanReport(
            originalUrl = rawUrl,
            domain = host.ifEmpty { "N/A" },
            isHttps = isHttps,
            threatScore = threatScore,
            verdict = verdict,
            findings = findings,
            entropy = (entropy * 100).toInt() / 100.0,
            recommendation = recommendation
        )
    }

    private fun calculateShannonEntropy(str: String): Double {
        if (str.isEmpty()) return 0.0
        val map = mutableMapOf<Char, Int>()
        for (c in str) {
            map[c] = (map[c] ?: 0) + 1
        }
        var entropy = 0.0
        val len = str.length.toDouble()
        for ((_, count) in map) {
            val p = count / len
            entropy -= p * log2(p)
        }
        return entropy
    }
}
