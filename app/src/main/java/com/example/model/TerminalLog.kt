package com.example.model

enum class LogLevel {
    INFO,
    WARN,
    ERROR,
    DEBUG,
    SUCCESS,
    SYSTEM
}

data class TerminalLog(
    val id: String = java.util.UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val level: LogLevel = LogLevel.INFO,
    val text: String,
    val source: String = "node" // node, npm, git, docker, system
)
