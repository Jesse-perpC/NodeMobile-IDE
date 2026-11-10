package com.example.data

import com.example.model.AuditCategory
import com.example.model.AuditRecord
import com.example.model.AuditStatus
import com.example.model.CodeComment
import com.example.model.FileLanguage
import com.example.model.GitBranch
import com.example.model.GitCommit
import com.example.model.GitRepositoryState
import com.example.model.PipelineStatus
import com.example.model.PipelineStep
import com.example.model.ProjectFile
import com.example.model.PullRequest
import com.example.model.ReviewStatus
import com.example.model.UserProfile
import com.example.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProjectRepository {

    private val _currentUser = MutableStateFlow(
        UserProfile(
            id = "user-1",
            name = "Alex Rivera",
            email = "alex@nodepod.internal",
            role = UserRole.ADMIN,
            avatarInitials = "AR"
        )
    )
    val currentUser: StateFlow<UserProfile> = _currentUser.asStateFlow()

    private val _files = MutableStateFlow<List<ProjectFile>>(getDefaultFiles())
    val files: StateFlow<List<ProjectFile>> = _files.asStateFlow()

    private val _activeFileId = MutableStateFlow<String>("file-index-js")
    val activeFileId: StateFlow<String> = _activeFileId.asStateFlow()

    private val _openTabIds = MutableStateFlow<List<String>>(listOf("file-index-js", "file-package-json"))
    val openTabIds: StateFlow<List<String>> = _openTabIds.asStateFlow()

    private val _gitState = MutableStateFlow(
        GitRepositoryState(
            currentBranch = "main",
            commitHistory = listOf(
                GitCommit(
                    hash = "7f2c19a",
                    message = "feat(api): add JWT authentication middleware & rate limiter",
                    author = "Alex Rivera",
                    timestamp = System.currentTimeMillis() - 3600000L * 2,
                    filesChanged = listOf("index.js", "package.json"),
                    isOffline = false
                ),
                GitCommit(
                    hash = "3a88d01",
                    message = "fix(docker): optimize multi-stage build image size to 64MB",
                    author = "DevOps Bot",
                    timestamp = System.currentTimeMillis() - 3600000L * 14,
                    filesChanged = listOf("Dockerfile"),
                    isOffline = false
                ),
                GitCommit(
                    hash = "109bbf4",
                    message = "init: bootstrap Node.js express microservice template",
                    author = "Alex Rivera",
                    timestamp = System.currentTimeMillis() - 3600000L * 48,
                    filesChanged = listOf("package.json", "index.js", "README.md"),
                    isOffline = false
                )
            )
        )
    )
    val gitState: StateFlow<GitRepositoryState> = _gitState.asStateFlow()

    private val _pullRequests = MutableStateFlow<List<PullRequest>>(
        listOf(
            PullRequest(
                id = "pr-42",
                number = 42,
                title = "feat: Async Event Driven Worker & WebSocket Stream",
                description = "Introduces worker event queue and real-time socket connections for high-throughput mobile telemetry.",
                author = "Elena Rostova",
                branch = "feature/api-v2",
                status = ReviewStatus.OPEN,
                approvals = listOf("DevLead (Sarah)"),
                comments = listOf(
                    CodeComment(
                        id = "c-1",
                        author = "Sarah Chen",
                        authorRole = UserRole.ADMIN,
                        filePath = "index.js",
                        lineNumber = 18,
                        comment = "Consider adding error boundary wrapper for uncaught async rejections.",
                        timestamp = System.currentTimeMillis() - 1800000L,
                        isResolved = false
                    ),
                    CodeComment(
                        id = "c-2",
                        author = "Marcus Vance",
                        authorRole = UserRole.REVIEWER,
                        filePath = "index.js",
                        lineNumber = 24,
                        comment = "Nice use of crypto.randomUUID() for request tracing ID!",
                        timestamp = System.currentTimeMillis() - 900000L,
                        isResolved = true
                    )
                )
            ),
            PullRequest(
                id = "pr-41",
                number = 41,
                title = "fix: Patch helmet security headers & CORS origins",
                description = "Tightens Content-Security-Policy headers and restricts mobile origins.",
                author = "Alex Rivera",
                branch = "hotfix/auth-leak",
                status = ReviewStatus.APPROVED,
                approvals = listOf("Alex Rivera", "Sarah Chen"),
                comments = emptyList()
            )
        )
    )
    val pullRequests: StateFlow<List<PullRequest>> = _pullRequests.asStateFlow()

    private val _auditLogs = MutableStateFlow<List<AuditRecord>>(
        listOf(
            AuditRecord(
                actor = "Alex Rivera",
                role = UserRole.ADMIN,
                category = AuditCategory.CONTAINER_DEPLOY,
                action = "CONTAINER_DEPLOY",
                target = "c-74a9b2 (express-api-prod)",
                status = AuditStatus.SUCCESS,
                details = "Deployed release tag v1.4.0 with port 3000:3000 on node:20-alpine"
            ),
            AuditRecord(
                actor = "Alex Rivera",
                role = UserRole.ADMIN,
                category = AuditCategory.GIT_PUSH,
                action = "REMOTE_SYNC",
                target = "origin/main",
                status = AuditStatus.SUCCESS,
                details = "Synchronized 2 offline commits to GitHub repository"
            ),
            AuditRecord(
                actor = "Elena Rostova",
                role = UserRole.DEVELOPER,
                category = AuditCategory.SCRIPT_EXEC,
                action = "EXECUTE_NODE",
                target = "index.js",
                status = AuditStatus.SUCCESS,
                details = "Executed script runtime with V8 polyfill in 14ms"
            )
        )
    )
    val auditLogs: StateFlow<List<AuditRecord>> = _auditLogs.asStateFlow()

    fun logAudit(
        category: AuditCategory,
        action: String,
        target: String,
        status: AuditStatus = AuditStatus.SUCCESS,
        details: String = ""
    ) {
        val user = _currentUser.value
        val record = AuditRecord(
            actor = user.name,
            role = user.role,
            category = category,
            action = action,
            target = target,
            status = status,
            details = details
        )
        _auditLogs.update { listOf(record) + it }
    }

    fun switchUserRole(role: UserRole) {
        val prev = _currentUser.value
        _currentUser.value = prev.copy(role = role)
        logAudit(
            category = AuditCategory.ROLE_CHANGE,
            action = "ROLE_CHANGED",
            target = prev.name,
            details = "Switched active role from ${prev.role.name} to ${role.name}"
        )
    }

    fun selectFile(fileId: String) {
        _activeFileId.value = fileId
        if (!_openTabIds.value.contains(fileId)) {
            _openTabIds.update { it + fileId }
        }
    }

    fun closeTab(fileId: String) {
        _openTabIds.update { list ->
            val updated = list.filterNot { it == fileId }
            if (_activeFileId.value == fileId && updated.isNotEmpty()) {
                _activeFileId.value = updated.last()
            }
            updated
        }
    }

    fun updateFileContent(fileId: String, newContent: String) {
        val user = _currentUser.value
        if (!user.role.canEditCode()) {
            logAudit(
                AuditCategory.SECURITY_ALERT,
                "UNAUTHORIZED_CODE_EDIT",
                fileId,
                AuditStatus.BLOCKED,
                "User ${user.name} with role ${user.role.name} denied edit access"
            )
            return
        }

        _files.update { list ->
            list.map { file ->
                if (file.id == fileId) {
                    file.copy(content = newContent, isModified = true)
                } else file
            }
        }

        // Add to git unstaged changes if not already present
        val activeFile = _files.value.firstOrNull { it.id == fileId }
        if (activeFile != null) {
            _gitState.update { state ->
                if (!state.unstagedFiles.contains(activeFile.path)) {
                    state.copy(unstagedFiles = state.unstagedFiles + activeFile.path)
                } else state
            }
        }
    }

    fun createFile(name: String, parentPath: String = "", initialContent: String = ""): Boolean {
        val user = _currentUser.value
        if (!user.role.canEditCode()) return false

        val path = if (parentPath.isBlank()) name else "$parentPath/$name"
        val id = "file-" + name.replace(".", "-") + "-" + (100..999).random()
        val newFile = ProjectFile(
            id = id,
            name = name,
            path = path,
            isDirectory = false,
            content = initialContent,
            parentPath = parentPath
        )

        _files.update { it + newFile }
        selectFile(id)
        logAudit(AuditCategory.CODE_EDIT, "CREATE_FILE", path, AuditStatus.SUCCESS, "Created new file")
        return true
    }

    fun deleteFile(fileId: String): Boolean {
        val user = _currentUser.value
        if (!user.role.canEditCode()) return false

        val target = _files.value.firstOrNull { it.id == fileId } ?: return false
        _files.update { it.filterNot { f -> f.id == fileId } }
        closeTab(fileId)
        logAudit(AuditCategory.CODE_EDIT, "DELETE_FILE", target.path, AuditStatus.SUCCESS, "Deleted file")
        return true
    }

    // Git Operations & Offline Commits
    fun stageFile(filePath: String) {
        _gitState.update { state ->
            state.copy(
                unstagedFiles = state.unstagedFiles.filterNot { it == filePath },
                stagedFiles = if (!state.stagedFiles.contains(filePath)) state.stagedFiles + filePath else state.stagedFiles
            )
        }
    }

    fun unstageFile(filePath: String) {
        _gitState.update { state ->
            state.copy(
                stagedFiles = state.stagedFiles.filterNot { it == filePath },
                unstagedFiles = if (!state.unstagedFiles.contains(filePath)) state.unstagedFiles + filePath else state.unstagedFiles
            )
        }
    }

    fun stageAll() {
        _gitState.update { state ->
            state.copy(
                stagedFiles = (state.stagedFiles + state.unstagedFiles).distinct(),
                unstagedFiles = emptyList()
            )
        }
    }

    fun commitChanges(message: String, isOffline: Boolean = true): Boolean {
        val state = _gitState.value
        if (state.stagedFiles.isEmpty() && state.unstagedFiles.isEmpty()) return false
        val filesToCommit = if (state.stagedFiles.isNotEmpty()) state.stagedFiles else state.unstagedFiles

        val hash = (1..7).map { "0123456789abcdef".random() }.joinToString("")
        val newCommit = GitCommit(
            hash = hash,
            message = message.ifBlank { "chore: update project files" },
            author = _currentUser.value.name,
            timestamp = System.currentTimeMillis(),
            filesChanged = filesToCommit,
            isOffline = isOffline
        )

        _gitState.update {
            it.copy(
                stagedFiles = emptyList(),
                unstagedFiles = emptyList(),
                commitHistory = listOf(newCommit) + it.commitHistory,
                pendingOfflineCommits = if (isOffline) it.pendingOfflineCommits + 1 else it.pendingOfflineCommits
            )
        }

        // Reset modified flags on files
        _files.update { list -> list.map { it.copy(isModified = false) } }

        logAudit(
            AuditCategory.GIT_COMMIT,
            if (isOffline) "OFFLINE_COMMIT" else "GIT_COMMIT",
            hash,
            AuditStatus.SUCCESS,
            "Committed ${filesToCommit.size} files: $message"
        )
        return true
    }

    fun switchBranch(branchName: String) {
        _gitState.update { state ->
            state.copy(
                currentBranch = branchName,
                branches = state.branches.map { it.copy(isCurrent = it.name == branchName) }
            )
        }
        logAudit(AuditCategory.GIT_COMMIT, "SWITCH_BRANCH", branchName, AuditStatus.SUCCESS, "Checked out branch $branchName")
    }

    fun syncWithGitHub() {
        _gitState.update { it.copy(isSyncing = true) }
        val pending = _gitState.value.pendingOfflineCommits
        _gitState.update { state ->
            state.copy(
                isSyncing = false,
                pendingOfflineCommits = 0,
                commitHistory = state.commitHistory.map { it.copy(isOffline = false) }
            )
        }
        logAudit(
            AuditCategory.GIT_PUSH,
            "GITHUB_SYNC",
            _gitState.value.remoteUrl,
            AuditStatus.SUCCESS,
            "Synced $pending offline commits with remote repository"
        )
    }

    fun addPrComment(prId: String, commentText: String, filePath: String = "index.js", line: Int = 1) {
        val user = _currentUser.value
        val newComment = CodeComment(
            id = "comm-" + System.currentTimeMillis(),
            author = user.name,
            authorRole = user.role,
            filePath = filePath,
            lineNumber = line,
            comment = commentText,
            timestamp = System.currentTimeMillis()
        )

        _pullRequests.update { list ->
            list.map { pr ->
                if (pr.id == prId) {
                    pr.copy(comments = pr.comments + newComment)
                } else pr
            }
        }
    }

    fun approvePr(prId: String) {
        val user = _currentUser.value
        if (!user.role.canReviewPR()) return

        _pullRequests.update { list ->
            list.map { pr ->
                if (pr.id == prId) {
                    val approvals = (pr.approvals + user.name).distinct()
                    pr.copy(
                        approvals = approvals,
                        status = if (approvals.size >= 2) ReviewStatus.APPROVED else pr.status
                    )
                } else pr
            }
        }
        logAudit(AuditCategory.ROLE_CHANGE, "APPROVE_PR", prId, AuditStatus.SUCCESS, "PR approved by ${user.name}")
    }

    fun loadTemplate(templateType: String) {
        when (templateType) {
            "PAPERCLIP" -> _files.value = getPaperclipFiles()
            "EXPRESS" -> _files.value = getDefaultFiles()
            "MICROSERVICE" -> _files.value = getMicroserviceFiles()
            "WEB_HTML" -> _files.value = getWebAppFiles()
            "CLI" -> _files.value = getCliFiles()
        }
        _activeFileId.value = _files.value.first().id
        _openTabIds.value = listOf(_files.value.first().id)
        logAudit(AuditCategory.CODE_EDIT, "LOAD_TEMPLATE", templateType, AuditStatus.SUCCESS, "Loaded project template: $templateType")
    }

    fun cloneRepository(url: String): String {
        val cleanUrl = url.trim()
        val repoName = cleanUrl.substringAfterLast("/").removeSuffix(".git").ifBlank { "cloned-repo" }

        val clonedFiles = if (cleanUrl.contains("paperclip", ignoreCase = true)) {
            getPaperclipFiles()
        } else {
            listOf(
                ProjectFile(
                    id = "file-server-js",
                    name = "server.js",
                    path = "server.js",
                    language = FileLanguage.JAVASCRIPT,
                    content = """
// Cloned from $cleanUrl
const express = require('express');
const app = express();
const PORT = process.env.PORT || 3000;

console.log("⚡ Running cloned repository: $repoName");

app.get('/', (req, res) => {
  return { message: "Application $repoName running on NodeForge", status: "ONLINE" };
});

app.listen(PORT, () => {
  console.log(`Server online at http://localhost:${'$'}{PORT}`);
});
""".trimIndent()
                ),
                ProjectFile(
                    id = "file-package-json",
                    name = "package.json",
                    path = "package.json",
                    language = FileLanguage.JSON,
                    content = """
{
  "name": "$repoName",
  "version": "1.0.0",
  "repository": "$cleanUrl",
  "scripts": {
    "start": "node server.js"
  },
  "dependencies": {
    "express": "^4.19.2"
  }
}
""".trimIndent()
                ),
                ProjectFile(
                    id = "file-readme",
                    name = "README.md",
                    path = "README.md",
                    language = FileLanguage.MARKDOWN,
                    content = "# $repoName\n\nCloned from `$cleanUrl` into NodeForge Android Runtime."
                )
            )
        }

        _files.value = clonedFiles
        _activeFileId.value = clonedFiles.first().id
        _openTabIds.value = listOf(clonedFiles.first().id)

        val newCommit = GitCommit(
            hash = "9c" + (1000..9999).random(),
            message = "clone: imported $repoName from $cleanUrl",
            author = "Paperclip / NodeForge",
            timestamp = System.currentTimeMillis(),
            filesChanged = clonedFiles.map { it.name },
            isOffline = false
        )

        _gitState.update {
            it.copy(
                remoteUrl = cleanUrl,
                currentBranch = "main",
                stagedFiles = emptyList(),
                unstagedFiles = emptyList(),
                commitHistory = listOf(newCommit),
                pendingOfflineCommits = 0
            )
        }

        logAudit(
            AuditCategory.GIT_PUSH,
            "GIT_CLONE",
            cleanUrl,
            AuditStatus.SUCCESS,
            "Successfully cloned $repoName into mobile workspace"
        )
        return repoName
    }

    companion object {
        fun getDefaultFiles(): List<ProjectFile> = listOf(
            ProjectFile(
                id = "file-index-js",
                name = "index.js",
                path = "src/index.js",
                language = FileLanguage.JAVASCRIPT,
                content = """
// 🚀 NodeForge Express Microservice Server
const express = require('express');
const os = require('os');
const crypto = require('crypto');

const app = express();
const PORT = process.env.PORT || 3000;

// In-memory data store for mobile prototyping
const tasks = [
  { id: '1', title: 'Compile Node.js V8 runtime on ARM64', status: 'COMPLETED' },
  { id: '2', title: 'Containerize Express microservice', status: 'IN_PROGRESS' },
  { id: '3', title: 'Sync offline git commits to cloud repo', status: 'PENDING' }
];

console.log("⚡ Initializing NodeForge Node.js Application...");
console.log("System CPU Architecture: " + os.cpus()[0].model);

// Health check endpoint
app.get('/health', (req, res) => {
  const healthData = {
    status: 'UP',
    uptime: process.uptime(),
    timestamp: new Date().toISOString(),
    nodeVersion: process.version,
    memoryUsage: os.freemem() / (1024 * 1024) + ' MB free'
  };
  console.log("GET /health -> 200 OK");
  return healthData;
});

// API Routes
app.get('/api/tasks', (req, res) => {
  console.log("GET /api/tasks -> Returned " + tasks.length + " items");
  return { success: true, count: tasks.length, data: tasks };
});

app.post('/api/tasks', (req, res) => {
  const newTask = {
    id: crypto.randomUUID(),
    title: 'New developer task created at ' + new Date().toLocaleTimeString(),
    status: 'ACTIVE'
  };
  tasks.push(newTask);
  console.log("POST /api/tasks -> Created task: " + newTask.id);
  return { success: true, created: newTask };
});

// Start Express Listener
app.listen(PORT, () => {
  console.log(`Server actively running on port ${'$'}{PORT}`);
  console.log("Try testing GET /health or GET /api/tasks via Live Preview!");
});
""".trimIndent()
            ),
            ProjectFile(
                id = "file-package-json",
                name = "package.json",
                path = "package.json",
                language = FileLanguage.JSON,
                content = """
{
  "name": "node-mobile-service",
  "version": "1.0.0",
  "description": "Containerized Node.js application running on Android with NodeForge",
  "main": "src/index.js",
  "scripts": {
    "start": "node src/index.js",
    "dev": "nodemon src/index.js",
    "test": "node --test",
    "docker:build": "docker build -t node-mobile-service:latest .",
    "docker:run": "docker run -p 3000:3000 node-mobile-service:latest"
  },
  "dependencies": {
    "express": "^4.19.2",
    "cors": "^2.8.5",
    "dotenv": "^16.4.5"
  },
  "devDependencies": {
    "nodemon": "^3.1.0"
  },
  "engines": {
    "node": ">=20.0.0"
  }
}
""".trimIndent()
            ),
            ProjectFile(
                id = "file-dockerfile",
                name = "Dockerfile",
                path = "Dockerfile",
                language = FileLanguage.DOCKERFILE,
                content = """
# Production Container Image for NodeForge Runtime
FROM node:20-alpine AS base
WORKDIR /app

# Install dependencies
COPY package*.json ./
RUN npm ci --only=production

# Copy application source
COPY . .

# Security: run as non-root node user
USER node
EXPOSE 3000

ENV NODE_ENV=production
ENV PORT=3000

HEALTHCHECK --interval=30s --timeout=3s \
  CMD wget --quiet --tries=1 --spider http://localhost:3000/health || exit 1

CMD ["node", "src/index.js"]
""".trimIndent()
            ),
            ProjectFile(
                id = "file-readme-md",
                name = "README.md",
                path = "README.md",
                language = FileLanguage.MARKDOWN,
                content = """
# 🛡️ NodeForge Mobile Application

A high-performance containerized Node.js application running directly on Android.

## Features
- **Containerized V8 Execution**: Run scripts locally or in isolated containers.
- **Interactive Terminal**: Integrated CLI for `npm`, `node`, `git`, and `docker`.
- **Offline Version Control**: Commit code on the go without active internet connectivity.
- **Automated CI/CD**: One-tap deployment pipelines with live health check validation.
- **RBAC & Security**: Role-based access control and immutable audit trails.
""".trimIndent()
            )
        )

        fun getMicroserviceFiles(): List<ProjectFile> = listOf(
            ProjectFile(
                id = "file-micro-server",
                name = "server.js",
                path = "server.js",
                language = FileLanguage.JAVASCRIPT,
                content = """
const express = require('express');
const app = express();
const PORT = 8080;

console.log("⚡ Starting Microservice Queue Worker...");

app.get('/metrics', (req, res) => {
  return {
    queueLength: Math.floor(Math.random() * 45),
    processedJobs: 1420,
    activeWorkers: 4,
    status: 'HEALTHY'
  };
});

app.listen(PORT, () => {
  console.log(`Worker listening on port ${'$'}{PORT}`);
});
""".trimIndent()
            ),
            ProjectFile(
                id = "file-compose",
                name = "docker-compose.yml",
                path = "docker-compose.yml",
                language = FileLanguage.YAML,
                content = """
version: '3.8'
services:
  api:
    image: node:20-alpine
    command: node server.js
    ports:
      - "8080:8080"
    environment:
      - NODE_ENV=production
""".trimIndent()
            )
        )

        fun getWebAppFiles(): List<ProjectFile> = listOf(
            ProjectFile(
                id = "file-web-server",
                name = "server.js",
                path = "server.js",
                language = FileLanguage.JAVASCRIPT,
                content = """
const express = require('express');
const app = express();
const PORT = 3000;

console.log("Serving modern HTML/CSS client on Android...");

app.get('/', (req, res) => {
  return "<h1>Hello from Node on Android!</h1><p>Full-stack web prototyping on the go.</p>";
});

app.listen(PORT, () => {
  console.log(`Web application online at http://localhost:${'$'}{PORT}`);
});
""".trimIndent()
            ),
            ProjectFile(
                id = "file-index-html",
                name = "index.html",
                path = "public/index.html",
                language = FileLanguage.HTML,
                content = """
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>NodeForge Web App</title>
  <style>
    body { background: #0F172A; color: #F8FAFC; font-family: sans-serif; padding: 24px; }
    h1 { color: #10B981; }
    .card { background: #1E293B; border: 1px solid #334155; padding: 16px; border-radius: 8px; }
  </style>
</head>
<body>
  <div class="card">
    <h1>NodeForge Web Preview</h1>
    <p>Running live Node server on Android container.</p>
  </div>
</body>
</html>
""".trimIndent()
            )
        )

        fun getCliFiles(): List<ProjectFile> = listOf(
            ProjectFile(
                id = "file-cli",
                name = "cli.js",
                path = "cli.js",
                language = FileLanguage.JAVASCRIPT,
                content = """
const crypto = require('crypto');
const os = require('os');

console.log("=== NodeForge CLI Diagnostics ===");
console.log("Node Version: " + process.version);
console.log("Platform: " + process.platform);
console.log("Architecture: " + process.arch);
console.log("Random UUID: " + crypto.randomUUID());
console.log("Uptime: " + process.uptime() + "s");
console.log("Done!");
""".trimIndent()
            )
        )

        fun getPaperclipFiles(): List<ProjectFile> = listOf(
            ProjectFile(
                id = "file-paperclip-server",
                name = "server.js",
                path = "server.js",
                language = FileLanguage.JAVASCRIPT,
                content = """
// 📎 Paperclip AI Control Plane Server (https://github.com/paperclipai/paperclip)
const express = require('express');
const os = require('os');
const crypto = require('crypto');

const app = express();
const PORT = process.env.PORT || 3100;

console.log("==========================================");
console.log("📎 Paperclip AI Agent Control Plane v1.0.0");
console.log("Repository: https://github.com/paperclipai/paperclip.git");
console.log("Control Plane Port: " + PORT);
console.log("Engine: Node.js " + process.version + " (" + process.platform + ")");
console.log("==========================================");

// Autonomous Agent Hierarchy
const agents = [
  { id: 'agent-ceo', name: 'Atlas (CEO Agent)', role: 'Executive', status: 'ACTIVE', budgetSpent: 4.80, budgetLimit: 25.00, currentTask: 'Monitoring high-level company OKRs' },
  { id: 'agent-cto', name: 'Nova (CTO Agent)', role: 'Architecture', status: 'ACTIVE', budgetSpent: 6.20, budgetLimit: 20.00, currentTask: 'Container topology & agent routing' },
  { id: 'agent-dev', name: 'CyberCode (Dev Agent)', role: 'Engineering', status: 'RUNNING', budgetSpent: 5.40, budgetLimit: 15.00, currentTask: 'Writing Node.js middleware' },
  { id: 'agent-qa', name: 'Sentinel (QA Agent)', role: 'Quality & Audit', status: 'IDLE', budgetSpent: 2.00, budgetLimit: 10.00, currentTask: 'Awaiting PR #42 test suite run' }
];

console.log("💓 Heartbeat scheduler started: checking agent health every 30s");
console.log("🔒 Governance Gates: Enabled (Approval required for production actions)");

// Paperclip API Endpoints
app.get('/api/agents', (req, res) => {
  console.log("GET /api/agents -> Returned 4 registered agents");
  return { success: true, count: agents.length, agents };
});

app.get('/api/agents/heartbeat', (req, res) => {
  return {
    status: 'HEALTHY',
    timestamp: new Date().toISOString(),
    activeCount: agents.filter(a => a.status !== 'IDLE').length,
    totalBudgetSpent: "$" + agents.reduce((sum, a) => sum + a.budgetSpent, 0).toFixed(2),
    approvalQueue: 1
  };
});

app.post('/api/tasks/delegate', (req, res) => {
  const taskId = 'task-' + crypto.randomUUID().substring(0, 8);
  console.log("[CEO Agent] Delegated task " + taskId + " to Nova (CTO): Scale container memory");
  return { success: true, taskId, status: 'DELEGATED' };
});

// Start Control Plane Listener
app.listen(PORT, () => {
  console.log(`⚡ Paperclip Control Plane online at http://localhost:${'$'}{PORT}`);
  console.log("👉 Tap 'Live Preview' on port 3100 to open the Agent Org Chart!");
});
""".trimIndent()
            ),
            ProjectFile(
                id = "file-paperclip-package",
                name = "package.json",
                path = "package.json",
                language = FileLanguage.JSON,
                content = """
{
  "name": "paperclip-control-plane",
  "version": "1.0.0",
  "description": "Paperclip AI Agent Orchestration Platform (paperclipai/paperclip)",
  "main": "server.js",
  "repository": {
    "type": "git",
    "url": "https://github.com/paperclipai/paperclip.git"
  },
  "scripts": {
    "start": "node server.js",
    "onboard": "node server.js --onboard",
    "agents:status": "node -e 'console.log(\"All 4 agents operational\")'",
    "test": "node --test"
  },
  "dependencies": {
    "express": "^4.19.2",
    "dotenv": "^16.4.5"
  }
}
""".trimIndent()
            ),
            ProjectFile(
                id = "file-paperclip-config",
                name = "paperclip.config.json",
                path = "paperclip.config.json",
                language = FileLanguage.JSON,
                content = """
{
  "company": "NodeForge Autonomous Labs",
  "version": "1.0.0",
  "port": 3100,
  "harnesses": ["claude-code", "gemini-cli", "codex", "cursor"],
  "agents": {
    "ceo": { "name": "Atlas", "role": "CEO", "dailyBudgetUsd": 25.00 },
    "cto": { "name": "Nova", "role": "CTO", "dailyBudgetUsd": 20.00 },
    "engineer": { "name": "CyberCode", "role": "Lead Developer", "dailyBudgetUsd": 15.00 },
    "qa": { "name": "Sentinel", "role": "QA & Auditor", "dailyBudgetUsd": 10.00 }
  },
  "approvalGates": {
    "requireApprovalForDeploy": true,
    "requireApprovalAboveBudgetUsd": 50.00
  }
}
""".trimIndent()
            ),
            ProjectFile(
                id = "file-paperclip-ceo",
                name = "ceo.js",
                path = "src/agents/ceo.js",
                language = FileLanguage.JAVASCRIPT,
                content = """
// Paperclip CEO Autonomous Agent
class CeoAgent {
  constructor(name = "Atlas") {
    this.name = name;
    this.role = "CEO";
  }

  evaluateCompanyHealth(metrics) {
    console.log(`[${'$'}{this.name}] Evaluating OKRs and agent resource allocations...`);
    return { status: "OPTIMAL", nextAction: "DELEGATE_SPRINT_TASKS" };
  }
}

module.exports = { CeoAgent };
""".trimIndent()
            ),
            ProjectFile(
                id = "file-paperclip-readme",
                name = "README.md",
                path = "README.md",
                language = FileLanguage.MARKDOWN,
                content = """
# 📎 Paperclip AI Control Plane

Open-source control plane for managing and orchestrating autonomous AI agent networks.
- Repository: [github.com/paperclipai/paperclip](https://github.com/paperclipai/paperclip)
- Web Interface: `http://localhost:3100`

### Running on NodeForge
1. Tap **Run** or type `npm start` in the terminal to launch the server on port 3100.
2. Tap **Live Preview** to inspect the real-time Org Chart, agent budgets, and approval gates!
""".trimIndent()
            )
        )
    }
}
