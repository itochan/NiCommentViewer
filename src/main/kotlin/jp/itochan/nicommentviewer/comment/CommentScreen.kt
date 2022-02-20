package jp.itochan.nicommentviewer.comment

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import jp.itochan.nicommentviewer.api.client.NiClient
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@Composable
fun CommentScreen() {
    Surface {
        var channelId by remember { mutableStateOf("") }
        Column {
            TextField(
                value = channelId,
                onValueChange = { channelId = it }
            )
            Button(onClick = {
                GlobalScope.launch {
                    val webSocketUrl = NiClient.getWebSocketUrl(channelId)
                    webSocketUrl?.let { NiClient.watch(it) }
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
