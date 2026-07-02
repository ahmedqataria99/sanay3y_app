package com.sanay3y.egy.presentation.screens.client

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sanay3y.egy.presentation.viewmodel.RequestViewModel
import com.sanay3y.egy.ui.theme.BgColor
import com.sanay3y.egy.ui.theme.TealContainer
import com.sanay3y.egy.ui.theme.TealPrimary
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceRequestScreen(
    viewModel: RequestViewModel = viewModel(),
    userId: String,
    providerId: String,
    serviceType: String,
    onConfirm: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onConfirm()
            viewModel.resetSuccessState()
        }
    }

    val fieldShape = RoundedCornerShape(14.dp)
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = TealPrimary,
        unfocusedBorderColor = Color.Gray,
        cursorColor = TealPrimary
    )

    Scaffold(
        containerColor = BgColor,
        topBar = {
            TopAppBar(
                title = { Text("Sanay3y", fontWeight = FontWeight.Bold, color = TealPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null)
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
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Create Service Request", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
            Text(
                text = "Describe your issue and schedule a visit from one of our certified experts",
                modifier = Modifier.alpha(0.5F)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier
                            .size(48.dp)
                            .background(TealContainer, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Build, null, tint = TealPrimary)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            "CATEGORY",
                            fontSize = 10.sp,
                            color = TealPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(text = serviceType, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (uiState.error != null) {
                Text(text = uiState.error!!, color = Color.Red, fontSize = 13.sp)
            }

            Text("Description of Issue", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            OutlinedTextField(
                value = uiState.notes,
                onValueChange = { viewModel.onNotesChange(it) },
                placeholder = { Text("Describe your issue here...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                shape = fieldShape,
                colors = fieldColors
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = uiState.selectedDate,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Date") },
                        leadingIcon = {
                            Icon(Icons.Default.CalendarMonth, null, tint = TealPrimary)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = fieldShape,
                        colors = fieldColors
                    )
                    Box(Modifier
                        .matchParentSize()
                        .clickable {
                            DatePickerDialog(
                                context,
                                { _, y, m, d -> viewModel.onDateChange("$d/${m + 1}/$y") },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        })
                }
                Box(Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = uiState.selectedTime,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Time") },
                        leadingIcon = { Icon(Icons.Default.Schedule, null, tint = TealPrimary) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = fieldShape,
                        colors = fieldColors
                    )
                    Box(Modifier
                        .matchParentSize()
                        .clickable {
                            TimePickerDialog(
                                context,
                                { _, h, min ->
                                    viewModel.onTimeChange(String.format("%02d:%02d", h, min))
                                },
                                calendar.get(Calendar.HOUR_OF_DAY),
                                calendar.get(Calendar.MINUTE),
                                false
                            ).show()
                        })
                }
            }

            Text("Service Location", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            OutlinedTextField(
                value = uiState.location,
                onValueChange = { viewModel.onLocationChange(it) },
                placeholder = { Text("Enter your address") },
                leadingIcon = { Icon(Icons.Default.LocationOn, null, tint = TealPrimary) },
                modifier = Modifier.fillMaxWidth(),
                shape = fieldShape,
                colors = fieldColors
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = Color.Gray.copy(alpha = 0.6f)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Map Selection Coming Soon",
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                        Text(
                            "Pin your location exactly on a map in the next update",
                            fontSize = 11.sp,
                            color = Color.Gray.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TealContainer.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(vertical = 14.dp, horizontal = 14.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = TealPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "The provider will review your request and send you a price quotation with a labor cost and materials cost breakdown.",
                    fontSize = 13.sp,
                    color = TealPrimary,
                    lineHeight = 18.sp
                )
            }

            Button(
                onClick = {
                    viewModel.createServiceRequest(
                        userId = userId,
                        providerId = providerId,
                        serviceType = serviceType
                    )
                },
                enabled = uiState.isFormValid && !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Send Request", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ServiceRequestScreenPreview() {
    ServiceRequestScreen(
        userId = "user123",
        providerId = "provider123",
        serviceType = "Plumbing",
        onConfirm = {},
        onBack = {}
    )
}