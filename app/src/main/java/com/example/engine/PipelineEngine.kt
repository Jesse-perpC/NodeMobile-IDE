package com.example.engine

import com.example.model.AuditCategory
import com.example.model.AuditStatus
import com.example.model.DeploymentPipeline
import com.example.model.PipelineStatus
import com.example.model.PipelineStep
import com.example.model.UserRole
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class PipelineEngine(
    private val onAuditLog: (AuditCategory, String, String, AuditStatus, String) -> Unit,
    private val onTerminalLog: (String, String) -> Unit
) {

    private val _pipeline = MutableStateFlow(
        DeploymentPipeline(
            id = "pipe-prod-01",
            name = "Deploy Express API to Containerized Edge",
            targetEnvironment = "Production Container (node:20-alpine)"
        )
    )
    val pipeline: StateFlow<DeploymentPipeline> = _pipeline.asStateFlow()

    suspend fun runPipeline(actorRole: UserRole, actorName: String): Boolean {
        if (!actorRole.canDeploy()) {
            onAuditLog(
                AuditCategory.SECURITY_ALERT,
                "UNAUTHORIZED_PIPELINE_TRIGGER",
                _pipeline.value.id,
                AuditStatus.BLOCKED,
                "User $actorName with role ${actorRole.name} attempted to trigger deployment pipeline without ADMIN permissions"
            )
            onTerminalLog("Security Error: Only ADMIN role can trigger production deployment pipeline", "deploy")
            return false
        }

        onAuditLog(
            AuditCategory.CONTAINER_DEPLOY,
            "PIPELINE_STARTED",
            _pipeline.value.id,
            AuditStatus.SUCCESS,
            "Automated deployment pipeline initiated by $actorName"
        )
        onTerminalLog("🚀 Starting automated deployment pipeline: ${_pipeline.value.name}", "deploy")

        _pipeline.update { it.copy(status = PipelineStatus.RUNNING, currentStepIndex = 0) }

        val stepOutputs = listOf(
            "Passed 0 lint errors, 0 security warnings. AST valid.",
            "npm audit checked 84 packages. 0 vulnerabilities found.",
            "Ran 4 test suites: 12 passed, 0 failed. Coverage 94.2%.",
            "Built OCI image node-mobile-service:v1.4.0 (size: 68.4 MB).",
            "Deployed to container runtime c-74a9b2 on port 3000:3000.",
            "Health check HTTP 200 OK received from http://localhost:3000/health in 12ms."
        )

        for (i in _pipeline.value.steps.indices) {
            _pipeline.update { p ->
                val updatedSteps = p.steps.toMutableList()
                updatedSteps[i] = updatedSteps[i].copy(status = PipelineStatus.RUNNING)
                p.copy(currentStepIndex = i, steps = updatedSteps)
            }

            val stepName = _pipeline.value.steps[i].name
            onTerminalLog("⏳ [$i/6] Running $stepName...", "deploy")
            delay(900)

            _pipeline.update { p ->
                val updatedSteps = p.steps.toMutableList()
                updatedSteps[i] = updatedSteps[i].copy(
                    status = PipelineStatus.SUCCESS,
                    output = stepOutputs.getOrElse(i) { "Step completed successfully." },
                    durationMs = (600..1200).random().toLong()
                )
                p.copy(steps = updatedSteps)
            }
            onTerminalLog("✅ $stepName completed: ${stepOutputs.getOrElse(i) { "" }}", "deploy")
        }

        _pipeline.update { it.copy(status = PipelineStatus.SUCCESS) }

        onAuditLog(
            AuditCategory.CONTAINER_DEPLOY,
            "PIPELINE_SUCCESS",
            _pipeline.value.id,
            AuditStatus.SUCCESS,
            "Pipeline successfully deployed to container runtime at http://localhost:3000"
        )
        onTerminalLog("🎉 Deployment finished! Containerized app is active at http://localhost:3000", "deploy")
        return true
    }

    fun resetPipeline() {
        _pipeline.update { p ->
            p.copy(
                status = PipelineStatus.IDLE,
                currentStepIndex = 0,
                steps = p.steps.map { it.copy(status = PipelineStatus.IDLE, output = "", durationMs = 0L) }
            )
        }
    }
}
