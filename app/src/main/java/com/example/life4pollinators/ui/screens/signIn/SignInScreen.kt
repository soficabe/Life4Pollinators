package com.example.life4pollinators.ui.screens.signIn

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.life4pollinators.R
import com.example.life4pollinators.data.repositories.SignInResult
import com.example.life4pollinators.ui.navigation.L4PRoute
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.compose.auth.composable.NativeSignInResult
import io.github.jan.supabase.compose.auth.composable.rememberSignInWithGoogle
import io.github.jan.supabase.compose.auth.composeAuth
import io.github.jan.supabase.compose.auth.ui.ProviderButtonContent
import io.github.jan.supabase.compose.auth.ui.annotations.AuthUiExperimental
import org.koin.compose.koinInject

/**
 * Schermata di login utente.
 * Permette l'accesso tramite email/password o Google.
 *
 * @param state Stato corrente della schermata di login
 * @param actions Interfaccia delle azioni disponibili
 * @param navController Controller di navigazione
 */
@OptIn(AuthUiExperimental::class)
@Composable
fun SignInScreen(
    state: SignInState,
    actions: SignInActions,
    navController: NavHostController
) {
    // Ottiene il client Supabase tramite Koin per la gestione dell'autenticazione Google
    val supabaseClient: SupabaseClient = koinInject()

    val scrollState = rememberScrollState()
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    // Stato per Snackbar (messaggi temporanei)
    val snackbarHostState = remember { SnackbarHostState() }
    // Messaggio da mostrare nella snackbar (se presente)
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    // Gestisce la navigazione post-login
    LaunchedEffect(state.signInResult) {
        when (state.signInResult) {
            SignInResult.Loading -> {
                // Stato di caricamento - nessuna azione richiesta
            }
            SignInResult.Success -> {
                navController.navigate(L4PRoute.Home) {
                    popUpTo(L4PRoute.SignIn) { inclusive = true }
                }
            }
            is SignInResult.Error -> {
                // Errori gestiti dalla UI tramite errorMessageRes
            }
            else -> {}
        }
    }

    // Messaggi localizzati per errori nella login Google
    val closedByUserMsg = stringResource(R.string.login_cancelled_by_user)
    val networkErrorMsg = stringResource(R.string.network_error)

    // Integrazione del login con Google tramite Supabase Compose Auth
    val googleAuthState = supabaseClient.composeAuth.rememberSignInWithGoogle(
        onResult = { result ->
            when (result) {
                is NativeSignInResult.Success -> {
                    navController.navigate(L4PRoute.Home) {
                        popUpTo(L4PRoute.SignIn) { inclusive = true }
                    }
                }
                is NativeSignInResult.ClosedByUser -> {
                    snackbarMessage = closedByUserMsg
                }
                is NativeSignInResult.Error -> {
                    snackbarMessage = result.message
                }
                is NativeSignInResult.NetworkError -> {
                    snackbarMessage = networkErrorMsg
                }
            }
        }
    )

    // Mostra la snackbar se c'è un messaggio da mostrare
    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            snackbarMessage = null
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(56.dp))

            Image(
                painter = painterResource(id = R.drawable.logo_l4p_no_bg),
                contentDescription = null,
                modifier = Modifier.size(70.dp)
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.sign_in_title),
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(Modifier.height(12.dp))

            // Card del form di login
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 2.dp,
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Campo email
                    OutlinedTextField(
                        value = state.email,
                        onValueChange = {
                            actions.setEmail(it)
                            actions.clearError()
                        },
                        label = { Text(stringResource(R.string.email)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = state.signInResult is SignInResult.Error.InvalidEmail ||
                                state.signInResult is SignInResult.Error.InvalidCredentials
                    )

                    // Campo password
                    OutlinedTextField(
                        value = state.psw,
                        onValueChange = {
                            actions.setPsw(it)
                            actions.clearError()
                        },
                        label = { Text(stringResource(R.string.password)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = if (passwordVisible)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible)
                                        Icons.Outlined.Visibility
                                    else
                                        Icons.Outlined.VisibilityOff,
                                    contentDescription = null
                                )
                            }
                        },
                        isError = state.signInResult is SignInResult.Error.InvalidCredentials ||
                                state.signInResult is SignInResult.Error.RequiredFields
                    )

                    // Mostra eventuale messaggio di errore localizzato
                    if (state.errorMessageRes != null) {
                        Text(
                            text = stringResource(id = state.errorMessageRes),
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        )
                    }

                    // Bottone per invio login, mostra loader se isLoading
                    Button(
                        onClick = { actions.signIn() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !state.isLoading
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(stringResource(R.string.sign_in_button))
                        }
                    }

                    // Pulsante per andare alla schermata di registrazione
                    TextButton(
                        onClick = { navController.navigate(L4PRoute.SignUp) },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            text = stringResource(R.string.dont_have_account),
                            textDecoration = TextDecoration.Underline
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Separatore visivo "oppure"
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text(stringResource(R.string.or), modifier = Modifier.padding(horizontal = 8.dp))
                HorizontalDivider(modifier = Modifier.weight(1f))
            }

            Spacer(Modifier.height(16.dp))

            // Bottone per login con Google
            OutlinedButton(
                onClick = { googleAuthState.startFlow() },
                content = { ProviderButtonContent(provider = Google) }
            )

            // Pulsante per continuare senza login
            TextButton(
                onClick = { navController.navigate(L4PRoute.Home) },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(
                    text = stringResource(R.string.continue_without_login),
                    textDecoration = TextDecoration.Underline
                )
            }

            // Snackbar per messaggi informativi/di errore temporanei
            SnackbarHost(hostState = snackbarHostState)
        }
    }
}