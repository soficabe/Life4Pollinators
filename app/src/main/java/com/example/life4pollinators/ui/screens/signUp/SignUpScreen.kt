package com.example.life4pollinators.ui.screens.signUp

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
import com.example.life4pollinators.data.repositories.SignUpResult
import com.example.life4pollinators.ui.navigation.L4PRoute

/**
 * Schermata di registrazione utente con email/password.
 * Ora mostra errori per campo in caso di validazione lato client.
 */
@Composable
fun SignUpScreen(
    state: SignUpState,
    actions: SignUpActions,
    navController: NavHostController
) {
    val scrollState = rememberScrollState()
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var confirmPasswordVisible by rememberSaveable { mutableStateOf(false) }

    // Gestisce navigazione post-registrazione
    LaunchedEffect(state.signUpResult) {
        when (state.signUpResult) {
            SignUpResult.Loading -> {}
            SignUpResult.Success -> {
                navController.navigate(L4PRoute.Home) {
                    popUpTo(L4PRoute.SignUp) { inclusive = true }
                }
            }
            else -> {}
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
                contentDescription = "Logo App",
                modifier = Modifier.size(70.dp)
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.create_account_title),
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(Modifier.height(12.dp))

            // Card del form di registrazione
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 2.dp,
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {

                    // Campo Username
                    OutlinedTextField(
                        value = state.username,
                        onValueChange = {
                            actions.setUsername(it)
                            actions.clearError()
                        },
                        label = { Text(stringResource(R.string.username)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = state.usernameError != null,
                        supportingText = {
                            state.usernameError?.let { Text(text = stringResource(it), color = MaterialTheme.colorScheme.error) }
                        }
                    )

                    // Campo First Name
                    OutlinedTextField(
                        value = state.firstName,
                        onValueChange = {
                            actions.setFirstName(it)
                            actions.clearError()
                        },
                        label = { Text(stringResource(R.string.first_name)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = state.firstNameError != null,
                        supportingText = {
                            state.firstNameError?.let { Text(text = stringResource(it), color = MaterialTheme.colorScheme.error) }
                        }
                    )

                    // Campo Last Name
                    OutlinedTextField(
                        value = state.lastName,
                        onValueChange = {
                            actions.setLastName(it)
                            actions.clearError()
                        },
                        label = { Text(stringResource(R.string.last_name)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = state.lastNameError != null,
                        supportingText = {
                            state.lastNameError?.let { Text(text = stringResource(it), color = MaterialTheme.colorScheme.error) }
                        }
                    )

                    // Campo Email
                    OutlinedTextField(
                        value = state.email,
                        onValueChange = {
                            actions.setEmail(it)
                            actions.clearError()
                        },
                        label = { Text(stringResource(R.string.email)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = state.emailError != null,
                        supportingText = {
                            state.emailError?.let { Text(text = stringResource(it), color = MaterialTheme.colorScheme.error) }
                        }
                    )

                    // Campo Password
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
                                    if (passwordVisible)
                                        Icons.Outlined.Visibility
                                    else
                                        Icons.Outlined.VisibilityOff,
                                    contentDescription = "Toggle password visibility"
                                )
                            }
                        },
                        isError = state.passwordError != null,
                        supportingText = {
                            state.passwordError?.let { Text(text = stringResource(it), color = MaterialTheme.colorScheme.error) }
                        }
                    )

                    // Campo Confirm Password
                    OutlinedTextField(
                        value = state.confirmPsw,
                        onValueChange = {
                            actions.setConfirmPsw(it)
                            actions.clearError()
                        },
                        label = { Text(stringResource(R.string.confirm_password)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = if (confirmPasswordVisible)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Icon(
                                    if (confirmPasswordVisible)
                                        Icons.Outlined.Visibility
                                    else
                                        Icons.Outlined.VisibilityOff,
                                    contentDescription = "Toggle confirm password visibility"
                                )
                            }
                        },
                        isError = state.confirmPasswordError != null,
                        supportingText = {
                            state.confirmPasswordError?.let { Text(text = stringResource(it), color = MaterialTheme.colorScheme.error) }
                        }
                    )

                    // Messaggio di errore generico (es. network error)
                    if (state.errorMessage != null) {
                        Text(
                            text = stringResource(id = state.errorMessage),
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        )
                    }

                    // Bottone per inviare i dati di registrazione.
                    Button(
                        onClick = { actions.signUp() },
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
                            Text(stringResource(R.string.sign_up_button))
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Link per navigare al login
            TextButton(
                onClick = { navController.navigate(L4PRoute.SignIn) }
            ) {
                Text(
                    text = stringResource(R.string.have_account_sign_in),
                    textDecoration = TextDecoration.Underline
                )
            }

            // Opzione per continuare senza login
            TextButton(
                onClick = { navController.navigate(L4PRoute.Home) }
            ) {
                Text(
                    text = stringResource(R.string.continue_without_login),
                    textDecoration = TextDecoration.Underline
                )
            }
        }
    }
}