import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.sanay3y.egy.R
import com.sanay3y.egy.presentation.viewmodel.ClientViewModel
import com.sanay3y.egy.ui.theme.Background
import com.sanay3y.egy.ui.theme.ManropeFamily
import com.sanay3y.egy.ui.theme.Primary
import com.sanay3y.egy.ui.theme.PrimaryLight
import com.sanay3y.egy.ui.theme.Success
import com.sanay3y.egy.ui.theme.TextPrimary
import com.sanay3y.egy.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderDetailsScreen(modifier: Modifier = Modifier, viewModel: ClientViewModel, onNavigateBack: () -> Unit) {
    val provider by viewModel.selectedProvider.collectAsState()
    Scaffold(

        containerColor = Background,
        topBar = {
            TopAppBar(
                title = {Text("Provider Details", color = PrimaryLight, fontSize = 20.sp,
                    style  = TextStyle(fontWeight = FontWeight.SemiBold))},
                navigationIcon = {
                    Icon(
                        painter = painterResource(R.drawable.back),
                        contentDescription = null,
                        modifier = Modifier.padding(end = 20.dp, start = 24.dp)
                    )
                }
            )
        },
        bottomBar = {
            BottomActionBar()
        }
    ) {
        innerPadding ->
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState())
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Box(


            ){
                Column {
                    // 1. Background Cover Photo
                    Image(
                        painter = painterResource(id = R.drawable.modern_workshop),
                        contentDescription = "Cover Photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(256.dp)
                    )

                    // 2. White space/Content area below the image
                    Spacer(modifier = Modifier.height(80.dp))
                }

                Surface(
                    shape = RoundedCornerShape(24.dp),
                    shadowElevation = 8.dp,
                    color = Color.White,
                    modifier = Modifier
                        .padding(start = 24.dp)
                        .size(130.dp)
                        .align(Alignment.BottomStart)
                        .offset(y = (-20).dp) // Pulls the image up to overlap the cover
                ) {
                    Box(modifier = Modifier.padding(6.dp)) {
                        Image(
                            painter = painterResource(id = R.drawable.profile_image),
                            contentDescription = "Profile Picture",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(18.dp))
                        )
                    }
                }
            }
            Column(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp)
            ) {
                Text(
                    text = provider?.name ?: "Ahmed Mansour",
                    fontWeight = FontWeight.Bold,
                    fontFamily = ManropeFamily,
                    fontSize = 32.sp,
                    color = TextPrimary,
                )
                Text(
                    text = provider?.category ?: "",
                    fontWeight = FontWeight.Normal,
                    fontFamily = ManropeFamily,
                    fontSize = 16.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 10.dp)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 12.dp)
                        .background(Color(0xff91F78E), shape = CircleShape),

                ){
                    Spacer(Modifier.width(12.dp))
                    Icon(
                        painter = painterResource(R.drawable.rating),
                        contentDescription = null,
                        tint = TextSecondary
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = provider?.rating.toString() + "(114 reviews)",
                        fontWeight = FontWeight.Normal,
                        fontFamily = ManropeFamily,
                        fontSize = 12.sp,
                        color = TextSecondary,
                    )
                    Spacer(Modifier.width(12.dp))

                }
                Row(
                    modifier = Modifier.padding(top = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Icon(
                        painter = painterResource(R.drawable.experience),
                        contentDescription = null,
                        tint = TextSecondary
                    )
                    Spacer(modifier.width(4.dp))
                    Text(
                        text = provider?.experienceYears.toString() + " years of experience",
                        fontFamily = ManropeFamily,
                        fontSize = 14.sp,
                        color = TextSecondary,

                    )

                }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(top = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Background),
                    border = BorderStroke(width = 1.dp, color = Color(0xffBDC9C5))

                ) {
                    Column(
                        modifier = Modifier.padding(24.dp)

                    ) {
                        Text(
                            text = "About",
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = ManropeFamily,
                            fontSize = 20.sp,
                            color = TextPrimary,
                        )
                        Text(
                            text = provider?.bio ?: ("I specialize in high-end electrical " +
                                    "installations and comprehensive home " +
                                    "automation systems. With over 15 years " +
                                    "of experience serving premium " +
                                    "residential properties in Cairo, I provide " +
                                    "reliable, safe, and efficient electrical " +
                                    "solutions tailored to your modern " +
                                    "lifestyle."),
                            fontSize = 16.sp,
                            fontFamily = ManropeFamily,
                            color = TextSecondary,
                            lineHeight = 24.sp
                        )

                    }
                }
            }
        }
    }
}

@Composable
fun BottomActionBar() {
    Surface(
        tonalElevation = 8.dp,
        shadowElevation = 8.dp,
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .windowInsetsPadding(WindowInsets.navigationBars),
            horizontalArrangement = Arrangement.Center
        ){
            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                modifier = Modifier.width(228.dp).height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {

                    Icon(
                        painter = painterResource(R.drawable.request),
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "Start Request",
                        fontSize = 16.sp,
                        fontFamily = ManropeFamily,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )


            }
        }
    }
}
