package com.example.model

enum class PipelineStatus {
    IDLE,
    RUNNING,
    SUCCESS,
    FAILED
}

data class PipelineStep(
    val id: String,
    val name: String,
    val status: PipelineStatus = PipelineStatus.IDLE,
    val output: String = "",
    val durationMs: Long = 0L
)

data class DeploymentPipeline(
    val id: String,
    val name: String,
    val targetEnvironment: String = "Production (Edge Container)",
    val status: PipelineStatus = PipelineStatus.IDLE,
    val currentStepIndex: Int = 0,
    val steps: List<PipelineStep> = listOf(
        PipelineStep("step-1", "Static Code & Security Lint"),
        PipelineStep("step-2", "Dependency Isolation & Audit"),
        PipelineStep("step-3", "Automated Jest/Mocha Unit Tests"),
        PipelineStep("step-4", "Container Image Build (Docker/OCI)"),
        PipelineStep("step-5", "Deploy to Local & Cloud Runtime"),
        PipelineStep("step-6", "Live Health Check & Traffic Cutover")
    ),
    val deployedUrl: String = "http://localhost:3000"
)
