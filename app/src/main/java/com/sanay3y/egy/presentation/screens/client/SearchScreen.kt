import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sanay3y.egy.R
import com.sanay3y.egy.data.model.Provider
import com.sanay3y.egy.presentation.viewmodel.ClientViewModel
import com.sanay3y.egy.ui.theme.Background
import com.sanay3y.egy.ui.theme.ManropeFamily
import com.sanay3y.egy.ui.theme.Primary
import com.sanay3y.egy.ui.theme.PrimaryLight
import com.sanay3y.egy.ui.theme.TextPrimary
import com.sanay3y.egy.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    viewModel: ClientViewModel = viewModel(),
    category: String = "",
    onNavigateToDetails: () -> Unit
) {
    var selectedRating by remember { mutableStateOf(false) }
    var selectedReset by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()
    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    LaunchedEffect(category) {
        if (category.isNotEmpty()) {
            viewModel.filterByCategory(category)
        }
    }


    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Sanay3y", color = PrimaryLight, fontSize = 20.sp,
                        style = TextStyle(fontWeight = FontWeight.SemiBold)
                    )
                },
                actions = {
                    Icon(
                        painter = painterResource(R.drawable.notification),
                        contentDescription = null
                    )
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .padding(top = 24.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            // search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    viewModel.search(searchQuery)
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(text = "Search for plumbers, electricians ....", color = Color.Gray)
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Icon",
                        tint = PrimaryLight
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color(0xFFCFD8DC),
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedContainerColor = Color.Transparent
                ),
                singleLine = true
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { showSheet = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = CircleShape
                ) {
                    Icon(
                        painter = painterResource(R.drawable.filter),
                        contentDescription = null
                    )
                    Spacer(Modifier.width(7.5.dp))
                    Text("Filters", style = TextStyle(fontWeight = FontWeight.Normal, fontSize = 16.sp, fontFamily = ManropeFamily))
                }
                if (showSheet) {
                    ModalBottomSheet(
                        onDismissRequest = { showSheet = false },
                        sheetState = sheetState,
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                        containerColor = Color.White
                    ) {
                        FilterSheetContent({ showSheet = false }, viewModel)
                    }
                }
                FilterChip(
                    selected = selectedRating,
                    onClick = {
                        selectedRating = !selectedRating
                        if (selectedRating) viewModel.loadTopRated()
                        else viewModel.loadProviders()
                    },
                    label = { Text("Rating") },
                    trailingIcon = { Icon(painter = painterResource(R.drawable.rating), contentDescription = null) },
                    shape = CircleShape
                )
                FilterChip(
                    selected = selectedReset,
                    onClick = {
                        selectedReset = false
                        searchQuery = ""
                        viewModel.loadProviders()
                    },
                    label = { Text("Reset") },
                    shape = CircleShape
                )
            }

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 50.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Search Results", fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = ManropeFamily,
                        color = TextPrimary
                    )
                    Text(
                        "${uiState.providers.size} Results", fontSize = 12.sp,
                        color = TextSecondary,
                        fontFamily = ManropeFamily
                    )
                }
                if (uiState.providers.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 50.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No providers available", color = TextSecondary)
                    }
                } else {
                    uiState.providers.forEach { provider ->
                        ProviderCard(
                            provider = provider,
                            onCardClick = {
                                viewModel.selectProvider(provider)
                                onNavigateToDetails()
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun ProviderCard(
    provider: Provider,
    onCardClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, PrimaryLight.copy(alpha = 0.5f)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        onClick = onCardClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                Image(
                    painter = painterResource(R.drawable.profile_image),
                    contentDescription = null,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                Surface(
                    color = Primary,
                    shape = RoundedCornerShape(bottomStart = 8.dp, topEnd = 8.dp),
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Text(
                        text = " ${provider.rating} ",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = ManropeFamily
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    provider.name, fontSize = 16.sp, fontWeight = FontWeight.Bold,
                    fontFamily = ManropeFamily, color = TextPrimary
                )
                Text(
                    "${provider.category} • ${provider.experienceYears} yrs exp",
                    fontSize = 13.sp, fontFamily = ManropeFamily, color = TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = onCardClick,
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("View Profile", color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun FilterSheetContent(onClose: () -> Unit, viewModel: ClientViewModel) {
    val categories = listOf("Plumber", "Electrical", "Cleaning", "Carpentry", "Painting", "AC Repair")
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp, start = 16.dp, end = 16.dp)
    ) {
        Text(
            "Select Category",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        categories.forEach { category ->
            TextButton(
                onClick = {
                    viewModel.filterByCategory(category)
                    onClose()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(category, modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Start)
            }
        }
    }
}
