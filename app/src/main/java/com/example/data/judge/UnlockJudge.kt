package com.example.data.judge

data class JudgeResult(
    val approved: Boolean,
    val comment: String,
    val isFallback: Boolean = false,
    val rawResponse: String? = null
)

interface UnlockJudge {
    suspend fun evaluateReason(
        appName: String,
        packageName: String,
        userReason: String
    ): Result<JudgeResult>
}
