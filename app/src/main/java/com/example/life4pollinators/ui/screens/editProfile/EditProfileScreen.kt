package com.example.life4pollinators.ui.screens.editProfile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType.Companion.Email
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.life4pollinators.data.models.NavBarTab
import com.example.life4pollinators.ui.composables.AppBar
import com.example.life4pollinators.ui.composables.BottomNavBar

@Composable
fun EditProfileScreen(
    state: EditProfileState,
    actions: EditProfileActions,
    navController: NavHostController
) {
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Feedback per errori
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            actions.clearMessages()
        }
    }
    // Feedback per successo
    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess && state.emailConfirmationSentMessage == null) {
            snackbarHostState.showSnackbar("Modifica profilo salvata!")
            actions.clearMessages()
            navController.navigateUp()
        }
    }
    // Notifica cambio email
    LaunchedEffect(state.emailConfirmationSentMessage) {
        state.emailConfirmationSentMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            actions.clearMessages()
            navController.navigateUp()
        }
    }

    Scaffold(
        topBar = { AppBar(navController) },
        bottomBar = { BottomNavBar(NavBarTab.Profile, navController) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Profile Avatar (da implementare upload img)
            Box(contentAlignment = Alignment.Center) {
                Surface(
                    modifier = Modifier.size(110.dp),
                    shape = CircleShape,
                    shadowElevation = 6.dp,
                    tonalElevation = 2.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                    radius = 110f
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = "Profile image",
                            modifier = Modifier.size(54.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Change Image Button (disabled per ora)
            TextButton(
                onClick = { /* Da implementare */ },
                enabled = false,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Change Image (soon)",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Username Field
                OutlinedTextField(
                    value = state.username,
                    onValueChange = { actions.setUsername(it) },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    enabled = !state.isSaving,
                )

                // First Name Field
                OutlinedTextField(
                    value = state.firstName,
                    onValueChange = { actions.setFirstName(it) },
                    label = { Text("First Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    enabled = !state.isSaving,
                )

                // Last Name Field
                OutlinedTextField(
                    value = state.lastName,
                    onValueChange = { actions.setLastName(it) },
                    label = { Text("Last Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    enabled = !state.isSaving,
                )

                // Email Field
                OutlinedTextField(
                    value = state.email,
                    onValueChange = { actions.setEmail(it) },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = Email),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !state.isSaving,
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Save Changes Button
            FilledTonalButton(
                onClick = { actions.saveChanges() },
                modifier = Modifier
                    .padding(horizontal = 40.dp)
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(20.dp),
                enabled = state.hasChanges && !state.isSaving && !state.isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(imageVector = Icons.Outlined.Check, contentDescription = "Edit Profile")
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Save Changes",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium)
                    )
                }
            }

            if (state.isLoading) {
                Spacer(modifier = Modifier.height(18.dp))
                CircularProgressIndicator()
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}