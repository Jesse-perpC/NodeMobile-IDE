package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.AiCodeAssistant
import com.example.ai.AiResponse
import com.example.model.ProjectFile
import com.example.ui.components.QuickCodeBar
import com.example.ui.components.SyntaxHighlighter
import com.example.ui.theme.CodeFontFamily
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    files: List<ProjectFile>,
    activeFileId: String,
    openTabIds: List<String>,
    onSelectFile: (String) -> Unit,
    onCloseTab: (String) -> Unit,
    onContentChange: (String, String) -> Unit,
    onRunScript: (ProjectFile) -> Unit,
    onToggleExplorer: () -> Unit,
    onOpenLivePreview: () -> Unit,
    canEdit: Boolean,
    modifier: Modifier = Modifier
) {
    val activeFile = files.firstOrNull { it.id == activeFileId } ?: files.firstOrNull()
    var showAiSheet by remember { mutableStateOf(false) }
    var showSearch by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val aiAssistant = remember { AiCodeAssistant() }
    var aiState by remember { mutableStateOf<AiResponse>(AiResponse.Idle) }
    var customAiPrompt by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SlateDarkest)
    ) {
        // Tab Bar & Action Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SlateBackground)
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onToggleExplorer,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("toggle_explorer_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "File Explorer",
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Open tabs list
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                openTabIds.forEach { tabId ->
                    val file = files.firstOrNull { it.id == tabId }
                    if (file != null) {
                        val isActive = file.id == activeFileId

                        Surface(
                            onClick = { onSelectFile(file.id) },
                            shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp),
                            color = if (isActive) SlateDarkest else SlateSurface,
                            border = if (isActive) androidx.compose.foundation.BorderStroke(1.dp, SlateBorder) else null,
                            modifier = Modifier.height(34.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = file.name,
                                    fontFamily = CodeFontFamily,
                                    fontSize = 12.sp,
                                    color = if (isActive) NodeEmeraldLight else TextSecondary,
                                    fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal
                                )

                                if (file.isModified) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .background(NodeEmerald, CircleShape)
                                    )
                                }

                                if (openTabIds.size > 1) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    IconButton(
                                        onClick = { onCloseTab(file.id) },
                                        modifier = Modifier.size(16.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Close tab",
                                            tint = TextMuted,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Run Node Script Button
            Button(
                onClick = {
                    if (activeFile != null) {
                        onRunScript(activeFile)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NodeEmerald),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier
                    .height(32.dp)
                    .padding(horizontal = 4.dp)
                    .testTag("run_node_script_button"),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Run Script",
                    tint = Color.Black,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Run", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            // AI Assistant Button
            FilledTonalButton(
                onClick = { showAiSheet = true },
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier
                    .height(32.dp)
                    .testTag("ai_assistant_trigger"),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = VioletAccent.copy(alpha = 0.25f),
                    contentColor = VioletAccent
                ),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "AI Assistant",
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("AI", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        HorizontalDivider(color = SlateBorder.copy(alpha = 0.5f), thickness = 1.dp)

        // Read-only indicator if user role doesn't have edit privileges
        if (!canEdit) {
            Surface(
                color = SlateSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "🔒 View-Only Mode: Your active role cannot modify source files",
                    fontFamily = CodeFontFamily,
                    fontSize = 11.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }

        // Editor Workspace (Line Numbers + Code Canvas)
        if (activeFile != null) {
            val lines = remember(activeFile.content) { activeFile.content.lines() }
            val scrollState = rememberScrollState()

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    // Line numbers gutter
                    Column(
                        modifier = Modifier
                            .background(SlateSurface.copy(alpha = 0.6f))
                            .padding(horizontal = 8.dp, vertical = 8.dp)
                            .width(36.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        for (i in 1..lines.size) {
                            Text(
                                text = "$i",
                                fontFamily = CodeFontFamily,
                                fontSize = 12.sp,
                                color = TextMuted,
                                lineHeight = 20.sp
                            )
                        }
                    }

                    // Code editing field with real-time syntax styling
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                            .horizontalScroll(rememberScrollState())
                    ) {
                        BasicTextField(
                            value = activeFile.content,
                            onValueChange = { newText ->
                                if (canEdit) {
                                    onContentChange(activeFile.id, newText)
                                }
                            },
                            readOnly = !canEdit,
                            textStyle = TextStyle(
                                fontFamily = CodeFontFamily,
                                fontSize = 12.sp,
                                color = TextPrimary,
                                lineHeight = 20.sp
                            ),
                            cursorBrush = SolidColor(NodeEmerald),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("code_editor_input"),
                            visualTransformation = {
                                androidx.compose.ui.text.input.TransformedText(
                                    SyntaxHighlighter.highlight(it.text, activeFile.language),
                                    androidx.compose.ui.text.input.OffsetMapping.Identity
                                )
                            }
                        )
                    }
                }
            }

            // Quick Code Bar pinned for mobile development
            QuickCodeBar(
                onInsertText = { token ->
                    if (canEdit) {
                        val updated = activeFile.content + token
                        onContentChange(activeFile.id, updated)
                    }
                }
            )
        }
    }

    // AI Code Assistant Modal Bottom Sheet
    if (showAiSheet && activeFile != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        ModalBottomSheet(
            onDismissRequest = { showAiSheet = false },
            sheetState = sheetState,
            containerColor = SlateSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Assistant",
                            tint = VioletAccent,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "NodeForge AI Assistant",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextPrimary
                        )
                    }

                    IconButton(onClick = { showAiSheet = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Text(
                    text = "Analyzing active file: ${activeFile.name} (${activeFile.language.displayName})",
                    fontFamily = CodeFontFamily,
                    fontSize = 12.sp,
                    color = TextMuted
                )

                // Quick AI Actions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val actions = listOf(
                        "EXPLAIN" to "Explain Code",
                        "FIX_BUGS" to "Detect Bugs",
                        "GENERATE_ENDPOINT" to "Generate Endpoint",
                        "WRITE_TESTS" to "Write Tests",
                        "SECURITY_REVIEW" to "Security Audit"
                    )

                    actions.forEach { (type, label) ->
                        FilledTonalButton(
                            onClick = {
                                scope.launch {
                                    aiState = AiResponse.Loading
                                    aiState = aiAssistant.analyzeCode(activeFile.content, activeFile.name, type)
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(containerColor = SlateSurfaceVariant)
                        ) {
                            Text(label, fontSize = 12.sp, color = TextPrimary)
                        }
                    }
                }

                // Custom AI prompt input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = customAiPrompt,
                        onValueChange = { customAiPrompt = it },
                        placeholder = { Text("Ask anything about this code...", fontSize = 12.sp, color = TextMuted) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("ai_custom_prompt_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VioletAccent,
                            unfocusedBorderColor = SlateBorder
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (customAiPrompt.isNotBlank()) {
                                scope.launch {
                                    aiState = AiResponse.Loading
                                    aiState = aiAssistant.analyzeCode(activeFile.content, activeFile.name, "CUSTOM", customAiPrompt)
                                    customAiPrompt = ""
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = VioletAccent),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Ask")
                    }
                }

                // AI Response Display
                when (val res = aiState) {
                    is AiResponse.Loading -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(color = VioletAccent, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Analyzing code architecture...", color = TextSecondary, fontSize = 13.sp)
                        }
                    }
                    is AiResponse.Success -> {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SlateDarkest,
                            border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = res.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = VioletAccent
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = res.explanation,
                                    fontSize = 12.sp,
                                    color = TextPrimary,
                                    lineHeight = 18.sp
                                )

                                if (res.suggestedCode != null) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = SlateSurface,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Text(
                                                text = res.suggestedCode,
                                                fontFamily = CodeFontFamily,
                                                fontSize = 11.sp,
                                                color = NodeEmeraldLight,
                                                lineHeight = 16.sp
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = {
                                            if (canEdit) {
                                                val merged = activeFile.content + "\n\n" + res.suggestedCode
                                                onContentChange(activeFile.id, merged)
                                                showAiSheet = false
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = NodeEmerald),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.testTag("apply_ai_code_button")
                                    ) {
                                        Text("Append to ${activeFile.name}", color = Color.Black, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}
