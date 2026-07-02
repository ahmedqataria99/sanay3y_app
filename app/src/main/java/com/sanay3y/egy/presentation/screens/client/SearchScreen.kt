package com.sanay3y.egy.presentation.screens.client

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sanay3y.egy.R
import com.sanay3y.egy.data.model.Provider
import com.sanay3y.egy.presentation.screens.LocationSelectionDialog
import com.sanay3y.egy.presentation.viewmodel.ClientViewModel
import com.sanay3y.egy.presentation.viewmodel.ProviderWithDistance

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: ClientViewModel = viewModel(),
    userId: String = "",
    category: String = "",
    onNavigateToDetails: (String) -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()
    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    var selectedRating by remember { mutableStateOf(false) }
    var selectedNearby by remember { mutableStateOf(false) }
    var sortMenuExpanded by remember { mutableStateOf(false) }
    var showLocationDialog by remember { mutableStateOf(false) }

    // Location Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        viewModel.updateLocationPermission(granted)
        if (granted) {
            viewModel.loadNearbyProviders(context, userId)
        }
    }

    // Snackbar للـ service area
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.isInServiceArea) {
        if (!uiState.isInServiceArea && uiState.userLocation != null) {
            snackbarHostState.showSnackbar(
                message = "Service not available in your area yet. Coming soon!",
                duration = SnackbarDuration.Long
            )
        }
    }

    LaunchedEffect(category) {
        if (category.isNotBlank()) {
            viewModel.filterByCategory(category)
        } else {
            viewModel.loadProviders()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Providers",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    selectedNearby = false
                    if (it.isBlank()) {
                        if (category.isNotBlank()) viewModel.filterByCategory(category)
                        else viewModel.loadProviders()
                    } else {
                        viewModel.search(it)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text("Search for experts...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                singleLine = true
            )

            // Filters
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box {
                    FilterChip(
                        selected = false,
                        onClick = { sortMenuExpanded = true },
                        label = { Text("Sort: ${uiState.sortBy.name.lowercase().replaceFirstChar { it.uppercase() }}") },
                        leadingIcon = {
                            Icon(painterResource(R.drawable.filter), null, modifier = Modifier.size(18.dp))
                        },
                        shape = CircleShape
                    )
                    DropdownMenu(
                        expanded = sortMenuExpanded,
                        onDismissRequest = { sortMenuExpanded = false }
                    ) {
                        com.sanay3y.egy.presentation.viewmodel.SortOption.entries.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                onClick = {
                                    viewModel.setSortOption(option)
                                    sortMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                FilterChip(
                    selected = false,
                    onClick = { showSheet = true },
                    label = { Text("Category") },
                    shape = CircleShape
                )

                FilterChip(
                    selected = false, // We'll improve this later
                    onClick = { showLocationDialog = true },
                    label = { Text("Location") },
                    leadingIcon = {
                        Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(18.dp))
                    },
                    shape = CircleShape
                )

                FilterChip(
                    selected = selectedRating,
                    onClick = {
                        selectedRating = !selectedRating
                        selectedNearby = false
                        if (selectedRating) viewModel.loadTopRated()
                        else viewModel.loadProviders()
                    },
                    label = { Text("Top Rated") },
                    leadingIcon = {
                        Icon(painterResource(R.drawable.rating), null, modifier = Modifier.size(18.dp))
                    },
                    shape = CircleShape
                )

                // ← Nearby Filter جديد
                FilterChip(
                    selected = selectedNearby,
                    onClick = {
                        selectedNearby = !selectedNearby
                        selectedRating = false
                        if (selectedNearby) {
                            permissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                            // Call with userId to allow profile location fallback
                            viewModel.loadNearbyProviders(context, userId)
                        } else {
                            viewModel.loadProviders()
                        }
                    },
                    label = { Text("Nearby") },
                    leadingIcon = {
                        Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(18.dp))
                    },
                    shape = CircleShape
                )

                if (searchQuery.isNotEmpty() || selectedRating || selectedNearby) {
                    FilterChip(
                        selected = false,
                        onClick = {
                            searchQuery = ""
                            selectedRating = false
                            selectedNearby = false
                            if (category.isNotBlank()) viewModel.filterByCategory(category)
                            else viewModel.loadProviders()
                        },
                        label = { Text("Reset") },
                        shape = CircleShape
                    )
                }
            }

            // Results Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = when {
                            selectedNearby -> "Nearest Experts"
                            searchQuery.isNotEmpty() -> "Search Results"
                            else -> "Available Experts"
                        },
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    // اسم المنطقة
                    if (selectedNearby && uiState.districtName != null) {
                        Text(
                            text = "📍 ${uiState.districtName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Text(
                    text = if (selectedNearby)
                        "${uiState.nearbyProviders.size} nearby providers"
                    else
                        "${uiState.providers.size} found",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Results Content
            // Results Content  ← سطر 250 خليه زي ما هو
            // Results Content
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(top = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (selectedNearby && uiState.isInServiceArea) {
                if (uiState.nearbyProviders.isEmpty()) {
                    EmptySearchState()
                } else {
                    Column {
                        uiState.nearbyProviders.forEach { providerWithDistance ->
                            ProviderCard(
                                provider = providerWithDistance.provider,
                                distanceText = providerWithDistance.formattedDistance,
                                onCardClick = {
                                    viewModel.selectProvider(providerWithDistance.provider)
                                    onNavigateToDetails(providerWithDistance.provider.id)
                                }
                            )
                        }
                    }
                }
            } else if (uiState.providers.isEmpty()) {
                EmptySearchState()
            } else {
                Column {
                    uiState.providers.forEach { provider ->
                        ProviderCard(
                            provider = provider,
                            distanceText = null,
                            onCardClick = {
                                viewModel.selectProvider(provider)
                                onNavigateToDetails(provider.id)
                            }
                        )
                    }
                }
            }
        }

        if (showLocationDialog) {
            LocationSelectionDialog(
                currentGov = "",
                currentDist = "",
                governorates = listOf("القاهرة"),
                districtsMap = mapOf("القاهرة" to listOf("شبرا", "مدينة نصر", "التجمع الخامس", "العاصمة الإدارية", "مصر الجديدة", "المعادي", "الزمالك", "وسط البلد", "المقطم", "الرحاب", "الشروق", "عين شمس", "حلوان", "المرج", "السلام", "النزهة", "بدر", "البساتين", "دار السلام", "حدائق القبة", "الزاوية الحمراء", "الزيتون", "روض الفرج", "الساحل")),
                onDismiss = { showLocationDialog = false },
                onSave = { gov, dist ->
                    viewModel.filterByLocation(gov, dist)
                    showLocationDialog = false
                }
            )
        }

        if (showSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSheet = false },
                sheetState = sheetState,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                FilterSheetContent(
                    onClose = { showSheet = false },
                    onCategorySelected = { cat ->
                        viewModel.filterByCategory(cat)
                        showSheet = false
                    }
                )
            }
        }
    }
}

