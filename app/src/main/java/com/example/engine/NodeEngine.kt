package com.example.engine

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import com.example.model.LogLevel
import com.example.model.TerminalLog
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.json.JSONObject

class NodeEngine(private val context: Context) {

    private val _logs = MutableSharedFlow<TerminalLog>(extraBufferCapacity = 500)
    val logs: SharedFlow<TerminalLog> = _logs.asSharedFlow()

    private var headlessWebView: WebView? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    // Simulated active HTTP endpoints registered by express / http server scripts
    private val registeredRoutes = mutableMapOf<String, (method: String, query: String) -> String>()
    var isServerRunning: Boolean = false
        private set
    var serverPort: Int = 3000
        private set

    init {
        mainHandler.post {
            initWebView()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        try {
            headlessWebView = WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.cacheMode = WebSettings.LOAD_NO_CACHE
                addJavascriptInterface(AndroidJsBridge(), "NodeDroidHost")
            }
        } catch (e: Exception) {
            emitLog(LogLevel.ERROR, "Failed to initialize Node.js runtime engine: ${e.message}")
        }
    }

    inner class AndroidJsBridge {
        @JavascriptInterface
        fun onLog(level: String, message: String, source: String = "node") {
            val logLevel = when (level.uppercase()) {
                "ERROR" -> LogLevel.ERROR
                "WARN" -> LogLevel.WARN
                "DEBUG" -> LogLevel.DEBUG
                "SUCCESS" -> LogLevel.SUCCESS
                "SYSTEM" -> LogLevel.SYSTEM
                else -> LogLevel.INFO
            }
            emitLog(logLevel, message, source)
        }

        @JavascriptInterface
        fun onServerStart(port: Int, banner: String) {
            isServerRunning = true
            serverPort = port
            emitLog(LogLevel.SUCCESS, "⚡ Node.js HTTP Server listening on http://localhost:$port", "http")
            emitLog(LogLevel.INFO, banner, "express")
        }

        @JavascriptInterface
        fun onExecutionComplete(executionTimeMs: Long) {
            emitLog(LogLevel.SYSTEM, "✨ Process exited with code 0 (completed in ${executionTimeMs}ms)", "process")
        }

        @JavascriptInterface
        fun onError(errorJson: String) {
            try {
                val json = JSONObject(errorJson)
                val name = json.optString("name", "Error")
                val msg = json.optString("message", "Unknown error")
                val stack = json.optString("stack", "")
                emitLog(LogLevel.ERROR, "❌ $name: $msg", "node")
                if (stack.isNotBlank()) {
                    stack.lines().drop(1).take(4).forEach { line ->
                        emitLog(LogLevel.WARN, "   $line", "stack")
                    }
                }
            } catch (e: Exception) {
                emitLog(LogLevel.ERROR, "Runtime Error: $errorJson", "node")
            }
        }
    }

    fun emitLog(level: LogLevel, text: String, source: String = "node") {
        _logs.tryEmit(TerminalLog(level = level, text = text, source = source))
    }

