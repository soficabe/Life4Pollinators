package com.example.life4pollinators.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.life4pollinators.R
import com.example.life4pollinators.ui.composables.AppBar
import com.example.life4pollinators.ui.composables.BottomNavBar
import com.example.life4pollinators.ui.navigation.L4PRoute
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.remember
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

            HorizontalDivider()
            SettingsClickable(
                stringResource(R.string.changeTheme),
                Icons.Filled.WbSunny,
                onClick = { showThemeDialog = true }
            )

            HorizontalDivider()

            //if (state.isAuthenticated) {
            SettingsClickable(
                stringResource(R.string.logout),
                Icons.AutoMirrored.Filled.Logout,
                Color.Red,
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
    }
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
                colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
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