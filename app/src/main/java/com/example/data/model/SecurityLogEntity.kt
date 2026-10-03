package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "security_logs")
data class SecurityLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: String, // CRYPTO, PHISHING_SCAN, NETWORK_AUDIT, APP_AUDIT, VIVA_NOTE
    val title: String,
    val details: String,
    val riskLevel: String = "INFO", // SAFE, LOW, MEDIUM, HIGH, CRITICAL, INFO
    val timestamp: Long = System.currentTimeMillis()
)