// ── Provider Card — بيعرض المسافة لو موجودة ──────────
@Composable
fun ProviderCard(
    provider: Provider,
    distanceText: String? = null,
    onCardClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        onClick = onCardClick
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                Image(
                    painter = painterResource(R.drawable.profile_image),
                    contentDescription = null,
                    modifier = Modifier.size(84.dp).clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(bottomStart = 8.dp, topEnd = 16.dp),
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painterResource(R.drawable.rating), null,
                            tint = Color.White, modifier = Modifier.size(10.dp)
                        )
                        Spacer(Modifier.width(2.dp))
                        Text(
                            provider.rating.toString(),
                            color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    provider.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    provider.category,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "${provider.experienceYears} Years Experience",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // ← المسافة لو موجودة
                if (distanceText != null) {
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(2.dp))
                        Text(
                            distanceText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onCardClick,
                    modifier = Modifier.height(36.dp),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                ) {
                    Text("View Profile", fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun FilterSheetContent(onClose: () -> Unit, onCategorySelected: (String) -> Unit) {
    val categories = remember {
        listOf("Plumbing", "Electrical", "Cleaning", "Carpentry", "Painting", "AC Repair")
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text(
            "Select Category",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 20.dp)
        )
        categories.forEach { category ->
            Surface(
                onClick = { onCategorySelected(category) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 16.dp, horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = category, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    Icon(painter = painterResource(id = android.R.drawable.ic_media_play), contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun EmptySearchState() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("No Experts Found", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            "Try searching for something else or reset filters",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(start = 40.dp, end = 40.dp, top = 4.dp)
        )
    }
}