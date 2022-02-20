// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.ui.window.application
import jp.itochan.nicommentviewer.comment.CommentWindow

fun main() = application {
    CommentWindow(onCloseRequest = ::exitApplication)
}
