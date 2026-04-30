package com.sanay3y.egy.presentation.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun RoleSelectionScreen(
    onRoleSelected: (String) -> Unit,
    onNavigateToLogin: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Sanay3y",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            "Choose your role",
            style = MaterialTheme.typography.headlineSmall
        )

        Text(
            "Join our community of experts and homeowners to get things done with confidence and quality.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

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
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            onClick = { onRoleSelected("client") }
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
                Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            onClick = { onRoleSelected("provider") }
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row {
            Text("Already have an account?")
            TextButton(onClick = onNavigateToLogin) {
                Text("Log in here")
            }
        }
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
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(10.dp)
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
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            points.forEach {
                Text("• $it", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(4.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(buttonText)
            }
        }
    }
}