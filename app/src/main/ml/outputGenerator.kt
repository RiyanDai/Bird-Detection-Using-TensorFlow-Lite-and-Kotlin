private fun outputGenerator(bitmap: Bitmap){
    // Declaring TensorFlow Lite model variable
    val birdsModel = BirdsModel.newInstance(this)

    // Converting bitmap into TensorFlow image
    val newBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
    val tfimage = TensorImage.fromBitmap(newBitmap)

    // Process the image using the trained model and sort it in descending order
    val outputs = birdsModel.process(tfimage)
        .probabilityAsCategoryList.apply {
            sortByDescending { it.score }
        }

    // Getting the result with the highest probability
    val highProbabilityOutput = outputs[0]

    // Save the image to a temporary file
    val imageUri = saveImageToCache(bitmap)

    // Start the ResultActivity with the image, label, and confidence score
    val intent = Intent(this, ResultActivity::class.java).apply {
        putExtra("label", highProbabilityOutput.label)
        putExtra("confidence", highProbabilityOutput.score)
        putExtra("imageUri", imageUri.toString())
    }
    startActivity(intent)
}

private fun saveImageToCache(bitmap: Bitmap): Uri {
    val filename = "bird_image_${System.currentTimeMillis()}.png"
    val cacheDir = externalCacheDir ?: cacheDir
    val file = File(cacheDir, filename)
    file.outputStream().use {
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
    }
    return file.toUri()
}
