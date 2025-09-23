package com.example.life4pollinators.ui.screens.editProfile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Photo
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.life4pollinators.R
import com.example.life4pollinators.data.models.NavBarTab
import com.example.life4pollinators.ui.composables.AppBar
import com.example.life4pollinators.ui.composables.BottomNavBar
import com.example.life4pollinators.ui.composables.ProfileIcon
import com.example.life4pollinators.ui.navigation.L4PRoute
import com.example.life4pollinators.utils.rememberCameraLauncher
import com.example.life4pollinators.utils.rememberGalleryLauncher

/**
 * Schermata di editing del profilo utente.
 *
 * Permette la modifica di tutti i dati anagrafici e dell'immagine profilo.
 * Gestisce feedback di successo/errore tramite snackbar e mostra loader durante il salvataggio.
 * Mostra errori per campo in caso di validazione lato client.
 * L'utente può scattare una foto o selezionarne una dalla galleria.
 *
 * @param state Stato attuale del profilo in editing
 * @param actions Interfaccia delle azioni disponibili dalla UI
 * @param navController Controller di navigazione Compose
 */
@Composable
fun EditProfileScreen(
    state: EditProfileState,
    actions: EditProfileActions,
    navController: NavHostController
) {
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Gestione dialog per scelta foto
    var showImagePicker by remember { mutableStateOf(false) }

    // Launcher per fotocamera
    val launchCamera = rememberCameraLauncher(
        onPhotoReady = { uri ->
            actions.onProfileImageSelected(uri, context)
            showImagePicker = false
        },
        onError = { errorMsgRes ->
            actions.setErrorRes(errorMsgRes)
            showImagePicker = false
        }
    )

    // Launcher per galleria
    val launchGallery = rememberGalleryLauncher { uri ->
        actions.onProfileImageSelected(uri, context)
        showImagePicker = false
    }

    // Mostra snackbar per errori
    LaunchedEffect(state.errorMessageRes, state.errorMessageArg) {
        if (state.errorMessageRes != null) {
            val msg = if (state.errorMessageArg != null)
                context.getString(state.errorMessageRes, state.errorMessageArg)
            else
                context.getString(state.errorMessageRes)
            snackbarHostState.showSnackbar(msg)
            actions.clearMessages()
        }
    }

    val profileSavedMsg = stringResource(R.string.profile_saved)

    // Mostra snackbar per salvataggio riuscito
    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess && state.emailConfirmationSentMessage == null) {
            snackbarHostState.showSnackbar(profileSavedMsg)
            actions.clearMessages()
            navController.navigate(L4PRoute.Profile) {
                popUpTo(L4PRoute.Profile) { inclusive = true }
            }
        }
    }
    // Mostra snackbar per conferma cambio email
    LaunchedEffect(state.emailConfirmationSentMessage, state.emailConfirmationSentArg) {
        if (state.emailConfirmationSentMessage != null && state.emailConfirmationSentArg != null) {
            val msg = context.getString(
                state.emailConfirmationSentMessage.toInt(),
                state.emailConfirmationSentArg
            )
            snackbarHostState.showSnackbar(msg)
            actions.clearMessages()
            navController.navigate(L4PRoute.Profile) {
                popUpTo(L4PRoute.Profile) { inclusive = true }
            }
        }
    }

    Scaffold(
        topBar = { AppBar(navController) },
        bottomBar = { BottomNavBar(selectedTab = NavBarTab.Profile, navController = navController) },
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

            // Avatar utente, cliccabile per cambiare foto
            ProfileIcon(
                imageUrl = state.newProfileImageUri?.toString() ?: state.image,
                isClickable = true,
                onClick = { showImagePicker = true },
                showLoader = state.isLoading || state.isUploadingImage
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Bottone per cambiare foto
            TextButton(
                onClick = { showImagePicker = true },
                enabled = !state.isUploadingImage && !state.isSaving && !state.isLoading,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = "Edit Profile Image",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    stringResource(R.string.change_image),
                    style = MaterialTheme.typography.labelLarge
                )
            }

            // Dialog scelta immagine (camera/galleria)
            if (showImagePicker) {
                AlertDialog(
                    onDismissRequest = { showImagePicker = false },
                    title = { Text(stringResource(R.string.choose_photo)) },
                    text = {
                        Column {
                            Button(
                                onClick = { launchCamera() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(stringResource(R.string.take_photo))
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Outlined.PhotoCamera,
                                    contentDescription = "Camera"
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = { launchGallery() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(stringResource(R.string.choose_gallery))
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Outlined.Photo,
                                    contentDescription = "Photo from Gallery"
                                )
                            }
                        }
                    },
                    confirmButton = {},
                    dismissButton = {
                        TextButton(onClick = { showImagePicker = false }) { Text(stringResource(R.string.cancel)) }
                    }
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
                    label = { Text(stringResource(R.string.username)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    enabled = !state.isSaving,
                    isError = state.usernameError != null,
                    supportingText = {
                        state.usernameError?.let { Text(text = stringResource(it), color = MaterialTheme.colorScheme.error) }
                    },
                    trailingIcon = {
                        val isModified = state.username != state.user?.username
                        IconButton(
                            onClick = { actions.resetUsername() },
                            enabled = isModified
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = "Reset username"
                            )
                        }
                    }
                )

                // First Name Field
                OutlinedTextField(
                    value = state.firstName,
                    onValueChange = { actions.setFirstName(it) },
                    label = { Text(stringResource(R.string.first_name)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    enabled = !state.isSaving,
                    isError = state.firstNameError != null,
                    supportingText = {
                        state.firstNameError?.let { Text(text = stringResource(it), color = MaterialTheme.colorScheme.error) }
                    },
                    trailingIcon = {
                        val isModified = state.firstName != state.user?.firstName
                        IconButton(
                            onClick = { actions.resetFirstName() },
                            enabled = isModified
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = "Reset first name"
                            )
                        }
                    }
                )

                // Last Name Field
                OutlinedTextField(
                    value = state.lastName,
                    onValueChange = { actions.setLastName(it) },
                    label = { Text(stringResource(R.string.last_name)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    enabled = !state.isSaving,
                    isError = state.lastNameError != null,
                    supportingText = {
                        state.lastNameError?.let { Text(text = stringResource(it), color = MaterialTheme.colorScheme.error) }
                    },
                    trailingIcon = {
                        val isModified = state.lastName != state.user?.lastName
                        IconButton(
                            onClick = { actions.resetLastName() },
                            enabled = isModified
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = "Reset last name"
                            )
                        }
                    }
                )

                // Email Field
                OutlinedTextField(
                    value = state.email,
                    onValueChange = { actions.setEmail(it) },
                    label = { Text(stringResource(R.string.email)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Email),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !state.isSaving,
                    isError = state.emailError != null,
                    supportingText = {
                        state.emailError?.let { Text(text = stringResource(it), color = MaterialTheme.colorScheme.error) }
                    },
                    trailingIcon = {
                        val isModified = state.email != state.user?.email
                        IconButton(
                            onClick = { actions.resetEmail() },
                            enabled = isModified
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = "Reset email"
                            )
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Bottone di salvataggio
            FilledTonalButton(
                onClick = { actions.saveChanges(context) },
                modifier = Modifier
                    .padding(horizontal = 40.dp)
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(20.dp),
                enabled = state.hasChanges && !state.isSaving && !state.isLoading && !state.isUploadingImage,
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
                        stringResource(R.string.save_changes),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
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