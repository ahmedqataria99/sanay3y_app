package com.sanay3y.egy.request

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
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
import com.sanay3y.egy.ui.theme.BgColor
import com.sanay3y.egy.ui.theme.TealContainer
import com.sanay3y.egy.ui.theme.TealPrimary
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceRequestScreen(
    onConfirm: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    var selectedDate by remember { mutableStateOf("Oct 24, 2023") }
    var selectedTime by remember { mutableStateOf("10:00 - 12:00") }
    var location by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val isFormValid = location.isNotBlank()

    val fieldShape = RoundedCornerShape(12.dp)
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = TealPrimary,
        unfocusedBorderColor = Color(0xFFDDE5E3),
        focusedLabelColor = TealPrimary,
        unfocusedLabelColor = Color.Gray,
        focusedTextColor = Color.Black,
        unfocusedTextColor = Color.Black,
        disabledTextColor = Color(0xFF333333),
        disabledBorderColor = Color(0xFFDDE5E3),
        disabledLabelColor = Color.Gray
    )

    Scaffold(
        containerColor = BgColor,
        topBar = {
            TopAppBar(
                title = { Text("Sanay3y", fontWeight = FontWeight.Bold, color = TealPrimary, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.Black)
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
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Create Service Request", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
            Text(
                "Describe your issue and schedule a visit from one of our certified experts.",
                fontSize = 14.sp, color = Color.Gray, lineHeight = 20.sp
            )

            // ── Service Category Card ──────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(52.dp).background(TealContainer, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Build, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(28.dp))
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text("SERVICE CATEGORY", fontSize = 11.sp, color = TealPrimary, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
                        Text("Emergency Plumbing", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }
            }

            // ── Description ───────────────────────────────────────────────
            Text("Description of Issue", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color.Black)
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                placeholder = { Text("Describe the leak or blockage in detail...", color = Color.LightGray, fontSize = 14.sp) },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                shape = fieldShape,
                colors = fieldColors
            )

            // ── Date & Time ───────────────────────────────────────────────
            Text("Preferred Date & Time", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color.Black)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = selectedDate, onValueChange = {}, readOnly = true, enabled = false,
                        label = { Text("Date") },
                        leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(20.dp)) },
                        modifier = Modifier.fillMaxWidth(), shape = fieldShape, colors = fieldColors
                    )
                    Box(modifier = Modifier.matchParentSize().clickable {
                        DatePickerDialog(context, { _, year, month, day ->
                            selectedDate = "$day/${month + 1}/$year"
                        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
                    })
                }
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = selectedTime, onValueChange = {}, readOnly = true, enabled = false,
                        label = { Text("Time Slot") },
                        leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(20.dp)) },
                        modifier = Modifier.fillMaxWidth(), shape = fieldShape, colors = fieldColors
                    )
                    Box(modifier = Modifier.matchParentSize().clickable {
                        TimePickerDialog(context, { _, hour, minute ->
                            selectedTime = String.format("%02d:%02d", hour, minute)
                        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show()
                    })
                }
            }

            // ── Location ──────────────────────────────────────────────────
            Text("Service Location", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color.Black)
            OutlinedTextField(
                value = location, onValueChange = { location = it },
                placeholder = { Text("123 Harmony Lane, Apartment 4B", color = Color.LightGray, fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = TealPrimary) },
                modifier = Modifier.fillMaxWidth(), shape = fieldShape, colors = fieldColors
            )

            // Map Placeholder
            Box(
                modifier = Modifier.fillMaxWidth().height(130.dp)
                    .clip(RoundedCornerShape(14.dp)).background(Color(0xFFB2DFDB)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(52.dp))
                    Text("Tap to set location", color = TealPrimary, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                }
            }

            // ── Estimated Price ───────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(44.dp).background(TealContainer, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) { Text("💰", fontSize = 22.sp) }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Estimated Price", color = Color.Gray, fontSize = 13.sp)
                        Text("Final cost confirmed after inspection", color = Color.Gray, fontSize = 11.sp)
                    }
                    Text("\$45 - \$80", fontWeight = FontWeight.ExtraBold, color = TealPrimary, fontSize = 20.sp)
                }
            }

            Spacer(Modifier.height(4.dp))

            // ── Confirm Button ────────────────────────────────────────────
            Button(
                onClick = { onConfirm() },
                enabled = isFormValid,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TealPrimary,
                    disabledContainerColor = Color(0xFFB0BEC5)
                )
            ) {
                Text(
                    "Confirm Request ✓", fontSize = 17.sp, fontWeight = FontWeight.Bold,
                    color = if (isFormValid) Color.White else Color.DarkGray
                )
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}