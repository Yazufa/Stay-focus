package com.example.data.judge

class OfflineFallbackJudge : UnlockJudge {

    override suspend fun evaluateReason(
        appName: String,
        packageName: String,
        userReason: String
    ): Result<JudgeResult> {
        val trimmed = userReason.trim().lowercase()
        if (trimmed.length < 8) {
            return Result.success(
                JudgeResult(
                    approved = false,
                    comment = "Alasan terlalu singkat. Silakan berikan alasan produktif atau selesaikan quest matematika.",
                    isFallback = true
                )
            )
        }

        // Keywords hinting at urgent / productive tasks
        val productiveKeywords = listOf(
            "kerja", "tugas", "ujian", "darurat", "urgent", "kuliah", "kantor", "meeting",
            "belajar", "riset", "klien", "dokumen", "laporan", "deadline", "guru", "dosen"
        )
        val leisureKeywords = listOf(
            "gabut", "bosan", "scrolling", "nonton", "main", "iseng", "santai", "lucu", "feed", "reels", "tiktok"
        )

        val hasProductive = productiveKeywords.any { trimmed.contains(it) }
        val hasLeisure = leisureKeywords.any { trimmed.contains(it) }

        return if (hasProductive && !hasLeisure) {
            Result.success(
                JudgeResult(
                    approved = true,
                    comment = "[Offline Mode] Alasan produktif terdeteksi. Gunakan waktu ini dengan bijak!",
                    isFallback = true
                )
            )
        } else {
            Result.success(
                JudgeResult(
                    approved = false,
                    comment = "[Offline Mode] Alasan tidak memenuhi kriteria darurat. Silakan selesaikan Quest Matematika untuk membuka.",
                    isFallback = true
                )
            )
        }
    }
}
