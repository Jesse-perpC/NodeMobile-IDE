package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ProjectRepository
import com.example.model.AuditCategory
import com.example.model.AuditRecord
import com.example.model.AuditStatus
import com.example.model.UserProfile
import com.example.model.UserRole
import com.example.ui.theme.AmberAlert
import com.example.ui.theme.CodeFontFamily
import com.example.ui.theme.CoralRed
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
import com.example.ui.theme.VioletAccent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AnalyticsAuditScreen(
    repository: ProjectRepository,
    modifier: Modifier = Modifier
) {
    val currentUser by repository.currentUser.collectAsState()
    val auditLogs by repository.auditLogs.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0: Dashboard Analytics & RBAC, 1: Immutable Audit Trail
    var auditSearchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("ALL") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SlateDarkest)
    ) {
        // Tab Row
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = SlateBackground,
            contentColor = TextPrimary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = NodeEmerald
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Analytics, contentDescription = "Analytics", tint = if (selectedTab == 0) NodeEmerald else TextMuted, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Analytics & RBAC", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Policy, contentDescription = "Audit", tint = if (selectedTab == 1) ElectricCyan else TextMuted, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Audit Trail (${auditLogs.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        if (selectedTab == 0) {
            // Dashboard Analytics & RBAC Screen
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Role-Based Access Control Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("rbac_card"),
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AdminPanelSettings, contentDescription = "RBAC", tint = NodeEmerald, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Role-Based Access Control",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "${currentUser.name} (${currentUser.email})",
                                        fontFamily = CodeFontFamily,
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                            }

                            Surface(
                                color = NodeEmerald.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = currentUser.role.name,
                                    fontFamily = CodeFontFamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NodeEmeraldLight,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Switch active profile role to simulate team permissions in real-time:",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )

                        // Role switcher buttons
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            UserRole.entries.forEach { role ->
                                val isSelected = currentUser.role == role
                                Surface(
                                    onClick = { repository.switchUserRole(role) },
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isSelected) NodeEmerald.copy(alpha = 0.25f) else SlateSurfaceVariant,
                                    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, NodeEmerald) else null,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("role_button_${role.name}")
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = role.displayName,
                                            fontFamily = CodeFontFamily,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) NodeEmeraldLight else TextPrimary
                                        )
                                    }
                                }
                            }
                        }

                        // Granular Permissions Matrix
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = SlateDarkest,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                PermissionRow("Edit Source Code", currentUser.role.canEditCode())
                                PermissionRow("Execute Scripts & Containers", currentUser.role.canRunScripts())
                                PermissionRow("Deploy to Production Pipeline", currentUser.role.canDeploy())
                                PermissionRow("Approve Pull Requests", currentUser.role.canReviewPR())
                                PermissionRow("Manage Secrets & Environment", currentUser.role.canManageSecrets())
                            }
                        }
                    }
                }

                // Analytics KPI Stat Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard("Deployment Success", "98.4%", "124 releases", NodeEmeraldLight, modifier = Modifier.weight(1f))
                    StatCard("Avg API Latency", "14ms", "Express v4.19", ElectricCyan, modifier = Modifier.weight(1f))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard("Active Containers", "2 OCI", "ARM64 Sandbox", VioletAccent, modifier = Modifier.weight(1f))
                    StatCard("Commit Velocity", "14 / wk", "3 offline pending", AmberAlert, modifier = Modifier.weight(1f))
                }

                // Multi-Language Environment Compatibility Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SlateSurface),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Code, contentDescription = "Languages", tint = ElectricCyan, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Multi-Language Environment",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = TextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        val envs = listOf(
                            Triple("Node.js (V8 Engine)", "v20.18.0 LTS (ARM64)", "Native Core"),
                            Triple("TypeScript Runtime", "v5.4.5 Transpiler", "Active"),
                            Triple("Python Analytics Sandbox", "v3.11-slim OCI", "Containerized"),
                            Triple("Redis In-Memory Cache", "v7.2.4 Alpine", "Running :6379"),
                            Triple("Linux Shell & REPL", "GNU Bash 5.2 Emulation", "Integrated")
                        )

                        envs.forEach { (name, ver, status) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(name, fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                                    Text(ver, fontFamily = CodeFontFamily, fontSize = 10.sp, color = TextMuted)
                                }
                                Surface(
                                    color = SlateSurfaceVariant,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = status,
                                        fontFamily = CodeFontFamily,
                                        fontSize = 10.sp,
                                        color = NodeEmeraldLight,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Immutable Audit Trail Screen
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Search & Filter
                OutlinedTextField(
                    value = auditSearchQuery,
                    onValueChange = { auditSearchQuery = it },
                    placeholder = { Text("Search actor, action, or target...", fontSize = 12.sp, color = TextMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = SlateBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Category Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("ALL", "CONTAINER_DEPLOY", "SCRIPT_EXEC", "GIT_COMMIT", "GIT_PUSH", "ROLE_CHANGE", "SECURITY_ALERT").forEach { cat ->
                        FilterChip(
                            selected = selectedCategoryFilter == cat,
                            onClick = { selectedCategoryFilter = cat },
                            label = { Text(cat.replace("_", " "), fontSize = 10.sp, fontFamily = CodeFontFamily) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ElectricCyan.copy(alpha = 0.25f),
                                selectedLabelColor = ElectricCyan
                            )
                        )
                    }
                }

                val filteredLogs = remember(auditLogs, auditSearchQuery, selectedCategoryFilter) {
                    auditLogs.filter { record ->
                        val matchesCat = selectedCategoryFilter == "ALL" || record.category.name == selectedCategoryFilter
                        val matchesQuery = auditSearchQuery.isBlank() ||
                            record.actor.contains(auditSearchQuery, ignoreCase = true) ||
                            record.action.contains(auditSearchQuery, ignoreCase = true) ||
                            record.target.contains(auditSearchQuery, ignoreCase = true) ||
                            record.details.contains(auditSearchQuery, ignoreCase = true)
                        matchesCat && matchesQuery
                    }
                }

                Text(
                    text = "Showing ${filteredLogs.size} verified tamper-evident security records",
                    fontSize = 11.sp,
                    color = TextMuted
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredLogs, key = { it.id }) { record ->
                        val timeStr = remember(record.timestamp) {
                            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(record.timestamp))
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = SlateSurface),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = record.action,
                                            fontFamily = CodeFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = when (record.status) {
                                                AuditStatus.BLOCKED -> CoralRed
                                                AuditStatus.WARNING -> AmberAlert
                                                else -> NodeEmeraldLight
                                            }
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "[${record.category.name}]",
                                            fontFamily = CodeFontFamily,
                                            fontSize = 10.sp,
                                            color = TextMuted
                                        )
                                    }

                                    Surface(
                                        color = if (record.status == AuditStatus.SUCCESS) NodeEmerald.copy(alpha = 0.15f) else CoralRed.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = record.status.name,
                                            fontFamily = CodeFontFamily,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (record.status == AuditStatus.SUCCESS) NodeEmeraldLight else CoralRed,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = "Target: ${record.target}",
                                    fontFamily = CodeFontFamily,
                                    fontSize = 11.sp,
                                    color = TextPrimary,
                                    modifier = Modifier.padding(top = 2.dp)
                                )

                                if (record.details.isNotBlank()) {
                                    Text(
                                        text = record.details,
                                        fontSize = 11.sp,
                                        color = TextSecondary,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$timeStr • Actor: ${record.actor} (${record.role.name}) • ${record.ipDevice}",
                                    fontFamily = CodeFontFamily,
                                    fontSize = 10.sp,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PermissionRow(label: String, granted: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 11.sp, color = TextSecondary)
        Text(
            text = if (granted) "ALLOWED" else "DENIED",
            fontFamily = CodeFontFamily,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (granted) NodeEmeraldLight else CoralRed
        )
    }
}

@Composable
fun StatCard(title: String, value: String, subtitle: String, accentColor: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = SlateSurface),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = title, fontSize = 11.sp, color = TextMuted)
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                modifier = Modifier.padding(vertical = 2.dp)
            )
            Text(text = subtitle, fontFamily = CodeFontFamily, fontSize = 10.sp, color = TextSecondary)
        }
    }
}
