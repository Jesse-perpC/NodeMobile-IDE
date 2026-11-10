package com.example.model

enum class AuditCategory {
    CONTAINER_DEPLOY,
    CONTAINER_LIFECYCLE,
    SCRIPT_EXEC,
    CODE_EDIT,
    GIT_COMMIT,
    GIT_PUSH,
    SECRET_ACCESS,
    ROLE_CHANGE,
    SECURITY_ALERT
}

enum class AuditStatus {
    SUCCESS,
    WARNING,
    BLOCKED,
    FAILURE
}

data class AuditRecord(
    val id: String = java.util.UUID.randomUUID().toString().take(8),
    val timestamp: Long = System.currentTimeMillis(),
    val actor: String,
    val role: UserRole,
    val category: AuditCategory,
    val action: String,
    val target: String,
    val status: AuditStatus = AuditStatus.SUCCESS,
    val details: String = "",
    val ipDevice: String = "Android/NodeForge-Host"
)
