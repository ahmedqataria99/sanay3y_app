package com.sanay3y.egy.presentation.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sanay3y.egy.data.model.UserRole
import com.sanay3y.egy.presentation.viewmodel.AuthState
import com.sanay3y.egy.presentation.viewmodel.AuthViewModel

@Composable
fun RoleSelectionScreen(
    uid: String,
    viewModel: AuthViewModel,
    onRoleAssigned: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val authState by viewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        if (authState is AuthState.Success && (authState as AuthState.Success).hasRole) {
            onRoleAssigned()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Sanay3y",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Choose your role",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Join our community of experts and homeowners to get things done with confidence and quality.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (authState is AuthState.Loading) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            RoleCard(
                title = "Continue as Client",
                subtitle = "Find experts for your home",
                points = listOf(
                    "Browse vetted experts",
                    "Post tasks & get quotes",
                    "Verified user reviews"
                ),
                buttonText = "Select Client",
                icon = {
                    Icon(Icons.Default.Home, null, tint = MaterialTheme.colorScheme.primary)
                },
                onClick = { viewModel.selectRole(uid, UserRole.CLIENT) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            RoleCard(
                title = "Continue as Service Provider",
                subtitle = "Grow your business",
                points = listOf(
                    "Showcase your portfolio",
                    "Access high-quality job leads",
                    "Manage service history"
                ),
                buttonText = "Select Provider",
                icon = {
                    Icon(Icons.Default.Build, null, tint = MaterialTheme.colorScheme.primary)
                },
                onClick = { viewModel.selectRole(uid, UserRole.PROVIDER) }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun RoleCard(
    title: String,
    subtitle: String,
    points: List<String>,
    buttonText: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {

        Column(modifier = Modifier.padding(16.dp)) {

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(title, style = MaterialTheme.typography.titleMedium)

            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            points.forEach {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(it, style = MaterialTheme.typography.bodySmall)
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(buttonText)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RoleSelectionScreenPreview() {
    RoleSelectionScreen(
        uid = "123",
        viewModel = AuthViewModel(),
        onRoleAssigned = {},
        onNavigateBack = {}
    )
}
