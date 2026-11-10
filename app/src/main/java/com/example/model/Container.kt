package com.example.model

enum class ContainerStatus {
    RUNNING,
    STOPPED,
    BUILDING,
    ERROR
}

data class NodeContainer(
    val id: String,
    val name: String,
    val image: String = "node:20-alpine",
    val status: ContainerStatus = ContainerStatus.STOPPED,
    val portMapping: String = "3000:3000",
    val memoryUsageMb: Int = 128,
    val memoryLimitMb: Int = 512,
    val cpuPercentage: Float = 4.2f,
    val uptimeSeconds: Long = 0L,
    val entrypoint: String = "node index.js",
    val envVars: Map<String, String> = mapOf("NODE_ENV" to "development", "PORT" to "3000"),
    val healthCheckStatus: String = "healthy (200 OK)",
    val restartPolicy: String = "unless-stopped"
)
