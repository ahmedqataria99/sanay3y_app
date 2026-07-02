package com.sanay3y.egy.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LocationSelectionDialog(
    currentGov: String,
    currentDist: String,
    governorates: List<String>,
    districtsMap: Map<String, List<String>>,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var selectedGov by remember { mutableStateOf(currentGov) }
    var selectedDist by remember { mutableStateOf(currentDist) }
    var govExpanded by remember { mutableStateOf(false) }
    var distExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Location") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Governorate
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedGov,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Governorate") },
                        trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, null) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Box(modifier = Modifier.matchParentSize().clickable { govExpanded = true })
                    DropdownMenu(expanded = govExpanded, onDismissRequest = { govExpanded = false }) {
                        governorates.forEach {
                            DropdownMenuItem(text = { Text(it) }, onClick = {
                                selectedGov = it
                                selectedDist = ""
                                govExpanded = false
                            })
                        }
                    }
                }

                // District
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedDist,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("District") },
                        trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, null) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = selectedGov.isNotBlank()
                    )
                    Box(modifier = Modifier.matchParentSize().clickable(enabled = selectedGov.isNotBlank()) { distExpanded = true })
                    DropdownMenu(expanded = distExpanded, onDismissRequest = { distExpanded = false }) {
                        districtsMap[selectedGov]?.forEach {
                            DropdownMenuItem(text = { Text(it) }, onClick = {
                                selectedDist = it
                                distExpanded = false
                            })
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(selectedGov, selectedDist) },
                enabled = selectedGov.isNotBlank() && selectedDist.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
