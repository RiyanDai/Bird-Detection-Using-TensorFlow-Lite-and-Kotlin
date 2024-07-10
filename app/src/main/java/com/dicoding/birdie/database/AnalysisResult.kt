package com.dicoding.birdie.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "analysis_results")
data class AnalysisResult(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val label: String,
    val imageUri: String
)