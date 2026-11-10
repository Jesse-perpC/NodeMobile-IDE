package com.example.ui.components

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import com.example.model.FileLanguage
import com.example.ui.theme.SyntaxComment
import com.example.ui.theme.SyntaxFunction
import com.example.ui.theme.SyntaxKeyword
import com.example.ui.theme.SyntaxNumber
import com.example.ui.theme.SyntaxOperator
import com.example.ui.theme.SyntaxString
import com.example.ui.theme.SyntaxTag
import com.example.ui.theme.TextPrimary
import java.util.regex.Pattern

object SyntaxHighlighter {

    private val JS_KEYWORDS = setOf(
        "const", "let", "var", "function", "return", "async", "await",
        "import", "export", "default", "from", "if", "else", "for", "while",
        "class", "extends", "new", "this", "super", "try", "catch", "throw",
        "finally", "switch", "case", "break", "continue", "typeof", "instanceof",
        "yield", "require", "process", "console"
    )

    private val DOCKER_KEYWORDS = setOf(
        "FROM", "WORKDIR", "COPY", "ADD", "RUN", "CMD", "ENTRYPOINT",
        "EXPOSE", "ENV", "USER", "VOLUME", "HEALTHCHECK", "AS"
    )

    fun highlight(text: String, language: FileLanguage): AnnotatedString {
        if (text.isEmpty()) return AnnotatedString("")

        return buildAnnotatedString {
            append(text)

            when (language) {
                FileLanguage.JAVASCRIPT, FileLanguage.TYPESCRIPT -> highlightJs(text, this)
                FileLanguage.JSON -> highlightJson(text, this)
                FileLanguage.DOCKERFILE -> highlightDockerfile(text, this)
                FileLanguage.YAML -> highlightYaml(text, this)
                FileLanguage.HTML -> highlightHtml(text, this)
                FileLanguage.MARKDOWN -> highlightMarkdown(text, this)
                else -> {
                    // Default style
                }
            }
        }
    }

    private fun highlightJs(code: String, builder: AnnotatedString.Builder) {
        // Strings ("..." or '...' or `...`)
        val stringRegex = Pattern.compile("(\"[^\"]*\"|'[^']*'|`[^`]*`)").matcher(code)
        while (stringRegex.find()) {
            builder.addStyle(SpanStyle(color = SyntaxString), stringRegex.start(), stringRegex.end())
        }

        // Comments (// ... or /* ... */)
        val commentRegex = Pattern.compile("(//.*|/\\*[\\s\\S]*?\\*/)").matcher(code)
        while (commentRegex.find()) {
            builder.addStyle(SpanStyle(color = SyntaxComment, fontWeight = FontWeight.Normal), commentRegex.start(), commentRegex.end())
        }

        // Numbers
        val numberRegex = Pattern.compile("\\b(\\d+(\\.\\d+)?)\\b").matcher(code)
        while (numberRegex.find()) {
            builder.addStyle(SpanStyle(color = SyntaxNumber), numberRegex.start(), numberRegex.end())
        }

        // Function calls: word followed by (
        val funcRegex = Pattern.compile("([a-zA-Z_\$][a-zA-Z0-9_\$]*)\\s*\\(").matcher(code)
        while (funcRegex.find()) {
            val start = funcRegex.start(1)
            val end = funcRegex.end(1)
            val word = code.substring(start, end)
            if (!JS_KEYWORDS.contains(word)) {
                builder.addStyle(SpanStyle(color = SyntaxFunction, fontWeight = FontWeight.SemiBold), start, end)
            }
        }

        // Keywords
        val wordRegex = Pattern.compile("\\b([a-zA-Z_\$][a-zA-Z0-9_\$]*)\\b").matcher(code)
        while (wordRegex.find()) {
            val word = wordRegex.group(1)
            if (JS_KEYWORDS.contains(word)) {
                builder.addStyle(SpanStyle(color = SyntaxKeyword, fontWeight = FontWeight.Bold), wordRegex.start(), wordRegex.end())
            }
        }
    }

