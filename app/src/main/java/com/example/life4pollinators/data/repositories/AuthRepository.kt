package com.example.life4pollinators.data.repositories

import android.util.Log
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

/**
 * Risultati possibili della registrazione utente
 */
sealed interface SignUpResult {
    data object Loading : SignUpResult
    data object Success : SignUpResult

    sealed interface Error : SignUpResult {
        data object UserAlreadyExists : Error
        data object UsernameAlreadyExists : Error
        data object EmailAlreadyExists : Error
        data object WeakPassword : Error
        data object InvalidEmail : Error
        data object PasswordMismatch : Error
        data object RequiredFields : Error
        data object NetworkError : Error
        data class UnknownError(val exception: Throwable) : Error
    }
}

/**
 * Risultati possibili dell'accesso all'account dell'utente
 */
sealed interface SignInResult {
    data object Loading : SignInResult
    data object Success : SignInResult

    sealed interface Error : SignInResult {
        data object InvalidCredentials : Error
        data object InvalidEmail : Error
        data object RequiredFields : Error
        data object NetworkError : Error
        data class UnknownError(val exception: Throwable) : Error
    }
}

/**
 * Risultati possibili del cambio password
 */
sealed interface ChangePasswordResult {
    data object Success : ChangePasswordResult

    sealed interface Error : ChangePasswordResult {
        data object RequiredFields : Error
        data object WeakPassword : Error
        data object PasswordMismatch : Error
        data object NetworkError : Error
        data class UnknownError(val exception: Throwable) : Error
    }
}

/**
 * Risultati possibili della modifica profilo
 */
sealed interface EditProfileResult {
    data object Success : EditProfileResult

    sealed interface Error : EditProfileResult {
        data object InvalidEmail : Error
        data object EmailAlreadyExists : Error
        data object NetworkError : Error
        data class UnknownError(val exception: Throwable) : Error
    }
}

/**
 * Repository per gestire autenticazione e logica utente:
 * sign up, sign in, sign out, cambio password e modifica email, usando Supabase Auth.
 */
