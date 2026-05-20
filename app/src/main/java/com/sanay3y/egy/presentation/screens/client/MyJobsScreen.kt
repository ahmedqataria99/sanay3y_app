import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanay3y.egy.R
import com.sanay3y.egy.data.model.Request
import com.sanay3y.egy.data.model.RequestStatus
import com.sanay3y.egy.presentation.viewmodel.ClientViewModel
import com.sanay3y.egy.presentation.viewmodel.RequestViewModel
import com.sanay3y.egy.ui.theme.ManropeFamily
import com.sanay3y.egy.ui.theme.Primary
import com.sanay3y.egy.ui.theme.PrimaryLight
import com.sanay3y.egy.ui.theme.TextPrimary
import com.sanay3y.egy.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyJobsScreen(modifier: Modifier = Modifier, clientViewModel: ClientViewModel, requestViewModel: RequestViewModel) {
    val uiState by requestViewModel.uiState.collectAsState()
    val clientUiState by clientViewModel.uiState.collectAsState()
    val activeRequests = uiState.activeRequests
    val completedRequests = uiState.completedRequests
    Scaffold(
        topBar = {
            TopAppBar(
                title = {Text("Sanay3y", color = PrimaryLight, fontSize = 20.sp,
                    style  = TextStyle(fontWeight = FontWeight.SemiBold))},

            )
        }

    ) { innerPadding ->
        var selectedIndex by remember{ mutableIntStateOf(0) }
        val tabs = listOf("Active Jobs", "History")
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ){
                tabs.forEachIndexed { index, title ->
                    val isSelected = selectedIndex == index

                    val backgroundColor by animateColorAsState(
                        targetValue = if(isSelected) Color.White else Color.Transparent
                    )

                    val textColor by animateColorAsState(
                        targetValue = if (isSelected) Color(0xFF0D5C46) else Color(0xFF555555)
                    )

                    Box(

                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(backgroundColor)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                selectedIndex = index
                            }
                    ){
                        Text(
                            text = title,
                            color = textColor,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = ManropeFamily
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            if(selectedIndex == 0)
                activeRequests.forEach { request ->
                    JobCard(request = request, requestViewModel = requestViewModel, clientViewModel = clientViewModel)
                }
            else
                completedRequests.forEach { request ->
                    JobCard(request = request, requestViewModel = requestViewModel, clientViewModel = clientViewModel)
                }
        }
    }
}

@Composable
fun JobCard(modifier: Modifier = Modifier, request: Request, requestViewModel: RequestViewModel, clientViewModel: ClientViewModel) {
    var providerName by remember {mutableStateOf("Loading ...")}
    var providerCategory by remember {mutableStateOf("")}
    val uiState by requestViewModel.uiState.collectAsState()

    LaunchedEffect(request.providerId) {
        val provider = clientViewModel.getProviderById(request.providerId)
        providerName = provider?.name ?: "Unknown Provider"
        providerCategory = provider?.category ?: ""
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(width = 1.dp, color = PrimaryLight.copy(alpha = 0.5f))
    ) {
        Column() {

        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,

        ){
            Image(
                painter = painterResource(R.drawable.profile_image),
                contentDescription = null,
                modifier = Modifier
                    .width(48.dp)
                    .height(48.dp)
                    .background(color = Color.Transparent,shape = RoundedCornerShape(12.dp))

            )
            Spacer(modifier = Modifier.width(width = 16.dp))
            Column() {
                Text(
                    text = providerName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = ManropeFamily,
                    color = TextPrimary
                )
                Text(
                    text = providerCategory,
                    fontSize = 14.sp,
                    fontFamily = ManropeFamily,
                    color = TextSecondary
                )
            }
            Spacer(modifier = Modifier.width(width = 15.dp))
            if(request.status == RequestStatus.COMPLETED_BY_PROVIDER.toString())
            Box(
                modifier = Modifier
                    .background(color = Color(0xff91F78E), shape = RoundedCornerShape(9999.dp))
            ) {
                Text(
                    "Action Required",
                    color = Primary,
                    fontSize = 10.sp,
                    fontFamily = ManropeFamily,
                    fontWeight = FontWeight.SemiBold

                )
            }
            else
            Box(
                modifier = Modifier
                    .background(color = Color(0xff006CC6), shape = RoundedCornerShape(9999.dp))
            ) {
                Text(
                    request.status,
                    color = Primary,
                    fontSize = 10.sp,
                    fontFamily = ManropeFamily,
                    fontWeight = FontWeight.SemiBold

                )
            }

            if(request.status == RequestStatus.COMPLETED_BY_PROVIDER.toString()) {
                Spacer(modifier = Modifier.height(30.dp))
                Row(
                ){
                   Icon(
                       painter = painterResource(R.drawable.note),
                       contentDescription = null
                   )
                   Spacer(modifier = Modifier.width(8.5.dp))
                   Text(
                       "Completed by Provider (Awaiting Your\n" + "Confirmation)",
                        fontSize = 12.sp,
                        fontFamily = ManropeFamily,
                        color = TextSecondary,
                        fontWeight = FontWeight.SemiBold
                       )


                }
                Spacer(modifier = Modifier.height(12.dp))
            }

        }
                Button(
                   onClick = {
                       requestViewModel.confirmJob(request.id)
                   },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(12.dp)

                ){
                    Text(
                        "Confirm & Pay",
                        fontSize = 12.sp,
                        fontFamily = ManropeFamily,
                        color = Color.White
                        )
                }
        }
        }
    }
}