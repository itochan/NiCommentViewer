package jp.itochan.nicommentviewer.client

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.websocket.*
import io.ktor.http.cio.websocket.*

object NiClient {

    private val client = HttpClient(CIO) {
        install(WebSockets)
    }

    suspend fun watch(url: String) {
        client.webSocket(urlString = url) {
            while (true) {
                val othersMessage = incoming.receive() as? Frame.Text
                println(othersMessage?.readText())
            }
        }
    }
}