package jp.itochan.nicommentviewer.comment

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import jp.itochan.nicommentviewer.api.client.NiClient
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@Composable
fun CommentScreen() {
    Surface {
        var url by remember { mutableStateOf("") }
        Column {
            TextField(
                value = url,
                onValueChange = { url = it }
            )
            Button(onClick = {
                GlobalScope.launch {
                    NiClient.watch(url)
                }
            }) {
                Text(text = "Get")
            }
        }
    }
}

@Preview
@Composable
fun CommentScreenPreview() {
    CommentScreen()
}
