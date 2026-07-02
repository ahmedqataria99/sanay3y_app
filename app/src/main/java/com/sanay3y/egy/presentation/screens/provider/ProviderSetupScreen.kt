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
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.icons.outlined.HomeRepairService
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Verified
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
import androidx.navigation.compose.rememberNavController
import com.sanay3y.egy.presentation.viewmodel.ProviderSetupViewModel
import com.sanay3y.egy.ui.theme.Sanay3yAppTheme
import androidx.compose.material.icons.filled.KeyboardArrowDown
import com.sanay3y.egy.utils.LocationHelper
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@Composable
fun ProviderSetupScreen(
    uid: String,
    navController: NavController,
    vm: ProviderSetupViewModel = viewModel()
) {
    val scope = rememberCoroutineScope()

    val context = LocalContext.current
    val locationHelper = remember {
        LocationHelper(context)
    }
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
                ProfileIdentify(vm, uid, locationHelper, scope)
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
fun ProfileIdentify(
    vm: ProviderSetupViewModel,
    uid: String ,
    locationHelper: LocationHelper,
    scope: CoroutineScope
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = CardDefaults.outlinedCardBorder()
    ) {
        val photoLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->
            uri?.let { vm.updateProfilePhoto(it) }
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
                        photoLauncher.launch("image/*")
                    },
                contentAlignment = Alignment.Center
            ) {
                if (vm.profilePhotoUri != null) {
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
                    var governorateExpanded by remember { mutableStateOf(false) }
                    var districtExpanded by remember { mutableStateOf(false) }

                    Box(
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        OutlinedTextField(
                            value = vm.governorate,
                            onValueChange = {},
                            readOnly = true,
                            enabled = false,
                            label = { Text("المحافظة") },
                            trailingIcon = {
                                Icon(
                                    Icons.Default.KeyboardArrowDown,
                                    contentDescription = null
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable {
                                    governorateExpanded = true
                                }
                        )

                        DropdownMenu(
                            expanded = governorateExpanded,
                            onDismissRequest = {
                                governorateExpanded = false
                            },
                            modifier = Modifier.fillMaxWidth(.9f)
                        ) {

                            vm.governorates.forEach {

                                DropdownMenuItem(
                                    text = {
                                        Text(it)
                                    },
                                    onClick = {
                                        vm.updateGovernorate(it)
                                        governorateExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val districts =
                        vm.districtsMap[vm.governorate] ?: emptyList()

                    Box(
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        OutlinedTextField(
                            value = vm.district,
                            onValueChange = {},
                            readOnly = true,
                            enabled = false,
                            label = { Text("المنطقة") },
                            trailingIcon = {
                                Icon(
                                    Icons.Default.KeyboardArrowDown,
                                    contentDescription = null
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable(
                                    enabled = vm.governorate.isNotBlank()
                                ) {
                                    districtExpanded = true
                                }
                        )

                        DropdownMenu(
                            expanded = districtExpanded,
                            onDismissRequest = {
                                districtExpanded = false
                            },
                            modifier = Modifier.fillMaxWidth(.9f)
                        ) {

                            districts.forEach {

                                DropdownMenuItem(
                                    text = {
                                        Text(it)
                                    },
                                    onClick = {
                                        vm.updateDistrict(it)
                                        districtExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    LaunchedEffect(vm.governorate, vm.district, vm.currentStep) {
                        if (vm.currentStep == 2 && vm.governorate.isNotBlank() && vm.district.isNotBlank()) {
                            vm.nextStep()
                        }
                    }

                    Spacer(Modifier.size(40.dp))
                    var currentDocumentButton by remember { mutableIntStateOf(0) }

                    val documentLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.OpenDocument()
                    ) { uri ->
                        uri?.let {
                            when (currentDocumentButton) {
                                1 -> vm.updateNationalIdFront(it)
                                2 -> vm.updateNationalIdBack(it)
                                3 -> vm.updatePoliceClearance(it)
                            }
                        }
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Verified,
                                contentDescription = null,
                                tint = Color(0xFF00796B)
                            )
                            Spacer(Modifier.size(8.dp))
                            Text(
                                "Verification",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(Modifier.size(12.dp))
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                currentDocumentButton = 1
                                documentLauncher.launch(arrayOf("image/*", "application/pdf"))
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.UploadFile, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (vm.nationalIdFrontUri != null) "ID Front Uploaded" else "Upload National ID Front")
                        }

                        OutlinedButton(
                            onClick = {
                                currentDocumentButton = 2
                                documentLauncher.launch(arrayOf("image/*", "application/pdf"))
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.UploadFile, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (vm.nationalIdBackUri != null) "ID Back Uploaded" else "Upload National ID Back")
                        }

                        OutlinedButton(
                            onClick = {
                                currentDocumentButton = 3
                                documentLauncher.launch(arrayOf("image/*", "application/pdf"))
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.UploadFile, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (vm.policeClearanceUri != null) "Clearance Uploaded" else "Upload Police Clearance")
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
                        onClick = { scope.launch {

                            val location = locationHelper.getCurrentLocation()

                            if (location != null) {

                                vm.updateLocation(
                                    location.latitude,
                                    location.longitude
                                )

                            }

                            vm.completeProviderSetup(uid)

                        } },
                        enabled = !vm.isLoading && 
                                vm.name.isNotBlank() &&
                                vm.phone.isNotBlank() &&
                                vm.select.isNotBlank() &&
                                vm.price.isNotBlank() &&
                                vm.governorate.isNotBlank() &&
                                vm.district.isNotBlank() &&
                                vm.profilePhotoUri != null &&
                                vm.nationalIdFrontUri != null &&
                                vm.nationalIdBackUri != null &&
                                vm.policeClearanceUri != null,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00796B),
                            disabledContainerColor = Color.Gray
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp)
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
