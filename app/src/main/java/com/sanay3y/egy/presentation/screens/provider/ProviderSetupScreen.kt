package com.sanay3y.egy.presentation.screens.provider

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.icons.outlined.HomeRepairService
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.rememberNavController
import com.sanay3y.egy.presentation.viewmodel.ProviderSetupViewModel
import com.sanay3y.egy.ui.theme.Sanay3yAppTheme

@Composable
fun ProviderSetupScreen(
    uid: String,
    navController: NavController,
    vm: ProviderSetupViewModel = viewModel()
) {
    LaunchedEffect(vm.isSuccess) {
        if (vm.isSuccess) {
            navController.navigate("provider_dashboard") {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
                restoreState = false
            }
        }
    }

    Surface(color = MaterialTheme.colorScheme.background) {
        if (vm.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                WelcomeCard()
                StepSection(vm)
                ProfileIdentify(vm, uid)
            }
        }
    }
}

@Composable
fun WelcomeCard() {
    Card(
        modifier = Modifier.padding(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF00796B))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Welcome to the Expert Hub",
                color = Color.White,
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))

            Text(
                text = "Let's set up your professional profile. This information helps clients trust your expertise and find you for their next project.",
                color = Color.White
            )
        }
    }
}

@Composable
fun StepSection(vm: ProviderSetupViewModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            StepItem(1, "Personal Details", vm.currentStep >= 0)
            StepItem(2, "Service Settings", vm.currentStep >= 1)
            StepItem(3, "Work Location", vm.currentStep >= 2)
        }
    }
}

@Composable
fun StepItem(number: Int, title: String, isCompleted: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    if (isCompleted) Color(0xFF00796B) else Color.LightGray
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number.toString(),
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        Spacer(Modifier.padding(5.dp))
        Text(title)
    }
    Spacer(Modifier.size(12.dp))
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileIdentify(vm: ProviderSetupViewModel, uid: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = CardDefaults.outlinedCardBorder()
    ) {
        var imageUri by remember { mutableStateOf<Uri?>(null) }

        val launcher = rememberLauncherForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->
            imageUri = uri
        }

        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color(0xFF00796B)
                )
                Spacer(Modifier.size(8.dp))
                Text(
                    "Profile Identity",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.size(12.dp))

            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(120.dp)
                    .border(
                        width = 2.dp,
                        color = Color(0xFFBBBBBB),
                        shape = CircleShape
                    )
                    .clickable {
                        launcher.launch("image/*")
                    },
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    // Placeholder for selected image
                    Box(modifier = Modifier.fillMaxSize().background(Color.Gray, CircleShape))
                } else {
                    Icon(
                        imageVector = Icons.Outlined.AddAPhoto,
                        contentDescription = null,
                        tint = Color(0xFF6E706F),
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
            Spacer(Modifier.size(12.dp))
            OutlinedTextField(
                value = vm.name,
                modifier = Modifier.fillMaxWidth(),
                onValueChange = { vm.updateName(it) },
                label = { Text("Full Name") }
            )

            OutlinedTextField(
                value = vm.phone,
                modifier = Modifier.fillMaxWidth(),
                onValueChange = {
                    if (it.all { char -> char.isDigit() } && it.length <= 11) {
                        vm.updatePhone(it)
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone
                ),
                label = { Text("Phone Number") }
            )
            
            LaunchedEffect(vm.name, vm.phone, vm.currentStep) {
                if (vm.currentStep == 0 && vm.name.isNotBlank() && vm.phone.isNotBlank()) {
                    vm.nextStep()
                }
            }
            
            Spacer(Modifier.size(30.dp))
            val categories = listOf("Plumbing", "Electrical", "Carpentry", "Painting", "HVAC", "Appliance Repair")
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.HomeRepairService,
                        contentDescription = null,
                        tint = Color(0xFF00796B)
                    )
                    Spacer(Modifier.size(8.dp))
                    Text(
                        "Service Expertise",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.size(12.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { category ->
                        FilterChip(
                            selected = vm.select == category,
                            onClick = { vm.selectCategory(category) },
                            label = { Text(category) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF00796B),
                                selectedLabelColor = Color.White
                            ),
                            shape = CircleShape
                        )
                    }
                }
                Spacer(Modifier.size(12.dp))
                OutlinedTextField(
                    value = vm.price,
                    modifier = Modifier.fillMaxWidth(),
                    onValueChange = {
                        if (it.all { char -> char.isDigit() }) {
                            vm.updatePrice(it)
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    label = { Text("Hourly Pricing (EGP)") }
                )
                LaunchedEffect(vm.price, vm.currentStep) {
                    if (vm.currentStep == 1 && vm.price.isNotBlank()) {
                        vm.nextStep()
                    }
                }

                Spacer(Modifier.size(15.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = null,
                            tint = Color(0xFF00796B)
                        )
                        Spacer(Modifier.size(8.dp))
                        Text(
                            "Service Area",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.size(12.dp))
                    OutlinedTextField(
                        value = vm.address,
                        modifier = Modifier.fillMaxWidth(),
                        onValueChange = { vm.updateAddress(it) },
                        label = { Text("Address") }
                    )

                    LaunchedEffect(vm.address, vm.currentStep) {
                        if (vm.currentStep == 2 && vm.address.isNotBlank()) {
                            vm.nextStep()
                        }
                    }

                    Spacer(Modifier.size(40.dp))

                    vm.errorMessage?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    Button(
                        onClick = { vm.completeProviderSetup(uid) },
                        enabled = !vm.isLoading && vm.name.isNotBlank() &&
                                vm.phone.isNotBlank() &&
                                vm.select.isNotBlank() &&
                                vm.price.isNotBlank() &&
                                vm.address.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00796B),
                            disabledContainerColor = Color.Gray
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .fillMaxWidth()
                            .padding(start = 6.dp, end = 6.dp)
                            .height(50.dp)
                    ) {
                        Text(
                            text = "Complete Setup >",
                            fontSize = 20.sp
                        )
                    }
                    Spacer(Modifier.size(15.dp))
                    Text(
                        text = "By clicking, you agree to our Service Provider Terms and Privacy Policy.",
                        color = Color(0xFF6E706F),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProviderSetupPreview() {
    Sanay3yAppTheme {
        ProviderSetupScreen("test_uid", rememberNavController())
    }
}
