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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.life4pollinators.R
import com.example.life4pollinators.ui.navigation.L4PRoute
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.compose.auth.composable.NativeSignInResult
import io.github.jan.supabase.compose.auth.composable.rememberSignInWithGoogle
import io.github.jan.supabase.compose.auth.composeAuth
import io.github.jan.supabase.compose.auth.ui.ProviderButtonContent
import io.github.jan.supabase.compose.auth.ui.annotations.AuthUiExperimental
import org.koin.compose.koinInject

@OptIn(AuthUiExperimental::class)
@Composable
fun SignInScreen(
    state: SignInState,
    actions: SignInActions,
    navController: NavHostController
) {
    val supabaseClient: SupabaseClient = koinInject()

    val scrollState = rememberScrollState()
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    val googleAuthState = supabaseClient.composeAuth.rememberSignInWithGoogle(
        onResult = { result ->
            when (result) {
                is NativeSignInResult.Success -> {
                    navController.navigate(L4PRoute.Home) {
                        popUpTo(L4PRoute.SignIn) { inclusive = true }
                    }
                }
                is NativeSignInResult.ClosedByUser -> {
                    snackbarMessage = R.string.login_cancelled.toString()
                }
                is NativeSignInResult.Error -> {
                    snackbarMessage = result.message
                }
                is NativeSignInResult.NetworkError -> {
                    snackbarMessage = R.string.network_error.toString()
                }
            }
        }
    )

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
                contentDescription = "Logo App",
                modifier = Modifier.size(70.dp)
            )
            Spacer(Modifier.height(12.dp))

            Text(
                text = "Sign In to continue",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(Modifier.height(12.dp))

            // Card che racchiude i campi
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
                    OutlinedTextField(
                        value = state.email,
                        onValueChange = actions::setEmail,
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = state.psw,
                        onValueChange = actions::setPsw,
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                )
                            }
                        }
                    )

                    Button(
                        onClick = { /*actions.signIn()*/ },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Sign In")
                    }

                    TextButton(
                        onClick = { navController.navigate(L4PRoute.SignUp) },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            text = "Don’t have an account? Sign Up",
                            textDecoration = TextDecoration.Underline
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Divider "Or"
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text("Or", modifier = Modifier.padding(horizontal = 8.dp))
                HorizontalDivider(modifier = Modifier.weight(1f))
            }

            Spacer(Modifier.height(16.dp))

            // Google SignIn Button (library compose-auth-ui)
            OutlinedButton(
                onClick = { googleAuthState.startFlow() },
                content = { ProviderButtonContent(provider = Google) }
            )

            TextButton(
                onClick = { navController.navigate(L4PRoute.Home) },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(
                    text = "Continue without logging in",
                    textDecoration = TextDecoration.Underline
                )
            }

            SnackbarHost(hostState = snackbarHostState)
        }
    }
}