    private fun highlightJson(json: String, builder: AnnotatedString.Builder) {
        // Keys: "key":
        val keyRegex = Pattern.compile("\"([^\"]+)\"\\s*:").matcher(json)
        while (keyRegex.find()) {
            builder.addStyle(SpanStyle(color = SyntaxKeyword), keyRegex.start(), keyRegex.end() - 1)
        }

        // String values
        val strRegex = Pattern.compile(":\\s*\"([^\"]*)\"").matcher(json)
        while (strRegex.find()) {
            val start = json.indexOf('"', strRegex.start())
            val end = strRegex.end()
            builder.addStyle(SpanStyle(color = SyntaxString), start, end)
        }

        // Numbers & Booleans
        val numRegex = Pattern.compile("\\b(true|false|null|\\d+(\\.\\d+)?)\\b").matcher(json)
        while (numRegex.find()) {
            builder.addStyle(SpanStyle(color = SyntaxNumber, fontWeight = FontWeight.Bold), numRegex.start(), numRegex.end())
        }
    }

    private fun highlightDockerfile(dockerfile: String, builder: AnnotatedString.Builder) {
        // Comments
        val commentRegex = Pattern.compile("#.*").matcher(dockerfile)
        while (commentRegex.find()) {
            builder.addStyle(SpanStyle(color = SyntaxComment), commentRegex.start(), commentRegex.end())
        }

        // Instructions
        val wordRegex = Pattern.compile("\\b([A-Z]+)\\b").matcher(dockerfile)
        while (wordRegex.find()) {
            val word = wordRegex.group(1)
            if (DOCKER_KEYWORDS.contains(word)) {
                builder.addStyle(SpanStyle(color = SyntaxKeyword, fontWeight = FontWeight.Bold), wordRegex.start(), wordRegex.end())
            }
        }

        // Strings
        val strRegex = Pattern.compile("(\"[^\"]*\"|'[^']*')").matcher(dockerfile)
        while (strRegex.find()) {
            builder.addStyle(SpanStyle(color = SyntaxString), strRegex.start(), strRegex.end())
        }
    }

    private fun highlightYaml(yaml: String, builder: AnnotatedString.Builder) {
        val keyRegex = Pattern.compile("^\\s*([a-zA-Z0-9_-]+):", Pattern.MULTILINE).matcher(yaml)
        while (keyRegex.find()) {
            builder.addStyle(SpanStyle(color = SyntaxKeyword), keyRegex.start(1), keyRegex.end(1))
        }

        val commentRegex = Pattern.compile("#.*").matcher(yaml)
        while (commentRegex.find()) {
            builder.addStyle(SpanStyle(color = SyntaxComment), commentRegex.start(), commentRegex.end())
        }
    }

    private fun highlightHtml(html: String, builder: AnnotatedString.Builder) {
        val tagRegex = Pattern.compile("</?([a-zA-Z0-9]+)[^>]*>").matcher(html)
        while (tagRegex.find()) {
            builder.addStyle(SpanStyle(color = SyntaxTag), tagRegex.start(), tagRegex.end())
        }
    }

    private fun highlightMarkdown(md: String, builder: AnnotatedString.Builder) {
        val headerRegex = Pattern.compile("^#+\\s+.*$", Pattern.MULTILINE).matcher(md)
        while (headerRegex.find()) {
            builder.addStyle(SpanStyle(color = SyntaxFunction, fontWeight = FontWeight.Bold), headerRegex.start(), headerRegex.end())
        }
        val codeRegex = Pattern.compile("`[^`]+`").matcher(md)
        while (codeRegex.find()) {
            builder.addStyle(SpanStyle(color = SyntaxString), codeRegex.start(), codeRegex.end())
        }
    }
}
