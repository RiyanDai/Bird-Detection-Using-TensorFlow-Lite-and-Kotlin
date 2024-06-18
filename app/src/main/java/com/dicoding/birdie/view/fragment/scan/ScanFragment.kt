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
import org.tensorflow.lite.support.image.TensorImage
import java.io.File
import java.io.IOException

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
        _binding =FragmentScanBinding.inflate(inflater, container, false)
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

        imageView.setOnLongClickListener {
            requestPermissionLauncher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            true
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

    private fun generateOutput(bitmap: Bitmap) {
        val birdsModel = BirdsModel.newInstance(requireContext())
        val newBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val tfImage = TensorImage.fromBitmap(newBitmap)

        val outputs = birdsModel.process(tfImage).probabilityAsCategoryList.apply {
            sortByDescending { it.score }
        }

        val highProbabilityOutput = outputs[0]
        val imageUri = saveImageToCache(bitmap)

        val intent = Intent(requireContext(), ResultActivity::class.java).apply {
            putExtra("label", highProbabilityOutput.label)
            putExtra("confidence", highProbabilityOutput.score)
            putExtra("imageUri", imageUri.toString())

        }
        startActivity(intent)
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

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            promptImageDownload()
        } else {
            Toast.makeText(requireContext(), "Please allow permission to download image", Toast.LENGTH_LONG).show()
        }
    }

    private fun promptImageDownload() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext()).setTitle("Download Image?")
            .setMessage("Do you want to download this image to your device?")
            .setPositiveButton("Yes") { _, _ ->
                val bitmap = (imageView.drawable as BitmapDrawable).bitmap
                downloadImage(bitmap)
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun downloadImage(bitmap: Bitmap): Uri? {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "Birds_Images${System.currentTimeMillis() / 1000}")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        }
        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        return uri?.let {
            requireContext().contentResolver.insert(it, contentValues)?.also { uri ->
                requireContext().contentResolver.openOutputStream(uri)?.use { outputStream ->
                    if (bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)) {
                        Toast.makeText(requireContext(), "Image Saved", Toast.LENGTH_LONG).show()
                    } else {
                        throw IOException("Couldn't save the bitmap")
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}