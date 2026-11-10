package com.example.engine

import com.example.model.AuditCategory
import com.example.model.AuditStatus
import com.example.model.ContainerStatus
import com.example.model.NodeContainer
import com.example.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random

class ContainerManager(
    private val onAuditLog: (AuditCategory, String, String, AuditStatus, String) -> Unit,
    private val onTerminalLog: (String, String) -> Unit
) {

    private val _containers = MutableStateFlow<List<NodeContainer>>(
        listOf(
            NodeContainer(
                id = "c-74a9b2",
                name = "express-api-prod",
                image = "node:20-alpine",
                status = ContainerStatus.RUNNING,
                portMapping = "3000:3000",
                memoryUsageMb = 142,
                memoryLimitMb = 512,
                cpuPercentage = 3.8f,
                uptimeSeconds = 4820L,
                entrypoint = "node dist/index.js",
                healthCheckStatus = "healthy (200 OK)"
            ),
            NodeContainer(
                id = "c-91d4e8",
                name = "redis-cache-dev",
                image = "redis:7.2-alpine",
                status = ContainerStatus.RUNNING,
                portMapping = "6379:6379",
                memoryUsageMb = 38,
                memoryLimitMb = 256,
                cpuPercentage = 0.9f,
                uptimeSeconds = 9420L,
                entrypoint = "redis-server --protected-mode no",
                healthCheckStatus = "healthy (PONG)"
            ),
            NodeContainer(
                id = "c-33b8f1",
                name = "worker-queue-service",
                image = "node:20-slim",
                status = ContainerStatus.STOPPED,
                portMapping = "8080:8080",
                memoryUsageMb = 0,
                memoryLimitMb = 512,
                cpuPercentage = 0.0f,
                uptimeSeconds = 0L,
                entrypoint = "node worker.js",
                healthCheckStatus = "exited (0)"
            )
        )
    )
    val containers: StateFlow<List<NodeContainer>> = _containers.asStateFlow()

    fun startContainer(id: String, actorRole: UserRole, actorName: String) {
        if (!actorRole.canDeploy() && !actorRole.canRunScripts()) {
            onAuditLog(
                AuditCategory.SECURITY_ALERT,
                "UNAUTHORIZED_CONTAINER_START",
                id,
                AuditStatus.BLOCKED,
                "User $actorName with role ${actorRole.name} attempted to start container $id without execution permissions"
            )
            onTerminalLog("Security: Permission denied to start container $id", "docker")
            return
        }

        _containers.update { list ->
            list.map { container ->
                if (container.id == id) {
                    container.copy(
                        status = ContainerStatus.RUNNING,
                        memoryUsageMb = Random.nextInt(120, 180),
                        cpuPercentage = Random.nextDouble(2.0, 7.5).toFloat(),
                        uptimeSeconds = 1L,
                        healthCheckStatus = "healthy (starting...)"
                    )
                } else container
            }
        }
        onAuditLog(AuditCategory.CONTAINER_LIFECYCLE, "CONTAINER_START", id, AuditStatus.SUCCESS, "Container $id started by $actorName")
        onTerminalLog("Container $id (started) - status RUNNING", "docker")
    }

    fun stopContainer(id: String, actorRole: UserRole, actorName: String) {
        _containers.update { list ->
            list.map { container ->
                if (container.id == id) {
                    container.copy(
                        status = ContainerStatus.STOPPED,
                        memoryUsageMb = 0,
                        cpuPercentage = 0.0f,
                        healthCheckStatus = "stopped by user"
                    )
                } else container
            }
        }
        onAuditLog(AuditCategory.CONTAINER_LIFECYCLE, "CONTAINER_STOP", id, AuditStatus.SUCCESS, "Container $id stopped by $actorName")
        onTerminalLog("Container $id (stopped)", "docker")
    }

    fun restartContainer(id: String, actorRole: UserRole, actorName: String) {
        stopContainer(id, actorRole, actorName)
        startContainer(id, actorRole, actorName)
    }

    fun spawnContainer(
        name: String,
        image: String,
        portMapping: String,
        entrypoint: String,
        envVars: Map<String, String>,
        actorRole: UserRole,
        actorName: String
    ): Boolean {
        if (!actorRole.canDeploy()) {
            onAuditLog(AuditCategory.SECURITY_ALERT, "UNAUTHORIZED_CONTAINER_SPAWN", name, AuditStatus.BLOCKED, "Role ${actorRole.name} denied deployment rights")
            return false
        }

        val newId = "c-" + java.util.UUID.randomUUID().toString().take(6)
        val newContainer = NodeContainer(
            id = newId,
            name = name.ifBlank { "node-service-" + Random.nextInt(100, 999) },
            image = image.ifBlank { "node:20-alpine" },
            status = ContainerStatus.RUNNING,
            portMapping = portMapping.ifBlank { "3000:3000" },
            entrypoint = entrypoint.ifBlank { "node index.js" },
            envVars = envVars,
            memoryUsageMb = Random.nextInt(90, 150),
            cpuPercentage = 2.4f,
            uptimeSeconds = 5L
        )

        _containers.update { it + newContainer }
        onAuditLog(AuditCategory.CONTAINER_DEPLOY, "CONTAINER_SPAWN", newId, AuditStatus.SUCCESS, "Spawned container $name ($image) by $actorName")
        onTerminalLog("Successfully created and launched container $newId [$image] -> $portMapping", "docker")
        return true
    }

    fun removeContainer(id: String, actorRole: UserRole, actorName: String) {
        if (!actorRole.canDeploy()) return
        _containers.update { it.filterNot { c -> c.id == id } }
        onAuditLog(AuditCategory.CONTAINER_LIFECYCLE, "CONTAINER_DELETE", id, AuditStatus.SUCCESS, "Deleted container $id by $actorName")
        onTerminalLog("Deleted container $id", "docker")
    }

    fun simulateTick() {
        _containers.update { list ->
            list.map { container ->
                if (container.status == ContainerStatus.RUNNING) {
                    val cpuJitter = (container.cpuPercentage + (Random.nextFloat() * 1.2f - 0.6f)).coerceIn(0.5f, 18.0f)
                    val memJitter = (container.memoryUsageMb + Random.nextInt(-2, 3)).coerceIn(40, container.memoryLimitMb - 10)
                    container.copy(
                        uptimeSeconds = container.uptimeSeconds + 1,
                        cpuPercentage = (cpuJitter * 10).toInt() / 10f,
                        memoryUsageMb = memJitter
                    )
                } else container
            }
        }
    }
}
