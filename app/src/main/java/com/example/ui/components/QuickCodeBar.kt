package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CodeFontFamily
import com.example.ui.theme.NodeEmerald
import com.example.ui.theme.SlateBackground
import com.example.ui.theme.SlateSurfaceVariant
import com.example.ui.theme.TextPrimary

@Composable
fun QuickCodeBar(
    onInsertText: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val quickTokens = listOf(
        "{ }", "( )", "[ ]", "=>", ";", "=", "\"", "'", "`",
        "const", "let", "function", "async", "await", "import", "export",
        "console.log()", "process.env", "req, res", "app.get()", "return"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(SlateBackground)
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        quickTokens.forEach { token ->
            ElevatedButton(
                onClick = {
                    val toInsert = when (token) {
                        "{ }" -> "{\n  \n}"
                        "( )" -> "()"
                        "[ ]" -> "[]"
                        "console.log()" -> "console.log();"
                        else -> "$token "
                    }
                    onInsertText(toInsert)
                },
                modifier = Modifier
                    .height(34.dp)
                    .testTag("quick_token_${token.replace(" ", "_")}"),
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = SlateSurfaceVariant,
                    contentColor = if (token.startsWith("console") || token == "=>") NodeEmerald else TextPrimary
                ),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 2.dp)
            ) {
                Text(
                    text = token,
                    fontFamily = CodeFontFamily,
                    fontSize = 12.sp
                )
            }
        }
    }
}
