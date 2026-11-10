package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.example.model.FileLanguage
import com.example.model.ProjectFile
import com.example.ui.theme.CodeFontFamily
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NodeEmerald
import com.example.ui.theme.NodeEmeraldLight
import com.example.ui.theme.SlateBackground
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.SlateSurfaceVariant
import com.example.ui.theme.SyntaxKeyword
import com.example.ui.theme.SyntaxNumber
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletAccent

@Composable
fun FileTreeDrawer(
    files: List<ProjectFile>,
    activeFileId: String,
    onSelectFile: (String) -> Unit,
    onCreateFile: (String) -> Unit,
    onDeleteFile: (String) -> Unit,
    onLoadTemplate: (String) -> Unit,
    onCloneRepository: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showNewFileDialog by remember { mutableStateOf(false) }
    var newFileName by remember { mutableStateOf("") }
    var showTemplatesDialog by remember { mutableStateOf(false) }
    var showCloneDialog by remember { mutableStateOf(false) }
    var cloneRepoUrl by remember { mutableStateOf("https://github.com/paperclipai/paperclip.git") }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(280.dp)
            .background(SlateBackground)
            .padding(12.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.FolderOpen,
                    contentDescription = "Project Files",
                    tint = NodeEmerald,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "EXPLORER",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextPrimary
                )
            }

            Row {
                IconButton(
                    onClick = { showCloneDialog = true },
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("clone_repo_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudDownload,
                        contentDescription = "Clone Git Repo",
                        tint = VioletAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = { showTemplatesDialog = true },
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("templates_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = "Load Template",
                        tint = ElectricCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = { showNewFileDialog = true },
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("new_file_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create File",
                        tint = NodeEmerald,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Text(
            text = "node-mobile-service [workspace]",
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        HorizontalDivider(color = SlateBorder, thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))

        // Files List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(files, key = { it.id }) { file ->
                val isSelected = file.id == activeFileId

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = if (isSelected) SlateSurfaceVariant else Color.Transparent,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .clickable { onSelectFile(file.id) }
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                        .testTag("file_item_${file.id}"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        val iconColor = when (file.language) {
                            FileLanguage.JAVASCRIPT -> SyntaxNumber
                            FileLanguage.TYPESCRIPT -> ElectricCyan
                            FileLanguage.JSON -> SyntaxKeyword
                            FileLanguage.DOCKERFILE -> ElectricCyan
                            FileLanguage.MARKDOWN -> VioletAccent
                            else -> NodeEmerald
                        }

                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = file.language.displayName,
                            tint = iconColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = file.name,
                            fontFamily = CodeFontFamily,
                            fontSize = 13.sp,
                            color = if (isSelected) TextPrimary else TextSecondary,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        )

                        if (file.isModified) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(NodeEmerald, CircleShape)
                            )
                        }
                    }

                    if (files.size > 1 && file.name != "package.json") {
                        IconButton(
                            onClick = { onDeleteFile(file.id) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete file",
                                tint = TextMuted,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // New File Dialog
    if (showNewFileDialog) {
        AlertDialog(
            onDismissRequest = { showNewFileDialog = false },
            title = { Text("Create New File", color = TextPrimary) },
            text = {
                Column {
                    Text("Enter file name (e.g., utils.js, api.routes.js, config.json):", color = TextSecondary, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newFileName,
                        onValueChange = { newFileName = it },
                        singleLine = true,
                        placeholder = { Text("routes/auth.js") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NodeEmerald,
                            unfocusedBorderColor = SlateBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_file_name_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newFileName.isNotBlank()) {
                            onCreateFile(newFileName.trim())
                            newFileName = ""
                            showNewFileDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NodeEmerald),
                    modifier = Modifier.testTag("confirm_create_file")
                ) {
                    Text("Create", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewFileDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = SlateSurface
        )
    }

    // Templates Dialog
    if (showTemplatesDialog) {
        AlertDialog(
            onDismissRequest = { showTemplatesDialog = false },
            title = { Text("Load Project Template", color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Select a starter template for rapid mobile Node.js development:", color = TextSecondary, fontSize = 13.sp)

                    val templates = listOf(
                        Triple("PAPERCLIP", "📎 Paperclip AI Agent Control Plane", "paperclipai/paperclip: Multi-agent orchestration, company org chart & heartbeats (Port 3100)"),
                        Triple("EXPRESS", "🚀 Express REST Microservice", "Full REST API with health check, task routes, Dockerfile"),
                        Triple("MICROSERVICE", "⚡ Background Queue Worker", "Worker queue metrics with docker-compose.yml"),
                        Triple("WEB_HTML", "🌐 Web Server + HTML/CSS Client", "Live web app with in-app browser preview"),
                        Triple("CLI", "🛠️ CLI Tool & Crypto Utilities", "Command line utility script with os and crypto info")
                    )

                    templates.forEach { (type, title, desc) ->
                        Surface(
                            onClick = {
                                onLoadTemplate(type)
                                showTemplatesDialog = false
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = SlateSurfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(title, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp)
                                Text(desc, color = TextMuted, fontSize = 11.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showTemplatesDialog = false }) {
                    Text("Close", color = TextSecondary)
                }
            },
            containerColor = SlateSurface
        )
    }

    // Clone Git Repository Dialog
    if (showCloneDialog) {
        AlertDialog(
            onDismissRequest = { showCloneDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CloudDownload, contentDescription = null, tint = VioletAccent, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Clone Git Repository", color = TextPrimary)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter repository URL to clone and run on Android:", color = TextSecondary, fontSize = 12.sp)

                    OutlinedTextField(
                        value = cloneRepoUrl,
                        onValueChange = { cloneRepoUrl = it },
                        placeholder = { Text("https://github.com/paperclipai/paperclip.git") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VioletAccent,
                            unfocusedBorderColor = SlateBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("clone_repo_url_input")
                    )

                    Text("Quick Suggestion:", color = TextMuted, fontSize = 11.sp)
                    Surface(
                        onClick = { cloneRepoUrl = "https://github.com/paperclipai/paperclip.git" },
                        shape = RoundedCornerShape(6.dp),
                        color = SlateSurfaceVariant
                    ) {
                        Text(
                            text = "📎 paperclipai/paperclip.git",
                            fontFamily = CodeFontFamily,
                            fontSize = 11.sp,
                            color = NodeEmeraldLight,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (cloneRepoUrl.isNotBlank()) {
                            onCloneRepository(cloneRepoUrl.trim())
                            showCloneDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VioletAccent),
                    modifier = Modifier.testTag("confirm_clone_repo_button")
                ) {
                    Text("Clone & Open", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCloneDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = SlateSurface
        )
    }
}
