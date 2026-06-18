package com.sanay3y.egy.presentation.screens.provider

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sanay3y.egy.data.model.Request
import com.sanay3y.egy.data.model.RequestStatus
import com.sanay3y.egy.data.model.User
import com.sanay3y.egy.presentation.viewmodel.ProviderViewModel
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobDetailsScreen(
    requestId: String,
    onBack: () -> Unit,
    viewModel: ProviderViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(requestId) {
        viewModel.loadRequestDetails(requestId)
    }

    val request = uiState.selectedRequest
    val clientUser = uiState.clientUser

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Job Details", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Outlined.Notifications, contentDescription = "Notifications")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF7F8FA)
    ) { padding ->
        when {
            uiState.isLoading || request == null -> LoadingState()
            else -> JobDetailsContent(
                request = request,
                clientUser = clientUser,
                modifier = Modifier.padding(padding),
                onCallClient = {
                    val phone = clientUser?.phone ?: return@JobDetailsContent
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                    context.startActivity(intent)
                },
                onUpdateStatus = { status ->
                    viewModel.updateJobStatus(request.id, status)
                }
            )
        }
    }
}

@Composable
private fun JobDetailsContent(
    request: Request,
    clientUser: User?,
    modifier: Modifier = Modifier,
    onCallClient: () -> Unit,
    onUpdateStatus: (RequestStatus) -> Unit
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF7F8FA)),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        // ── Client Card ──────────────────────────────
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(3.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(
                                text = "CLIENT",
                                fontSize = 10.sp,
                                color = Color(0xFF6B7280),
                                letterSpacing = 1.sp
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = clientUser?.name ?: "Loading...",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFF1A1A2E)
                            )
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                repeat(4) {
                                    Icon(
                                        Icons.Filled.Star,
                                        contentDescription = null,
                                        tint = Color(0xFFF5A623),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Icon(
                                    Icons.Filled.StarHalf,
                                    contentDescription = null,
                                    tint = Color(0xFFF5A623),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text("4.9", fontSize = 12.sp, color = Color(0xFF6B7280))
                            }
                        }
                        RequestStatusChip(status = request.status)
                    }

                    Spacer(Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.LocationOn,
                            contentDescription = null,
                            tint = Color(0xFF1B8A5A),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = request.location,
                            fontSize = 13.sp,
                            color = Color(0xFF6B7280)
                        )
                    }
                }
            }
        }

        // ── Call Client Button ───────────────────────
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B8A5A)),
                elevation = CardDefaults.cardElevation(4.dp),
                onClick = onCallClient
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Phone,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Call Client",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        "Instant connection",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                }
            }
        }

        // ── Job Description ──────────────────────────
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(3.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.Description,
                            contentDescription = null,
                            tint = Color(0xFF1B8A5A),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Job Description",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF1A1A2E)
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = request.serviceType,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF1B8A5A)
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = request.description,
                        fontSize = 14.sp,
                        color = Color(0xFF4B5563),
                        lineHeight = 22.sp
                    )
                }
            }
        }

        // ── Status Timeline ──────────────────────────
        item {
            StatusTimelineCard(
                request = request,
                onUpdateStatus = onUpdateStatus
            )
        }
    }
}

