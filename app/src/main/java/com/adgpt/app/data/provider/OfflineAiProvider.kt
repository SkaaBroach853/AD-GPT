package com.adgpt.app.data.provider

import com.adgpt.app.domain.model.ChatMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineAiProvider @Inject constructor(
    private val apiKeyStore: ApiKeyStore,
    private val okHttpClient: OkHttpClient
) : AiProvider {
    override val id: String = "api"
    private val json = Json { ignoreUnknownKeys = true }
    private val mediaType = "application/json".toMediaType()

    override suspend fun complete(messages: List<ChatMessage>): String {
        val activeKey = apiKeyStore.activeKey()
            ?: return "Add an API key from the sidebar, activate it, then send your message again."

        return runCatching {
            requestChatCompletion(activeKey, messages)
        }.getOrElse { error ->
            "API request failed: ${error.message ?: "Unknown error"}"
        }
    }

    private suspend fun requestChatCompletion(
        key: SavedApiKey,
        messages: List<ChatMessage>
    ): String = withContext(Dispatchers.IO) {
        val endpoint = endpointFor(key.providerId)
        val requestBody = buildRequestBody(key, messages).toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .url(endpoint)
            .addHeader("Authorization", "Bearer ${key.key}")
            .addHeader("Content-Type", "application/json")
            .post(requestBody)
            .build()

        okHttpClient.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) error("HTTP ${response.code}: ${body.take(220)}")
            parseAssistantText(body)
        }
    }

    private fun buildRequestBody(key: SavedApiKey, messages: List<ChatMessage>): JsonObject =
        buildJsonObject {
            put("model", JsonPrimitive(key.model))
            put("temperature", JsonPrimitive(0.7))
            put(
                "messages",
                buildJsonArray {
                    messages.forEach { message ->
                        add(
                            buildJsonObject {
                                put("role", JsonPrimitive(message.role.name.lowercase()))
                                put("content", JsonPrimitive(message.content))
                            }
                        )
                    }
                }
            )
            if (key.providerId != "glm") put("max_tokens", JsonPrimitive(1024))
        }

    private fun parseAssistantText(body: String): String {
        val root = json.parseToJsonElement(body).jsonObject
        val choices = root["choices"] as? JsonArray
        val first = choices?.firstOrNull()?.jsonObject
        val message = first?.get("message")?.jsonObject
        val content = message?.get("content")?.jsonPrimitive?.contentOrNull
        return content
            ?: first?.get("text")?.jsonPrimitive?.contentOrNull
            ?: root["output"]?.jsonPrimitive?.contentOrNull
            ?: body.take(1200)
    }

    private fun endpointFor(providerId: String): String =
        when (providerId) {
            "nvidia" -> "https://integrate.api.nvidia.com/v1/chat/completions"
            "glm" -> "https://open.bigmodel.cn/api/paas/v4/chat/completions"
            "openrouter" -> "https://openrouter.ai/api/v1/chat/completions"
            "groq" -> "https://api.groq.com/openai/v1/chat/completions"
            "mistral" -> "https://api.mistral.ai/v1/chat/completions"
            "perplexity" -> "https://api.perplexity.ai/chat/completions"
            "xai" -> "https://api.x.ai/v1/chat/completions"
            else -> "https://api.openai.com/v1/chat/completions"
        }
}
