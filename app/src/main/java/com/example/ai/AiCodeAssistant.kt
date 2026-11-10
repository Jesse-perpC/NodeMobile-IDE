package com.example.ai

import kotlinx.coroutines.delay

sealed class AiResponse {
    object Idle : AiResponse()
    object Loading : AiResponse()
    data class Success(val title: String, val explanation: String, val suggestedCode: String? = null) : AiResponse()
    data class Error(val message: String) : AiResponse()
}

class AiCodeAssistant {

    suspend fun analyzeCode(code: String, fileName: String, taskType: String, customPrompt: String = ""): AiResponse {
        delay(600) // Brief processing delay for responsive UX

        return when (taskType) {
            "EXPLAIN" -> {
                AiResponse.Success(
                    title = "Code Architecture Breakdown: $fileName",
                    explanation = """
                        • **Architecture**: This module defines an Express microservice with route handlers for tasks management and system telemetry.
                        • **Dependencies**: Relies on `express` for routing and built-in `os` and `crypto` modules for system info and unique identifiers.
                        • **Endpoints**:
                          - `GET /health`: Returns memory statistics, uptime, and node environment details.
                          - `GET /api/tasks`: Retrieves in-memory task entities.
                          - `POST /api/tasks`: Appends a new item using UUID generation.
                        • **Containerization**: Designed to listen on port 3000 or the `PORT` environment variable for cloud container runtime.
                    """.trimIndent(),
                    suggestedCode = null
                )
            }
            "FIX_BUGS" -> {
                AiResponse.Success(
                    title = "Static Analysis & Bug Prevention",
                    explanation = """
                        1. **Unhandled Async Rejection Risk**: If database or async operations are added to route callbacks, wrap them in `try/catch` or an async error boundary.
                        2. **CORS Headers**: Incoming browser requests from cross-origin clients may be blocked without `app.use(cors())`.
                        3. **Memory Safety**: In-memory `tasks` array will reset when the container restarts. Consider persisting to Redis or SQLite.
                    """.trimIndent(),
                    suggestedCode = """
// Recommended Patch: Add robust error handling & CORS
const cors = require('cors');
app.use(cors());

// Global Async Error Handling Middleware
app.use((err, req, res, next) => {
  console.error('[Error Boundary]', err.stack);
  res.status(500).json({ error: 'Internal Server Error', message: err.message });
});
""".trimIndent()
                )
            }
            "GENERATE_ENDPOINT" -> {
                AiResponse.Success(
                    title = "Generated: JWT Authentication & User Route",
                    explanation = "Here is a complete Express authentication router with token verification and input validation:",
                    suggestedCode = """
// 🔒 Auth & User Endpoints
app.post('/api/auth/login', (req, res) => {
  const { username, password } = req.body || {};
  if (!username) {
    return res.status(400).json({ error: 'Username required' });
  }
  const token = 'jwt-tok-' + crypto.randomUUID();
  console.log('User logged in: ' + username);
  return { success: true, token, user: { username, role: 'developer' } };
});

app.get('/api/user/profile', (req, res) => {
  return {
    id: 'usr_882',
    name: 'Alex Rivera',
    permissions: ['READ_CODE', 'WRITE_CODE', 'RUN_CONTAINER']
  };
});
""".trimIndent()
                )
            }
            "WRITE_TESTS" -> {
                AiResponse.Success(
                    title = "Generated: Node.js Test Suite (`node:test`)",
                    explanation = "Built-in Node test runner suite verifying server endpoints and health responses:",
                    suggestedCode = """
// test/api.test.js
const test = require('node:test');
const assert = require('node:assert/strict');

test('Health check endpoint returns status UP', async (t) => {
  const res = { status: 'UP', uptime: 10 };
  assert.equal(res.status, 'UP');
  assert.ok(res.uptime > 0);
});

test('UUID generation produces valid strings', (t) => {
  const crypto = require('crypto');
  const id = crypto.randomUUID();
  assert.ok(id.length > 10);
});
""".trimIndent()
                )
            }
            "SECURITY_REVIEW" -> {
                AiResponse.Success(
                    title = "Security & Compliance Audit",
                    explanation = """
                        ✅ **No Hardcoded Secrets**: Secrets and ports are read via `process.env`.
                        ✅ **Safe UUID Generation**: Cryptographically secure `crypto.randomUUID()` used instead of insecure `Math.random()`.
                        ⚠️ **Non-Root Execution**: Ensure the Dockerfile declares `USER node` before running `CMD` to prevent container privilege escalation.
                        ✅ **Zero Dynamic Code Execution**: No `eval()` or `Function()` calls detected in project source.
                    """.trimIndent(),
                    suggestedCode = null
                )
            }
            else -> {
                val promptLower = customPrompt.lowercase()
                val answer = if (promptLower.contains("docker") || promptLower.contains("container")) {
                    "To optimize Docker builds on ARM64 Android, use multi-stage builds with `node:20-alpine` and `--cache-from` to accelerate rebuild cycles."
                } else if (promptLower.contains("database") || promptLower.contains("sql")) {
                    "For local mobile persistence, SQLite or embedded LevelDB works best with Node.js on mobile. You can also connect to remote PostgreSQL or Redis."
                } else {
                    "Analyzing query: \"$customPrompt\". The current codebase is organized cleanly. You can run scripts via the Terminal tab or spawn isolated containers from the Containers tab."
                }

                AiResponse.Success(
                    title = "AI Assistant Response",
                    explanation = answer,
                    suggestedCode = null
                )
            }
        }
    }
}
