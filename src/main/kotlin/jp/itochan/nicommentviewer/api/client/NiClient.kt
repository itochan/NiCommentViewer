package jp.itochan.nicommentviewer.api.client

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.websocket.WebSockets
import io.ktor.client.features.websocket.webSocket
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.http.cio.websocket.send
import jp.itochan.nicommentviewer.api.response.Watch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

object NiClient {

    private val client = HttpClient(CIO) {
        install(WebSockets)
    }

    suspend fun watch(url: String) {
        client.webSocket(urlString = url) {
            send(
                Json.encodeToString(
                    Watch(
                        type = "startWatching",
                        data = buildJsonObject {
                            putJsonObject("stream") {
                                put("quality", "abr")
                                put("protocol", "hls")
                                put("latency", "low")
                                put("chasePlay", false)
                            }
                            putJsonObject("room") {
                                put("protocol", "webSocket")
                                put("commentable", true)
                            }
                            put("reconnect", false)
                        }
                    )
                )
            )
            send(
                Json.encodeToString(
                    Watch(
                        type = "getAkashic",
                        data = buildJsonObject {
                            put("chaseplay", false)
                        }
                    )
                )
            )

            while (true) {
                val othersMessage = incoming.receive() as? Frame.Text
                val message = othersMessage?.readText()?.let { Json.decodeFromString<Watch>(it) } ?: continue

                println(message)
                when (message.type) {
                    "ping" -> {
                        send(Json.encodeToString(Watch(type = "pong")))
                        send(Json.encodeToString(Watch(type = "keepSeat")))
                    }
                }
            }
        }
    }
}
