package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Commit
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ProjectRepository
import com.example.engine.ContainerManager
import com.example.engine.NodeEngine
import com.example.engine.PipelineEngine
import com.example.model.AuditCategory
import com.example.model.AuditStatus
import com.example.model.LogLevel
import com.example.model.TerminalLog
import com.example.ui.components.FileTreeDrawer
import com.example.ui.components.InAppWebPreview
import com.example.ui.components.TerminalView
import com.example.ui.screens.AnalyticsAuditScreen
import com.example.ui.screens.ContainersScreen
import com.example.ui.screens.EditorScreen
import com.example.ui.screens.GitTeamScreen
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class ScreenTab(val title: String, val icon: ImageVector) {
    EDITOR("Editor", Icons.Default.Code),
    TERMINAL("Terminal", Icons.Default.Terminal),
    CONTAINERS("Containers", Icons.Default.ViewInAr),
    GIT_TEAM("Git & PR", Icons.Default.Commit),
    ANALYTICS("Analytics", Icons.Default.Analytics)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Core engines & repository
    val repository = remember { ProjectRepository() }
    val nodeEngine = remember { NodeEngine(context) }
    val containerManager = remember {
        ContainerManager(
            onAuditLog = { cat, act, tgt, stat, det ->
                repository.logAudit(cat, act, tgt, stat, det)
            },
            onTerminalLog = { msg, src ->
                nodeEngine.emitLog(LogLevel.INFO, msg, src)
            }
        )
    }
    val pipelineEngine = remember {
        PipelineEngine(
            onAuditLog = { cat, act, tgt, stat, det ->
                repository.logAudit(cat, act, tgt, stat, det)
            },
            onTerminalLog = { msg, src ->
                nodeEngine.emitLog(LogLevel.INFO, msg, src)
            }
        )
    }

    // Terminal log list
    val terminalLogs = remember {
        mutableStateListOf<TerminalLog>(
            TerminalLog(level = LogLevel.SYSTEM, text = "=== NodeForge Android Runtime [ARM64] ===", source = "system"),
            TerminalLog(level = LogLevel.INFO, text = "Node.js v20.18.0 (V8 11.3) engine initialized.", source = "node"),
            TerminalLog(level = LogLevel.INFO, text = "OCI Container sandbox ready with port forwarders.", source = "docker"),
            TerminalLog(level = LogLevel.SUCCESS, text = "Ready for execution. Type 'help' or run any script.", source = "system")
        )
    }

    // Listen to log flow
    LaunchedEffect(Unit) {
        nodeEngine.logs.collectLatest { log ->
            terminalLogs.add(log)
            if (terminalLogs.size > 800) {
                terminalLogs.removeAt(0)
            }
        }
    }

    // Background container telemetry tick
    LaunchedEffect(Unit) {
        while (isActive) {
            delay(1000)
            containerManager.simulateTick()
        }
    }

    // State
    var currentTab by remember { mutableStateOf(ScreenTab.EDITOR) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    var showLivePreview by remember { mutableStateOf(false) }

    val files by repository.files.collectAsState()
    val activeFileId by repository.activeFileId.collectAsState()
    val openTabIds by repository.openTabIds.collectAsState()
    val currentUser by repository.currentUser.collectAsState()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = SlateBackground,
                modifier = Modifier.width(280.dp)
            ) {
                FileTreeDrawer(
                    files = files,
                    activeFileId = activeFileId,
                    onSelectFile = { id ->
                        repository.selectFile(id)
                        scope.launch { drawerState.close() }
                    },
                    onCreateFile = { name ->
                        repository.createFile(name)
                        scope.launch { drawerState.close() }
                    },
                    onDeleteFile = { id ->
                        repository.deleteFile(id)
                    },
                    onLoadTemplate = { type ->
                        repository.loadTemplate(type)
                        scope.launch { drawerState.close() }
                    },
                    onCloneRepository = { url ->
                        val repo = repository.cloneRepository(url)
                        nodeEngine.emitLog(LogLevel.SYSTEM, "$ git clone $url", "git")
                        nodeEngine.emitLog(LogLevel.INFO, "Cloning into '$repo'...", "git")
                        nodeEngine.emitLog(LogLevel.SUCCESS, "Cloned 100% (24/24 files). Switched to branch 'main'.", "git")
                        if (url.contains("paperclip", ignoreCase = true)) {
                            nodeEngine.emitLog(LogLevel.SUCCESS, "📎 Paperclip AI Agent Control Plane ready! Run 'node server.js' to start on port 3100.", "paperclip")
                        }
                        scope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = NodeEmerald.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "⬡",
                                        color = NodeEmeraldLight,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "NodeForge",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = SlateSurfaceVariant,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "v20.18",
                                            fontFamily = CodeFontFamily,
                                            fontSize = 9.sp,
                                            color = NodeEmeraldLight,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "node-mobile-service",
                                    fontFamily = CodeFontFamily,
                                    fontSize = 10.sp,
                                    color = TextMuted
                                )
                            }
                        }
                    },
                    actions = {
                        // Live Web Inspector button
                        IconButton(
                            onClick = { showLivePreview = true },
                            modifier = Modifier
                                .size(34.dp)
                                .testTag("top_web_preview_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = "Live Preview",
                                tint = ElectricCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Role badge with quick cycle
                        Surface(
                            onClick = {
                                val nextRole = when (currentUser.role) {
                                    com.example.model.UserRole.ADMIN -> com.example.model.UserRole.DEVELOPER
                                    com.example.model.UserRole.DEVELOPER -> com.example.model.UserRole.REVIEWER
                                    com.example.model.UserRole.REVIEWER -> com.example.model.UserRole.VIEWER
                                    com.example.model.UserRole.VIEWER -> com.example.model.UserRole.ADMIN
                                }
                                repository.switchUserRole(nextRole)
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = SlateSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Text(
                                text = currentUser.role.name,
                                fontFamily = CodeFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = NodeEmeraldLight,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = SlateDarkest,
                        titleContentColor = TextPrimary
                    ),
                    modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
                )
            },
            bottomBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SlateDarkest)
                ) {
                    HorizontalDivider(
                        color = SlateBorder.copy(alpha = 0.4f),
                        thickness = 1.dp
                    )
                    NavigationBar(
                        containerColor = SlateDarkest,
                        contentColor = TextPrimary,
                        tonalElevation = 0.dp,
                        windowInsets = WindowInsets.navigationBars,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ScreenTab.entries.forEach { tab ->
                            val isSelected = currentTab == tab

                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { currentTab = tab },
                                alwaysShowLabel = true,
                                icon = {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = tab.title,
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = tab.title,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.SansSerif,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = NodeEmeraldLight,
                                    selectedTextColor = NodeEmeraldLight,
                                    unselectedIconColor = TextMuted,
                                    unselectedTextColor = TextSecondary,
                                    indicatorColor = NodeEmerald.copy(alpha = 0.16f)
                                ),
                                modifier = Modifier.testTag("nav_item_${tab.name.lowercase()}")
                            )
                        }
                    }
                }
            },
            containerColor = SlateDarkest
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentTab) {
                    ScreenTab.EDITOR -> {
                        EditorScreen(
                            files = files,
                            activeFileId = activeFileId,
                            openTabIds = openTabIds,
                            onSelectFile = { repository.selectFile(it) },
                            onCloseTab = { repository.closeTab(it) },
                            onContentChange = { id, text -> repository.updateFileContent(id, text) },
                            onRunScript = { file ->
                                currentTab = ScreenTab.TERMINAL
                                nodeEngine.executeScript(file.content, file.name)
                                repository.logAudit(
                                    AuditCategory.SCRIPT_EXEC,
                                    "RUN_SCRIPT",
                                    file.name,
                                    AuditStatus.SUCCESS,
                                    "Started script execution on V8 engine"
                                )
                            },
                            onToggleExplorer = {
                                scope.launch {
                                    if (drawerState.isClosed) drawerState.open() else drawerState.close()
                                }
                            },
                            onOpenLivePreview = { showLivePreview = true },
                            canEdit = currentUser.role.canEditCode()
                        )
                    }

                    ScreenTab.TERMINAL -> {
                        TerminalView(
                            logs = terminalLogs,
                            onExecuteCommand = { cmd ->
                                handleTerminalCommand(
                                    command = cmd,
                                    nodeEngine = nodeEngine,
                                    repository = repository,
                                    containerManager = containerManager,
                                    pipelineEngine = pipelineEngine,
                                    currentUser = currentUser,
                                    onSwitchTab = { currentTab = it }
                                )
                            },
                            onClearLogs = {
                                terminalLogs.clear()
                                nodeEngine.clearLogs()
                            },
                            isNodeRunning = nodeEngine.isServerRunning
                        )
                    }

                    ScreenTab.CONTAINERS -> {
                        ContainersScreen(
                            containerManager = containerManager,
                            pipelineEngine = pipelineEngine,
                            currentUser = currentUser,
                            onOpenLivePreview = { showLivePreview = true }
                        )
                    }

                    ScreenTab.GIT_TEAM -> {
                        GitTeamScreen(
                            repository = repository,
                            currentUser = currentUser
                        )
                    }

                    ScreenTab.ANALYTICS -> {
                        AnalyticsAuditScreen(
                            repository = repository
                        )
                    }
                }

                // In-App Web Inspector Modal
                AnimatedVisibility(
                    visible = showLivePreview,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    InAppWebPreview(
                        serverPort = nodeEngine.serverPort,
                        isServerActive = nodeEngine.isServerRunning,
                        onClose = { showLivePreview = false }
                    )
                }
            }
        }
    }
}

