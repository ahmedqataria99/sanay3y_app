package com.sanay3y.egy.presentation.screens.client

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanay3y.egy.R
import com.sanay3y.egy.data.model.Request
import com.sanay3y.egy.data.model.RequestStatus
import com.sanay3y.egy.presentation.viewmodel.ClientViewModel
import com.sanay3y.egy.presentation.viewmodel.RequestViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyJobsScreen(
    userId: String,
    clientViewModel: ClientViewModel,
    requestViewModel: RequestViewModel,
    onViewQuotation: (String) -> Unit = {},
    onRateProvider: (String, String) -> Unit = { _, _ -> },
    onTrackOrder: (String) -> Unit = {}
) {
    val uiState by requestViewModel.uiState.collectAsState()
    var selectedIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Active Jobs", "History")

    LaunchedEffect(userId) {
        requestViewModel.loadActiveRequests(userId)
        requestViewModel.loadCompletedRequests(userId)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "My Jobs",
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
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Custom Tab Switcher
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    tabs.forEachIndexed { index, title ->
                        val isSelected = selectedIndex == index
                        val backgroundColor by animateColorAsState(
                            targetValue = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent
                        )
                        val textColor by animateColorAsState(
                            targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(backgroundColor, RoundedCornerShape(12.dp))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    selectedIndex = index
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                color = textColor,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Content List
            Box(modifier = Modifier.fillMaxSize()) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    val requests = if (selectedIndex == 0) uiState.activeRequests else uiState.completedRequests

                    if (requests.isEmpty()) {
                        EmptyJobsState(
                            title = if (selectedIndex == 0) "No Active Jobs" else "No Job History",
                            description = if (selectedIndex == 0)
                                "You don't have any ongoing service requests at the moment."
                            else "Your completed jobs will appear here."
                        )
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            requests.forEach { request ->
                                JobCard(
                                    request = request,
                                    requestViewModel = requestViewModel,
                                    clientViewModel = clientViewModel,
                                    onViewQuotation = onViewQuotation,
                                    onRateProvider = onRateProvider,
                                    onTrackOrder = onTrackOrder
                                )
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun JobCard(
    request: Request,
    requestViewModel: RequestViewModel,
    clientViewModel: ClientViewModel,
    onViewQuotation: (String) -> Unit = {},
    onRateProvider: (String, String) -> Unit = { _, _ -> },
    onTrackOrder: (String) -> Unit = {}
) {
    var providerName by remember { mutableStateOf("Loading...") }
    var providerCategory by remember { mutableStateOf("") }

    LaunchedEffect(request.providerId) {
        val provider = clientViewModel.getProviderById(request.providerId)
        providerName = provider?.name ?: "Unknown Provider"
        providerCategory = provider?.category ?: "Unknown Category"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(R.drawable.profile_image),
                    contentDescription = null,
                    modifier = Modifier
                        .size(52.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = providerName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = providerCategory,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                StatusBadge(status = request.status)
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Estimated Cost",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "E£ ${request.totalPrice}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                // ✅ لوجيك الأزرار حسب حالة الطلب
                when (request.status) {
                    RequestStatus.COMPLETED_BY_PROVIDER.name -> {
                        Button(
                            onClick = { requestViewModel.confirmJob(request.id) },
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 0.dp),
                            modifier = Modifier.height(40.dp)
                        ) {
                            Text("Confirm & Pay", fontSize = 13.sp)
                        }
                    }
                    RequestStatus.QUOTED.name -> {
                        Button(
                            onClick = { onViewQuotation(request.id) },
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 0.dp),
                            modifier = Modifier.height(40.dp)
                        ) {
                            Text("Review Quote", fontSize = 13.sp)
                        }
                    }
                    RequestStatus.COMPLETED_BY_CLIENT.name -> {
                        OutlinedButton(
                            onClick = { onRateProvider(request.id, request.providerId) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(40.dp)
                        ) {
                            Text("Rate Provider", fontSize = 13.sp)
                        }
                    }
                    RequestStatus.ACCEPTED.name, RequestStatus.IN_PROGRESS.name -> {
                        OutlinedButton(
                            onClick = { onTrackOrder(request.id) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(40.dp)
                        ) {
                            Text("Track Order", fontSize = 13.sp)
                        }
                    }
                    else -> Unit // مفيش زرار للحالات التانية
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val color = when (status) {
        RequestStatus.PENDING.name -> MaterialTheme.colorScheme.primary
        RequestStatus.QUOTED.name -> Color(0xFF2196F3) // أزرق - محتاج رد منك
        RequestStatus.ACCEPTED.name -> MaterialTheme.colorScheme.primary
        RequestStatus.IN_PROGRESS.name -> Color(0xFF7B1FA2) // بنفسجي - شغال دلوقتي
        RequestStatus.COMPLETED_BY_PROVIDER.name -> Color(0xFFF59E0B) // برتقالي - محتاج تأكيدك
        RequestStatus.COMPLETED_BY_CLIENT.name -> Color(0xFF16A34A) // أخضر - خلص
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val label = when (status) {
        RequestStatus.PENDING.name -> "PENDING"
        RequestStatus.QUOTED.name -> "QUOTED"
        RequestStatus.ACCEPTED.name -> "ACCEPTED"
        RequestStatus.IN_PROGRESS.name -> "IN PROGRESS"
        RequestStatus.COMPLETED_BY_PROVIDER.name -> "ACTION REQUIRED"
        RequestStatus.COMPLETED_BY_CLIENT.name -> "COMPLETED"
        else -> status
    }

    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = label,
            color = color,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun EmptyJobsState(title: String, description: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Build,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(start = 40.dp, end = 40.dp, top = 4.dp)
        )
    }
}