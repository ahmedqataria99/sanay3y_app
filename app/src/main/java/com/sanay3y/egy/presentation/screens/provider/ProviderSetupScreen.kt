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
import androidx.compose.ui.res.stringResource
import com.sanay3y.egy.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@Composable
fun ProviderSetupScreen(
    uid: String,
    navController: NavController,
    vm: ProviderSetupViewModel = viewModel(),
    onSetupComplete: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()

    val context = LocalContext.current
    val locationHelper = remember {
        LocationHelper(context)
    }
    LaunchedEffect(vm.isSuccess) {
        if (vm.isSuccess) {
            onSetupComplete()
            // Auth state change will handle navigation in App.kt
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
                text = stringResource(R.string.expert_hub_welcome),
                color = Color.White,
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.setup_desc),
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
            StepItem(1, stringResource(R.string.personal_details), vm.currentStep >= 0)
            StepItem(2, stringResource(R.string.service_settings), vm.currentStep >= 1)
            StepItem(3, stringResource(R.string.work_location), vm.currentStep >= 2)
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
                    stringResource(R.string.profile_identity),
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
                label = { Text(stringResource(R.string.full_name)) }
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
                label = { Text(stringResource(R.string.phone_number)) }
            )
            
            LaunchedEffect(vm.name, vm.phone, vm.currentStep) {
                if (vm.currentStep == 0 && vm.name.isNotBlank() && vm.phone.isNotBlank()) {
                    vm.nextStep()
                }
            }
            
            Spacer(Modifier.size(30.dp))
            val categories = listOf(
                R.string.cat_plumbing to "Plumbing",
                R.string.cat_electrical to "Electrical",
                R.string.cat_cleaning to "Cleaning",
                R.string.cat_carpentry to "Carpentry",
                R.string.cat_painting to "Painting",
                R.string.cat_ac_repair to "AC Repair"
            )
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.HomeRepairService,
                        contentDescription = null,
                        tint = Color(0xFF00796B)
                    )
                    Spacer(Modifier.size(8.dp))
                    Text(
                        stringResource(R.string.service_expertise),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.size(12.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { (resId, categoryName) ->
                        FilterChip(
                            selected = vm.select == categoryName,
                            onClick = { vm.selectCategory(categoryName) },
                            label = { Text(stringResource(resId)) },
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
                    label = { Text(stringResource(R.string.hourly_pricing)) }
                )

                Spacer(Modifier.size(12.dp))

                OutlinedTextField(
                    value = vm.experienceYears,
                    modifier = Modifier.fillMaxWidth(),
                    onValueChange = {
                        if (it.all { char -> char.isDigit() } && it.length <= 2) {
                            vm.updateExperience(it)
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    label = { Text(stringResource(R.string.years_of_experience)) }
                )
                
                LaunchedEffect(vm.price, vm.experienceYears, vm.currentStep) {
                    if (vm.currentStep == 1 && vm.price.isNotBlank() && vm.experienceYears.isNotBlank()) {
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
                            stringResource(R.string.service_area),
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
                            label = { Text(stringResource(R.string.governorate)) },
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
                            label = { Text(stringResource(R.string.district)) },
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

                    /* Removed automatic step advancement for optional location */

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
                                stringResource(R.string.verification),
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
                            Text(if (vm.nationalIdFrontUri != null) stringResource(R.string.id_front_uploaded) else stringResource(R.string.upload_id_front))
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
                            Text(if (vm.nationalIdBackUri != null) stringResource(R.string.id_back_uploaded) else stringResource(R.string.upload_id_back))
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
                            Text(if (vm.policeClearanceUri != null) stringResource(R.string.clearance_uploaded) else stringResource(R.string.upload_clearance))
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
                                vm.experienceYears.isNotBlank() &&
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
                            text = stringResource(R.string.complete_setup),
                            fontSize = 20.sp
                        )
                    }
                    Spacer(Modifier.size(15.dp))
                    Text(
                        text = stringResource(R.string.terms_privacy),
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
        ProviderSetupScreen("test_uid", rememberNavController(), onSetupComplete = {})
    }
}
