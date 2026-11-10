package com.example.model

data class GitCommit(
    val hash: String,
    val message: String,
    val author: String,
    val timestamp: Long,
    val filesChanged: List<String>,
    val isOffline: Boolean = false
)

data class GitBranch(
    val name: String,
    val isCurrent: Boolean = false,
    val commitCount: Int = 1
)

data class GitRepositoryState(
    val currentBranch: String = "main",
    val branches: List<GitBranch> = listOf(
        GitBranch("main", isCurrent = true, commitCount = 12),
        GitBranch("feature/api-v2", isCurrent = false, commitCount = 3),
        GitBranch("hotfix/auth-leak", isCurrent = false, commitCount = 1)
    ),
    val stagedFiles: List<String> = emptyList(),
    val unstagedFiles: List<String> = listOf("src/index.js", "package.json"),
    val commitHistory: List<GitCommit> = emptyList(),
    val remoteUrl: String = "https://github.com/nodepod-dev/mobile-node-service.git",
    val isSyncing: Boolean = false,
    val pendingOfflineCommits: Int = 0
)
