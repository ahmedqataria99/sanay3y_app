package com.sanay3y.egy.presentation.screens.client

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sanay3y.egy.data.model.RequestStatus
import com.sanay3y.egy.data.model.UserRole
import com.sanay3y.egy.presentation.viewmodel.JobTrackingViewModel
import com.sanay3y.egy.ui.theme.BgColor
import com.sanay3y.egy.ui.theme.TealContainer
import com.sanay3y.egy.ui.theme.TealPrimary
import kotlinx.coroutines.launch


enum class JobStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED_BY_PROVIDER,
    CONFIRMED_BY_CLIENT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveJobScreen(
    requestId: String,
    userRole: UserRole,
    viewModel: JobTrackingViewModel = viewModel(),
    onBack: () -> Unit,
    onFinishJob: () -> Unit
) {
    val context = LocalContext.current
    val currentRequest by viewModel.currentRequest.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showReportDialog by remember { mutableStateOf(false) }

    // ابدأ الـ real-time listener لما الشاشة تفتح
    LaunchedEffect(requestId) {
        viewModel.observeRequest(requestId)
    }

    // حوّل الـ status من String لـ JobStatus
    val currentStatus = when (currentRequest?.status) {
        RequestStatus.PENDING.name -> JobStatus.PENDING
        RequestStatus.IN_PROGRESS.name -> JobStatus.IN_PROGRESS
        RequestStatus.COMPLETED_BY_PROVIDER.name -> JobStatus.COMPLETED_BY_PROVIDER
        RequestStatus.COMPLETED_BY_CLIENT.name -> JobStatus.CONFIRMED_BY_CLIENT
        else -> JobStatus.PENDING
    }

    if (showReportDialog) {
        ReportIssueDialog(
            onDismiss = { showReportDialog = false },
            onSubmit = {
                showReportDialog = false
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Your report has been received and will be reviewed shortly.",
                        duration = SnackbarDuration.Short
                    )
                }
            }
        )
    }

    Scaffold(
        containerColor = BgColor,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Sanay3y",
                        fontWeight = FontWeight.Bold,
                        color = TealPrimary,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.Black)
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Notifications, null, tint = Color.DarkGray)
                    }
                    Box(
                        Modifier
                            .padding(end = 12.dp)
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFCFD8DC)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Scrollable content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Job Status", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
                Text(
                    "Service ID: #${
                        currentRequest?.id?.take(8)?.uppercase() ?: "..."
                    } • ${currentRequest?.serviceType ?: ""}",
                    fontSize = 13.sp,
                    color = Color.Gray
                )

                // Provider Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFCFD8DC)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("👷", fontSize = 28.sp)
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    "Ahmed Mansour",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    "⭐ 4.9 (124 reviews)",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(TealPrimary, CircleShape)
                                    .clickable {
                                        val intent = Intent(
                                            Intent.ACTION_DIAL,
                                            Uri.parse("tel:01012345678")
                                        )
                                        context.startActivity(intent)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Call,
                                    null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // Certified Badge
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .border(
                                    1.dp,
                                    TealPrimary.copy(alpha = 0.4f),
                                    RoundedCornerShape(20.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                Icons.Default.Verified,
                                null,
                                tint = TealPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "Certified Professional",
                                fontSize = 12.sp,
                                color = TealPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Timeline
                JobStatusTimeline(currentStatus)
            }

            // Buttons ثابتة في الأسفل
            if (userRole == UserRole.CLIENT) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BgColor)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.confirmJob(requestId)
                            onFinishJob()
                        },
                        enabled = currentStatus == JobStatus.COMPLETED_BY_PROVIDER,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TealPrimary,
                            disabledContainerColor = Color(0xFFB0BEC5)
                        )
                    ) {
                        Text(
                            "Confirm Job Done",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }

                    TextButton(
                        onClick = { showReportDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.ReportProblem,
                            null,
                            tint = Color.Red,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Report Issue", color = Color.Red, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun JobStatusTimeline(currentStatus: JobStatus) {
    val steps = listOf(
        Triple("Pending", "Request sent on Oct 24, 10:30 AM", JobStatus.PENDING),
        Triple("In Progress", "Provider is currently working on site", JobStatus.IN_PROGRESS),
        Triple(
            "Completed by Provider",
            "Waiting for professional to finish",
            JobStatus.COMPLETED_BY_PROVIDER
        ),
        Triple(
            "Confirmed by Client",
            "Final verification from your side",
            JobStatus.CONFIRMED_BY_CLIENT
        )
    )

    val currentIndex = when (currentStatus) {
        JobStatus.PENDING -> 0
        JobStatus.IN_PROGRESS -> 1
        JobStatus.COMPLETED_BY_PROVIDER -> 2
        JobStatus.CONFIRMED_BY_CLIENT -> 3
    }

    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(20.dp)) {
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
                                .background(
                                    if (isDone) TealPrimary else Color(0xFFE0E0E0),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isDone) Icon(
                                Icons.Default.Check,
                                null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        if (i < steps.size - 1)
                            Box(
                                Modifier
                                    .width(2.dp)
                                    .height(48.dp)
                                    .background(if (isDone) TealPrimary else Color(0xFFE0E0E0))
                            )
                    }

                    Spacer(Modifier.width(14.dp))

                    Column {
                        Text(
                            title,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            color = if (isDone) Color.Black else Color.Gray,
                            fontSize = 15.sp
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            subtitle,
                            fontSize = 12.sp,
                            color = if (isDone) Color.DarkGray else Color(0xFFBDBDBD)
                        )
                        Spacer(Modifier.height(if (i < steps.size - 1) 4.dp else 0.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ReportIssueDialog(
    onDismiss: () -> Unit,
    onSubmit: () -> Unit
) {
    var selectedReason by remember { mutableStateOf("") }
    var otherText by remember { mutableStateOf("") }

    val reasons = listOf(
        "Provider didn't show up",
        "Work not completed properly",
        "Provider was unprofessional",
        "Charged more than agreed",
        "Other"
    )

    val isSubmitEnabled = selectedReason.isNotEmpty() &&
            (selectedReason != "Other" || otherText.isNotBlank())

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text("Report an Issue", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("What went wrong?", fontSize = 13.sp, color = Color.Black)

                reasons.forEach { reason ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (selectedReason == reason) TealContainer
                                else Color(0xFFF5F5F5)
                            )
                            .clickable { selectedReason = reason }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedReason == reason,
                            onClick = { selectedReason = reason },
                            colors = RadioButtonDefaults.colors(selectedColor = TealPrimary)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(reason, fontSize = 13.sp)
                    }
                }

                if (selectedReason == "Other") {
                    Spacer(Modifier.height(4.dp))
                    OutlinedTextField(
                        value = otherText,
                        onValueChange = { otherText = it },
                        placeholder = { Text("Describe the issue...", fontSize = 13.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TealPrimary,
                            unfocusedBorderColor = Color.LightGray
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (isSubmitEnabled) onSubmit() },
                enabled = isSubmitEnabled,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TealPrimary,
                    disabledContainerColor = Color(0xFFB0BEC5)
                )
            ) {
                Text("Submit", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )
}