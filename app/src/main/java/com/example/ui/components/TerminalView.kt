package com.example.ui.components

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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LogLevel
import com.example.model.TerminalLog
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
import com.example.ui.theme.VioletAccent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TerminalView(
    logs: List<TerminalLog>,
    onExecuteCommand: (String) -> Unit,
    onClearLogs: () -> Unit,
    isNodeRunning: Boolean,
    modifier: Modifier = Modifier
) {
    var commandInput by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") }
    val listState = rememberLazyListState()

    // Auto-scroll on new log entry
    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            listState.animateScrollToItem(logs.size - 1)
        }
    }

    val filteredLogs = remember(logs, selectedFilter) {
        when (selectedFilter) {
            "ERRORS" -> logs.filter { it.level == LogLevel.ERROR }
            "HTTP" -> logs.filter { it.source == "http" || it.source == "express" }
            "CONTAINER" -> logs.filter { it.source == "docker" || it.source == "deploy" }
            "GIT" -> logs.filter { it.source == "git" }
            else -> logs
        }
    }

    val quickCommands = listOf(
        "git clone https://github.com/paperclipai/paperclip.git",
        "npx paperclipai onboard",
        "node server.js",
        "npm test",
        "docker ps",
        "git status",
        "curl http://localhost:3100/api/agents",
        "help"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SlateDarkest)
    ) {
        // Top Terminal Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SlateBackground)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Terminal,
                    contentDescription = "Terminal",
                    tint = NodeEmerald,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "NodeForge Shell & REPL",
                    fontFamily = CodeFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.width(8.dp))
                // Process Indicator
                Surface(
                    color = if (isNodeRunning) NodeEmerald.copy(alpha = 0.2f) else SlateSurfaceVariant,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isNodeRunning) "● RUNNING" else "○ IDLE",
                        fontFamily = CodeFontFamily,
                        fontSize = 10.sp,
                        color = if (isNodeRunning) NodeEmeraldLight else TextMuted,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            IconButton(
                onClick = onClearLogs,
                modifier = Modifier
                    .size(28.dp)
                    .testTag("clear_terminal_button")
            ) {
                Icon(
                    imageVector = Icons.Default.ClearAll,
                    contentDescription = "Clear logs",
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Filter chips bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SlateBackground)
                .padding(horizontal = 12.dp, vertical = 4.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("ALL", "ERRORS", "HTTP", "CONTAINER", "GIT").forEach { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { selectedFilter = filter },
                    label = { Text(filter, fontSize = 11.sp, fontFamily = CodeFontFamily) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NodeEmerald.copy(alpha = 0.25f),
                        selectedLabelColor = NodeEmeraldLight,
                        containerColor = SlateSurface,
                        labelColor = TextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedFilter == filter,
                        borderColor = if (selectedFilter == filter) NodeEmerald else SlateBorder
                    ),
                    modifier = Modifier.height(26.dp)
                )
            }
        }

        HorizontalDivider(color = SlateBorder.copy(alpha = 0.5f), thickness = 1.dp)

        // Log streaming window
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            items(filteredLogs, key = { it.id }) { log ->
                val timeString = remember(log.timestamp) {
                    SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date(log.timestamp))
                }

                val logColor = when (log.level) {
                    LogLevel.ERROR -> CoralRed
                    LogLevel.WARN -> AmberAlert
                    LogLevel.SUCCESS -> NodeEmeraldLight
                    LogLevel.DEBUG -> VioletAccent
                    LogLevel.SYSTEM -> ElectricCyan
                    LogLevel.INFO -> TextPrimary
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = timeString,
                        fontFamily = CodeFontFamily,
                        fontSize = 10.sp,
                        color = TextMuted,
                        modifier = Modifier.width(72.dp)
                    )

                    Surface(
                        color = SlateSurfaceVariant.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Text(
                            text = log.source.uppercase(),
                            fontFamily = CodeFontFamily,
                            fontSize = 9.sp,
                            color = when (log.source) {
                                "docker", "deploy" -> CyanGlow
                                "git" -> VioletAccent
                                "http", "express" -> NodeEmeraldLight
                                "stderr" -> CoralRed
                                else -> TextSecondary
                            },
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }

                    Text(
                        text = log.text,
                        fontFamily = CodeFontFamily,
                        fontSize = 12.sp,
                        color = logColor,
                        lineHeight = 16.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Quick Command Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SlateBackground)
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            quickCommands.forEach { cmd ->
                AssistChip(
                    onClick = {
                        commandInput = cmd
                        onExecuteCommand(cmd)
                        commandInput = ""
                    },
                    label = { Text(cmd, fontFamily = CodeFontFamily, fontSize = 11.sp) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = SlateSurfaceVariant,
                        labelColor = TextPrimary
                    ),
                    border = AssistChipDefaults.assistChipBorder(enabled = true, borderColor = SlateBorder),
                    modifier = Modifier.height(28.dp)
                )
            }
        }

        // Terminal Command Line Input
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SlateBackground)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$ ",
                fontFamily = CodeFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = NodeEmerald,
                modifier = Modifier.padding(start = 4.dp, end = 6.dp)
            )

            OutlinedTextField(
                value = commandInput,
                onValueChange = { commandInput = it },
                modifier = Modifier
                    .weight(1f)
                    .testTag("terminal_command_input"),
                placeholder = {
                    Text("Type command (node, npm, git, docker, or JS expression)", fontFamily = CodeFontFamily, fontSize = 12.sp, color = TextMuted)
                },
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontFamily = CodeFontFamily,
                    fontSize = 13.sp,
                    color = TextPrimary
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NodeEmerald,
                    unfocusedBorderColor = SlateBorder,
                    cursorColor = NodeEmerald
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (commandInput.isNotBlank()) {
                        onExecuteCommand(commandInput.trim())
                        commandInput = ""
                    }
                })
            )

            IconButton(
                onClick = {
                    if (commandInput.isNotBlank()) {
                        onExecuteCommand(commandInput.trim())
                        commandInput = ""
                    }
                },
                modifier = Modifier
                    .padding(start = 4.dp)
                    .testTag("send_command_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Execute",
                    tint = NodeEmerald
                )
            }
        }
    }
}