@Composable
private fun StatusTimelineCard(
    request: Request,
    onUpdateStatus: (RequestStatus) -> Unit
) {
    data class TimelineStep(val status: String, val label: String, val subtitle: String)

    val steps = listOf(
        TimelineStep(RequestStatus.ACCEPTED.name, "Job Accepted", "Today, 09:45 AM"),
        TimelineStep(RequestStatus.IN_PROGRESS.name, "Work in Progress", "Completed at 11:30 AM"),
        TimelineStep(RequestStatus.COMPLETED_BY_PROVIDER.name, "Waiting for Client Confirmation", "Completion request sent"),
        TimelineStep(RequestStatus.COMPLETED_BY_CLIENT.name, "Finalized & Payment", "Last step after confirmation")
    )

    val currentIndex = steps.indexOfFirst { it.status == request.status }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Status Timeline",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color(0xFF1A1A2E)
            )
            Spacer(Modifier.height(16.dp))

            steps.forEachIndexed { index, step ->
                val isDone = index < currentIndex
                val isCurrent = index == currentIndex

                Row {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isDone || isCurrent -> Color(0xFF1B8A5A)
                                        else -> Color(0xFFE5E7EB)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            when {
                                isDone -> Icon(
                                    Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                isCurrent -> Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                )
                                else -> {}
                            }
                        }
                        if (index < steps.lastIndex) {
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(48.dp)
                                    .background(
                                        if (isDone) Color(0xFF1B8A5A) else Color(0xFFE5E7EB)
                                    )
                            )
                        }
                    }

                    Spacer(Modifier.width(12.dp))

                    Column(modifier = Modifier.padding(top = 2.dp)) {
                        Text(
                            text = step.label,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp,
                            color = if (isDone || isCurrent) Color(0xFF1A1A2E) else Color(0xFF9CA3AF)
                        )
                        Text(
                            text = step.subtitle,
                            fontSize = 12.sp,
                            color = Color(0xFF6B7280)
                        )

                        if (isCurrent && request.status == RequestStatus.COMPLETED_BY_PROVIDER.name) {
                            Spacer(Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFF0F9FF)
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Outlined.Info,
                                        contentDescription = null,
                                        tint = Color(0xFF0284C7),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        "The job is not yet finalized. Waiting for client to confirm the work.",
                                        fontSize = 11.sp,
                                        color = Color(0xFF0284C7),
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(if (index < steps.lastIndex) 8.dp else 0.dp))
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            val (buttonText, nextStatus) = when (request.status) {
                RequestStatus.ACCEPTED.name ->
                    "Start Work" to RequestStatus.IN_PROGRESS
                RequestStatus.IN_PROGRESS.name ->
                    "Request Completion" to RequestStatus.COMPLETED_BY_PROVIDER
                RequestStatus.COMPLETED_BY_PROVIDER.name ->
                    "Pending client confirmation" to null
                else -> null to null
            }

            buttonText?.let { text ->
                Button(
                    onClick = { nextStatus?.let { onUpdateStatus(it) } },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = nextStatus != null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1B8A5A),
                        disabledContainerColor = Color(0xFF6B7280)
                    )
                ) {
                    if (nextStatus == null) {
                        Icon(
                            Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun JobDetailsScreenPreview() {
    val fakeRequest = Request(
        id = "3",
        userId = "user3",
        providerId = "provider1",
        description = "Client requires a professional repaint of a 25sqm living room. Walls need minor crack filling and sanding. The client has already purchased the paint (Jotun Fanomastic, color: Eggshell White). Brushes, rollers, and protective sheets must be provided by the pro.",
        serviceType = "Full Living Room Repaint",
        status = RequestStatus.COMPLETED_BY_PROVIDER.name,
        estimatedPrice = 150.0,
        date = "Today",
        location = "124 Garden District, Apartment 4B, New Cairo, Egypt"
    )

    val fakeClient = User(
        id = "user3",
        firebaseUid = "user3",
        name = "Sarah Johnson",
        email = "sarah@email.com",
        phone = "+20 100 000 0000"
    )

    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text("Job Details", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    },
                    navigationIcon = {
                        IconButton(onClick = {}) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = {}) {
                            Icon(Icons.Outlined.Notifications, contentDescription = "Notifications")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
            },
            containerColor = Color(0xFFF7F8FA)
        ) { padding ->
            JobDetailsContent(
                request = fakeRequest,
                clientUser = fakeClient,
                modifier = Modifier.padding(padding),
                onCallClient = {},
                onUpdateStatus = {}
            )
        }
    }
}