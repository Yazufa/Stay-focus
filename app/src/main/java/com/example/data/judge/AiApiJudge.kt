package com.example.data.judge

import android.content.Context
import com.example.BuildConfig
import com.example.data.repository.SecureStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class AiApiJudge(
    private val context: Context,
    private val secureStorage: SecureStorage = SecureStorage(context)
) : UnlockJudge {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    override suspend fun evaluateReason(
        appName: String,
        packageName: String,
        userReason: String
    ): Result<JudgeResult> = withContext(Dispatchers.IO) {
        val apiKey = getActiveApiKey()
        if (apiKey.isNullOrBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(
                IllegalStateException("API Key belum dikonfigurasi atau tidak valid.")
            )
        }

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

            val prompt = """
                Anda adalah juri produktivitas tegas pada aplikasi pengunci 'stayfocus'.
                Pengguna ingin membuka aplikasi: "$appName" ($packageName).
                Alasan pengguna: "$userReason".
                
                Instruksi Evaluasi:
                1. Nilai apakah alasan ini benar-benar penting, mendesak, edukatif, atau darurat pekerjaan.
                2. Tolak alasan yang terkesan sekadar bosan, scrolling medsos tanpa tujuan, hiburan impulsif, atau menunda pekerjaan.
                3. Balas HANYA dalam format JSON valid tanpa format markdown tambahan:
                {
                   "approved": true atau false,
                   "comment": "Komentar evaluasi singkat 1-2 kalimat dalam Bahasa Indonesia"
                }
            """.trimIndent()

            val requestJson = JSONObject().apply {
                val contents = JSONArray().apply {
                    put(JSONObject().apply {
                        val parts = JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        }
                        put("parts", parts)
                    })
                }
                put("contents", contents)

                val generationConfig = JSONObject().apply {
                    put("responseMimeType", "application/json")
                    put("temperature", 0.2)
                }
                put("generationConfig", generationConfig)
            }

            val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(
                    Exception("HTTP ${response.code}: ${response.message}")
                )
            }

            val responseBody = response.body?.string() ?: ""
            val jsonResponse = JSONObject(responseBody)
            val candidates = jsonResponse.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text") ?: ""

            if (text.isBlank()) {
                return@withContext Result.failure(Exception("Jawaban AI kosong."))
            }

            // Clean text if wrapped in markdown code fence
            val cleanJson = text.trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            val parsedResult = JSONObject(cleanJson)
            val approved = parsedResult.optBoolean("approved", false)
            val comment = parsedResult.optString("comment", if (approved) "Alasan diterima." else "Alasan ditolak.")

            Result.success(
                JudgeResult(
                    approved = approved,
                    comment = comment,
                    isFallback = false,
                    rawResponse = text
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getActiveApiKey(): String? {
        val userKey = secureStorage.getApiKey()
        if (!userKey.isNullOrBlank()) {
            return userKey
        }
        return try {
            val buildKey = BuildConfig.GEMINI_API_KEY
            if (buildKey.isNotBlank() && buildKey != "MY_GEMINI_API_KEY") buildKey else null
        } catch (e: Throwable) {
            null
        }
    }
}
