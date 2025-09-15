package com.example.life4pollinators.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AppSettingsAlt
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.life4pollinators.R
import com.example.life4pollinators.ui.composables.AppBar
import com.example.life4pollinators.ui.composables.BottomNavBar
import com.example.life4pollinators.ui.navigation.L4PRoute
import com.example.life4pollinators.data.models.Theme

@Composable
fun SettingsScreen (
    state: SettingsState,
    actions: SettingsActions,
    navController: NavHostController
) {
    val scrollState = rememberScrollState()

    // Cambio tema (scuro, chiaro, default)
    var showThemeDialog by rememberSaveable { mutableStateOf(false) }
    var selectedTheme by remember { mutableStateOf(state.theme) }
    val themeOptions = Theme.entries

    // Dialog di conferma logout
    var showLogoutDialog by rememberSaveable { mutableStateOf(false) }

    // Cambio password - TUTTI E TRE I CAMPI
    var showPasswordDialog by rememberSaveable { mutableStateOf(false) }
    var currentPassword by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var currentPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var confirmPasswordVisible by rememberSaveable { mutableStateOf(false) }

    Scaffold (
        topBar = { AppBar(navController) },
        bottomBar = { BottomNavBar(navController = navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 24.dp)
                .verticalScroll(scrollState)
        ) {

            // Sezione Aspetto
            TextTitle(stringResource(R.string.appearance))
            HorizontalDivider()
            SettingsClickable(
                stringResource(R.string.changeTheme),
                when(state.theme) {
                    Theme.Light -> Icons.Filled.LightMode
                    Theme.Dark -> Icons.Filled.DarkMode
                    Theme.System -> Icons.Filled.AppSettingsAlt
                },
                onClick = { showThemeDialog = true }
            )

            // Sezione Privacy e Sicurezza (solo se autenticato)
            //if (state.isAuthenticated) {
            TextTitle(stringResource(R.string.privacy))

            HorizontalDivider()
            SettingsClickable(
                stringResource(R.string.changePassword),
                Icons.Filled.Lock,
                onClick = { showPasswordDialog = true }
            )

            HorizontalDivider()
            SettingsClickable(
                stringResource(R.string.logout),
                Icons.AutoMirrored.Filled.Logout,
                MaterialTheme.colorScheme.error,
                onClick = { showLogoutDialog = true }
            )
            //}

        }

        // Dialog per il cambio tema
        if(showThemeDialog) {
            ThemeRadioOptionsDialog(
                title = stringResource(R.string.chooseTheme),
                options = themeOptions,
                selectedOption = selectedTheme,
                onOptionSelected = {
                    selectedTheme = it
                    showThemeDialog = false
                    actions.changeTheme(it)
                },
                onDismiss = { showThemeDialog = false }
            )
        }

        // Dialog di conferma logout
        if(showLogoutDialog) {
            LogoutConfirmationDialog(
                onConfirm = {
                    showLogoutDialog = false
                    actions.logout()
                    navController.navigate(L4PRoute.Home) {
                        popUpTo(navController.graph.id) {
                            inclusive = true
                        }
                    }
                },
                onDismiss = { showLogoutDialog = false }
            )
        }

        // Dialog cambio password - CON TUTTI E TRE I CAMPI
        if (showPasswordDialog) {
            ChangePasswordDialog(
                currentPassword = currentPassword,
                newPassword = newPassword,
                confirmPassword = confirmPassword,
                currentPasswordVisible = currentPasswordVisible,
                passwordVisible = passwordVisible,
                confirmPasswordVisible = confirmPasswordVisible,
                onCurrentPasswordChange = { currentPassword = it },
                onNewPasswordChange = { newPassword = it },
                onConfirmPasswordChange = { confirmPassword = it },
                onCurrentPasswordVisibilityChange = { currentPasswordVisible = !currentPasswordVisible },
                onPasswordVisibilityChange = { passwordVisible = !passwordVisible },
                onConfirmPasswordVisibilityChange = { confirmPasswordVisible = !confirmPasswordVisible },
                onConfirm = {
                    // TODO: Implementare logica cambio password
                    // actions.updatePassword(currentPassword, newPassword, confirmPassword)
                    showPasswordDialog = false
                    currentPassword = ""
                    newPassword = ""
                    confirmPassword = ""
                },
                onDismiss = {
                    showPasswordDialog = false
                    currentPassword = ""
                    newPassword = ""
                    confirmPassword = ""
                }
            )
        }
    }
}

