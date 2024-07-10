package com.dicoding.birdie.view.fragment.scan

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import coil.load
import com.dicoding.birdie.R
import com.dicoding.birdie.database.AnalysisResult
import com.dicoding.birdie.database.AppDatabase
import com.dicoding.birdie.databinding.ActivityResultBinding
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.NumberFormat

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding
    private lateinit var birdDescriptions: Map<String, String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadBirdDescriptions()
        setupListeners()
        resultview()
    }

    private fun loadBirdDescriptions() {
        val inputStream = resources.openRawResource(R.raw.bird_descriptions)
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        val jsonString = bufferedReader.use { it.readText() }
        val jsonObject = JSONObject(jsonString)
        birdDescriptions = jsonObject.keys().asSequence().associateWith { jsonObject.getString(it) }
    }

    private fun setupListeners() {
        binding.apply {
            tvLabel.setOnClickListener { searchOnGoogle() }
            btnSaveAnalysis.setOnClickListener { saveCurrentAnalysis() }
        }
    }

    private fun searchOnGoogle() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=${binding.tvLabel.text}"))
        startActivity(intent)
    }

    private fun resultview() {
        val label = intent.getStringExtra("label")
        val confidence = intent.getFloatExtra("confidence", 0.0f)
        val imageUri = intent.getStringExtra("imageUri")

        binding.resultText.text = getString(R.string.hasil_analisis)
        binding.tvLabel.text = getString(R.string.analysis_type, label)
        binding.ada.text = getString(R.string.moreInfo)

        binding.imageView.load(imageUri)

        // Display the bird description if available
        val description = birdDescriptions[label] ?: "Deskripsi tidak tersedia untuk burung ini."
        binding.additionalInfoTextView.text = description
    }

    private fun saveCurrentAnalysis() {
        val label = intent.getStringExtra("label") ?: return

        val imageUri = intent.getStringExtra("imageUri") ?: return

        saveAnalysisResult(label, imageUri)
    }

    private fun saveAnalysisResult(label: String, imageUri: String) {
        val analysisResult = AnalysisResult(label = label, imageUri = imageUri)
        val db = AppDatabase.getDatabase(this)
        val dao = db.analysisResultDao()

        lifecycleScope.launch {
            dao.insert(analysisResult)
            runOnUiThread {
                // Display a toast message to inform the user
                Toast.makeText(this@ResultActivity, "Analysis saved successfully", Toast.LENGTH_SHORT).show()
            }
        }
    }
}