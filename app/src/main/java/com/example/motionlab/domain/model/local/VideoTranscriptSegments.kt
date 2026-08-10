package com.example.motionlab.domain.model.local

import androidx.compose.ui.text.style.TextAlign
import com.google.gson.*
import java.lang.reflect.Type

// For transcripts within the video
sealed class VideoTranscriptSegments {
    data class Text(val content: String, val style: TextStyles? = null, val align: TextAlign? = null) : VideoTranscriptSegments()
    data class Formula(val latex: String) : VideoTranscriptSegments()

    companion object {
        // Register this deserializer with GsonBuilder
        fun registerGson(): Gson {
            return GsonBuilder()
                .registerTypeAdapter(VideoTranscriptSegments::class.java, VideoTranscriptSegmentsDeserializer())
                .create()
        }
    }
}

enum class TextStyles {
    HEADING, BOLD, ITALIC, NORMAL
}

class
VideoTranscriptSegmentsDeserializer : JsonDeserializer<VideoTranscriptSegments> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): VideoTranscriptSegments {
        val jsonObj = json.asJsonObject
        val type = jsonObj["type"].asString

        return when (type) {
            "text" -> VideoTranscriptSegments.Text(
                content = jsonObj["content"].asString,
                style = jsonObj["style"]?.asString?.let { runCatching { TextStyles.valueOf(it) }.getOrNull() },
                align = jsonObj["align"]?.asString?.let {
                    when (it.uppercase()) {
                        "LEFT" -> TextAlign.Left
                        "CENTER" -> TextAlign.Center
                        "RIGHT" -> TextAlign.Right
                        "JUSTIFY" -> TextAlign.Justify
                        else -> null
                    }
                }
            )
            "formula" -> VideoTranscriptSegments.Formula(
                latex = jsonObj["latex"].asString
            )
            else -> throw JsonParseException("Unknown type: $type")


        }
    }
}