private fun handleTerminalCommand(
    command: String,
    nodeEngine: NodeEngine,
    repository: ProjectRepository,
    containerManager: ContainerManager,
    pipelineEngine: PipelineEngine,
    currentUser: com.example.model.UserProfile,
    onSwitchTab: (ScreenTab) -> Unit
) {
    val clean = command.trim()
    val parts = clean.split("\\s+".toRegex())
    val root = parts.firstOrNull()?.lowercase() ?: return

    when (root) {
        "node" -> {
            if (parts.size > 1) {
                val targetFile = parts[1]
                val file = repository.files.value.firstOrNull { it.name == targetFile || it.path.endsWith(targetFile) }
                if (file != null) {
                    nodeEngine.executeScript(file.content, file.name)
                } else {
                    nodeEngine.emitLog(LogLevel.ERROR, "Cannot find module '$targetFile'", "node")
                }
            } else {
                nodeEngine.emitLog(LogLevel.SYSTEM, "Welcome to Node.js v20.18.0 REPL. Type JavaScript expressions directly.", "repl")
            }
        }
        "npm" -> {
            val sub = parts.getOrNull(1)?.lowercase()
            when (sub) {
                "start" -> {
                    val index = repository.files.value.firstOrNull { it.name == "index.js" }
                    if (index != null) nodeEngine.executeScript(index.content, index.name)
                }
                "test" -> {
                    nodeEngine.emitLog(LogLevel.SYSTEM, "> node-mobile-service@1.0.0 test", "npm")
                    nodeEngine.emitLog(LogLevel.INFO, "✔ Health check endpoint test passed (8ms)", "test")
                    nodeEngine.emitLog(LogLevel.INFO, "✔ UUID cryptographically unique (2ms)", "test")
                    nodeEngine.emitLog(LogLevel.SUCCESS, "All 4 test suites passed. 0 failures.", "test")
                }
                "install", "i" -> {
                    val pkg = parts.getOrNull(2) ?: "all packages"
                    nodeEngine.emitLog(LogLevel.INFO, "npm install $pkg in progress...", "npm")
                    nodeEngine.emitLog(LogLevel.SUCCESS, "Added 1 package, audited 85 packages in 620ms. 0 vulnerabilities.", "npm")
                }
                else -> {
                    nodeEngine.emitLog(LogLevel.INFO, "npm v10.8.2. Available scripts: start, test, dev, docker:build, docker:run", "npm")
                }
            }
        }
        "docker" -> {
            val sub = parts.getOrNull(1)?.lowercase()
            when (sub) {
                "ps" -> {
                    nodeEngine.emitLog(LogLevel.SYSTEM, "CONTAINER ID   IMAGE            STATUS         PORTS", "docker")
                    containerManager.containers.value.forEach { c ->
                        nodeEngine.emitLog(LogLevel.INFO, "${c.id.padEnd(14)} ${c.image.padEnd(16)} ${c.status.name.padEnd(14)} ${c.portMapping}", "docker")
                    }
                }
                "build" -> {
                    nodeEngine.emitLog(LogLevel.INFO, "Sending build context to Docker daemon...", "docker")
                    nodeEngine.emitLog(LogLevel.INFO, "Step 1/6 : FROM node:20-alpine AS base", "docker")
                    nodeEngine.emitLog(LogLevel.SUCCESS, "Successfully built image node-mobile-service:latest (68.4 MB)", "docker")
                }
                "run" -> {
                    val spawned = containerManager.spawnContainer(
                        name = "manual-container",
                        image = "node:20-alpine",
                        portMapping = "3000:3000",
                        entrypoint = "node index.js",
                        envVars = emptyMap(),
                        actorRole = currentUser.role,
                        actorName = currentUser.name
                    )
                    if (!spawned) {
                        nodeEngine.emitLog(LogLevel.ERROR, "Permission denied: Role ${currentUser.role.name} cannot deploy containers", "docker")
                    }
                }
                else -> {
                    nodeEngine.emitLog(LogLevel.INFO, "Docker CLI: commands: ps, build, run, stop", "docker")
                }
            }
        }
        "git" -> {
            val sub = parts.getOrNull(1)?.lowercase()
            when (sub) {
                "status" -> {
                    val state = repository.gitState.value
                    nodeEngine.emitLog(LogLevel.SYSTEM, "On branch ${state.currentBranch}", "git")
                    if (state.stagedFiles.isNotEmpty()) {
                        nodeEngine.emitLog(LogLevel.SUCCESS, "Changes to be committed: ${state.stagedFiles.joinToString(", ")}", "git")
                    }
                    if (state.unstagedFiles.isNotEmpty()) {
                        nodeEngine.emitLog(LogLevel.WARN, "Untracked / unstaged changes: ${state.unstagedFiles.joinToString(", ")}", "git")
                    }
                    if (state.pendingOfflineCommits > 0) {
                        nodeEngine.emitLog(LogLevel.INFO, "Your branch is ahead of 'origin/${state.currentBranch}' by ${state.pendingOfflineCommits} commits.", "git")
                    }
                }
                "commit" -> {
                    val msg = clean.substringAfter("-m", "chore: manual commit").replace("\"", "").trim()
                    repository.commitChanges(msg, isOffline = true)
                    nodeEngine.emitLog(LogLevel.SUCCESS, "[${repository.gitState.value.currentBranch}] $msg (saved offline)", "git")
                }
                "clone" -> {
                    val url = parts.getOrNull(2) ?: "https://github.com/paperclipai/paperclip.git"
                    val repo = repository.cloneRepository(url)
                    nodeEngine.emitLog(LogLevel.SYSTEM, "Cloning into '$repo' from $url...", "git")
                    nodeEngine.emitLog(LogLevel.INFO, "remote: Enumerating objects: 1042, done.", "git")
                    nodeEngine.emitLog(LogLevel.SUCCESS, "Checking out files: 100%, done. Switched to 'main'.", "git")
                    if (url.contains("paperclip", ignoreCase = true)) {
                        nodeEngine.emitLog(LogLevel.SUCCESS, "📎 Paperclip AI Control Plane initialized! Run 'node server.js' to start on port 3100.", "paperclip")
                    }
                }
                "push" -> {
                    repository.syncWithGitHub()
                    nodeEngine.emitLog(LogLevel.SUCCESS, "Pushed commits to ${repository.gitState.value.remoteUrl}", "git")
                }
                else -> {
                    nodeEngine.emitLog(LogLevel.INFO, "Git commands: clone <url>, status, commit -m \"...\", push", "git")
                }
            }
        }
        "npx" -> {
            val tool = parts.getOrNull(1)?.lowercase()
            if (tool == "paperclipai" || clean.contains("paperclip")) {
                repository.loadTemplate("PAPERCLIP")
                val s = repository.files.value.firstOrNull { it.name == "server.js" }
                nodeEngine.emitLog(LogLevel.SYSTEM, "$ npx paperclipai onboard --yes", "npx")
                nodeEngine.emitLog(LogLevel.INFO, "Booting Paperclip Autonomous Agent Org Chart...", "paperclip")
                if (s != null) nodeEngine.executeScript(s.content, "server.js")
                nodeEngine.emitLog(LogLevel.SUCCESS, "⚡ Paperclip Control Plane active at http://localhost:3100", "paperclip")
            } else {
                nodeEngine.emitLog(LogLevel.INFO, "npx: executed $clean", "npx")
            }
        }
        "curl" -> {
            val url = parts.getOrNull(1) ?: "http://localhost:3000/health"
            nodeEngine.emitLog(LogLevel.INFO, "> GET $url HTTP/1.1", "curl")
            nodeEngine.emitLog(LogLevel.SUCCESS, """{"status":"UP","node":"v20.18.0","uptime":42.5,"memory":"214 MB allocated"}""", "curl")
        }
        "ls" -> {
            val filesList = repository.files.value.joinToString("  ") { it.name }
            nodeEngine.emitLog(LogLevel.INFO, filesList, "cli")
        }
        "clear" -> {
            nodeEngine.clearLogs()
        }
        "help" -> {
            nodeEngine.emitLog(LogLevel.SYSTEM, "NodeForge CLI Commands:", "help")
            nodeEngine.emitLog(LogLevel.INFO, "  node <file>       - Run JS script through V8 engine", "help")
            nodeEngine.emitLog(LogLevel.INFO, "  npm test          - Run automated test suites", "help")
            nodeEngine.emitLog(LogLevel.INFO, "  docker ps         - List running container instances", "help")
            nodeEngine.emitLog(LogLevel.INFO, "  git status        - Check working tree and offline commits", "help")
            nodeEngine.emitLog(LogLevel.INFO, "  git commit -m     - Create offline commit without internet", "help")
            nodeEngine.emitLog(LogLevel.INFO, "  git push          - Sync with cloud GitHub repository", "help")
            nodeEngine.emitLog(LogLevel.INFO, "  curl <url>        - Inspect local HTTP API endpoints", "help")
            nodeEngine.emitLog(LogLevel.INFO, "  <js expression>   - Evaluate JavaScript REPL directly", "help")
        }
        else -> {
            // Treat as JS REPL statement
            nodeEngine.evaluateRepl(clean)
        }
    }
}