    fun executeScript(code: String, fileName: String = "index.js", env: Map<String, String> = emptyMap()) {
        emitLog(LogLevel.SYSTEM, "$ node $fileName", "cli")
        emitLog(LogLevel.DEBUG, "Loading environment [NODE_ENV=${env["NODE_ENV"] ?: "development"}, V8_ENGINE=Active]...", "system")

        val envJson = JSONObject(env).toString()

        val wrapper = """
            (function() {
                const __startTime = Date.now();
                
                // Polyfill console
                const formatArgs = (args) => {
                    return Array.from(args).map(a => {
                        if (typeof a === 'object' && a !== null) {
                            try { return JSON.stringify(a, null, 2); } catch(e) { return String(a); }
                        }
                        return String(a);
                    }).join(' ');
                };

                console.log = function() { NodeDroidHost.onLog('INFO', formatArgs(arguments), 'stdout'); };
                console.info = function() { NodeDroidHost.onLog('INFO', formatArgs(arguments), 'info'); };
                console.warn = function() { NodeDroidHost.onLog('WARN', formatArgs(arguments), 'warn'); };
                console.error = function() { NodeDroidHost.onLog('ERROR', formatArgs(arguments), 'stderr'); };
                console.debug = function() { NodeDroidHost.onLog('DEBUG', formatArgs(arguments), 'debug'); };
                console.table = function(data) { console.log(JSON.stringify(data, null, 2)); };

                // Process polyfill
                window.process = {
                    version: 'v20.18.0',
                    platform: 'android',
                    arch: 'arm64',
                    pid: 1042,
                    env: Object.assign({
                        NODE_ENV: 'development',
                        PORT: '3000',
                        PATH: '/usr/local/bin:/usr/bin:/bin'
                    }, $envJson),
                    cwd: () => '/app',
                    uptime: () => (Date.now() - __startTime) / 1000,
                    exit: (code = 0) => {
                        NodeDroidHost.onLog('SYSTEM', 'Process explicitly exited with code ' + code, 'process');
                    }
                };

                // Node.js Require Simulation
                window.require = function(mod) {
                    if (mod === 'express') {
                        return function() {
                            const app = {
                                routes: {},
                                get: function(path, handler) { this.routes['GET ' + path] = handler; return this; },
                                post: function(path, handler) { this.routes['POST ' + path] = handler; return this; },
                                put: function(path, handler) { this.routes['PUT ' + path] = handler; return this; },
                                delete: function(path, handler) { this.routes['DELETE ' + path] = handler; return this; },
                                use: function(middleware) { return this; },
                                listen: function(port, callback) {
                                    const p = port || 3000;
                                    NodeDroidHost.onServerStart(p, 'Express 4.19.2 initialized with ' + Object.keys(this.routes).length + ' routes');
                                    if (callback) callback();
                                    return app;
                                }
                            };
                            return app;
                        };
                    } else if (mod === 'fs' || mod === 'fs/promises') {
                        return {
                            readFileSync: (path) => "// [NodeFS] mock content for " + path,
                            writeFileSync: (path, data) => { NodeDroidHost.onLog('INFO', '[fs.writeFile] Wrote ' + (data ? data.length : 0) + ' bytes to ' + path, 'fs'); },
                            existsSync: (path) => true
                        };
                    } else if (mod === 'path') {
                        return {
                            join: (...parts) => parts.join('/').replace(/\/+/g, '/'),
                            resolve: (...parts) => '/' + parts.join('/').replace(/\/+/g, '/'),
                            basename: (p) => p.split('/').pop() || ''
                        };
                    } else if (mod === 'os') {
                        return {
                            platform: () => 'linux',
                            cpus: () => [ { model: 'ARM Cortex-A78', speed: 2840 }, { model: 'ARM Cortex-A78', speed: 2840 } ],
                            totalmem: () => 8 * 1024 * 1024 * 1024,
                            freemem: () => 3.2 * 1024 * 1024 * 1024
                        };
                    } else if (mod === 'crypto') {
                        return {
                            randomUUID: () => 'uuid-' + Math.random().toString(36).substring(2, 9),
                            createHash: (alg) => ({
                                update: (str) => ({ digest: (enc) => 'sha256-hash-' + Math.random().toString(16).substring(2, 10) })
                            })
                        };
                    }
                    return {};
                };

                try {
                    // Execute user code
                    $code
                    
                    NodeDroidHost.onExecutionComplete(Date.now() - __startTime);
                } catch(err) {
                    NodeDroidHost.onError(JSON.stringify({
                        name: err.name || 'Error',
                        message: err.message || String(err),
                        stack: err.stack || ''
                    }));
                }
            })();
        """.trimIndent()

        mainHandler.post {
            if (headlessWebView == null) {
                initWebView()
            }
            headlessWebView?.evaluateJavascript(wrapper) { result ->
                // Evaluation callback
            }
        }
    }

    fun evaluateRepl(statement: String) {
        emitLog(LogLevel.SYSTEM, "> $statement", "repl")
        val clean = statement.trim()
        if (clean.isBlank()) return

        val replWrapper = """
            (function() {
                try {
                    let result = eval(${JSONObject.quote(clean)});
                    if (typeof result === 'undefined') {
                        NodeDroidHost.onLog('INFO', 'undefined', 'repl');
                    } else if (typeof result === 'object' && result !== null) {
                        NodeDroidHost.onLog('SUCCESS', JSON.stringify(result, null, 2), 'repl');
                    } else {
                        NodeDroidHost.onLog('SUCCESS', String(result), 'repl');
                    }
                } catch(e) {
                    NodeDroidHost.onLog('ERROR', e.name + ': ' + e.message, 'repl');
                }
            })();
        """.trimIndent()

        mainHandler.post {
            headlessWebView?.evaluateJavascript(replWrapper, null)
        }
    }

    fun stopServer() {
        if (isServerRunning) {
            isServerRunning = false
            emitLog(LogLevel.WARN, "🛑 Node.js HTTP Server on port $serverPort terminated.", "http")
        }
    }

    fun clearLogs() {
        emitLog(LogLevel.SYSTEM, "Terminal buffer cleared.", "system")
    }
}
