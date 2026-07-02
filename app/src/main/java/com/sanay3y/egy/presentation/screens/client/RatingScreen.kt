
//snackbar problem


package com.sanay3y.egy.presentation.screens.client

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.res.stringResource
import com.sanay3y.egy.R
import com.sanay3y.egy.data.model.Review
import com.sanay3y.egy.ui.theme.BgColor
import com.sanay3y.egy.ui.theme.TealContainer
import com.sanay3y.egy.ui.theme.TealPrimary
import com.sanay3y.egy.presentation.viewmodel.RatingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RatingScreen(
    requestId: String,
    userId: String,
    providerId: String,
    onBackToHome: () -> Unit,
    viewModel: RatingViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(providerId) {
        viewModel.loadProvider(providerId)
    }

    Scaffold(
        containerColor = BgColor,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.feedback), fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = { IconButton(onClick = onBackToHome) { Icon(Icons.Default.ArrowBack, null, tint = Color.Black) } },
                actions = { IconButton(onClick = {}) { Icon(Icons.Default.MoreHoriz, null, tint = Color.Black) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Button(
                    onClick = {
                        val successMsg = context.getString(R.string.feedback_success)
                        viewModel.submitFeedback(
                            requestId = requestId,
                            userId = userId,
                            providerId = providerId
                        ) {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = successMsg,
                                    duration = SnackbarDuration.Short
                                )
                            }
                            onBackToHome()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    enabled = uiState.selectedStars > 0,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TealPrimary,
                        disabledContainerColor = Color(0xFFB0BEC5)
                    )
                ) {
                    Text(stringResource(R.string.submit_feedback), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(stringResource(R.string.rate_service), fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = TealPrimary)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            stringResource(R.string.rate_experience_desc),
                            fontSize = 13.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider(color = Color(0xFFEEEEEE))
                        Spacer(Modifier.height(16.dp))

                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier.size(52.dp).clip(CircleShape).background(Color(0xFFCFD8DC)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("👷", fontSize = 26.sp)
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                if (uiState.provider == null) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = TealPrimary,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    // ✅ let بدل !!
                                    uiState.provider?.let { provider ->
                                        Text(provider.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        Text(provider.category, fontSize = 12.sp, color = Color.Gray)
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider(color = Color(0xFFEEEEEE))
                        Spacer(Modifier.height(16.dp))

                        Text(stringResource(R.string.tap_to_rate), fontSize = 11.sp, color = Color.Gray, letterSpacing = 1.sp)
                        Spacer(Modifier.height(10.dp))

                        StarRatingRow(uiState.selectedStars) { viewModel.onStarsChanged(it) }
                        if (uiState.selectedStars > 0) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                starLabel(uiState.selectedStars),
                                color = Color(0xFFF57C00),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }

                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider(color = Color(0xFFEEEEEE))
                        Spacer(Modifier.height(16.dp))

                        OutlinedTextField(
                            value = uiState.comment,
                            onValueChange = { viewModel.onCommentChanged(it) },
                            placeholder = { Text(stringResource(R.string.feedback_placeholder)) },
                            label = { Text(stringResource(R.string.tell_us_exp)) },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TealPrimary,
                                unfocusedBorderColor = Color(0xFFEEEEEE)
                            )
                        )
                    }
                }
            }

            if (uiState.submitted) {
                item {
                    Card(
                        Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                    ) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Check, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.thanks_review), color = Color(0xFF2E7D32), fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            if (uiState.reviews.isNotEmpty()) {
                item {
                    Text(stringResource(R.string.all_reviews, uiState.reviews.size), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                items(uiState.reviews) { review: Review -> ReviewCard(review) }
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

    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(36.dp).background(TealContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(stringResource(R.string.user_you).take(1).uppercase(), fontWeight = FontWeight.Bold, color = TealPrimary, fontSize = 14.sp)
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.user_you), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Text(dateString, color = Color.Gray, fontSize = 12.sp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                repeat(5) { i ->
                    Icon(
                        Icons.Filled.Star,
                        null,
                        tint = if (i < review.rating) Color(0xFFFFC107) else Color.LightGray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Text(review.comment, color = Color.DarkGray, fontSize = 13.sp, lineHeight = 18.sp)
        }
    }
}

@Composable
fun StarRatingRow(selectedStars: Int, onStarClick: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        for (i in 1..5) {
            Icon(
                imageVector = if (i <= selectedStars) Icons.Filled.Star else Icons.Outlined.StarOutline,
                contentDescription = null,
                tint = if (i <= selectedStars) Color(0xFFFFC107) else Color.LightGray,
                modifier = Modifier.size(42.dp).clickable { onStarClick(i) }
            )
        }
    }
}

@Composable
fun starLabel(stars: Int) = when (stars) {
    1 -> stringResource(R.string.poor)
    2 -> stringResource(R.string.fair)
    3 -> stringResource(R.string.good)
    4 -> stringResource(R.string.very_good)
    5 -> stringResource(R.string.excellent)
    else -> ""
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RatingScreenPreview() {
    RatingScreen(
        requestId = "request_123",
        userId = "user_123",
        providerId = "provider_123",
        onBackToHome = {}
    )
}