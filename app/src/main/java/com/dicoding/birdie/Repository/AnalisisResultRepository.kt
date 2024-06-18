package com.dicoding.birdie.Repository

import androidx.annotation.WorkerThread
import com.dicoding.birdie.database.AnalysisResult
import com.dicoding.birdie.database.AnalysisResultDao
import kotlinx.coroutines.flow.Flow


class AnalysisResultRepository(private val analysisResultDao: AnalysisResultDao) {

    val allResults: Flow<List<AnalysisResult>> = analysisResultDao.getAll()

    @WorkerThread
    suspend fun delete(analysisResult: AnalysisResult) {
        analysisResultDao.delete(analysisResult)
    }
}