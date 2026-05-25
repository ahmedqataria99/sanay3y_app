package com.sanay3y.egy.screens.client

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.Alignment // 🟢 مصلح: إضافة الـ Import الخاص بالـ Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Star // 🟢 مصلح: إضافة الـ Import للـ Filled Star
import androidx.compose.material.icons.outlined.StarOutline // 🟢 مصلح: إضافة الـ Import للـ Outlined Star
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import com.sanay3y.egy.data.model.Review
import com.sanay3y.egy.ui.theme.BgColor
import com.sanay3y.egy.ui.theme.TealContainer
import com.sanay3y.egy.ui.theme.TealPrimary
import com.sanay3y.egy.viewmodel.RatingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RatingScreen(
    onBackToHome: () -> Unit,
    viewModel: RatingViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = BgColor,
        snackbarHost   = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Feedback", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = { IconButton(onClick = onBackToHome) { Icon(Icons.Default.ArrowBack, null, tint = Color.Black) } },
                actions = { IconButton(onClick = {}) { Icon(Icons.Default.MoreHoriz, null, tint = Color.Black) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Column(modifier = Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 16.dp, vertical = 12.dp)) {
                Button(
                    onClick = {
                        viewModel.submitFeedback {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message  = "Thank you, your feedback has been submitted.",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape    = RoundedCornerShape(14.dp),
                    enabled  = uiState.selectedStars > 0,
                    colors   = ButtonDefaults.buttonColors(containerColor = TealPrimary, disabledContainerColor = Color(0xFFB0BEC5))
                ) {
                    Text("Submit Feedback", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier            = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding      = PaddingValues(vertical = 16.dp)
        ) {
            item {
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {

                        Text("Rate Service", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = TealPrimary)
                        Spacer(Modifier.height(4.dp))
                        Text("How was your experience with your\nservice provider today?", fontSize = 13.sp, color = Color.Gray, textAlign = TextAlign.Center)

                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider(color = Color(0xFFEEEEEE))
                        Spacer(Modifier.height(16.dp))

                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(52.dp).clip(CircleShape).background(Color(0xFFCFD8DC)), contentAlignment = Alignment.Center) {
                                Text("👷", fontSize = 26.sp)
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("Ahmed Mansour", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text("Senior Maintenance Specialist", fontSize = 12.sp, color = Color.Gray)
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider(color = Color(0xFFEEEEEE))
                        Spacer(Modifier.height(16.dp))

                        Text("TAP TO RATE", fontSize = 11.sp, color = Color.Gray, letterSpacing = 1.sp)
                        Spacer(Modifier.height(10.dp))

                        StarRatingRow(uiState.selectedStars) { viewModel.onStarsChanged(it) }
                        if (uiState.selectedStars > 0) {
                            Spacer(Modifier.height(6.dp))
                            Text(starLabel(uiState.selectedStars), color = Color(0xFFF57C00), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }

                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider(color = Color(0xFFEEEEEE))
                        Spacer(Modifier.height(16.dp))

                        OutlinedTextField(
                            value = uiState.comment,
                            onValueChange = { viewModel.onCommentChanged(it) },
                            placeholder = { Text("Write your feedback here...", color = Color.LightGray) },
                            label       = { Text("Tell us about your experience") },
                            modifier    = Modifier.fillMaxWidth(),
                            minLines    = 3,
                            shape       = RoundedCornerShape(12.dp),
                            colors      = OutlinedTextFieldDefaults.colors(focusedBorderColor = TealPrimary, unfocusedBorderColor = Color(0xFFEEEEEE))
                        )
                    }
                }
            }

            if (uiState.submitted) {
                item {
                    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Check, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Thanks for your review!", color = Color(0xFF2E7D32), fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            if (uiState.reviews.isNotEmpty()) {
                item {
                    Text("All Reviews (${uiState.reviews.size})", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                items(uiState.reviews) { ReviewCard(it) }
            }
        }
    }
}

@Composable
fun ReviewCard(review: Review) {
    val dateString = remember(review.timestamp) {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        sdf.format(Date(review.timestamp))
    }

    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(36.dp).background(TealContainer, CircleShape), contentAlignment = Alignment.Center) {
                        Text("U", fontWeight = FontWeight.Bold, color = TealPrimary, fontSize = 14.sp)
                    }
                    Spacer(Modifier.width(8.dp))
                    Text("User (You)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Text(dateString, color = Color.Gray, fontSize = 12.sp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                repeat(5) { i ->
                    Icon(Icons.Filled.Star, null, tint = if (i < review.rating) Color(0xFFFFC107) else Color.LightGray, modifier = Modifier.size(16.dp))
                }
            }
            Text(review.comment, color = Color.DarkGray, fontSize = 13.sp, lineHeight = 18.sp)
        }
    }
}

// 🟢 مصلح: إضافة دالة النجوم الناقصة في ملف السكرين المَفصول
@Composable
fun StarRatingRow(selectedStars: Int, onStarClick: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        for (i in 1..5) {
            Icon(
                imageVector        = if (i <= selectedStars) Icons.Filled.Star else Icons.Outlined.StarOutline,
                contentDescription = null,
                tint               = if (i <= selectedStars) Color(0xFFFFC107) else Color.LightGray,
                modifier           = Modifier.size(42.dp).clickable { onStarClick(i) }
            )
        }
    }
}

// 🟢 مصلح: إضافة دالة التسمية الناقصة في ملف السكرين المَفصول
fun starLabel(stars: Int) = when (stars) {
    1    -> "Poor "
    2    -> "Fair "
    3    -> "Good "
    4    -> "Very Good "
    5    -> "Excellent "
    else -> ""
}