package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Commit
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Merge
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.model.GitCommit
import com.example.model.PullRequest
import com.example.model.ReviewStatus
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
import com.example.ui.theme.VioletAccent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun GitTeamScreen(
    repository: ProjectRepository,
    currentUser: UserProfile,
    modifier: Modifier = Modifier
) {
    val gitState by repository.gitState.collectAsState()
    val pullRequests by repository.pullRequests.collectAsState()

    var selectedSubTab by remember { mutableStateOf(0) } // 0: Version Control & Offline Commits, 1: Team Reviews & PRs
    var commitMessage by remember { mutableStateOf("") }
    var showAddCommentDialog by remember { mutableStateOf(false) }
    var selectedPrId by remember { mutableStateOf<String?>(null) }
    var newCommentText by remember { mutableStateOf("") }
    var newCommentLine by remember { mutableStateOf("18") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SlateDarkest)
    ) {
        // Sub-tabs
        TabRow(
            selectedTabIndex = selectedSubTab,
            containerColor = SlateBackground,
            contentColor = TextPrimary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedSubTab]),
                    color = NodeEmerald
                )
            }
        ) {
            Tab(
                selected = selectedSubTab == 0,
                onClick = { selectedSubTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Commit, contentDescription = "Git", tint = if (selectedSubTab == 0) NodeEmerald else TextMuted, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Git & Offline Commits", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            )
            Tab(
                selected = selectedSubTab == 1,
                onClick = { selectedSubTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Forum, contentDescription = "PR Reviews", tint = if (selectedSubTab == 1) ElectricCyan else TextMuted, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Team Feedback Loops", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        if (selectedSubTab == 0) {
            // Version Control Tab
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Branch & Remote Sync Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
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
                                Text(
                                    text = "Current Branch: ${gitState.currentBranch}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = TextPrimary
                                )
                                Text(
                                    text = gitState.remoteUrl,
                                    fontFamily = CodeFontFamily,
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }

                            // Remote Sync Button
                            Button(
                                onClick = { repository.syncWithGitHub() },
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                                shape = RoundedCornerShape(8.dp),
                                enabled = !gitState.isSyncing,
                                modifier = Modifier.testTag("github_sync_button")
                            ) {
                                if (gitState.isSyncing) {
                                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(16.dp))
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.CloudUpload,
                                        contentDescription = "Sync",
                                        tint = Color.Black,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Sync GitHub", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        }

                        if (gitState.pendingOfflineCommits > 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = AmberAlert.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "⚡ ${gitState.pendingOfflineCommits} offline commit(s) stored locally. Tap 'Sync GitHub' to push to cloud.",
                                    fontFamily = CodeFontFamily,
                                    fontSize = 11.sp,
                                    color = AmberAlert,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                            }
                        }

                        // Branch Switcher Chips
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            gitState.branches.forEach { branch ->
                                FilterChip(
                                    selected = branch.isCurrent,
                                    onClick = { repository.switchBranch(branch.name) },
                                    label = { Text(branch.name, fontSize = 11.sp, fontFamily = CodeFontFamily) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = NodeEmerald.copy(alpha = 0.2f),
                                        selectedLabelColor = NodeEmeraldLight
                                    )
                                )
                            }
                        }

                        // Quick Clone Remote Repository
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Import from GitHub:", fontSize = 11.sp, color = TextMuted)
                            Surface(
                                onClick = {
                                    repository.cloneRepository("https://github.com/paperclipai/paperclip.git")
                                },
                                shape = RoundedCornerShape(6.dp),
                                color = VioletAccent.copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, VioletAccent.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("📎 Clone paperclipai/paperclip", fontFamily = CodeFontFamily, fontSize = 11.sp, color = VioletAccent, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }

                // Staging & Offline Commit Box
                Card(
                    modifier = Modifier.fillMaxWidth(),
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
                            Text(
                                text = "Working Tree Changes",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = TextPrimary
                            )

                            TextButton(onClick = { repository.stageAll() }) {
                                Text("Stage All", color = NodeEmeraldLight, fontSize = 11.sp)
                            }
                        }

                        // Unstaged Files
                        if (gitState.unstagedFiles.isNotEmpty()) {
                            Text("Unstaged (${gitState.unstagedFiles.size}):", fontSize = 11.sp, color = TextMuted)
                            gitState.unstagedFiles.forEach { file ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(file, fontFamily = CodeFontFamily, fontSize = 12.sp, color = AmberAlert)
                                    TextButton(onClick = { repository.stageFile(file) }) {
                                        Text("+ Stage", fontSize = 11.sp, color = ElectricCyan)
                                    }
                                }
                            }
                        }

                        // Staged Files
                        if (gitState.stagedFiles.isNotEmpty()) {
                            Text("Staged for Commit (${gitState.stagedFiles.size}):", fontSize = 11.sp, color = NodeEmeraldLight)
                            gitState.stagedFiles.forEach { file ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(file, fontFamily = CodeFontFamily, fontSize = 12.sp, color = NodeEmeraldLight)
                                    TextButton(onClick = { repository.unstageFile(file) }) {
                                        Text("- Unstage", fontSize = 11.sp, color = TextMuted)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Offline Commit input
                        OutlinedTextField(
                            value = commitMessage,
                            onValueChange = { commitMessage = it },
                            placeholder = { Text("feat: commit changes (saved offline)...", fontSize = 12.sp, color = TextMuted) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("commit_message_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NodeEmerald,
                                unfocusedBorderColor = SlateBorder
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                if (commitMessage.isNotBlank()) {
                                    repository.commitChanges(commitMessage.trim(), isOffline = true)
                                    commitMessage = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NodeEmerald),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("commit_offline_button")
                        ) {
                            Icon(Icons.Default.Commit, contentDescription = "Commit", tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Create Offline Commit", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                // Commit History Log
                Text(
                    text = "COMMIT HISTORY (${gitState.commitHistory.size})",
                    fontFamily = CodeFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = TextMuted
                )

                gitState.commitHistory.forEach { commit ->
                    val timeStr = remember(commit.timestamp) {
                        SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(commit.timestamp))
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
                                        text = commit.hash,
                                        fontFamily = CodeFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = ElectricCyan
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = commit.author,
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }

                                if (commit.isOffline) {
                                    Surface(
                                        color = AmberAlert.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "OFFLINE",
                                            fontFamily = CodeFontFamily,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AmberAlert,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                } else {
                                    Icon(Icons.Default.CloudDone, contentDescription = "Synced", tint = NodeEmerald, modifier = Modifier.size(14.dp))
                                }
                            }

                            Text(
                                text = commit.message,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )

                            Text(
                                text = "$timeStr • Changed: ${commit.filesChanged.joinToString(", ")}",
                                fontFamily = CodeFontFamily,
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                        }
                    }
                }
            }
        } else {
            // Team Reviews & PRs Tab
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Team Feedback & Pull Requests",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Real-time inline code reviews & collaborative approvals",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }

                pullRequests.forEach { pr ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
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
                                Text(
                                    text = "#${pr.number} ${pr.title}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = TextPrimary
                                )

                                Surface(
                                    color = when (pr.status) {
                                        ReviewStatus.APPROVED -> NodeEmerald.copy(alpha = 0.2f)
                                        ReviewStatus.OPEN -> ElectricCyan.copy(alpha = 0.2f)
                                        else -> AmberAlert.copy(alpha = 0.2f)
                                    },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = pr.status.name,
                                        fontFamily = CodeFontFamily,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (pr.status) {
                                            ReviewStatus.APPROVED -> NodeEmeraldLight
                                            ReviewStatus.OPEN -> CyanGlow
                                            else -> AmberAlert
                                        },
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Text(
                                text = pr.description,
                                fontSize = 12.sp,
                                color = TextSecondary,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )

                            Text(
                                text = "Branch: ${pr.branch} → ${pr.targetBranch} • Author: ${pr.author}",
                                fontFamily = CodeFontFamily,
                                fontSize = 11.sp,
                                color = TextMuted
                            )

                            // Approvals
                            if (pr.approvals.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = "Approved", tint = NodeEmerald, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Approved by: ${pr.approvals.joinToString(", ")}",
                                        fontSize = 11.sp,
                                        color = NodeEmeraldLight
                                    )
                                }
                            }

                            HorizontalDivider(color = SlateBorder.copy(alpha = 0.5f), thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))

                            // Inline comments thread
                            Text(
                                text = "Inline Code Comments (${pr.comments.size}):",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = TextPrimary
                            )

                            pr.comments.forEach { comment ->
                                Surface(
                                    color = SlateSurfaceVariant,
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "${comment.filePath}:${comment.lineNumber}",
                                                    fontFamily = CodeFontFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp,
                                                    color = ElectricCyan
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "by ${comment.author} [${comment.authorRole.name}]",
                                                    fontSize = 10.sp,
                                                    color = TextMuted
                                                )
                                            }

                                            if (comment.isResolved) {
                                                Text("RESOLVED", fontFamily = CodeFontFamily, fontSize = 9.sp, color = NodeEmeraldLight)
                                            }
                                        }

                                        Text(
                                            text = comment.comment,
                                            fontSize = 12.sp,
                                            color = TextPrimary,
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }
                                }
                            }

                            // Actions
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ElevatedButton(
                                    onClick = {
                                        selectedPrId = pr.id
                                        showAddCommentDialog = true
                                    },
                                    shape = RoundedCornerShape(6.dp),
                                    colors = ButtonDefaults.elevatedButtonColors(containerColor = SlateSurfaceVariant),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.AddComment, contentDescription = "Comment", modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add Comment", fontSize = 11.sp, color = TextPrimary)
                                }

                                if (currentUser.role.canReviewPR() && pr.status != ReviewStatus.APPROVED) {
                                    Button(
                                        onClick = { repository.approvePr(pr.id) },
                                        shape = RoundedCornerShape(6.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = NodeEmerald),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = "Approve", tint = Color.Black, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Approve PR", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Review Comment Dialog
    if (showAddCommentDialog && selectedPrId != null) {
        AlertDialog(
            onDismissRequest = { showAddCommentDialog = false },
            title = { Text("Add Inline Code Review Comment", color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Comment will be linked to source line number:", color = TextSecondary, fontSize = 12.sp)

                    OutlinedTextField(
                        value = newCommentLine,
                        onValueChange = { newCommentLine = it },
                        label = { Text("Line Number") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = SlateBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newCommentText,
                        onValueChange = { newCommentText = it },
                        label = { Text("Feedback / Code Suggestion") },
                        placeholder = { Text("Consider wrapping in try/catch...") },
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
                        val line = newCommentLine.toIntOrNull() ?: 1
                        if (newCommentText.isNotBlank()) {
                            repository.addPrComment(selectedPrId!!, newCommentText.trim(), "index.js", line)
                            newCommentText = ""
                            showAddCommentDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan)
                ) {
                    Text("Post Feedback", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCommentDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = SlateSurface
        )
    }
}
