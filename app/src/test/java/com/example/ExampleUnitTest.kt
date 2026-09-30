package com.example

import com.example.data.judge.OfflineFallbackJudge
import com.example.data.math.MathQuestGenerator
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testMathQuestGenerator_generatesCorrectCountAndLevel() {
        for (level in 1..5) {
            val questions = MathQuestGenerator.generateQuestions(level = level, count = 5)
            assertEquals(5, questions.size)
            questions.forEach { q ->
                assertEquals(level, q.level)
                assertEquals(4, q.options.size)
                assertTrue("Options must contain the correct answer", q.options.contains(q.correctAnswer))
            }
        }
    }

    @Test
    fun testOfflineFallbackJudge_productiveReasons() = runBlocking {
        val judge = OfflineFallbackJudge()

        val approvedResult = judge.evaluateReason(
            appName = "Instagram",
            packageName = "com.instagram.android",
            userReason = "Perlu mengirim dokumen tugas kuliah ke grup riset"
        ).getOrThrow()

        assertTrue("Productive reason should be approved in fallback", approvedResult.approved)

        val rejectedResult = judge.evaluateReason(
            appName = "TikTok",
            packageName = "com.zhiliaoapp.musically",
            userReason = "Lagi bosan mau scrolling feed tiktok santai"
        ).getOrThrow()

        assertFalse("Leisure reason should be rejected in fallback", rejectedResult.approved)
    }
}
