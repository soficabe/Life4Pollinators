package com.example.life4pollinators.ui.screens.signUp

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
            SignUpResult.Loading -> {
                // Stato di caricamento - nessuna azione richiesta
            }
            SignUpResult.Success -> {
                // Navigazione alla home con rimozione dello stack di registrazione
                navController.navigate(L4PRoute.Home) {
                    popUpTo(L4PRoute.SignUp) { inclusive = true }
                }
            }
            else -> {
                // Altri stati gestiti tramite errorMessage nella UI
            }
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

            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 2.dp,
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
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
                        isError = state.errorMessage == R.string.username_already_exists
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
                        singleLine = true
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
                        singleLine = true
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
                        isError = state.errorMessage == R.string.email_already_exists ||
                                state.errorMessage == R.string.email_invalid_format ||
                                state.errorMessage == R.string.userExisting_error
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
                        isError = state.errorMessage == R.string.weakPassword
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
                        isError = state.errorMessage == R.string.passwordNotMatch_error
                    )

                    // Messaggio di errore
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

                    // Bottone Sign Up
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