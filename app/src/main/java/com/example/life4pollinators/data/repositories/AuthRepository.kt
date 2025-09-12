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
 * Risultati possibili della registrazione
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
 * Risultati possibili del login
 */
sealed interface SignInResult {
    data object Loading : SignInResult
    data object Success : SignInResult

    sealed interface Error : SignInResult {
        data object InvalidCredentials : Error
        data object RequiredFields : Error // Aggiunto per validazione campi
        data object InvalidEmail : Error // Aggiunto per consistenza
        data object NetworkError : Error
        data class UnknownError(val exception: Throwable) : Error
    }
}

/**
 * Repository per gestire autenticazione (con trigger automatico per tabella user)
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

    // Recupera le informazioni dell'utente dalla sessione corrente.
    suspend fun getUser(): UserInfo {
        return auth.retrieveUserForCurrentSession(true)
    }

    // Verifica se un username è già presente nel database.
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

    // Verifica se un'email è già presente nel database pubblico.
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

    // Valida i dati di registrazione.
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

        // Controllo unicità username
        if (isUsernameExists(username)) {
            return SignUpResult.Error.UsernameAlreadyExists
        }

        // Controllo unicità email
        if (isEmailExists(email)) {
            return SignUpResult.Error.EmailAlreadyExists
        }

        return null // Validazione superata
    }

    // Valida i dati di login
    private fun validateSignInData(email: String, password: String): SignInResult.Error? {
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

    // Registra un nuovo utente con email/password.
    suspend fun signUp(
        username: String,
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        confirmPassword: String
    ): SignUpResult {
        return try {
            // Validazione completa
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
                // Metadati per la funzione trigger
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
     */
    suspend fun signIn(email: String, password: String): SignInResult {
        return try {
            // Validazione locale prima della chiamata di rete
            val validationError = validateSignInData(email, password)
            if (validationError != null) {
                return validationError
            }

            auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            SignInResult.Success
        } catch (e: AuthRestException) {
            Log.e("AuthRepository", "Auth signin failed", e)
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
}