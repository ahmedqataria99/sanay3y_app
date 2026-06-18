package com.sanay3y.egy.presentation.screens.provider

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.WorkOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sanay3y.egy.data.model.Request
import com.sanay3y.egy.data.model.RequestStatus
import com.sanay3y.egy.presentation.viewmodel.ProviderViewModel

@Composable
fun ActiveJobScreen(
    viewModel: ProviderViewModel,
    onNavigateToDetails: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when {
        uiState.isLoading -> LoadingState()
        uiState.activeJobs.isEmpty() -> EmptyActiveJobsState()
        else -> LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(uiState.activeJobs, key = { it.id }) { request ->
                ActiveJobCard(
                    request = request,
                    onViewDetails = { onNavigateToDetails(request.id) }
                )
            }
        }
    }
}

@Composable
internal fun ActiveJobCard(
    request: Request,
    onViewDetails: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(3.dp),
        border = BorderStroke(1.dp, Color(0xFF1B8A5A).copy(alpha = 0.25f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Status Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RequestStatusChip(status = request.status)
                Text(
                    text = request.date,
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280)
                )
            }

            Spacer(Modifier.height(10.dp))

            // Service Type
            Text(
                text = request.serviceType,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF1A1A2E)
            )

            Spacer(Modifier.height(6.dp))

            // Description
            Text(
                text = request.description,
                fontSize = 12.sp,
                color = Color(0xFF6B7280),
                lineHeight = 18.sp
            )

            Spacer(Modifier.height(10.dp))

            // Progress indicator based on status
            val progress = when (request.status) {
                RequestStatus.ACCEPTED.name -> 0.25f
                RequestStatus.IN_PROGRESS.name -> 0.65f
                RequestStatus.COMPLETED_BY_PROVIDER.name -> 0.85f
                RequestStatus.COMPLETED_BY_CLIENT.name -> 1f
                else -> 0f
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = Color(0xFF1B8A5A),
                trackColor = Color(0xFFE8F5EF)
            )

            Spacer(Modifier.height(12.dp))

            // Location
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = Color(0xFF1B8A5A),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(request.location, fontSize = 12.sp, color = Color(0xFF6B7280))
            }

            Spacer(Modifier.height(14.dp))

            // View Details Button
            Button(
                onClick = onViewDetails,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B8A5A))
            ) {
                Text("View Job Details", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun RequestStatusChip(status: String) {
    val (label, bg, textColor) = when (status) {
        RequestStatus.PENDING.name ->
            Triple("Pending", Color(0xFFF3F4F6), Color(0xFF6B7280))
        RequestStatus.ACCEPTED.name ->
            Triple("Active Now", Color(0xFF1B8A5A), Color.White)
        RequestStatus.IN_PROGRESS.name ->
            Triple("In Progress", Color(0xFFCCE5FF), Color(0xFF004085))
        RequestStatus.COMPLETED_BY_PROVIDER.name ->
            Triple("Waiting Confirmation", Color(0xFFFFF3CD), Color(0xFF856404))
        RequestStatus.COMPLETED_BY_CLIENT.name ->
            Triple("Completed", Color(0xFFD4EDDA), Color(0xFF155724))
        else -> Triple("Unknown", Color(0xFFF3F4F6), Color(0xFF6B7280))
    }
    Surface(shape = RoundedCornerShape(20.dp), color = bg) {
        Text(
            label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun EmptyActiveJobsState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.WorkOff,
            contentDescription = null,
            tint = Color(0xFF9CA3AF),
            modifier = Modifier.size(72.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text("No active jobs yet", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1A1A2E))
        Spacer(Modifier.height(6.dp))
        Text("Finish your active jobs to build your history.", fontSize = 13.sp, color = Color(0xFF6B7280))
    }
}

@Composable
fun LoadingState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = Color(0xFF1B8A5A))
    }
}