@Composable
fun TextTitle (text: String) {
    Text(text = text,
        modifier = Modifier.padding(vertical = 8.dp),
        fontWeight = FontWeight.Bold)
}

@Composable
fun SettingsClickable(
    title: String,
    icon: ImageVector,
    color: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 4.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = title, tint = color,
            modifier = Modifier
                .size(32.dp)
                .background(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(10.dp))
                .padding(5.dp))
        Spacer(Modifier.width(16.dp))
        Text(title, color = color, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
    }
}

@Composable
fun ThemeRadioOptionsDialog(
    title: String,
    options: List<Theme>,
    selectedOption: Theme,
    onOptionSelected: (Theme) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = {
            Column {
                options.forEach { option ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                                onClick = { onOptionSelected(option) }
                            )
                    ) {
                        RadioButton(
                            selected = option == selectedOption,
                            onClick = { onOptionSelected(option) }
                        )
                        Text(text = stringResource(id = option.themeName))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.closeButton))
            }
        }
    )
}

@Composable
fun LogoutConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.logout_confirmation_title),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Text(
                text = stringResource(R.string.logout_confirmation_message),
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(stringResource(R.string.logout_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
fun ChangePasswordDialog(
    currentPassword: String,
    newPassword: String,
    confirmPassword: String,
    currentPasswordVisible: Boolean,
    passwordVisible: Boolean,
    confirmPasswordVisible: Boolean,
    onCurrentPasswordChange: (String) -> Unit,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onCurrentPasswordVisibilityChange: () -> Unit,
    onPasswordVisibilityChange: () -> Unit,
    onConfirmPasswordVisibilityChange: () -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    // Validazioni solo per feedback visivo locale
    val isNewPasswordTooShort = newPassword.isNotEmpty() && newPassword.length < 6
    val doPasswordsMatch = newPassword.isNotEmpty() && confirmPassword.isNotEmpty() && newPassword == confirmPassword
    val isFormValid = currentPassword.isNotEmpty() && newPassword.length >= 6 && doPasswordsMatch

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(R.string.changePassword),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column {
                // 1. CAMPO PASSWORD ATTUALE
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = onCurrentPasswordChange,
                    label = { Text(stringResource(R.string.currentPassword)) },
                    singleLine = true,
                    visualTransformation = if (currentPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = onCurrentPasswordVisibilityChange) {
                            Icon(
                                if (currentPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                "Toggle current password visibility",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        cursorColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                // 2. CAMPO NUOVA PASSWORD
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = onNewPasswordChange,
                    label = { Text(stringResource(R.string.newPassword)) },
                    singleLine = true,
                    isError = isNewPasswordTooShort,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = onPasswordVisibilityChange) {
                            Icon(
                                if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                "Toggle password visibility",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        errorBorderColor = MaterialTheme.colorScheme.error,
                        errorLabelColor = MaterialTheme.colorScheme.error
                    ),
                    supportingText = if (isNewPasswordTooShort) {
                        { Text(
                            stringResource(R.string.password_too_short),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        ) }
                    } else null,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                // 3. CAMPO CONFERMA PASSWORD
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = onConfirmPasswordChange,
                    label = { Text(stringResource(R.string.confirmNewPassword)) },
                    singleLine = true,
                    isError = confirmPassword.isNotEmpty() && !doPasswordsMatch,
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = onConfirmPasswordVisibilityChange) {
                            Icon(
                                if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                "Toggle confirm password visibility",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        errorBorderColor = MaterialTheme.colorScheme.error,
                        errorLabelColor = MaterialTheme.colorScheme.error
                    ),
                    supportingText = if (confirmPassword.isNotEmpty() && !doPasswordsMatch) {
                        { Text(
                            stringResource(R.string.passwords_not_match),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        ) }
                    } else null,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = isFormValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text(stringResource(R.string.saveString))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}