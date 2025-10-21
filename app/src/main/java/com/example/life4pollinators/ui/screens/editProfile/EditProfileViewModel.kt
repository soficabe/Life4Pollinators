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
    val emailConfirmationSentArg: String? = null,
    val usernameError: Int? = null,
    val firstNameError: Int? = null,
    val lastNameError: Int? = null,
    val emailError: Int? = null
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
            _state.update { it.copy(username = username, usernameError = null, errorMessageRes = null, errorMessageArg = null) }
        }
        override fun setFirstName(firstName: String) {
            _state.update { it.copy(firstName = firstName, firstNameError = null, errorMessageRes = null, errorMessageArg = null) }
        }
        override fun setLastName(lastName: String) {
            _state.update { it.copy(lastName = lastName, lastNameError = null, errorMessageRes = null, errorMessageArg = null) }
        }
        override fun setEmail(email: String) {
            _state.update { it.copy(email = email, emailError = null, errorMessageRes = null, errorMessageArg = null) }
        }

        override fun resetUsername() {
            _state.update { state ->
                state.user?.let { user ->
                    state.copy(username = user.username, usernameError = null)
                } ?: state
            }
        }

        override fun resetFirstName() {
            _state.update { state ->
                state.user?.let { user ->
                    state.copy(firstName = user.firstName, firstNameError = null)
                } ?: state
            }
        }

        override fun resetLastName() {
            _state.update { state ->
                state.user?.let { user ->
                    state.copy(lastName = user.lastName, lastNameError = null)
                } ?: state
            }
        }

        override fun resetEmail() {
            _state.update { state ->
                state.user?.let { user ->
                    state.copy(email = user.email, emailError = null)
                } ?: state
            }
        }

        override fun clearMessages() {
            _state.update { it.copy(
                errorMessageRes = null, errorMessageArg = null, isSuccess = false,
                emailConfirmationSentMessage = null, emailConfirmationSentArg = null,
                usernameError = null, firstNameError = null, lastNameError = null, emailError = null
            ) }
        }

        override fun saveChanges(context: Context) {
            val currentState = _state.value
            _state.update {
                it.copy(
                    errorMessageRes = null,
                    errorMessageArg = null,
                    usernameError = null,
                    firstNameError = null,
                    lastNameError = null,
                    emailError = null
                )
            }

            var hasError = false
            if (currentState.username.isBlank()) {
                _state.update { it.copy(usernameError = R.string.requiredFields_error) }
                hasError = true
            }
            if (currentState.firstName.isBlank()) {
                _state.update { it.copy(firstNameError = R.string.requiredFields_error) }
                hasError = true
            }
            if (currentState.lastName.isBlank()) {
                _state.update { it.copy(lastNameError = R.string.requiredFields_error) }
                hasError = true
            }
            if (currentState.email.isBlank()) {
                _state.update { it.copy(emailError = R.string.requiredFields_error) }
                hasError = true
            } else if (!currentState.email.contains("@") || !currentState.email.contains(".")) {
                _state.update { it.copy(emailError = R.string.invalid_email_format) }
                hasError = true
            }
            if (!currentState.hasChanges) {
                _state.update { it.copy(errorMessageRes = R.string.no_changes_to_save) }
                hasError = true
            }
            if (hasError) return

            saveUserProfile(context)
        }

        override suspend fun loadUserData() {
            loadUserDataInternal()
        }

        override fun setErrorRes(resId: Int, arg: String?) {
            _state.update { it.copy(errorMessageRes = resId, errorMessageArg = arg) }
        }

        override fun onProfileImageSelected(uri: Uri, context: Context) {
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
                        newProfileImageUri = null
                    )
                }
            } ?: run {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessageRes = R.string.network_error_connection
                    )
                }
            }
        } catch (e: Exception) {
            _state.update {
                it.copy(
                    isLoading = false,
                    errorMessageRes = when {
                        e.message?.contains("network", ignoreCase = true) == true ||
                                e.message?.contains("unable to resolve host", ignoreCase = true) == true ||
                                e.message?.contains("failed to connect", ignoreCase = true) == true -> {
                            R.string.network_error_connection
                        }
                        else -> R.string.error_loading_profile
                    }
                )
            }
        }
    }

    /**
     * Salva tutti i dati modificati e aggiorna il profilo lato backend.
     * Gestisce anche la validazione dei campi e la gestione degli errori.
     * l'immagine viene caricata su Supabase SOLO dopo che la validazione e l'update
     * dei dati testuali (username, firstName, lastName) hanno avuto successo.
     */
    private fun saveUserProfile(context: Context) {
        viewModelScope.launch {
            val currentState = _state.value
            val originalUser = currentState.user

            _state.update { it.copy(isSaving = true, errorMessageRes = null, errorMessageArg = null, isSuccess = false) }

            var emailJustChanged = false

            try {
                val authUser = authRepository.getAuthUser()
                val userId = authUser.id

                // Cambio email (se modificata)
                if (originalUser != null && currentState.email != originalUser.email) {
                    when (authRepository.updateUserEmail(currentState.email)) {
                        is EditProfileResult.Success -> {
                            emailJustChanged = true
                        }
                        is EditProfileResult.Error.EmailAlreadyExists -> {
                            _state.update {
                                it.copy(
                                    isSaving = false,
                                    emailError = R.string.email_already_exists
                                )
                            }
                            return@launch
                        }
                        is EditProfileResult.Error.InvalidEmail -> {
                            _state.update {
                                it.copy(
                                    isSaving = false,
                                    emailError = R.string.invalid_email_format
                                )
                            }
                            return@launch
                        }
                        is EditProfileResult.Error.NetworkError -> {
                            _state.update {
                                it.copy(
                                    isSaving = false,
                                    errorMessageRes = R.string.network_error_connection  // ✅ Messaggio chiaro
                                )
                            }
                            return@launch
                        }
                        is EditProfileResult.Error.UnknownError -> {
                            _state.update {
                                it.copy(
                                    isSaving = false,
                                    errorMessageRes = R.string.network_error_connection  // ✅ Messaggio chiaro
                                )
                            }
                            return@launch
                        }
                    }
                }

                // Aggiorna PRIMA i dati testuali (username, firstName, lastName), senza immagine!
                val userUpdateResult = userRepository.updateUserProfile(
                    userId = userId,
                    username = currentState.username,
                    firstName = currentState.firstName,
                    lastName = currentState.lastName,
                    image = null
                )

                when (userUpdateResult) {
                    is UpdateUserProfileResult.Success -> {
                        // SOLO se i dati sono ok, carica la foto se serve, (query param per invalidare la cache)
                        var imageUrl: String? = currentState.image
                        val newImageUri = currentState.newProfileImageUri
                        if (newImageUri != null) {
                            val uploadedUrl = imageRepository.uploadProfileImage(userId, newImageUri, context)
                            if (uploadedUrl != null) {
                                // Aggiorna il campo image nel backend
                                val imageUpdateResult = userRepository.updateUserProfile(
                                    userId = userId,
                                    username = currentState.username,
                                    firstName = currentState.firstName,
                                    lastName = currentState.lastName,
                                    image = "$uploadedUrl?t=${System.currentTimeMillis()}"
                                )
                                // Forza cache busting aggiungendo timestamp all'URL
                                if (imageUpdateResult is UpdateUserProfileResult.Success) {
                                    imageUrl = "$uploadedUrl?t=${System.currentTimeMillis()}"
                                }
                            } else {
                                _state.update { it.copy(isSaving = false, errorMessageRes = R.string.image_upload_error) }
                                return@launch
                            }
                        }

                        val updatedUser = originalUser!!.copy(
                            username = currentState.username,
                            firstName = currentState.firstName,
                            lastName = currentState.lastName,
                            email = currentState.email,
                            image = imageUrl
                        )
                        _state.update {
                            it.copy(
                                isSaving = false,
                                isSuccess = true,
                                user = updatedUser,
                                image = imageUrl,
                                newProfileImageUri = null
                            )
                        }
                    }
                    is UpdateUserProfileResult.Error.UsernameAlreadyExists -> {
                        _state.update {
                            it.copy(isSaving = false, usernameError = R.string.username_already_exists)
                        }
                        return@launch
                    }
                    is UpdateUserProfileResult.Error.NetworkError -> {
                        _state.update {
                            it.copy(isSaving = false, errorMessageRes = R.string.network_error_connection)  // ✅ Messaggio chiaro
                        }
                        return@launch
                    }
                    is UpdateUserProfileResult.Error.UnknownError -> {
                        _state.update {
                            it.copy(isSaving = false, errorMessageRes = R.string.network_error_connection)  // ✅ Messaggio chiaro
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
                // Gestisci errore generico
                _state.update {
                    it.copy(
                        isSaving = false,
                        errorMessageRes = when {
                            e.message?.contains("network", ignoreCase = true) == true ||
                                    e.message?.contains("unable to resolve host", ignoreCase = true) == true ||
                                    e.message?.contains("failed to connect", ignoreCase = true) == true -> {
                                R.string.network_error_connection
                            }
                            else -> R.string.unknown_error
                        }
                    )
                }
            }
        }
    }
}