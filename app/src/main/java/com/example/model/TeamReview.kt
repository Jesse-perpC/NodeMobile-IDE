package com.example.model

enum class UserRole(val displayName: String, val level: Int) {
    ADMIN("Admin", 4),
    DEVELOPER("Developer", 3),
    REVIEWER("Reviewer", 2),
    VIEWER("Viewer", 1);

    fun canEditCode(): Boolean = this == ADMIN || this == DEVELOPER
    fun canRunScripts(): Boolean = this == ADMIN || this == DEVELOPER
    fun canDeploy(): Boolean = this == ADMIN
    fun canReviewPR(): Boolean = this == ADMIN || this == REVIEWER
    fun canManageSecrets(): Boolean = this == ADMIN
}

data class UserProfile(
    val id: String = "user-1",
    val name: String = "Alex Rivera",
    val email: String = "alex@nodepod.internal",
    val role: UserRole = UserRole.DEVELOPER,
    val avatarInitials: String = "AR"
)

enum class ReviewStatus {
    OPEN,
    CHANGES_REQUESTED,
    APPROVED,
    MERGED
}

data class CodeComment(
    val id: String,
    val author: String,
    val authorRole: UserRole,
    val filePath: String,
    val lineNumber: Int,
    val comment: String,
    val timestamp: Long,
    val isResolved: Boolean = false
)

data class PullRequest(
    val id: String,
    val number: Int,
    val title: String,
    val description: String,
    val author: String,
    val branch: String,
    val targetBranch: String = "main",
    val status: ReviewStatus = ReviewStatus.OPEN,
    val comments: List<CodeComment> = emptyList(),
    val approvals: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)
