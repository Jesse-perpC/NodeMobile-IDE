package com.example.ui.components

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.CodeFontFamily
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NodeEmerald
import com.example.ui.theme.NodeEmeraldLight
import com.example.ui.theme.SlateBackground
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateDarkest
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.SlateSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import org.json.JSONObject

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun InAppWebPreview(
    serverPort: Int = 3000,
    isServerActive: Boolean = true,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var urlPath by remember { mutableStateOf("/health") }
    var selectedMethod by remember { mutableStateOf("GET") }
    var responseBody by remember {
        mutableStateOf(
            """
            {
              "status": "UP",
              "service": "node-mobile-service",
              "port": 3000,
              "nodeVersion": "v20.18.0",
              "uptime": 142.5,
              "memoryUsage": "214 MB allocated",
              "health": "200 OK",
              "architecture": "ARM64_Android"
            }
            """.trimIndent()
        )
    }
    var responseStatus by remember { mutableStateOf("200 OK (14ms)") }
    var isHtmlMode by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SlateDarkest)
    ) {
        // Top Browser Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SlateBackground)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = "Live Preview",
                    tint = ElectricCyan,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Live In-App Web Inspector",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextPrimary
                )
            }

            IconButton(
                onClick = onClose,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close Preview",
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // URL bar & Method selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SlateSurface)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                onClick = { selectedMethod = if (selectedMethod == "GET") "POST" else "GET" },
                shape = RoundedCornerShape(6.dp),
                color = if (selectedMethod == "GET") NodeEmerald.copy(alpha = 0.2f) else ElectricCyan.copy(alpha = 0.2f),
                modifier = Modifier.padding(end = 6.dp)
            ) {
                Text(
                    text = selectedMethod,
                    fontFamily = CodeFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = if (selectedMethod == "GET") NodeEmeraldLight else ElectricCyan,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                )
            }

            Text(
                text = "http://localhost:$serverPort",
                fontFamily = CodeFontFamily,
                fontSize = 12.sp,
                color = TextSecondary,
                modifier = Modifier.padding(end = 4.dp)
            )

            OutlinedTextField(
                value = urlPath,
                onValueChange = { urlPath = it },
                singleLine = true,
                modifier = Modifier.weight(1f),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontFamily = CodeFontFamily,
                    fontSize = 12.sp,
                    color = TextPrimary
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NodeEmerald,
                    unfocusedBorderColor = SlateBorder
                )
            )

            IconButton(
                onClick = {
                    if (urlPath == "/" || urlPath.endsWith(".html")) {
                        isHtmlMode = true
                    } else if (urlPath.contains("agents") || serverPort == 3100) {
                        isHtmlMode = false
                        if (urlPath.contains("heartbeat")) {
                            responseBody = """
                            {
                              "status": "HEALTHY",
                              "controlPlane": "Paperclip v1.0.0",
                              "repository": "https://github.com/paperclipai/paperclip.git",
                              "port": 3100,
                              "timestamp": "${java.util.Date()}",
                              "activeAgents": 3,
                              "idleAgents": 1,
                              "budgetSpentToday": "$18.40 / $70.00",
                              "approvalGates": { "pendingApprovals": 1, "status": "GUARDED" }
                            }
                            """.trimIndent()
                        } else {
                            responseBody = """
                            {
                              "company": "Paperclip Autonomous Org",
                              "repository": "https://github.com/paperclipai/paperclip.git",
                              "agents": [
                                { "id": "agent-ceo", "name": "Atlas", "role": "CEO", "status": "ACTIVE", "budget": "$4.80 / $25.00" },
                                { "id": "agent-cto", "name": "Nova", "role": "CTO", "status": "ACTIVE", "budget": "$6.20 / $20.00" },
                                { "id": "agent-dev", "name": "CyberCode", "role": "Lead Dev", "status": "RUNNING", "budget": "$5.40 / $15.00" },
                                { "id": "agent-qa", "name": "Sentinel", "role": "QA Auditor", "status": "IDLE", "budget": "$2.00 / $10.00" }
                              ]
                            }
                            """.trimIndent()
                        }
                        responseStatus = "200 OK (6ms)"
                    } else if (urlPath.contains("tasks")) {
                        isHtmlMode = false
                        responseBody = if (selectedMethod == "POST") {
                            """{"success":true,"created":{"id":"uuid-771a","title":"Task created via Mobile Inspector","status":"ACTIVE"}}"""
                        } else {
                            """{"success":true,"count":3,"data":[{"id":"1","title":"Compile Node.js V8 runtime on ARM64","status":"COMPLETED"},{"id":"2","title":"Containerize Express microservice","status":"IN_PROGRESS"},{"id":"3","title":"Sync offline git commits to cloud repo","status":"PENDING"}]}"""
                        }
                        responseStatus = "200 OK (8ms)"
                    } else {
                        isHtmlMode = false
                        responseBody = """{"status":"UP","uptime":158.2,"timestamp":"${java.util.Date()}","port":$serverPort}"""
                        responseStatus = "200 OK (5ms)"
                    }
                },
                modifier = Modifier
                    .padding(start = 4.dp)
                    .testTag("send_request_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send Request",
                    tint = NodeEmerald
                )
            }
        }

        // Status badge
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SlateSurfaceVariant.copy(alpha = 0.5f))
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Status: $responseStatus",
                fontFamily = CodeFontFamily,
                fontSize = 11.sp,
                color = NodeEmeraldLight,
                fontWeight = FontWeight.SemiBold
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    onClick = { isHtmlMode = !isHtmlMode },
                    shape = RoundedCornerShape(4.dp),
                    color = if (isHtmlMode) ElectricCyan.copy(alpha = 0.2f) else SlateSurface
                ) {
                    Text(
                        text = if (isHtmlMode) "HTML View" else "JSON View",
                        fontFamily = CodeFontFamily,
                        fontSize = 10.sp,
                        color = if (isHtmlMode) ElectricCyan else TextSecondary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }

        HorizontalDivider(color = SlateBorder, thickness = 1.dp)

        // Content Display
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            if (isHtmlMode) {
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            settings.javaScriptEnabled = true
                            webViewClient = WebViewClient()
                            val htmlContent = if (serverPort == 3100 || urlPath.contains("agent") || urlPath.contains("paperclip")) {
                                """
                                <html>
                                <head>
                                  <style>
                                    body { background: #0F172A; color: #F8FAFC; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; padding: 16px; margin: 0; }
                                    .header { display: flex; align-items: center; justify-content: space-between; border-bottom: 1px solid #334155; padding-bottom: 12px; margin-bottom: 16px; }
                                    .badge { background: #064E3B; color: #34D399; font-size: 11px; padding: 4px 8px; border-radius: 6px; font-weight: bold; }
                                    .card { background: #1E293B; border: 1px solid #334155; padding: 12px; border-radius: 8px; margin-bottom: 10px; }
                                    .agent-title { font-weight: bold; color: #38BDF8; display: flex; justify-content: space-between; }
                                    .budget { color: #FBBF24; font-size: 11px; }
                                    .task { color: #94A3B8; font-size: 12px; margin-top: 4px; }
                                    .pill-active { color: #10B981; font-size: 11px; font-weight: bold; }
                                    .pill-run { color: #06B6D4; font-size: 11px; font-weight: bold; }
                                    .gate { background: #451A03; border: 1px solid #B45309; padding: 10px; border-radius: 6px; margin-top: 12px; }
                                  </style>
                                </head>
                                <body>
                                  <div class="header">
                                    <div>
                                      <h3 style="margin:0;color:#10B981;">📎 Paperclip AI Control Plane</h3>
                                      <small style="color:#64748B;">github.com/paperclipai/paperclip • Port 3100</small>
                                    </div>
                                    <span class="badge">● 4 AGENTS ONLINE</span>
                                  </div>

                                  <div class="card">
                                    <div class="agent-title">
                                      <span>👔 Atlas (CEO Agent)</span>
                                      <span class="pill-active">ACTIVE</span>
                                    </div>
                                    <div class="task">Monitoring OKRs & Autonomous Delegation</div>
                                    <div class="budget">Daily Budget: $4.80 / $25.00</div>
                                  </div>

                                  <div class="card">
                                    <div class="agent-title">
                                      <span>💻 Nova (CTO Agent)</span>
                                      <span class="pill-active">ACTIVE</span>
                                    </div>
                                    <div class="task">Container Topology & Microservice Architecture</div>
                                    <div class="budget">Daily Budget: $6.20 / $20.00</div>
                                  </div>

                                  <div class="card">
                                    <div class="agent-title">
                                      <span>🛠️ CyberCode (Lead Dev)</span>
                                      <span class="pill-run">RUNNING</span>
                                    </div>
                                    <div class="task">Generating Node.js middleware & REST endpoints</div>
                                    <div class="budget">Daily Budget: $5.40 / $15.00</div>
                                  </div>

                                  <div class="card">
                                    <div class="agent-title">
                                      <span>🔍 Sentinel (QA Agent)</span>
                                      <span style="color:#64748B;font-size:11px;">IDLE</span>
                                    </div>
                                    <div class="task">Awaiting PR #42 test suite execution</div>
                                    <div class="budget">Daily Budget: $2.00 / $10.00</div>
                                  </div>

                                  <div class="gate">
                                    <b style="color:#F59E0B;">⚠️ Governance Gate Active</b>
                                    <div style="font-size:12px;color:#FDE68A;margin-top:2px;">Lead Dev requested automated container deployment to port 3000. Approval required.</div>
                                  </div>
                                </body>
                                </html>
                                """.trimIndent()
                            } else {
                                """
                                <html>
                                <body style="background:#0F172A;color:#F8FAFC;font-family:sans-serif;padding:20px;">
                                  <h2 style="color:#10B981;">🚀 NodeForge Web Application Online</h2>
                                  <p>Serving live from Node container port <b>$serverPort</b>.</p>
                                  <div style="background:#1E293B;padding:12px;border-radius:8px;border:1px solid #334155;">
                                    <b>Client Preview:</b> Mobile Developer Workspace ready!
                                  </div>
                                </body>
                                </html>
                                """.trimIndent()
                            }
                            loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(8.dp),
                    color = SlateSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = responseBody,
                            fontFamily = CodeFontFamily,
                            fontSize = 12.sp,
                            color = NodeEmeraldLight,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}
