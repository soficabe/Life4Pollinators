package com.example.life4pollinators.ui.screens.editProfile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.life4pollinators.data.database.entities.User
import com.example.life4pollinators.data.repositories.AuthRepository
import com.example.life4pollinators.data.repositories.EditProfileResult
import com.example.life4pollinators.data.repositories.UpdateUserProfileResult
import com.example.life4pollinators.data.repositories.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditProfileState(
    val user: User? = null,
    val username: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false,
    val emailConfirmationSentMessage: String? = null // <--- nuovo campo
) {
    val hasChanges: Boolean
        get() = user?.let {
            username != it.username ||
                    firstName != it.firstName ||
                    lastName != it.lastName ||
                    email != it.email
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
    fun saveChanges()
    fun clearMessages()
    suspend fun loadUserData()
}

class EditProfileViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(EditProfileState(isLoading = true))
    val state = _state.asStateFlow()

    val actions = object : EditProfileActions {
        override fun setUsername(username: String) {
            _state.update { it.copy(username = username, errorMessage = null) }
        }
        override fun setFirstName(firstName: String) {
            _state.update { it.copy(firstName = firstName, errorMessage = null) }
        }
        override fun setLastName(lastName: String) {
            _state.update { it.copy(lastName = lastName, errorMessage = null) }
        }
        override fun setEmail(email: String) {
            _state.update { it.copy(email = email, errorMessage = null) }
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
            _state.update { it.copy(errorMessage = null, isSuccess = false, emailConfirmationSentMessage = null) }
        }
        override fun saveChanges() {
            saveUserProfile()
        }
        override suspend fun loadUserData() {
            loadUserDataInternal()
        }
    }

    init {
        viewModelScope.launch { loadUserDataInternal() }
    }

    private suspend fun loadUserDataInternal() {
        _state.update { it.copy(isLoading = true, errorMessage = null) }
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
                        isLoading = false,
                        errorMessage = null
                    )
                }
            } ?: run {
                _state.update { it.copy(isLoading = false, errorMessage = "Utente non trovato") }
            }
        } catch (e: Exception) {
            _state.update {
                it.copy(
                    isLoading = false,
                    errorMessage = "Errore nel caricare il profilo"
                )
            }
        }
    }

    private fun saveUserProfile() {
        viewModelScope.launch {
            val currentState = _state.value
            val originalUser = currentState.user

            // Validazione base
            if (currentState.username.isBlank() ||
                currentState.firstName.isBlank() ||
                currentState.lastName.isBlank() ||
                currentState.email.isBlank()
            ) {
                _state.update { it.copy(errorMessage = "Tutti i campi sono obbligatori") }
                return@launch
            }

            if (!currentState.email.contains("@") || !currentState.email.contains(".")) {
                _state.update { it.copy(errorMessage = "Formato email non valido") }
                return@launch
            }

            if (!currentState.hasChanges) {
                _state.update { it.copy(errorMessage = "Nessuna modifica da salvare") }
                return@launch
            }

            _state.update { it.copy(isSaving = true, errorMessage = null, isSuccess = false) }

            var emailJustChanged = false

            try {
                val authUser = authRepository.getAuthUser()
                val userId = authUser.id

                // Aggiorna email se è cambiata
                if (originalUser != null && currentState.email != originalUser.email) {
                    when (authRepository.updateUserEmail(currentState.email)) {
                        is EditProfileResult.Success -> {
                            emailJustChanged = true
                        }
                        is EditProfileResult.Error.EmailAlreadyExists -> {
                            _state.update {
                                it.copy(
                                    isSaving = false,
                                    errorMessage = "Email già esistente"
                                )
                            }
                            return@launch
                        }
                        is EditProfileResult.Error.InvalidEmail -> {
                            _state.update {
                                it.copy(
                                    isSaving = false,
                                    errorMessage = "Formato email non valido"
                                )
                            }
                            return@launch
                        }
                        is EditProfileResult.Error.NetworkError -> {
                            _state.update {
                                it.copy(
                                    isSaving = false,
                                    errorMessage = "Errore di rete"
                                )
                            }
                            return@launch
                        }
                        is EditProfileResult.Error.UnknownError -> {
                            _state.update {
                                it.copy(
                                    isSaving = false,
                                    errorMessage = "Errore sconosciuto"
                                )
                            }
                            return@launch
                        }
                    }
                }

                // Aggiorna username/firstName/lastName se cambiati
                if (originalUser != null && (
                            currentState.username != originalUser.username ||
                                    currentState.firstName != originalUser.firstName ||
                                    currentState.lastName != originalUser.lastName
                            )
                ) {
                    when (userRepository.updateUserProfile(
                        userId = userId,
                        username = currentState.username,
                        firstName = currentState.firstName,
                        lastName = currentState.lastName
                    )) {
                        is UpdateUserProfileResult.Success -> {
                            val updatedUser = originalUser.copy(
                                username = currentState.username,
                                firstName = currentState.firstName,
                                lastName = currentState.lastName,
                                email = currentState.email
                            )
                            _state.update {
                                it.copy(
                                    isSaving = false,
                                    isSuccess = true,
                                    user = updatedUser
                                )
                            }
                        }
                        is UpdateUserProfileResult.Error.UsernameAlreadyExists -> {
                            _state.update {
                                it.copy(isSaving = false, errorMessage = "Username già esistente")
                            }
                            return@launch
                        }
                        is UpdateUserProfileResult.Error.NetworkError -> {
                            _state.update {
                                it.copy(isSaving = false, errorMessage = "Errore di rete")
                            }
                            return@launch
                        }
                        is UpdateUserProfileResult.Error.UnknownError -> {
                            _state.update {
                                it.copy(isSaving = false, errorMessage = "Errore sconosciuto")
                            }
                            return@launch
                        }
                    }
                } else if (!emailJustChanged) {
                    // Solo email era da aggiornare
                    if (originalUser != null) {
                        val updatedUser = originalUser.copy(email = currentState.email)
                        _state.update {
                            it.copy(
                                isSaving = false,
                                isSuccess = true,
                                user = updatedUser
                            )
                        }
                    }
                }

                // Mostra la notifica solo se email appena cambiata
                if (emailJustChanged) {
                    _state.update {
                        it.copy(
                            isSaving = false,
                            isSuccess = true,
                            emailConfirmationSentMessage = "Una email di conferma è stata inviata a ${currentState.email}. Controlla la nuova casella per confermare il cambio."
                        )
                    }
                }

            } catch (e: Exception) {
                _state.update {
                    it.copy(isSaving = false, errorMessage = "Errore inatteso: ${e.message}")
                }
            }
        }
    }
}