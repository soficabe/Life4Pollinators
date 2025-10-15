package com.example.life4pollinators.ui.screens.addSighting

import android.Manifest
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.life4pollinators.R
import com.example.life4pollinators.data.models.NavBarTab
import com.example.life4pollinators.ui.composables.AppBar
import com.example.life4pollinators.ui.composables.BottomNavBar
import com.example.life4pollinators.ui.composables.ImagePickerDialog
import com.example.life4pollinators.ui.composables.LocationMapDialog
import com.example.life4pollinators.utils.LocationService
import com.example.life4pollinators.utils.PermissionStatus
import com.example.life4pollinators.utils.rememberCameraLauncher
import com.example.life4pollinators.utils.rememberGalleryLauncher
import com.example.life4pollinators.utils.rememberMultiplePermissions
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.time.ZoneId

@Composable
fun AddSightingScreen(
    state: AddSightingState,
    actions: AddSightingActions,
    userId: String,
    isAuthenticated: Boolean,
    navController: NavHostController
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val locationService = remember { LocationService(context) }

    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var showMapDialog by rememberSaveable { mutableStateOf(false) }
    var showImagePicker by rememberSaveable { mutableStateOf(false) }
    var isLoadingLocation by rememberSaveable { mutableStateOf(false) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var showTimePicker by rememberSaveable { mutableStateOf(false) }
    var showPollinatorDropdown by rememberSaveable { mutableStateOf(false) }
    var showPlantDropdown by rememberSaveable { mutableStateOf(false) }
    var showLocationDisabledWarning by rememberSaveable { mutableStateOf(false) }
    var showPermissionDeniedWarning by rememberSaveable { mutableStateOf(false) }
    var showPermissionPermanentlyDeniedWarning by rememberSaveable { mutableStateOf(false) }

    val cameraLauncher = rememberCameraLauncher(
        onPhotoReady = { uri ->
            actions.setImageUri(uri)
            errorMessage = null
            showImagePicker = false
        },
        onError = { resId ->
            errorMessage = context.getString(resId)
            showImagePicker = false
        }
    )

    val galleryLauncher = rememberGalleryLauncher { uri ->
        actions.setImageUri(uri)
        errorMessage = null
        showImagePicker = false
    }

    val locationPermission = rememberMultiplePermissions(
        listOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
    ) { statuses ->
        when {
            statuses.any { it.value.isGranted } -> {
                scope.launch {
                    isLoadingLocation = true
                    try {
                        val coords = locationService.getCurrentLocation()
                        coords?.let { actions.setLocation(it.latitude, it.longitude) }
                    } catch (_: SecurityException) {
                    } catch (ex: IllegalStateException) {
                        showLocationDisabledWarning = true
                    } finally {
                        isLoadingLocation = false
                    }
                }
            }
            statuses.all { it.value == PermissionStatus.PermanentlyDenied } ->
                showPermissionPermanentlyDeniedWarning = true
            else -> showPermissionDeniedWarning = true
        }
    }

    Scaffold(
        topBar = { AppBar(navController) },
        bottomBar = {
            BottomNavBar(
                isAuthenticated = isAuthenticated,
                selectedTab = NavBarTab.AddSighting,
                navController = navController
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            // Image section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = if (state.isImageInvalid) {
                    BorderStroke(2.dp, MaterialTheme.colorScheme.error)
                } else null
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.add_sighting_image_section),
                            style = MaterialTheme.typography.titleSmall,
                            color = if (state.isImageInvalid) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.primary
                        )
                        if (state.isImageInvalid) {
                            Text(
                                text = "*",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    if (state.imageUri != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(state.imageUri),
                                contentDescription = "Selected image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        OutlinedButton(
                            onClick = { showImagePicker = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.change_image))
                        }
                    } else {
                        Button(
                            onClick = { showImagePicker = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CameraAlt,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.add_sighting_select_image))
                        }
                    }

                    errorMessage?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // Date & Time section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = if (state.isDateInvalid || state.isTimeInvalid) {
                    BorderStroke(2.dp, MaterialTheme.colorScheme.error)
                } else null
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.add_sighting_datetime_section),
                            style = MaterialTheme.typography.titleSmall,
                            color = if (state.isDateInvalid || state.isTimeInvalid)
                                MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.primary
                        )
                        if (state.isDateInvalid || state.isTimeInvalid) {
                            Text(
                                text = "*",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = state.date?.toString() ?: "",
                            onValueChange = {},
                            label = { Text(stringResource(R.string.add_sighting_date)) },
                            readOnly = true,
                            modifier = Modifier.weight(1.3f),
                            isError = state.isDateInvalid,
                            trailingIcon = {
                                IconButton(onClick = { showDatePicker = true }) {
                                    Icon(
                                        imageVector = Icons.Outlined.CalendarToday,
                                        contentDescription = "Select date"
                                    )
                                }
                            },
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = state.time?.toString() ?: "",
                            onValueChange = {},
                            label = { Text(stringResource(R.string.add_sighting_time)) },
                            readOnly = true,
                            modifier = Modifier.weight(1f),
                            isError = state.isTimeInvalid,
                            trailingIcon = {
                                IconButton(onClick = { showTimePicker = true }) {
                                    Icon(
                                        imageVector = Icons.Outlined.Schedule,
                                        contentDescription = "Select time"
                                    )
                                }
                            },
                            singleLine = true
                        )
                    }
                }
            }

            // Location section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = if (state.isLocationInvalid) {
                    BorderStroke(2.dp, MaterialTheme.colorScheme.error)
                } else null
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.add_sighting_location_section),
                            style = MaterialTheme.typography.titleSmall,
                            color = if (state.isLocationInvalid) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.primary
                        )
                        if (state.isLocationInvalid) {
                            Text(
                                text = "*",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                if (locationPermission.statuses.any { it.value.isGranted }) {
                                    scope.launch {
                                        isLoadingLocation = true
                                        try {
                                            val coords = locationService.getCurrentLocation()
                                            coords?.let { actions.setLocation(it.latitude, it.longitude) }
                                        } catch (ex: SecurityException) {
                                            showPermissionDeniedWarning = true
                                        } catch (ex: IllegalStateException) {
                                            showLocationDisabledWarning = true
                                        } finally {
                                            isLoadingLocation = false
                                        }
                                    }
                                } else {
                                    locationPermission.launchPermissionRequest()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = !isLoadingLocation
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.MyLocation,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(stringResource(R.string.add_sighting_current_location_short))
                        }

                        OutlinedButton(
                            onClick = { showMapDialog = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Map,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(stringResource(R.string.add_sighting_choose_location_short))
                        }
                    }

                    if (isLoadingLocation) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.loading_location),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else if (state.latitude != null && state.longitude != null) {
                        Text(
                            text = stringResource(R.string.add_sighting_lat_lng, state.latitude, state.longitude),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Species selection section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = if (state.isPollinatorInvalid || state.isPlantInvalid) {
                    BorderStroke(2.dp, MaterialTheme.colorScheme.error)
                } else null
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.add_sighting_species_section),
                            style = MaterialTheme.typography.titleSmall,
                            color = if (state.isPollinatorInvalid || state.isPlantInvalid)
                                MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.primary
                        )
                        if (state.isPollinatorInvalid || state.isPlantInvalid) {
                            Text(
                                text = "*",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    // Pollinator
                    Box {
                        OutlinedTextField(
                            value = state.pollinatorQuery,
                            onValueChange = {
                                if (state.selectedPollinatorId != null && it != state.selectedPollinatorName) {
                                    actions.clearPollinator()
                                }
                                actions.onPollinatorQueryChange(it)
                                showPollinatorDropdown = it.isNotBlank() && state.selectedPollinatorId == null
                            },
                            label = { Text(stringResource(R.string.add_sighting_pollinator_hint)) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = state.selectedPlantId == null,
                            isError = state.isPollinatorInvalid,
                            supportingText = if (state.isPollinatorInvalid) {
                                { Text(stringResource(R.string.validation_select_pollinator)) }
                            } else null,
                            trailingIcon = {
                                if (state.pollinatorQuery.isNotBlank()) {
                                    IconButton(onClick = {
                                        actions.clearPollinator()
                                        showPollinatorDropdown = false
                                    }) {
                                        Icon(Icons.Filled.Clear, contentDescription = "Clear")
                                    }
                                }
                            },
                            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                            readOnly = state.selectedPollinatorId != null,
                            colors = if (state.selectedPollinatorId != null) {
                                OutlinedTextFieldDefaults.colors(
                                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                    disabledBorderColor = MaterialTheme.colorScheme.primary,
                                    disabledLabelColor = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                OutlinedTextFieldDefaults.colors()
                            },
                            singleLine = true
                        )

                        DropdownMenu(
                            expanded = showPollinatorDropdown && state.pollinatorSuggestions.isNotEmpty(),
                            onDismissRequest = { showPollinatorDropdown = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            state.pollinatorSuggestions.forEach { (id, name) ->
                                DropdownMenuItem(
                                    text = { Text(name) },
                                    onClick = {
                                        actions.selectPollinator(id, name)
                                        showPollinatorDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    // Plant
                    Box {
                        OutlinedTextField(
                            value = state.plantQuery,
                            onValueChange = {
                                if (state.selectedPlantId != null && it != state.selectedPlantName) {
                                    actions.clearPlant()
                                }
                                actions.onPlantQueryChange(it)
                                showPlantDropdown = it.isNotBlank() && state.selectedPlantId == null
                            },
                            label = { Text(stringResource(R.string.add_sighting_plant_hint)) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = state.pollinatorQuery.isBlank() && state.selectedPollinatorId == null,
                            isError = state.isPlantInvalid,
                            supportingText = if (state.isPlantInvalid) {
                                { Text(stringResource(R.string.validation_select_plant)) }
                            } else null,
                            trailingIcon = {
                                if (state.plantQuery.isNotBlank()) {
                                    IconButton(onClick = {
                                        actions.clearPlant()
                                        showPlantDropdown = false
                                    }) {
                                        Icon(Icons.Filled.Clear, contentDescription = "Clear")
                                    }
                                }
                            },
                            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                            readOnly = state.selectedPlantId != null,
                            colors = if (state.selectedPlantId != null) {
                                OutlinedTextFieldDefaults.colors(
                                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                    disabledBorderColor = MaterialTheme.colorScheme.primary,
                                    disabledLabelColor = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                OutlinedTextFieldDefaults.colors()
                            },
                            singleLine = true
                        )

                        DropdownMenu(
                            expanded = showPlantDropdown && state.plantSuggestions.isNotEmpty(),
                            onDismissRequest = { showPlantDropdown = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            state.plantSuggestions.take(5).forEach { suggestion ->
                                DropdownMenuItem(
                                    text = { Text(suggestion.second) },
                                    onClick = {
                                        actions.selectPlant(suggestion.first, suggestion.second)
                                        showPlantDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Submit button
            Button(
                onClick = { actions.submitSighting(context, userId) },
                enabled = !state.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        stringResource(R.string.add_sighting_submit),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            state.errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            if (state.isSuccess) {
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(2000)
                    navController.popBackStack()
                }
                Text(
                    stringResource(R.string.add_sighting_success),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            Spacer(Modifier.height(4.dp))
        }
    }

    // Image Picker Dialog
    if (showImagePicker) {
        ImagePickerDialog(
            onDismiss = { showImagePicker = false },
            onCameraClick = {
                cameraLauncher()
                showImagePicker = false
            },
            onGalleryClick = {
                galleryLauncher()
                showImagePicker = false
            }
        )
    }

    // Map Dialog
    if (showMapDialog) {
        LocationMapDialog(
            initialLatitude = state.latitude,
            initialLongitude = state.longitude,
            onLocationSelected = { lat, lng ->
                actions.setLocation(lat, lng)
            },
            onDismiss = { showMapDialog = false }
        )
    }

    // Date Picker Dialog
    if (showDatePicker) {
        MaterialDatePickerDialog(
            initialValue = state.date,
            onDateSelected = {
                actions.setDate(it)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }

    // Time Picker Dialog
    if (showTimePicker) {
        MaterialTimePickerDialog(
            initialValue = state.time,
            onTimeSelected = {
                actions.setTime(it)
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }

    // Location permission dialogs
    if (showLocationDisabledWarning) {
        AlertDialog(
            title = { Text(stringResource(R.string.gps_disabled_title)) },
            text = { Text(stringResource(R.string.gps_disabled_message)) },
            confirmButton = {
                TextButton(onClick = {
                    locationService.openLocationSettings()
                    showLocationDisabledWarning = false
                }) { Text(stringResource(R.string.enable)) }
            },
            dismissButton = {
                TextButton(onClick = { showLocationDisabledWarning = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            onDismissRequest = { showLocationDisabledWarning = false }
        )
    }

    if (showPermissionDeniedWarning) {
        AlertDialog(
            title = { Text(stringResource(R.string.location_permission_denied_title)) },
            text = { Text(stringResource(R.string.location_permission_denied_message)) },
            confirmButton = {
                TextButton(onClick = {
                    locationPermission.launchPermissionRequest()
                    showPermissionDeniedWarning = false
                }) { Text(stringResource(R.string.grant)) }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDeniedWarning = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            onDismissRequest = { showPermissionDeniedWarning = false }
        )
    }

    if (showPermissionPermanentlyDeniedWarning) {
        AlertDialog(
            title = { Text(stringResource(R.string.permission_required_title)) },
            text = { Text(stringResource(R.string.permission_permanently_denied_message)) },
            confirmButton = {
                TextButton(onClick = {
                    val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = android.net.Uri.fromParts("package", context.packageName, null)
                        flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    if (intent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(intent)
                    }
                    showPermissionPermanentlyDeniedWarning = false
                }) { Text(stringResource(R.string.settings)) }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionPermanentlyDeniedWarning = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            onDismissRequest = { showPermissionPermanentlyDeniedWarning = false }
        )
    }
}

@Composable
fun MaterialDatePickerDialog(
    initialValue: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

    val pickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialValue?.let {
            java.time.LocalDate.of(it.year, it.monthNumber, it.dayOfMonth)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        },
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val date = java.time.Instant.ofEpochMilli(utcTimeMillis)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                val kotlinDate = LocalDate(date.year, date.monthValue, date.dayOfMonth)
                return kotlinDate <= today
            }
        }
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val millis = pickerState.selectedDateMillis
                    if (millis != null) {
                        val date = java.time.Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        onDateSelected(LocalDate(date.year, date.monthValue, date.dayOfMonth))
                    }
                    onDismiss()
                }
            ) { Text("Ok") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    ) {
        DatePicker(state = pickerState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialTimePickerDialog(
    initialValue: LocalTime?,
    onTimeSelected: (LocalTime) -> Unit,
    onDismiss: () -> Unit
) {
    val initialHour = initialValue?.hour ?: 12
    val initialMinute = initialValue?.minute ?: 0
    val pickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = false
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    onTimeSelected(LocalTime(pickerState.hour, pickerState.minute))
                    onDismiss()
                }
            ) { Text("Ok") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
        text = {
            TimePicker(state = pickerState)
        }
    )
}