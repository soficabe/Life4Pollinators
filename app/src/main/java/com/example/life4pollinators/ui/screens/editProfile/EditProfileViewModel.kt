package com.example.life4pollinators.ui.screens.editProfile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.life4pollinators.data.database.entities.User
import com.example.life4pollinators.data.repositories.AuthRepository
import com.example.life4pollinators.data.repositories.EditProfileResult
import com.example.life4pollinators.data.repositories.ImageRepository
import com.example.life4pollinators.data.repositories.UpdateUserProfileResult
import com.example.life4pollinators.data.repositories.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.life4pollinators.R

data class EditProfileState(
    val user: User? = null,
    val username: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val image: String? = null,
    val newProfileImageUri: Uri? = null,
    val isUploadingImage: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessageRes: Int? = null,
    val errorMessageArg: String? = null,
    val isSuccess: Boolean = false,
    val emailConfirmationSentMessage: String? = null,
    val emailConfirmationSentArg: String? = null
) {
    val hasChanges: Boolean
        get() = user?.let {
            username != it.username ||
                    firstName != it.firstName ||
                    lastName != it.lastName ||
                    email != it.email ||
                    newProfileImageUri != null
        } ?: false
}

interface EditProfileActions {
    fun setUsername(username: String)
    fun setFirstName(firstName: String)
    fun setLastName(lastName: String)
    fun setEmail(email: String)
    fun resetUsername()
    fun resetFirstName()
    fun resetLastName()
    fun resetEmail()
    fun saveChanges(context: Context)
    fun clearMessages()
    suspend fun loadUserData()
    fun setErrorRes(resId: Int, arg: String? = null)
    fun onProfileImageSelected(uri: Uri, context: Context)
}

class EditProfileViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val imageRepository: ImageRepository
) : ViewModel() {

    private val _state = MutableStateFlow(EditProfileState(isLoading = true))
    val state = _state.asStateFlow()

    val actions = object : EditProfileActions {
        override fun setUsername(username: String) {
            _state.update { it.copy(username = username, errorMessageRes = null, errorMessageArg = null) }
        }
        override fun setFirstName(firstName: String) {
            _state.update { it.copy(firstName = firstName, errorMessageRes = null, errorMessageArg = null) }
        }
        override fun setLastName(lastName: String) {
            _state.update { it.copy(lastName = lastName, errorMessageRes = null, errorMessageArg = null) }
        }
        override fun setEmail(email: String) {
            _state.update { it.copy(email = email, errorMessageRes = null, errorMessageArg = null) }
        }

        override fun resetUsername() {
            _state.update { state ->
                state.user?.let { user ->
                    state.copy(username = user.username)
                } ?: state
            }
        }

        override fun resetFirstName() {
            _state.update { state ->
                state.user?.let { user ->
                    state.copy(firstName = user.firstName)
                } ?: state
            }
        }

        override fun resetLastName() {
            _state.update { state ->
                state.user?.let { user ->
                    state.copy(lastName = user.lastName)
                } ?: state
            }
        }

        override fun resetEmail() {
            _state.update { state ->
                state.user?.let { user ->
                    state.copy(email = user.email)
                } ?: state
            }
        }

        override fun clearMessages() {
            _state.update { it.copy(errorMessageRes = null, errorMessageArg = null, isSuccess = false, emailConfirmationSentMessage = null, emailConfirmationSentArg = null) }
        }

        override fun saveChanges(context: Context) {
            saveUserProfile(context)
        }

        override suspend fun loadUserData() {
            loadUserDataInternal()
        }

        override fun setErrorRes(resId: Int, arg: String?) {
            _state.update { it.copy(errorMessageRes = resId, errorMessageArg = arg) }
        }

        override fun onProfileImageSelected(uri: Uri, context: Context) {
            // Salva solo l'uri della foto scelta/scattata (già salvata in galleria)
            _state.update { it.copy(newProfileImageUri = uri, errorMessageRes = null, errorMessageArg = null) }
        }
    }

    init {
        viewModelScope.launch { loadUserDataInternal() }
    }

    private suspend fun loadUserDataInternal() {
        _state.update { it.copy(isLoading = true, errorMessageRes = null, errorMessageArg = null) }
        try {
            val currentUser = authRepository.getAuthUser()
            val userProfile = userRepository.getUser(currentUser.id)
            userProfile?.let {
                _state.update { state ->
                    state.copy(
                        user = it,
                        username = it.username,
                        firstName = it.firstName,
                        lastName = it.lastName,
                        email = it.email,
                        image = it.image,
                        isLoading = false,
                        errorMessageRes = null,
                        errorMessageArg = null,
                        newProfileImageUri = null // Reset eventuale immagine temporanea
                    )
                }
            } ?: run {
                _state.update { it.copy(isLoading = false, errorMessageRes = R.string.user_not_found) }
            }
        } catch (e: Exception) {
            _state.update {
                it.copy(
                    isLoading = false,
                    errorMessageRes = R.string.error_loading_profile
                )
            }
        }
    }

    private fun saveUserProfile(context: Context) {
        viewModelScope.launch {
            val currentState = _state.value
            val originalUser = currentState.user

            if (currentState.username.isBlank() ||
                currentState.firstName.isBlank() ||
                currentState.lastName.isBlank() ||
                currentState.email.isBlank()
            ) {
                _state.update { it.copy(errorMessageRes = R.string.all_fields_required) }
                return@launch
            }

            if (!currentState.email.contains("@") || !currentState.email.contains(".")) {
                _state.update { it.copy(errorMessageRes = R.string.invalid_email_format) }
                return@launch
            }

            if (!currentState.hasChanges) {
                _state.update { it.copy(errorMessageRes = R.string.no_changes_to_save) }
                return@launch
            }

            _state.update { it.copy(isSaving = true, errorMessageRes = null, errorMessageArg = null, isSuccess = false) }

            var emailJustChanged = false

            try {
                val authUser = authRepository.getAuthUser()
                val userId = authUser.id

                if (originalUser != null && currentState.email != originalUser.email) {
                    when (authRepository.updateUserEmail(currentState.email)) {
                        is EditProfileResult.Success -> {
                            emailJustChanged = true
                        }
                        is EditProfileResult.Error.EmailAlreadyExists -> {
                            _state.update {
                                it.copy(
                                    isSaving = false,
                                    errorMessageRes = R.string.email_already_exists
                                )
                            }
                            return@launch
                        }
                        is EditProfileResult.Error.InvalidEmail -> {
                            _state.update {
                                it.copy(
                                    isSaving = false,
                                    errorMessageRes = R.string.invalid_email_format
                                )
                            }
                            return@launch
                        }
                        is EditProfileResult.Error.NetworkError -> {
                            _state.update {
                                it.copy(
                                    isSaving = false,
                                    errorMessageRes = R.string.network_error
                                )
                            }
                            return@launch
                        }
                        is EditProfileResult.Error.UnknownError -> {
                            _state.update {
                                it.copy(
                                    isSaving = false,
                                    errorMessageRes = R.string.unknown_error
                                )
                            }
                            return@launch
                        }
                    }
                }

                // upload immagine SOLO ora, se nuova selezionata
                var imageUrl: String? = null
                val newImageUri = currentState.newProfileImageUri
                if (newImageUri != null) {
                    imageUrl = imageRepository.uploadProfileImage(userId, newImageUri, context)
                    if (imageUrl == null) {
                        _state.update { it.copy(isSaving = false, errorMessageRes = R.string.image_upload_error) }
                        return@launch
                    }
                }

                val updatedImage = imageUrl ?: currentState.image

                val userUpdateResult = userRepository.updateUserProfile(
                    userId = userId,
                    username = currentState.username,
                    firstName = currentState.firstName,
                    lastName = currentState.lastName,
                    image = updatedImage
                )

                when (userUpdateResult) {
                    is UpdateUserProfileResult.Success -> {
                        val updatedUser = originalUser!!.copy(
                            username = currentState.username,
                            firstName = currentState.firstName,
                            lastName = currentState.lastName,
                            email = currentState.email,
                            image = updatedImage
                        )
                        _state.update {
                            it.copy(
                                isSaving = false,
                                isSuccess = true,
                                user = updatedUser,
                                image = updatedImage
                            )
                        }
                    }
                    is UpdateUserProfileResult.Error.UsernameAlreadyExists -> {
                        _state.update {
                            it.copy(isSaving = false, errorMessageRes = R.string.username_already_exists)
                        }
                        return@launch
                    }
                    is UpdateUserProfileResult.Error.NetworkError -> {
                        _state.update {
                            it.copy(isSaving = false, errorMessageRes = R.string.network_error)
                        }
                        return@launch
                    }
                    is UpdateUserProfileResult.Error.UnknownError -> {
                        _state.update {
                            it.copy(isSaving = false, errorMessageRes = R.string.unknown_error)
                        }
                        return@launch
                    }
                }

                if (emailJustChanged) {
                    _state.update {
                        it.copy(
                            isSaving = false,
                            isSuccess = true,
                            emailConfirmationSentMessage = R.string.confirmation_email_sent.toString(),
                            emailConfirmationSentArg = currentState.email
                        )
                    }
                }

            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isSaving = false,
                        errorMessageRes = R.string.unexpected_error,
                        errorMessageArg = e.message
                    )
                }
            }
        }
    }
}