package com.example.model

enum class FileLanguage(val extension: String, val displayName: String) {
    JAVASCRIPT("js", "JavaScript"),
    TYPESCRIPT("ts", "TypeScript"),
    JSON("json", "JSON"),
    HTML("html", "HTML"),
    CSS("css", "CSS"),
    MARKDOWN("md", "Markdown"),
    PYTHON("py", "Python"),
    DOCKERFILE("dockerfile", "Dockerfile"),
    YAML("yml", "YAML"),
    TEXT("txt", "Text");

    companion object {
        fun fromFileName(name: String): FileLanguage {
            val ext = name.substringAfterLast('.', "").lowercase()
            if (name.equals("Dockerfile", ignoreCase = true)) return DOCKERFILE
            return entries.firstOrNull { it.extension == ext } ?: when (ext) {
                "cjs", "mjs" -> JAVASCRIPT
                "tsx" -> TYPESCRIPT
                "yaml" -> YAML
                else -> TEXT
            }
        }
    }
}

data class ProjectFile(
    val id: String,
    val name: String,
    val path: String,
    val isDirectory: Boolean = false,
    val content: String = "",
    val language: FileLanguage = FileLanguage.fromFileName(name),
    val isModified: Boolean = false,
    val parentPath: String = ""
)
