package com.example.data.repository

import com.example.data.local.SecurityLogDao
import com.example.data.model.SecurityLogEntity
import kotlinx.coroutines.flow.Flow

class SecurityRepository(private val dao: SecurityLogDao) {
    val allLogs: Flow<List<SecurityLogEntity>> = dao.getAllLogs()

    fun getLogsByCategory(category: String): Flow<List<SecurityLogEntity>> = dao.getLogsByCategory(category)

    suspend fun insertLog(category: String, title: String, details: String, riskLevel: String = "INFO"): Long {
        val log = SecurityLogEntity(
            category = category,
            title = title,
            details = details,
            riskLevel = riskLevel
        )
        return dao.insertLog(log)
    }

    suspend fun deleteLog(id: Long) = dao.deleteLogById(id)

    suspend fun clearLogs() = dao.clearAllLogs()
}
