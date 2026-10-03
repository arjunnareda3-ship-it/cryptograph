package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.SecurityLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SecurityLogDao {
    @Query("SELECT * FROM security_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<SecurityLogEntity>>

    @Query("SELECT * FROM security_logs WHERE category = :category ORDER BY timestamp DESC")
    fun getLogsByCategory(category: String): Flow<List<SecurityLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: SecurityLogEntity): Long

    @Query("DELETE FROM security_logs WHERE id = :id")
    suspend fun deleteLogById(id: Long)

    @Query("DELETE FROM security_logs")
    suspend fun clearAllLogs()
}
