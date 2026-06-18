package com.sanay3y.egy.presentation.screens.provider


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sanay3y.egy.presentation.viewmodel.ProviderViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import com.sanay3y.egy.data.model.Request
import com.sanay3y.egy.data.model.RequestStatus

@Composable
fun JobBoardScreen(
    onNavigateToDetails: (String) -> Unit,
    viewModel: ProviderViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage, uiState.actionSuccess) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
        uiState.actionSuccess?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFFF7F8FA)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Header ──────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Job Board",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A2E)
                )
                Text(
                    text = "Manage your service requests and active projects.",
                    fontSize = 13.sp,
                    color = Color(0xFF6B7280)
                )
            }

            // ── Tabs ─────────────────────────────────────────
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = Color(0xFF1B8A5A),
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Color(0xFF1B8A5A),
                        height = 3.dp
                    )
                },
                divider = {}
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            "Available Requests",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp,
                            color = if (selectedTab == 0) Color(0xFF1B8A5A) else Color(0xFF6B7280)
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            "My Active Jobs",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp,
                            color = if (selectedTab == 1) Color(0xFF1B8A5A) else Color(0xFF6B7280)
                        )
                    }
                )
            }

            // ── Content ──────────────────────────────────────
            when (selectedTab) {
                0 -> AvailableRequestsScreen(
                    viewModel = viewModel,
                    onNavigateToDetails = onNavigateToDetails
                )
                1 -> ActiveJobScreen(
                    viewModel = viewModel,
                    onNavigateToDetails = onNavigateToDetails
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun JobBoardScreenPreview() {
    val fakeRequests = listOf(
        Request(
            id = "1",
            userId = "user1",
            providerId = "",
            description = "Main kitchen faucet is leaking from the base when turned on. Requires immediate attention.",
            serviceType = "Leaking Kitchen Faucet",
            status = RequestStatus.PENDING.name,
            estimatedPrice = 80.0,
            date = "Today, 09:00 AM",
            location = "Nasr City, Cairo"
        ),
        Request(
            id = "2",
            userId = "user2",
            providerId = "",
            description = "Need to upgrade the main circuit breaker panel to support new AC units in the bedrooms.",
            serviceType = "Circuit Breaker Upgrade",
            status = RequestStatus.PENDING.name,
            estimatedPrice = 150.0,
            date = "Today, 11:30 AM",
            location = "Maadi, Cairo"
        )
    )

    val fakeActiveJobs = listOf(
        Request(
            id = "3",
            userId = "user3",
            providerId = "provider1",
            description = "Grouting and sealing the floor tiles.",
            serviceType = "Full Bathroom Retiling",
            status = RequestStatus.IN_PROGRESS.name,
            estimatedPrice = 200.0,
            date = "2 days ago",
            location = "New Cairo"
        )
    )

    MaterialTheme {
        var selectedTab by remember { mutableIntStateOf(0) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF7F8FA))
        ) {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Job Board",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A2E)
                )
                Text(
                    text = "Manage your service requests and active projects.",
                    fontSize = 13.sp,
                    color = Color(0xFF6B7280)
                )
            }

            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = Color(0xFF1B8A5A),
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Color(0xFF1B8A5A),
                        height = 3.dp
                    )
                },
                divider = {}
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            "Available Requests",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp,
                            color = if (selectedTab == 0) Color(0xFF1B8A5A) else Color(0xFF6B7280)
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            "My Active Jobs",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp,
                            color = if (selectedTab == 1) Color(0xFF1B8A5A) else Color(0xFF6B7280)
                        )
                    }
                )
            }

            // Content
            when (selectedTab) {
                0 -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(fakeRequests) { request ->
                        AvailableRequestCard(
                            request = request,
                            onAccept = {},
                            onReject = {}
                        )
                    }
                }
                1 -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(fakeActiveJobs) { request ->
                        ActiveJobCard(
                            request = request,
                            onViewDetails = {}
                        )
                    }
                }
            }
        }
    }
}