class AuthRepository(
    private val supabase: SupabaseClient,
    private val auth: Auth
) {
    // Utente attualmente autenticato
    val user: UserInfo?
        get() = (auth.sessionStatus.value as? SessionStatus.Authenticated)?.session?.user

    // Stato delle sessione corrente
    val sessionStatus: StateFlow<SessionStatus> = auth.sessionStatus

    /**
     * Recupera le informazioni dell'utente dalla sessione corrente
     *
     * @return UserInfo contenente le informazioni utente
     */
    suspend fun getAuthUser(): UserInfo {
        return auth.retrieveUserForCurrentSession(true)
    }

    /**
     * Registra un nuovo utente con email/password.
     *
     * @return SignUpResult con l'esito della registrazione
     */
    suspend fun signUp(
        username: String,
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        confirmPassword: String
    ): SignUpResult {
        return try {
            // Validazione locale
            val validationError = validateSignUpData(
                username, firstName, lastName, email, password, confirmPassword
            )
            if (validationError != null) {
                return validationError
            }

            // Registrazione con Supabase Auth
            auth.signUpWith(Email) {
                this.email = email
                this.password = password
                // Metadati per la funzione trigger su Supabase
                data = buildJsonObject {
                    put("username", JsonPrimitive(username))
                    put("first_name", JsonPrimitive(firstName))
                    put("last_name", JsonPrimitive(lastName))
                }
            }
            SignUpResult.Success
        } catch (e: AuthRestException) {
            Log.e("AuthRepository", "Auth signup failed", e)

            // Gestione errori specifici di Supabase Auth
            when {
                e.message?.contains("email", ignoreCase = true) == true &&
                        e.message?.contains("already", ignoreCase = true) == true -> {
                    SignUpResult.Error.UserAlreadyExists
                }
                e.message?.contains("weak password", ignoreCase = true) == true -> {
                    SignUpResult.Error.WeakPassword
                }
                e.message?.contains("network", ignoreCase = true) == true -> {
                    SignUpResult.Error.NetworkError
                }
                else -> SignUpResult.Error.UnknownError(e)
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Sign up failed", e)
            SignUpResult.Error.UnknownError(e)
        }
    }

    /**
     * Autentica un utente esistente con email/password.
     *
     * @return SignInResult con l'esito del signIn
     */
    suspend fun signIn(
        email: String,
        password: String
    ): SignInResult {
        return try {
            // Validazione locale prima della chiamata di rete
            val validationError = validateSignInData(email, password)
            if (validationError != null) {
                return validationError
            }

            // SignIn con Supabase Auth
            auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            SignInResult.Success
        } catch (e: AuthRestException) {
            Log.e("AuthRepository", "Auth signin failed", e)
            // Gestione errori specifici di Supabase Auth
            SignInResult.Error.InvalidCredentials
        } catch (e: Exception) {
            Log.e("AuthRepository", "Sign in failed", e)
            when {
                e.message?.contains("network", ignoreCase = true) == true -> {
                    SignInResult.Error.NetworkError
                }
                else -> SignInResult.Error.UnknownError(e)
            }
        }
    }

    /**
     * Termina la sessione dell'utente corrente.
     */
    suspend fun signOut() {
        auth.signOut()
    }

    /**
     * Modifica email con Supabase Auth (il trigger su Supabase aggiornerà la tabella user)
     *
     * @param email nuova email scelta dall'utente
     * @return EditProfileResult esito del cambio email
     */
    suspend fun updateUserEmail(email: String): EditProfileResult {
        return try {
            auth.updateUser {
                this.email = email
            }
            EditProfileResult.Success
        } catch (e: AuthRestException) {
            Log.e("AuthRepository", "Update user email failed - Auth exception", e)
            if (e.message?.contains("email", ignoreCase = true) == true &&
                e.message?.contains("already", ignoreCase = true) == true) {
                EditProfileResult.Error.EmailAlreadyExists
            } else if (e.message?.contains("network", ignoreCase = true) == true) {
                EditProfileResult.Error.NetworkError
            } else {
                EditProfileResult.Error.UnknownError(e)
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Update user email failed", e)
            EditProfileResult.Error.UnknownError(e)
        }
    }

    /**
     * Aggiorna la password dell'utente autenticato.
     *
     * @param newPassword nuova password
     * @param confirmPassword conferma nuova password
     * @return ChangePasswordResult indicante l'esito dell'operazione
     */
    suspend fun changePassword(
        newPassword: String,
        confirmPassword: String
    ): ChangePasswordResult {
        // Validazione locale
        validateChangePasswordData(newPassword, confirmPassword)?.let { return it }

        return try {
            // Aggiorna direttamente la password senza verificare quella attuale
            auth.updateUser {
                password = newPassword
            }
            ChangePasswordResult.Success
        } catch (e: AuthRestException) {
            when {
                e.message?.contains("network", true) == true -> {
                    ChangePasswordResult.Error.NetworkError
                }
                else -> ChangePasswordResult.Error.UnknownError(e)
            }
        } catch (e: Exception) {
            ChangePasswordResult.Error.UnknownError(e)
        }
    }

    /**
     * Verifica se un username è già presente nel database (controllo SignUp)
     *
     * @param username scelto dall'utente
     * @return Boolean
     */
    private suspend fun isUsernameExists(username: String): Boolean {
        return try {
            val result = supabase.from("user")
                .select(columns = Columns.list("username")) {
                    filter {
                        eq("username", username)
                    }
                }
                .decodeList<Map<String, String>>()

            result.isNotEmpty()
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error checking username existence", e)
            false
        }
    }

    /**
     * Verifica se un'email è già presente nel database (controllo SignUp)
     *
     * @param email scelta dall'utente
     * @return Boolean
     */
    private suspend fun isEmailExists(email: String): Boolean {
        return try {
            val result = supabase.from("user")
                .select(columns = Columns.list("email")) {
                    filter {
                        eq("email", email)
                    }
                }
                .decodeList<Map<String, String>>()

            result.isNotEmpty()
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error checking email existence", e)
            false
        }
    }

    /**
     * Valida i dati di registrazione (controllo SignUp)
     *
     * @return SignUpResult.Error eventuale
     */
    private suspend fun validateSignUpData(
        username: String,
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        confirmPassword: String
    ): SignUpResult.Error? {
        // Controllo campi obbligatori
        if (username.isBlank() || firstName.isBlank() || lastName.isBlank() ||
            email.isBlank() || password.isBlank()
        ) {
            return SignUpResult.Error.RequiredFields
        }

        // Controllo lunghezza password
        if (password.length < 6) {
            return SignUpResult.Error.WeakPassword
        }

        // Controllo corrispondenza password
        if (password != confirmPassword) {
            return SignUpResult.Error.PasswordMismatch
        }

        // Controllo formato email
        if (!email.contains("@") || !email.contains(".")) {
            return SignUpResult.Error.InvalidEmail
        }

        // Controllo unicità username (controllo a livello di database)
        if (isUsernameExists(username)) {
            return SignUpResult.Error.UsernameAlreadyExists
        }

        // Controllo unicità email (controllo a livello di database)
        if (isEmailExists(email)) {
            return SignUpResult.Error.EmailAlreadyExists
        }

        return null // Validazione superata
    }

    /**
     * Valida i dati di login (controllo SignIn)
     *
     * @return SignInResult.Error eventuale
     */
    private fun validateSignInData(
        email: String,
        password: String
    ): SignInResult.Error? {
        // Controllo campi obbligatori
        if (email.isBlank() || password.isBlank()) {
            return SignInResult.Error.RequiredFields
        }

        // Controllo formato email base
        if (!email.contains("@") || !email.contains(".")) {
            return SignInResult.Error.InvalidEmail
        }

        return null // Validazione superata
    }

    /**
     * Valida i dati di cambio password
     *
     * @return ChangePasswordResult.Error eventuale
     */
    private fun validateChangePasswordData(
        newPassword: String,
        confirmPassword: String
    ): ChangePasswordResult.Error? {
        // Controllo campi obbligatori
        if (newPassword.isBlank() || confirmPassword.isBlank()) {
            return ChangePasswordResult.Error.RequiredFields
        }

        // Controllo lunghezza password
        if (newPassword.length < 6) {
            return ChangePasswordResult.Error.WeakPassword
        }

        // Controllo corrispondenza password
        if (newPassword != confirmPassword) {
            return ChangePasswordResult.Error.PasswordMismatch
        }
        return null
    }
}