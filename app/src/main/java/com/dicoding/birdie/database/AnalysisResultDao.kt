package com.dicoding.birdie.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow


@Dao
interface AnalysisResultDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(analysisResult: AnalysisResult)

    @Query("SELECT * FROM analysis_results")
    fun getAll(): Flow<List<AnalysisResult>>

    @Delete
    suspend fun delete(analysisResult: AnalysisResult)
}