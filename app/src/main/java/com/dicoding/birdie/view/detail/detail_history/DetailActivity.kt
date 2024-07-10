package com.dicoding.birdie.view.detail.detail_history

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.dicoding.birdie.R
import com.dicoding.birdie.databinding.ActivityDetailBinding


class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        show()
        setupListeners()

    }


    private fun setupListeners() {
        binding.apply {
            tvLabel.setOnClickListener { searchOnGoogle() }

        }
    }

    private fun searchOnGoogle() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=${binding.tvLabel.text}"))
        startActivity(intent)
    }

    private fun show(){
        // Get data from intent
        val imageUri = intent.getStringExtra("imageUri")
        val label = intent.getStringExtra("label")
        val score = intent.getFloatExtra("score", 0f)

        // Load data into views
        binding.resultText.text = getString(R.string.hasil_analisis)
        binding.tvLabel.text = getString(R.string.analysis_type, label)
        binding.ada.text = getString(R.string.moreInfo)
       
        binding.imageView.load(imageUri)
    }
}