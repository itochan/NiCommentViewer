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
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import java.text.SimpleDateFormat
import java.util.Date

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
                val responseText = incoming.receive() as? Frame.Text
                val message = responseText?.readText()?.let { Json.decodeFromString<Watch>(it) } ?: continue

                println(message)
                when (message.type) {
                    "ping" -> {
                        send(Json.encodeToString(Watch(type = "pong")))
                        send(Json.encodeToString(Watch(type = "keepSeat")))
                    }
                    "room" -> connectMessages(message)
                }
            }
        }
    }

    private suspend fun connectMessages(message: Watch) {
        val jsonObject = message.data?.jsonObject ?: return
        val messageServerUri = jsonObject["messageServer"]?.jsonObject?.get("uri")?.jsonPrimitive?.content ?: return
        val threadId = jsonObject["threadId"]?.jsonPrimitive?.content ?: return
        // val threadKey = jsonObject["yourPostKey"]?.jsonPrimitive?.content ?: return

        val pingMessage = buildJsonArray {
            addJsonObject {
                putJsonObject("ping") {
                    put("content", "rs:0")
                }
                putJsonObject("ping") {
                    put("content", "ps:0")
                }
                putJsonObject("thread") {
                    put("thread", threadId)
                    put("version", "20061206")
                    put("user_id", "guest")
                    put("res_from", -150)
                    put("with_global", 1)
                    put("scores", 1)
                    put("nicoru", 0)
                    // put("threadkey", threadKey)
                }
                putJsonObject("ping") {
                    put("content", "pf:0")
                }
                putJsonObject("ping") {
                    put("content", "rf:0")
                }
            }
        }

        client.webSocket(urlString = messageServerUri) {
            send(Json.encodeToString(pingMessage))

            while (true) {
                val responseText = incoming.receive() as? Frame.Text
                val chatResponse = responseText?.readText()?.let { Json.decodeFromString<JsonObject>(it) } ?: continue
                val chat = chatResponse["chat"]?.jsonObject ?: continue
                val date = chat["date"]?.jsonPrimitive?.content ?: continue

                val dateString = SimpleDateFormat("HH:mm:ss").format(Date(date.toLong() * 1000))
                val comment = chat["content"]?.jsonPrimitive?.content ?: continue
                println("$dateString $comment")
            }
        }
    }
}
