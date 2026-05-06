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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
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
    var selected by remember{mutableStateOf(false)}
    var searchQuery by remember{mutableStateOf("")}
    val uiState by viewModel.uiState.collectAsState()
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
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
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
                Button(
                    onClick = {},
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
                FilterChip(
                    selected = selected,
                    onClick = { selected = !selected },
                    label = { Text("Rating") },
                    trailingIcon = {Icon(painter = painterResource(R.drawable.rating,),contentDescription = null)},
                    shape = CircleShape
                )
            }
            if(uiState.isLoading){
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ){
                    CircularProgressIndicator()

                }
            }
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
                if(uiState.providers.isEmpty()){
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ){

                        Text("No providers available")
                    }
                }
                else {

                    uiState.providers.forEach { provider ->
                        ProviderCard(provider = provider)
                    }
                }

                

            }

        }
    }
}

@Composable
fun ProviderCard(modifier: Modifier = Modifier, provider: Provider) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, PrimaryLight)
    ) {
        Box{
            Image(
                painter = painterResource(R.drawable.profile),
                contentDescription = null,
                modifier = Modifier.size(100.dp).clip(RoundedCornerShape(12.dp)),
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
            modifier = Modifier.width(16.dp)
        )
        Column{
            Text(provider.name, fontSize = 16.sp, fontWeight = FontWeight.Bold,
                fontFamily = ManropeFamily,
                color = TextPrimary
            )
            Text(
                provider.category,
                fontSize = 16.sp,
                fontFamily = ManropeFamily,
                color = TextSecondary
            )

        }
    }
}



@Preview(showSystemUi = true)
@Composable
private fun SearchScreenPrev() {
    SearchScreen()
}