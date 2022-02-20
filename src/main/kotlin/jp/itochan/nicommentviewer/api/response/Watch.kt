package jp.itochan.nicommentviewer.api.response

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class Watch(
    val type: String,
    val data: JsonObject? = null
)
