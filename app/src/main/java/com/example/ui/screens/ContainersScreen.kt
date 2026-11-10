package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.ContainerManager
import com.example.engine.PipelineEngine
import com.example.model.ContainerStatus
import com.example.model.DeploymentPipeline
import com.example.model.NodeContainer
import com.example.model.PipelineStatus
import com.example.model.UserProfile
import com.example.model.UserRole
import com.example.ui.theme.AmberAlert
import com.example.ui.theme.CodeFontFamily
import com.example.ui.theme.CoralRed
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NodeEmerald
import com.example.ui.theme.NodeEmeraldLight
import com.example.ui.theme.SlateBackground
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateDarkest
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.SlateSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun ContainersScreen(
    containerManager: ContainerManager,
    pipelineEngine: PipelineEngine,
    currentUser: UserProfile,
    onOpenLivePreview: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val containers by containerManager.containers.collectAsState()
    val pipeline by pipelineEngine.pipeline.collectAsState()
    val scope = rememberCoroutineScope()

    var showSpawnDialog by remember { mutableStateOf(false) }
    var newContainerName by remember { mutableStateOf("") }
    var selectedImage by remember { mutableStateOf("node:20-alpine") }
    var portMapping by remember { mutableStateOf("3000:3000") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SlateDarkest)
            .verticalScroll(rememberScrollState())
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ViewInAr,
                        contentDescription = "Containers",
                        tint = ElectricCyan,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Containerized Runtime",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                }
                Text(
                    text = "${containers.count { it.status == ContainerStatus.RUNNING }} Running • ARM64 Android Sandbox",
                    fontFamily = CodeFontFamily,
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }

            Button(
                onClick = { showSpawnDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("spawn_container_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New Container",
                    tint = Color.Black,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Spawn", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        // Automated Deployment Pipeline Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("deployment_pipeline_card"),
            colors = CardDefaults.cardColors(containerColor = SlateSurface),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.RocketLaunch,
                            contentDescription = "CI/CD Pipeline",
                            tint = NodeEmerald,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Automated CI/CD Pipeline",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextPrimary
                        )
                    }

                    Surface(
                        color = when (pipeline.status) {
                            PipelineStatus.SUCCESS -> NodeEmerald.copy(alpha = 0.2f)
                            PipelineStatus.RUNNING -> AmberAlert.copy(alpha = 0.2f)
                            PipelineStatus.FAILED -> CoralRed.copy(alpha = 0.2f)
                            else -> SlateSurfaceVariant
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = pipeline.status.name,
                            fontFamily = CodeFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (pipeline.status) {
                                PipelineStatus.SUCCESS -> NodeEmeraldLight
                                PipelineStatus.RUNNING -> AmberAlert
                                PipelineStatus.FAILED -> CoralRed
                                else -> TextMuted
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Text(
                    text = "Automated multi-stage container deployment pipeline for isolated production releases.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(vertical = 6.dp)
                )

                // Pipeline Step Progression
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    pipeline.steps.forEachIndexed { index, step ->
                        val isCurrent = pipeline.status == PipelineStatus.RUNNING && pipeline.currentStepIndex == index

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (isCurrent) SlateSurfaceVariant else SlateDarkest.copy(alpha = 0.5f),
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                when (step.status) {
                                    PipelineStatus.SUCCESS -> Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Success",
                                        tint = NodeEmerald,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    PipelineStatus.RUNNING -> CircularProgressIndicator(
                                        color = AmberAlert,
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    else -> Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(SlateBorder, CircleShape)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = step.name,
                                    fontSize = 12.sp,
                                    color = if (step.status == PipelineStatus.SUCCESS) TextPrimary else TextSecondary,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                                )
                            }

                            if (step.durationMs > 0) {
                                Text(
                                    text = "${step.durationMs}ms",
                                    fontFamily = CodeFontFamily,
                                    fontSize = 11.sp,
                                    color = NodeEmeraldLight
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            scope.launch {
                                pipelineEngine.runPipeline(currentUser.role, currentUser.name)
                            }
                        },
                        enabled = pipeline.status != PipelineStatus.RUNNING,
                        colors = ButtonDefaults.buttonColors(containerColor = NodeEmerald),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("trigger_deployment_pipeline")
                    ) {
                        Text(
                            text = if (pipeline.status == PipelineStatus.RUNNING) "Deploying..." else "Deploy Container",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    ElevatedButton(
                        onClick = { onOpenLivePreview(3000) },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = SlateSurfaceVariant,
                            contentColor = ElectricCyan
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Preview",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Live Preview", fontSize = 12.sp)
                    }
                }
            }
        }

        // Active Containers List
        Text(
            text = "ACTIVE INSTANCES",
            fontFamily = CodeFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = TextMuted
        )

        containers.forEach { container ->
            ContainerCard(
                container = container,
                onStart = { containerManager.startContainer(container.id, currentUser.role, currentUser.name) },
                onStop = { containerManager.stopContainer(container.id, currentUser.role, currentUser.name) },
                onRestart = { containerManager.restartContainer(container.id, currentUser.role, currentUser.name) },
                onRemove = { containerManager.removeContainer(container.id, currentUser.role, currentUser.name) },
                onInspect = { onOpenLivePreview(3000) },
                canManage = currentUser.role.canDeploy() || currentUser.role.canRunScripts()
            )
        }
    }

    // Spawn Container Dialog
    if (showSpawnDialog) {
        val images = listOf("node:20-alpine", "node:18-slim", "node:22-bullseye", "redis:7.2-alpine", "python:3.11-slim")

        AlertDialog(
            onDismissRequest = { showSpawnDialog = false },
            title = { Text("Spawn New Container", color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Select base OCI image & specify configuration:", color = TextSecondary, fontSize = 12.sp)

                    Column {
                        Text("Base Image:", color = TextMuted, fontSize = 11.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            images.take(3).forEach { img ->
                                Surface(
                                    onClick = { selectedImage = img },
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (selectedImage == img) ElectricCyan.copy(alpha = 0.25f) else SlateSurfaceVariant
                                ) {
                                    Text(
                                        text = img.substringBefore("-").replace(":", " "),
                                        fontSize = 11.sp,
                                        fontFamily = CodeFontFamily,
                                        color = if (selectedImage == img) ElectricCyan else TextSecondary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = newContainerName,
                        onValueChange = { newContainerName = it },
                        label = { Text("Container Name") },
                        placeholder = { Text("auth-service-node") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = SlateBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = portMapping,
                        onValueChange = { portMapping = it },
                        label = { Text("Port Mapping (host:container)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = SlateBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        containerManager.spawnContainer(
                            name = newContainerName,
                            image = selectedImage,
                            portMapping = portMapping,
                            entrypoint = "node index.js",
                            envVars = mapOf("NODE_ENV" to "development"),
                            actorRole = currentUser.role,
                            actorName = currentUser.name
                        )
                        showSpawnDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan)
                ) {
                    Text("Launch", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSpawnDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = SlateSurface
        )
    }
}

@Composable
fun ContainerCard(
    container: NodeContainer,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onRestart: () -> Unit,
    onRemove: () -> Unit,
    onInspect: () -> Unit,
    canManage: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("container_card_${container.id}"),
        colors = CardDefaults.cardColors(containerColor = SlateSurface),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = container.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "[${container.image}]",
                            fontFamily = CodeFontFamily,
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }

                    Text(
                        text = "Ports: ${container.portMapping} • Uptime: ${container.uptimeSeconds}s",
                        fontFamily = CodeFontFamily,
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                // Status Badge
                Surface(
                    color = when (container.status) {
                        ContainerStatus.RUNNING -> NodeEmerald.copy(alpha = 0.2f)
                        ContainerStatus.STOPPED -> SlateSurfaceVariant
                        ContainerStatus.BUILDING -> AmberAlert.copy(alpha = 0.2f)
                        ContainerStatus.ERROR -> CoralRed.copy(alpha = 0.2f)
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = container.status.name,
                        fontFamily = CodeFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (container.status) {
                            ContainerStatus.RUNNING -> NodeEmeraldLight
                            ContainerStatus.STOPPED -> TextMuted
                            ContainerStatus.BUILDING -> AmberAlert
                            ContainerStatus.ERROR -> CoralRed
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            // Real-time telemetry indicators
            if (container.status == ContainerStatus.RUNNING) {
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Memory: ${container.memoryUsageMb} MB / ${container.memoryLimitMb} MB",
                        fontFamily = CodeFontFamily,
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = "CPU: ${container.cpuPercentage}%",
                        fontFamily = CodeFontFamily,
                        fontSize = 11.sp,
                        color = if (container.cpuPercentage > 10f) AmberAlert else NodeEmeraldLight
                    )
                }

                val progress = (container.memoryUsageMb.toFloat() / container.memoryLimitMb).coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .padding(top = 4.dp),
                    color = ElectricCyan,
                    trackColor = SlateSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (container.status == ContainerStatus.RUNNING) {
                    IconButton(onClick = onStop, enabled = canManage, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Stop, contentDescription = "Stop", tint = AmberAlert, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onRestart, enabled = canManage, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Refresh, contentDescription = "Restart", tint = ElectricCyan, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onInspect, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Language, contentDescription = "Inspect", tint = NodeEmerald, modifier = Modifier.size(18.dp))
                    }
                } else {
                    IconButton(onClick = onStart, enabled = canManage, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Start", tint = NodeEmerald, modifier = Modifier.size(18.dp))
                    }
                }

                IconButton(onClick = onRemove, enabled = canManage, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Remove", tint = TextMuted, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
