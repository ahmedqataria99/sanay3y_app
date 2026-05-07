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
import androidx.compose.material3.ButtonColors
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.api.DistributionOrBuilder
import com.sanay3y.egy.R
import com.sanay3y.egy.data.model.Provider
import com.sanay3y.egy.presentation.viewmodel.ClientViewModel
import com.sanay3y.egy.ui.theme.Background
import com.sanay3y.egy.ui.theme.Border
import com.sanay3y.egy.ui.theme.ManropeFamily
import com.sanay3y.egy.ui.theme.Primary
import com.sanay3y.egy.ui.theme.PrimaryLight
import com.sanay3y.egy.ui.theme.Surface
import com.sanay3y.egy.ui.theme.TextPrimary
import com.sanay3y.egy.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(modifier: Modifier = Modifier, viewModel: ClientViewModel = viewModel()) {
    var selectedRating by remember{mutableStateOf(false)}
    var selectedReset by remember{mutableStateOf(false)}
    var searchQuery by remember{mutableStateOf("")}
    val uiState by viewModel.uiState.collectAsState()
    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = {Text("Sanay3y", color = PrimaryLight, fontSize = 20.sp,
                    style  = TextStyle(fontWeight = FontWeight.SemiBold))},
                actions = {
                    Icon(
                        painter = painterResource(R.drawable.notification),
                        contentDescription = null
                    )
                }
            )
        }


    ){ innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(start = 16.dp, end = 16.dp)
                .padding(top = 24.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            // search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    // here we take the value in the search bar and search for providers with it
                    searchQuery = it
                    viewModel.search(searchQuery)
                                },
                modifier = Modifier
                    .fillMaxWidth(),
                placeholder = {
                    Text(text = "Search for plumbers, electricians ....", color = Color.Gray)
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Icon",
                        tint =PrimaryLight
                    )
                },
                shape = RoundedCornerShape(12.dp), // Controls the roundness of the border
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color(0xFFCFD8DC), // Soft grey border
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
                    // this the filter button
                    Button(
                        onClick = {
                            showSheet = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = CircleShape
                    ){
                        Icon(
                            painter = painterResource(R.drawable.filter),
                            contentDescription = null
                        )
                        Spacer(Modifier.width(7.5.dp))
                        Text("Filters", style = TextStyle(fontWeight = FontWeight.Normal, fontSize = 16.sp, fontFamily = ManropeFamily))
                    }
                    // here we determine when to show the filter menu
                    if(showSheet){
                        // if showSheet var is true we show a menu that appears from bottom of the screen
                        ModalBottomSheet(
                            onDismissRequest = { showSheet = false },
                            sheetState = sheetState,
                            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                            containerColor = Color.White
                        ) {
                            // this is a composable function to show the content of the filter menu
                            // it takes a function that will be executed when closing the menu
                            // and also the viewModel in order to search with chosen category
                            FilterSheetContent({showSheet = false}, viewModel)
                        }
                    }
                // this is button used to filter the top rated providers
                FilterChip(
                    selected = selectedRating,
                    onClick = { selectedRating = !selectedRating
                        if(selectedRating)viewModel.loadTopRated()
                        else viewModel.loadProviders()
                              },
                    label = { Text("Rating") },
                    trailingIcon = {Icon(painter = painterResource(R.drawable.rating,),contentDescription = null)},
                    shape = CircleShape
                )
                // this is button to reset filters
                FilterChip(
                    selected = selectedReset,
                    onClick = {
                        selectedReset = !selectedReset
                        viewModel.loadProviders()


                    },
                    label = { Text("Reset") },
                    shape = CircleShape
                )
            }
            // when the view model sends loading state we show the loading circle
            // we listen to the view model using the uiState var
            if(uiState.isLoading){
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ){
                    CircularProgressIndicator()

                }
            }
            // else we show the resulted providers
            else {


                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.SpaceBetween
                ){

                    Text("Top Rated Professionals", fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = ManropeFamily,
                        color = TextPrimary
                    )
                    Text("${uiState.providers.size} Results", fontSize = 12.sp,
                        color = TextSecondary,
                        fontFamily = ManropeFamily
                    )
                }
                // if there is no providers we show this text
                if(uiState.providers.isEmpty()){
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ){

                        Text("No providers available")
                    }
                }
                // else we show them in cards
                else {
                    // we iterate over each provider then we put it inside the card then displaying it
                    uiState.providers.forEach { provider ->
                        ProviderCard(provider = provider)
                    }
                }
//                  ProviderCard(provider = provider)
//                ProviderCard(provider = provider)
//                ProviderCard(provider = provider)


            }

        }
    }
}


// this is the provider card, when clicked on it navigates to the provider profile
@Composable
fun ProviderCard(modifier: Modifier = Modifier, provider: Provider) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(178.dp)
            .padding(top = 17.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, PrimaryLight),
        colors = CardDefaults.cardColors(containerColor = Background)
    ) {
        Row(
            modifier = Modifier.padding(16.dp)
        )
        {
        Box{
            Image(
                painter = painterResource(R.drawable.profile_image),
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Surface(
                color = Primary,
                shape = RoundedCornerShape(bottomStart = 12.dp, topEnd = 12.dp),
                modifier = Modifier.align(Alignment.TopEnd)
            ){
                Text(
                    text = "${provider.rating}",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontFamily = ManropeFamily
                )

            }
        }
        Spacer(
            modifier = Modifier.width(20.dp)
        )
        Column{
            Text(provider.name, fontSize = 16.sp, fontWeight = FontWeight.Bold,
                fontFamily = ManropeFamily,
                color = TextPrimary
            )
            Text(
                "${ provider.category } / ${provider.experienceYears} years of experience",
                fontSize = 16.sp,
                fontFamily = ManropeFamily,
                color = TextSecondary
            )
            Spacer(
                modifier = Modifier.height(24.dp)
            )
            Row {

                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Book now",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontFamily = ManropeFamily
                        )
                }
                Spacer(
                    modifier = Modifier.width(8.dp)
                )
                Button(
                    onClick = {},
                    border = BorderStroke(width = 1.dp,color = Primary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.message),
                        contentDescription = null,
                    )
                }
            }

        }

        }
    }
}

@Composable
fun FilterSheetContent(onClose: () -> Unit, viewModel: ClientViewModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp, start = 16.dp, end = 16.dp)
    ) {
        // here we have different text button where you click on, it searches with the selected category
        TextButton(
            onClick = {
                viewModel.filterByCategory("Plumber")
                onClose
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Plumber")
        }
        TextButton(
            onClick = {
                viewModel.filterByCategory("Electrical")
                onClose
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Electrical")
        }
        TextButton(
            onClick = {
                viewModel.filterByCategory("Cleaning")
                onClose
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cleaning")
        }
        TextButton(
            onClick = {
                viewModel.filterByCategory("Carpentry")
                onClose
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Carpentry")
        }
        TextButton(
            onClick = {
                viewModel.filterByCategory("Painting")
                onClose
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Painting")
        }
        TextButton(
            onClick = {
                viewModel.filterByCategory("AC Repair")
                onClose
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("AC Repair")
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun SearchScreenPrev() {
    SearchScreen()
}