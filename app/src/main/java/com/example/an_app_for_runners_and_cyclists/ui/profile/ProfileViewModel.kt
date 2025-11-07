package com.example.an_app_for_runners_and_cyclists.ui.profile

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.an_app_for_runners_and_cyclists.RunnersExchangeApplication
import com.example.an_app_for_runners_and_cyclists.data.model.User
import com.example.an_app_for_runners_and_cyclists.data.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class ProfileViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _isEditing = MutableStateFlow(false)
    val isEditing: StateFlow<Boolean> = _isEditing.asStateFlow()

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState.asStateFlow()

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            userRepository.getCurrentUser()?.let { currentUser ->
                _user.value = currentUser
            }
        }
    }

    fun startEditing() {
        _isEditing.value = true
    }

    fun saveUserData(
        name: String,
        email: String,
        height: Int?,
        weight: Int?,
        runningReason: String,
        address: String
    ) {
        val currentUser = _user.value ?: return

        if (name.isEmpty() || email.isEmpty()) {
            _saveState.value = SaveState.Error("Name and email are required")
            return
        }

        if (!isValidEmail(email)) {
            _saveState.value = SaveState.Error("Please enter a valid email")
            return
        }

        _saveState.value = SaveState.Loading

        viewModelScope.launch {
            try {
                val updatedUser = currentUser.copy(
                    name = name,
                    email = email,
                    height = height,
                    weight = weight,
                    runningReason = runningReason,
                    address = address
                )

                userRepository.updateUser(updatedUser)
                _user.value = updatedUser
                _isEditing.value = false
                _saveState.value = SaveState.Success
            } catch (e: Exception) {
                _saveState.value = SaveState.Error("Failed to save profile: ${e.message}")
            }
        }
    }

    fun cancelEditing() {
        _isEditing.value = false
        _saveState.value = SaveState.Idle
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun updateProfileImage(imageUri: String) {
        viewModelScope.launch {
            val currentUser = _user.value ?: return@launch

            // Всегда копируем изображение в постоянное хранилище
            val permanentUri = copyImageToInternalStorage(imageUri)

            val updatedUser = currentUser.copy(profileImage = permanentUri)
            userRepository.updateUser(updatedUser)
            _user.value = updatedUser

            Timber.d("Profile image updated to: $permanentUri")
        }
    }

    private suspend fun copyImageToInternalStorage(imageUri: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val context = RunnersExchangeApplication.getAppContext()
                    ?: return@withContext imageUri

                // Создаем директорию для профильных фото
                val internalDir = File(context.filesDir, "profile_images")
                if (!internalDir.exists()) {
                    internalDir.mkdirs()
                }

                // Создаем постоянный файл
                val permanentFile = File(internalDir, "profile_${System.currentTimeMillis()}.jpg")

                val uri = Uri.parse(imageUri)

                when {
                    // Если это content URI (из галереи)
                    uri.scheme == "content" -> {
                        copyContentUriToFile(context, uri, permanentFile)
                    }
                    // Если это file URI (из камеры)
                    uri.scheme == "file" || imageUri.startsWith("/") -> {
                        val sourceFile = if (imageUri.startsWith("/")) {
                            File(imageUri)
                        } else {
                            File(uri.path ?: return@withContext imageUri)
                        }
                        if (sourceFile.exists()) {
                            sourceFile.copyTo(permanentFile, overwrite = true)
                        } else {
                            return@withContext imageUri
                        }
                    }
                    else -> {
                        return@withContext imageUri
                    }
                }

                Timber.d("Image copied from $imageUri to ${permanentFile.absolutePath}")
                permanentFile.absolutePath
            } catch (e: Exception) {
                Timber.e(e, "Failed to copy image to internal storage")
                imageUri // В случае ошибки возвращаем оригинальный URI
            }
        }
    }

    private fun copyContentUriToFile(context: Context, contentUri: Uri, destinationFile: File) {
        val contentResolver: ContentResolver = context.contentResolver
        var inputStream: InputStream? = null
        var outputStream: FileOutputStream? = null

        try {
            inputStream = contentResolver.openInputStream(contentUri)
            outputStream = FileOutputStream(destinationFile)

            inputStream?.copyTo(outputStream)
        } catch (e: Exception) {
            throw e
        } finally {
            inputStream?.close()
            outputStream?.close()
        }
    }

    sealed class SaveState {
        object Idle : SaveState()
        object Loading : SaveState()
        object Success : SaveState()
        data class Error(val message: String) : SaveState()
    }
}