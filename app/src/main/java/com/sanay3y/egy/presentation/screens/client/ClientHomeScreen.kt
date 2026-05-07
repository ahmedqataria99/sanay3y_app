package com.sanay3y.egy.presentation.screens.client

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterStart
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanay3y.egy.R
import com.sanay3y.egy.data.model.Category
import com.sanay3y.egy.ui.theme.Background
import com.sanay3y.egy.ui.theme.ManropeFamily
import com.sanay3y.egy.ui.theme.PrimaryLight
import com.sanay3y.egy.ui.theme.TextPrimary
import com.sanay3y.egy.ui.theme.TextSecondary


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientHomeScreen(modifier: Modifier = Modifier, userId: String) {
    var searchQuery by remember { mutableStateOf("") }

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
    ) {
        innerPadding->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(start = 16.dp, end = 16.dp)
                .padding(top = 24.dp)
                .verticalScroll(rememberScrollState()),

        ) {
            Text("Hello, John", modifier = Modifier.align(Alignment.Start),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                style = TextStyle(fontFamily = ManropeFamily),
                color = TextPrimary
            )
            Text("What can we help you with today?", modifier = Modifier.padding(top = 8.dp),
                fontSize = 16.sp,
                color = TextSecondary,
                fontFamily = ManropeFamily,
                fontWeight = FontWeight.Normal
            )
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp,),
                placeholder = {
                    Text(text = "Search for a service...", color = Color.Gray)
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
            CategoryGrid(Modifier.padding(top = 32.dp))
        }
    }
}

@Composable
fun CategoryGrid(modifier: Modifier = Modifier) {
    val categories = listOf(
        Category("Plumbing", Color(0xFF99F2E1), R.drawable.plumping),
        Category("Electrical", Color(0xFFB4F58C), R.drawable.electrical),
        Category("Cleaning", Color(0xFFD9E7FF), R.drawable.cleaning),
        Category("Carpentry", Color(0xFFEBE8E2), R.drawable.carpentry),
        Category("Painting", Color(0xFF86D9C5), R.drawable.painting),
        Category("AC Repair", Color(0xFF81E675), R.drawable.ac)
    )
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(top = 32.dp)) {
        // Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Categories", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(text = "See All", color = Color(0xFF00796B), fontWeight = FontWeight.Medium)
        }

        // The "Manual" Grid logic
        // .chunked(2) turns [1, 2, 3, 4] into [[1, 2], [3, 4]]
        categories.chunked(2).forEach { rowCategories ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                for (category in rowCategories) {
                    // weight(1f) ensures both cards take up equal half-width
                    Box(modifier = Modifier.weight(1f)) {
                        CategoryItem(category)
                    }
                }

                // If a row has only 1 item, add an empty space to keep the alignment
                if (rowCategories.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}




@Composable
fun CategoryItem(category: Category) {
    Card(
        modifier = Modifier.aspectRatio(1f), // Keeps the cards square
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon Container
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(category.color, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(category.icon),
                    contentDescription = null
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = category.name,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun ClientHomeScreenPrev() {
    ClientHomeScreen(Modifier, "userId")
}
