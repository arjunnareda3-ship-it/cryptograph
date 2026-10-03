package com.example.scanner

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object AppAuditHelper {

    data class AppSecurityReport(
        val packageName: String,
        val appName: String,
        val isSystemApp: Boolean,
        val dangerousPermissions: List<String>,
        val riskScore: Int,
        val riskLevel: String // LOW, MEDIUM, HIGH, CRITICAL
    )

    private val DANGEROUS_PERMS = mapOf(
        "android.permission.RECORD_AUDIO" to ("Microphone Eavesdropping" to 25),
        "android.permission.CAMERA" to ("Camera Spy Risk" to 25),
        "android.permission.ACCESS_FINE_LOCATION" to ("Precise GPS Tracking" to 20),
        "android.permission.READ_SMS" to ("SMS / OTP Interception" to 30),
        "android.permission.SEND_SMS" to ("Toll Fraud SMS" to 25),
        "android.permission.READ_CONTACTS" to ("Contact Harvesting" to 20),
        "android.permission.READ_CALL_LOG" to ("Call Metadata Surveillance" to 20),
        "android.permission.SYSTEM_ALERT_WINDOW" to ("Screen Overlay / Tapjacking" to 25),
        "android.permission.READ_PHONE_STATE" to ("Device ID / IMEI Tracking" to 15)
    )

    suspend fun auditInstalledApps(context: Context): List<AppSecurityReport> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val reports = mutableListOf<AppSecurityReport>()

        try {
            val packages = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)
            for (pkg in packages) {
                val appInfo = pkg.applicationInfo ?: continue
                val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0

                val requested = pkg.requestedPermissions ?: emptyArray()
                val foundDangerous = mutableListOf<String>()
                var score = 0

                for (perm in requested) {
                    val entry = DANGEROUS_PERMS[perm]
                    if (entry != null) {
                        foundDangerous.add("${perm.substringAfterLast('.')}: ${entry.first}")
                        score += entry.second
                    }
                }

                // If no dangerous permissions, give a baseline score
                val normalizedScore = score.coerceIn(0, 100)
                val riskLevel = when {
                    normalizedScore >= 65 -> "HIGH"
                    normalizedScore >= 35 -> "MEDIUM"
                    else -> "LOW"
                }

                // Include user-installed apps or system apps with high risk
                if (!isSystem || normalizedScore >= 35) {
                    val appName = try {
                        pm.getApplicationLabel(appInfo).toString()
                    } catch (_: Exception) {
                        pkg.packageName
                    }

                    reports.add(
                        AppSecurityReport(
                            packageName = pkg.packageName,
                            appName = appName,
                            isSystemApp = isSystem,
                            dangerousPermissions = foundDangerous,
                            riskScore = normalizedScore,
                            riskLevel = riskLevel
                        )
                    )
                }
            }
        } catch (_: Exception) {}

        reports.sortedByDescending { it.riskScore }
    }
}
