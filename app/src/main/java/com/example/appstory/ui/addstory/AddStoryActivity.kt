package com.example.appstory.ui.addstory

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.appstory.MainActivity
import com.example.appstory.databinding.ActivityAddStoryBinding
import com.example.appstory.ui.AuthViewModel
import com.example.appstory.ui.StoryViewModel
import com.example.appstory.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Locale

@Suppress("DEPRECATION")
@AndroidEntryPoint
class AddStoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddStoryBinding
    private var selectedPhotoUri: Uri? = null
    private val viewModel: StoryViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()
    private var authToken: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("AddStoryActivity", "onCreate called")
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonSelectPhoto.setOnClickListener { openGallery() }
        setupButtonAdd()
        observeAuthState()
        observeAddStory()
    }

    private fun observeAuthState() {
        authViewModel.authState.observe(this) { resource ->
            when (resource) {
                is Resource.Success -> {
                    authToken = resource.data
                }
                is Resource.Error -> {
                    Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading -> {
                    showLoading()
                }
            }
        }
    }

    private fun setupButtonAdd() {
        binding.buttonAdd.setOnClickListener {
            if (!validateInput()) return@setOnClickListener

            authToken?.let { token ->
                uploadStory(token)
            } ?: run {
                Toast.makeText(this, "Token tidak tersedia, silakan login kembali", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadStory(token: String) {
        val description = binding.edAddDescription.text.toString()
        val photoFile = selectedPhotoUri?.let { uriToFile(it) } ?: return
        val reducedFile = reduceFileImage(photoFile)

        val requestImageFile = reducedFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val photoPart = MultipartBody.Part.createFormData(
            "photo",
            reducedFile.name,
            requestImageFile
        )
        val descriptionRequestBody = description.toRequestBody("text/plain".toMediaTypeOrNull())

        viewModel.addStory(photoPart, descriptionRequestBody)
    }

    private fun validateInput(): Boolean {
        val description = binding.edAddDescription.text.toString()
        return when {
            selectedPhotoUri == null -> {
                Toast.makeText(this, "Please select a photo", Toast.LENGTH_SHORT).show()
                false
            }
            description.isBlank() -> {
                Toast.makeText(this, "Please add a description", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }

    private fun observeAddStory() {
        viewModel.addStoryStatus.observe(this) { result ->
            when (result) {
                is Resource.Success -> {
                    hideLoading()
                    Toast.makeText(this, "Story uploaded successfully", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                }
                is Resource.Error -> {
                    hideLoading()
                    Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading -> {
                    showLoading()
                }
            }
        }
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.buttonAdd.isEnabled = false
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
        binding.buttonAdd.isEnabled = true
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_CODE_GALLERY)
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_GALLERY && resultCode == Activity.RESULT_OK) {
            selectedPhotoUri = data?.data
            binding.ivPhotoPreview.setImageURI(selectedPhotoUri)
        }
    }

    companion object {
        private const val REQUEST_CODE_GALLERY = 100
    }

    private fun uriToFile(uri: Uri): File {
        val contentResolver: ContentResolver = applicationContext.contentResolver
        val myFile = createCustomTempFile(applicationContext)

        val inputStream = contentResolver.openInputStream(uri) as InputStream
        val outputStream: OutputStream = FileOutputStream(myFile)
        val buf = ByteArray(1024)
        var len: Int
        while (inputStream.read(buf).also { len = it } > 0) outputStream.write(buf, 0, len)
        outputStream.close()
        inputStream.close()

        return myFile
    }

    private fun createCustomTempFile(context: Context): File {
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(timeStamp, ".jpg", storageDir)
    }

    private val timeStamp: String = SimpleDateFormat(
        "dd-MMM-yyyy",
        Locale.US
    ).format(System.currentTimeMillis())

    private fun reduceFileImage(file: File): File {
        val bitmap = BitmapFactory.decodeFile(file.path)
        var compressQuality = 100
        var streamLength: Int
        do {
            val bmpStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, bmpStream)
            val bmpPicByteArray = bmpStream.toByteArray()
            streamLength = bmpPicByteArray.size
            compressQuality -= 5
        } while (streamLength > 1000000 && compressQuality > 0)

        bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, FileOutputStream(file))
        return file
    }
}