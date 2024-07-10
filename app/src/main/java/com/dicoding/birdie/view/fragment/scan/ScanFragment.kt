package com.dicoding.birdie.view.fragment.scan

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.dicoding.birdie.R
import com.dicoding.birdie.databinding.FragmentScanBinding
import com.dicoding.birdie.ml.BirdsModel
import com.dicoding.birdie.ml.ModelUnquant
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ScanFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ScanFragment : Fragment(R.layout.fragment_scan) {
    private var _binding: FragmentScanBinding? = null
    private val binding get() = _binding!!

    private lateinit var captureImageButton: Button
    private lateinit var loadImageButton: Button
    private lateinit var imageView: ImageView

    private val GALLERY_REQUEST_CODE = 123

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeUIComponents()
        setupListeners()
    }

    private fun initializeUIComponents() {
        imageView = binding.imageView
        captureImageButton = binding.btnCaptureImage
        loadImageButton = binding.btnLoadImage
    }

    private fun setupListeners() {
        captureImageButton.setOnClickListener {
            handleCaptureImage()
        }

        loadImageButton.setOnClickListener {
            handleLoadImage()
        }
    }

    private fun handleCaptureImage() {
        if (isPermissionGranted(android.Manifest.permission.CAMERA)) {
            takePicturePreview.launch(null)
        } else {
            requestPermission.launch(android.Manifest.permission.CAMERA)
        }
    }

    private fun handleLoadImage() {
        if (isPermissionGranted(android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
            openGallery()
        } else {
            requestPermission.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/png", "image/jpg"))
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        onResult.launch(intent)
    }

    private fun isPermissionGranted(permission: String) =
        ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED

    private val requestPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            takePicturePreview.launch(null)
        } else {
            Toast.makeText(requireContext(), "Permission Denied !! Try again", Toast.LENGTH_SHORT).show()
        }
    }

    private val takePicturePreview = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        bitmap?.let {
            imageView.setImageBitmap(it)
            generateOutput(it)
        }
    }

    private val onResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        onActivityResultReceived(GALLERY_REQUEST_CODE, result)
    }

    private fun onActivityResultReceived(requestCode: Int, result: ActivityResult?) {
        if (requestCode == GALLERY_REQUEST_CODE && result?.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                val bitmap = BitmapFactory.decodeStream(requireContext().contentResolver.openInputStream(uri))
                imageView.setImageBitmap(bitmap)
                generateOutput(bitmap)
            } ?: Log.e("TAG", "Error in selecting image")
        }
    }

    private val labels = arrayOf(
        "Leucopsar rothschildi",
        "Pityriasis gymnocephala",
        "Lonchura oryzivora",
        "Clamator coromandus",
        "Columba livia",
        "Chloropsis sonnerati",
        "Copsychus saularis",
        "Corvus Corax",
        "Unknown"
    )

    private val CONFIDENCE_THRESHOLD = 0.5f // Adjust this threshold as needed

    private fun generateOutput(bitmap: Bitmap) {
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true)

        val byteBuffer = ByteBuffer.allocateDirect(1 * 224 * 224 * 3 * 4)  // 1 image, 224x224, 3 channels, 4 bytes per float
        byteBuffer.order(ByteOrder.nativeOrder())

        val intValues = IntArray(224 * 224)
        resizedBitmap.getPixels(intValues, 0, 224, 0, 0, 224, 224)
        var pixel = 0
        for (i in 0 until 224) {
            for (j in 0 until 224) {
                val value = intValues[pixel++]
                byteBuffer.putFloat((value shr 16 and 0xFF) / 255.0f)
                byteBuffer.putFloat((value shr 8 and 0xFF) / 255.0f)
                byteBuffer.putFloat((value and 0xFF) / 255.0f)
            }
        }

        val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.FLOAT32)
        inputFeature0.loadBuffer(byteBuffer)

        val model = ModelUnquant.newInstance(requireContext())
        val outputs = model.process(inputFeature0)
        val outputFeature0 = outputs.outputFeature0AsTensorBuffer

        val (maxIndex, maxConfidence) = getMaxIndexWithConfidence(outputFeature0.floatArray)
        val label = if (maxConfidence >= CONFIDENCE_THRESHOLD) {
            labels[maxIndex]
        } else {
            "Unknown"
        }

        model.close()

        val imageUri = saveImageToCache(bitmap)
        val intent = Intent(requireContext(), ResultActivity::class.java).apply {
            putExtra("label", label)
            putExtra("imageUri", imageUri.toString())
        }
        startActivity(intent)
    }

    private fun getMaxIndexWithConfidence(arr: FloatArray): Pair<Int, Float> {
        var maxIndex = 0
        var max = arr[0]
        for (i in arr.indices) {
            if (arr[i] > max) {
                max = arr[i]
                maxIndex = i
            }
        }
        return Pair(maxIndex, max)
    }

    private fun saveImageToCache(bitmap: Bitmap): Uri {
        val filename = "bird_image_${System.currentTimeMillis()}.png"
        val cacheDir = requireContext().externalCacheDir ?: requireContext().cacheDir
        val file = File(cacheDir, filename)
        file.outputStream().use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        return file.toUri()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}