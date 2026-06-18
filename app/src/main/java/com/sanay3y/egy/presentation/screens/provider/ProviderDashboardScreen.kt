package com.sanay3y.egy.presentation.screens.provider

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sanay3y.egy.data.model.Request
import com.sanay3y.egy.presentation.viewmodel.ProviderViewModel
import com.sanay3y.egy.ui.theme.Primary

@Composable
fun ProviderDashboardScreen(
    providerId: String = "",
    viewModel: ProviderViewModel = viewModel(),
    onNavigateToRequestDetails: (String) -> Unit = {},
    onNavigateToNotifications: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(providerId) {
        viewModel.loadAvailableRequests()
        if (providerId.isNotEmpty()) {
            viewModel.loadActiveJobs(providerId)
            viewModel.loadCompletedJobs(providerId)
        }
    }

    Scaffold(
        topBar = { DashboardHeader(onNotificationsClick = onNavigateToNotifications) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            DashboardStats(
                totalJobs = uiState.activeJobs.size + uiState.completedJobs.size,
                rating = "4.9",
                successRate = "98.5%",
                responseTime = "< 15m"
            )

            Spacer(modifier = Modifier.height(16.dp))

            DashboardTabs(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (selectedTab == 0) {
                        items(uiState.availableRequests, key = { it.id }) { request ->
                            AvailableRequestCard(
                                request = request,
                                onAccept = {
                                    if (providerId.isNotEmpty()) {
                                        viewModel.acceptRequest(request.id, providerId)
                                    }
                                }
                            )
                        }
                    } else {
                        items(uiState.activeJobs, key = { it.id }) { request ->
                            ActiveJobCard(
                                request = request,
                                onClick = { onNavigateToRequestDetails(request.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardHeader(onNotificationsClick: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = "Sanay3y",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            Box(
                modifier = Modifier
                    .padding(start = 16.dp, end = 12.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
            )
        },
        actions = {
            AssistChip(
                onClick = {},
                label = { Text("ONLINE", color = Color.White) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = Color(0xFF4CAF50)
                ),
                border = null,
                modifier = Modifier.padding(end = 8.dp)
            )
            IconButton(onClick = onNotificationsClick) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "Notifications"
                )
            }
        }
    )
}

@Composable
private fun DashboardStats(
    totalJobs: Int,
    rating: String,
    successRate: String,
    responseTime: String
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard(
                title = "TOTAL JOBS",
                value = totalJobs.toString(),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "RATING",
                value = rating,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Primary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Performance Summary", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.labelMedium)
                    Text(successRate, color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Response Time", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.labelMedium)
                    Text(responseTime, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun DashboardTabs(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        TabItem("Available Requests", selectedTab == 0) { onTabSelected(0) }
        Spacer(modifier = Modifier.width(24.dp))
        TabItem("My Active Jobs", selectedTab == 1) { onTabSelected(1) }
    }
}

@Composable
private fun TabItem(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = text,
            modifier = Modifier.clickable { onClick() },
            color = if (isSelected) Primary else Color.Gray,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
        if (isSelected) {
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .height(2.dp)
                    .width(40.dp)
                    .background(Primary)
            )
        }
    }
}

@Composable
private fun AvailableRequestCard(request: Request, onAccept: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = request.serviceType, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.weight(1f))
                Text(text = request.date, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = request.location, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)

            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("ESTIMATED PRICE", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text("EGP ${request.estimatedPrice}", style = MaterialTheme.typography.titleMedium, color = Primary, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onAccept,
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Accept")
                }
            }
        }
    }
}

@Composable
private fun ActiveJobCard(request: Request, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = request.serviceType, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(text = request.location, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Status: ${request.status.replace("_", " ")}",
                color = if (request.status == "IN_PROGRESS") Primary else Color.Gray,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}
