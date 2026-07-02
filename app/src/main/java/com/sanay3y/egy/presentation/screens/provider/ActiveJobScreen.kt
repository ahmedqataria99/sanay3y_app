package com.sanay3y.egy.presentation.screens.provider

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.res.stringResource
import com.sanay3y.egy.R
import com.sanay3y.egy.data.model.RequestStatus
import com.sanay3y.egy.presentation.viewmodel.JobTrackingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveJobScreen(
    requestId: String,
    userId: String = "",
    viewModel: JobTrackingViewModel = viewModel(),
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val currentRequest by viewModel.currentRequest.collectAsStateWithLifecycle()
    val otherPartyName by viewModel.otherPartyName.collectAsStateWithLifecycle()
    val otherPartyPhone by viewModel.otherPartyPhone.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    LaunchedEffect(requestId) {
        viewModel.observeRequest(requestId, userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.job_details), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cancel))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (currentRequest == null && isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (currentRequest == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.job_not_found), style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            val request = currentRequest!!
            val isProvider = userId == request.providerId
            val otherPartyRole = if (isProvider) stringResource(R.string.customer) else stringResource(R.string.service_provider)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = request.serviceType,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.LocationOn, 
                                contentDescription = null, 
                                tint = MaterialTheme.colorScheme.onSurfaceVariant, 
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = request.location, 
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        Text(
                            text = stringResource(R.string.currency_symbol) + " ${request.totalPrice}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(if (isProvider) "👤" else "👷", fontSize = 24.sp)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                otherPartyRole, 
                                style = MaterialTheme.typography.labelMedium, 
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                otherPartyName, 
                                style = MaterialTheme.typography.titleMedium, 
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Surface(
                            modifier = Modifier.size(48.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            onClick = { 
                                if (otherPartyPhone.isNotBlank()) {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$otherPartyPhone"))
                                    context.startActivity(intent)
                                }
                            }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Phone, 
                                    contentDescription = null, 
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                Column(modifier = Modifier.padding(horizontal = 4.dp)) {
                    Text(
                        stringResource(R.string.job_notes), 
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (request.description.isBlank()) stringResource(R.string.no_job_notes) else request.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                }

                // Status Timeline
                JobStatusTimeline(currentStatus = when (request.status) {
                    RequestStatus.PENDING.name -> RequestStatus.PENDING
                    RequestStatus.ACCEPTED.name -> RequestStatus.ACCEPTED
                    RequestStatus.IN_PROGRESS.name -> RequestStatus.IN_PROGRESS
                    RequestStatus.COMPLETED_BY_PROVIDER.name -> RequestStatus.COMPLETED_BY_PROVIDER
                    RequestStatus.COMPLETED_BY_CLIENT.name -> RequestStatus.COMPLETED_BY_CLIENT
                    else -> RequestStatus.PENDING
                })

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons based on Status
                if (isProvider) {
                    when (request.status) {
                        RequestStatus.ACCEPTED.name -> {
                            Button(
                                onClick = { viewModel.startJob(request.id) },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                enabled = isLoading == false
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(stringResource(R.string.start_job), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                            }
                        }
                        RequestStatus.IN_PROGRESS.name -> {
                            Button(
                                onClick = { viewModel.completeJob(request.id) },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                enabled = isLoading == false
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(stringResource(R.string.mark_completed), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                            }
                        }
                        RequestStatus.COMPLETED_BY_PROVIDER.name -> {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(
                                    stringResource(R.string.waiting_confirmation),
                                    modifier = Modifier.padding(20.dp),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        RequestStatus.COMPLETED_BY_CLIENT.name -> {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = Color(0xFFE8F5E9),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(
                                    stringResource(R.string.job_finished_confirmed),
                                    modifier = Modifier.padding(20.dp),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                        }
                    }
                } else {
                    // Client View
                    when (request.status) {
                        RequestStatus.ACCEPTED.name -> {
                             Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(
                                    stringResource(R.string.provider_accepted_wait),
                                    modifier = Modifier.padding(20.dp),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        RequestStatus.IN_PROGRESS.name -> {
                             Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(
                                    stringResource(R.string.service_in_progress),
                                    modifier = Modifier.padding(20.dp),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        RequestStatus.COMPLETED_BY_PROVIDER.name -> {
                            Button(
                                onClick = { viewModel.confirmJob(request.id) },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                enabled = isLoading == false
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(stringResource(R.string.confirm_completion), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                            }
                        }
                        RequestStatus.COMPLETED_BY_CLIENT.name -> {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = Color(0xFFE8F5E9),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(
                                    stringResource(R.string.job_completed_confirmed),
                                    modifier = Modifier.padding(20.dp),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun JobStatusTimeline(currentStatus: RequestStatus) {
    val steps = listOf(
        Triple(stringResource(R.string.pending), stringResource(R.string.request_submitted), RequestStatus.PENDING),
        Triple(stringResource(R.string.accepted), stringResource(R.string.provider_accepted), RequestStatus.ACCEPTED),
        Triple(stringResource(R.string.job_in_progress), stringResource(R.string.working_on_site), RequestStatus.IN_PROGRESS),
        Triple(stringResource(R.string.finished), stringResource(R.string.waiting_confirmation), RequestStatus.COMPLETED_BY_PROVIDER),
        Triple(stringResource(R.string.confirmed), stringResource(R.string.job_completed_success), RequestStatus.COMPLETED_BY_CLIENT)
    )

    val currentIndex = RequestStatus.entries.indexOf(currentStatus)

    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(stringResource(R.string.job_progress), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(16.dp))
            
            steps.forEachIndexed { i, (title, subtitle, _) ->
                val isDone = i <= currentIndex
                val isCurrent = i == currentIndex

                Row(verticalAlignment = Alignment.Top) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(24.dp)
                    ) {
                        Box(
                            Modifier
                                .size(24.dp)
                                .background(if (isDone) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isDone) Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(14.dp))
                            else Box(Modifier.size(8.dp).background(MaterialTheme.colorScheme.outline, CircleShape))
                        }
                        if (i < steps.size - 1)
                            Box(Modifier.width(2.dp).height(36.dp).background(if (isDone) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant))
                    }

                    Spacer(Modifier.width(16.dp))

                    Column {
                        Text(
                            title,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                            color = if (isDone) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            subtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isDone) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.outline
                        )
                        Spacer(Modifier.height(if (i < steps.size - 1) 16.dp else 0.dp))
                    }
                }
            }
        }
    }
}
