package com.example.life4pollinators.ui.screens.addSighting

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.life4pollinators.utils.rememberCameraLauncher
import com.example.life4pollinators.utils.rememberGalleryLauncher
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

    var errorMessage by remember { mutableStateOf<String?>(null) }

    val cameraLauncher = rememberCameraLauncher(
        onPhotoReady = { uri ->
            actions.setImageUri(uri)
            errorMessage = null
        },
        onError = { resId ->
            errorMessage = context.getString(resId)
        }
    )

    val galleryLauncher = rememberGalleryLauncher { uri ->
        actions.setImageUri(uri)
        errorMessage = null
    }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    // Per gestire il dropdown dei suggerimenti
    var showPollinatorDropdown by remember { mutableStateOf(false) }
    var showPlantDropdown by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { AppBar(navController) },
        bottomBar = {
            BottomNavBar(
                isAuthenticated = isAuthenticated,
                selectedTab = NavBarTab.None,
                navController = navController
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                // Header
                Text(
                    text = stringResource(R.string.add_sighting_header),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "Contribute by sending us an image of a pollinator on a plant:",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Image selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { cameraLauncher() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.add_sighting_take_photo))
                    }

                    Text(
                        text = "or",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )

                    OutlinedButton(
                        onClick = { galleryLauncher() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Image,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.add_sighting_choose_picture))
                    }
                }

                // Preview immagine
                if (state.imageUri != null) {
                    Spacer(Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
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
                }

                errorMessage?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(text = it, color = MaterialTheme.colorScheme.error)
                }

                Spacer(Modifier.height(24.dp))

                // Date
                OutlinedTextField(
                    value = state.date?.toString() ?: "",
                    onValueChange = {},
                    label = { Text("Date") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(
                                imageVector = Icons.Outlined.CalendarToday,
                                contentDescription = "Select date"
                            )
                        }
                    },
                    placeholder = { Text("MM/DD/YYYY") }
                )

                Spacer(Modifier.height(16.dp))

                // Time
                OutlinedTextField(
                    value = state.time?.toString() ?: "",
                    onValueChange = {},
                    label = { Text("Enter time") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { showTimePicker = true }) {
                            Icon(
                                imageVector = Icons.Outlined.Schedule,
                                contentDescription = "Select time"
                            )
                        }
                    }
                )

                Spacer(Modifier.height(24.dp))

                // Location
                Text(
                    text = "Location:",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Map placeholder (puoi implementare Google Maps qui)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Map placeholder")
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            // Usa coordinate fittizie per ora
                            val lat = 45.4642
                            val lng = 9.19
                            actions.setLocation(lat, lng)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.add_sighting_current_location))
                    }

                    Text(
                        text = "or",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )

                    OutlinedButton(
                        onClick = {
                            val lat = 45.4642
                            val lng = 9.19
                            actions.setLocation(lat, lng)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.add_sighting_choose_location))
                    }
                }

                if (state.latitude != null && state.longitude != null) {
                    Text(
                        text = stringResource(R.string.add_sighting_lat_lng, state.latitude, state.longitude),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(Modifier.height(24.dp))

                // Pollinator selection
                Box {
                    OutlinedTextField(
                        value = state.pollinatorQuery,
                        onValueChange = {
                            // Se c'era una selezione precedente, la rimuoviamo quando l'utente modifica
                            if (state.selectedPollinatorId != null && it != state.selectedPollinatorName) {
                                actions.clearPollinator()
                            }
                            actions.onPollinatorQueryChange(it)
                            showPollinatorDropdown = it.isNotBlank() && state.selectedPollinatorId == null
                        },
                        label = { Text(stringResource(R.string.add_sighting_pollinator_hint)) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = state.selectedPlantId == null,
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
                        }
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

                Spacer(Modifier.height(16.dp))

                // Plant selection
                Box {
                    OutlinedTextField(
                        value = state.plantQuery,
                        onValueChange = {
                            actions.onPlantQueryChange(it)
                            showPlantDropdown = it.isNotBlank()
                        },
                        label = { Text(stringResource(R.string.add_sighting_plant_hint)) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = state.pollinatorQuery.isBlank() && state.selectedPollinatorId == null,
                        trailingIcon = {
                            if (state.plantQuery.isNotBlank()) {
                                IconButton(onClick = {
                                    actions.onPlantQueryChange("")
                                    showPlantDropdown = false
                                }) {
                                    Icon(Icons.Filled.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done)
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

                Spacer(Modifier.height(32.dp))

                // Submit button
                Button(
                    onClick = { actions.submitSighting(context, userId) },
                    enabled = !state.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(stringResource(R.string.add_sighting_submit))
                }

                if (state.isLoading) {
                    Spacer(Modifier.height(16.dp))
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }

                state.errorMessage?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(text = it, color = MaterialTheme.colorScheme.error)
                }

                if (state.isSuccess) {
                    LaunchedEffect(Unit) {
                        kotlinx.coroutines.delay(2000)
                        navController.popBackStack()
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.add_sighting_success),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(Modifier.height(32.dp))
            }
        }
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
        // Limita le date selezionabili fino ad oggi
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
            ) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
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
            ) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        text = {
            TimePicker(state = pickerState)
        }
    )
}