package jp.itochan.nicommentviewer.comment

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.rememberWindowState
import jp.itochan.nicommentviewer.api.client.NiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@Composable
fun CommentWindow(
    onCloseRequest: () -> Unit
) {
    val windowState = rememberWindowState().apply {
        size = DpSize(width = 400.dp, height = Dp.Unspecified)
    }
    var title by remember { mutableStateOf("NiCommentViewer") }
    Window(
        onCloseRequest = onCloseRequest,
        state = windowState,
        title = title
    ) {
        MaterialTheme {
            CommentScreen(
                onChangeChannelId = { title = "NiCommentViewer: $it" }
            )
        }
    }
}

@Composable
fun CommentScreen(
    onChangeChannelId: (String) -> Unit
) {
    Surface {
        val comments = remember { mutableStateListOf<String>() }
        val commentsListState = rememberLazyListState()
        var channelId by remember { mutableStateOf("") }

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(modifier = Modifier.padding(8.dp)) {
                TextField(
                    value = channelId,
                    onValueChange = { channelId = it }
                )
                Button(
                    onClick = {
                        onChangeChannelId(channelId)
                        CoroutineScope(Dispatchers.IO).launch {
                            NiClient.connect(channelId).collect { (date, comment) ->
                                comments.add("$date $comment")
                            }
                        }
                    },
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(text = "Get")
                }
            }
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                        .padding(8.dp),
                    state = commentsListState
                ) {
                    itemsIndexed(comments) { _, item ->
                        Text(text = item)
                    }
                    CoroutineScope(Dispatchers.Main).launch {
                        commentsListState.scrollToItem(comments.size)
                    }
                }
                VerticalScrollbar(
                    modifier = Modifier.align(Alignment.CenterEnd)
                        .fillMaxHeight(),
                    adapter = rememberScrollbarAdapter(scrollState = commentsListState)
                )
            }
        }
    }
}

@Preview
@Composable
fun CommentScreenPreview() {
    CommentScreen(
        onChangeChannelId = {}
    )
}
