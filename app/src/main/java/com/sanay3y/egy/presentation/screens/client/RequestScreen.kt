import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanay3y.egy.ui.theme.BgColor
import com.sanay3y.egy.ui.theme.TealContainer
import com.sanay3y.egy.ui.theme.TealLight
import com.sanay3y.egy.ui.theme.TealPrimary
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceRequestScreen(onConfirm: () -> Unit, onBack: () -> Unit) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    var selectedDate by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var currentFare by remember { mutableStateOf(150) }
    val numberFormatter = remember { java.text.NumberFormat.getNumberInstance(java.util.Locale.US) }

    // التعديل هنا: أضفنا التحقق من الوصف (notes) لضمان ملء كل الخانات بالكامل
    val isFormValid = selectedDate.isNotBlank() &&
            selectedTime.isNotBlank() &&
            location.isNotBlank() &&
            notes.isNotBlank()

    val fieldShape = RoundedCornerShape(14.dp)
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = TealPrimary,
        unfocusedBorderColor = Color(0xFFDDE5E3),
        cursorColor = TealPrimary
    )

    Scaffold(
        containerColor = BgColor,
        topBar = {
            TopAppBar(
                title = { Text("Sanay3y", fontWeight = FontWeight.Bold, color = TealPrimary) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
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
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier
                        .size(48.dp)
                        .background(TealContainer, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Build, null, tint = TealPrimary)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("CATEGORY", fontSize = 10.sp, color = TealPrimary, fontWeight = FontWeight.Bold)
                        Text("Emergency Plumbing", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Text("Description of Issue", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            OutlinedTextField(
                value = notes, onValueChange = { notes = it },
                placeholder = { Text("Describe your issue here...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                shape = fieldShape, colors = fieldColors
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(Modifier.weight(1f)) {
                    OutlinedTextField(value = selectedDate, onValueChange = {}, readOnly = true, enabled = false, label = { Text("Date") }, leadingIcon = { Icon(Icons.Default.CalendarMonth, null, tint = TealPrimary) }, modifier = Modifier.fillMaxWidth(), shape = fieldShape, colors = fieldColors)
                    Box(Modifier
                        .matchParentSize()
                        .clickable {
                            DatePickerDialog(
                                context,
                                { _, y, m, d -> selectedDate = "$d/${m + 1}/$y" },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        })
                }
                Box(Modifier.weight(1f)) {
                    OutlinedTextField(value = selectedTime, onValueChange = {}, readOnly = true, enabled = false, label = { Text("Time") }, leadingIcon = { Icon(Icons.Default.Schedule, null, tint = TealPrimary) }, modifier = Modifier.fillMaxWidth(), shape = fieldShape, colors = fieldColors)
                    Box(Modifier
                        .matchParentSize()
                        .clickable {
                            TimePickerDialog(
                                context,
                                { _, h, min -> selectedTime = String.format("%02d:%02d", h, min) },
                                calendar.get(Calendar.HOUR_OF_DAY),
                                calendar.get(Calendar.MINUTE),
                                false
                            ).show()
                        })
                }
            }

            Text("Service Location", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            OutlinedTextField(
                value = location, onValueChange = { location = it },
                placeholder = { Text("Enter your address") },
                leadingIcon = { Icon(Icons.Default.LocationOn, null, tint = TealPrimary) },
                modifier = Modifier.fillMaxWidth(), shape = fieldShape, colors = fieldColors
            )

            // --- مكون كارت السعر ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TealContainer.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                    .padding(vertical = 16.dp, horizontal = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(TealLight, CircleShape)
                            .clickable {
                                if (currentFare >= 10) {
                                    currentFare -= 10
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Remove, "Decrease", tint = Color.White, modifier = Modifier.size(24.dp))
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp)
                            .padding(horizontal = 12.dp)
                            .background(Color.White, RoundedCornerShape(16.dp))
                            .border(1.5.dp, TealLight.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "E£${numberFormatter.format(currentFare)}",
                            color = TealPrimary,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(TealLight, CircleShape)
                            .clickable {
                                currentFare += 10
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, "Increase", tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Recommended Price",
                    color = TealPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // زر تأكيد الطلب (سيتفعل فقط عند اكتمال جميع البيانات)
            Button(
                onClick = onConfirm,
                enabled = isFormValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
            ) {
                Text("Confirm Request", fontSize = 17.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}