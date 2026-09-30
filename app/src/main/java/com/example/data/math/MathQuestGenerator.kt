package com.example.data.math

import kotlin.random.Random

data class MathQuestion(
    val id: Int,
    val prompt: String,
    val correctAnswer: Int,
    val level: Int
)

data class QuestLevelInfo(
    val level: Int,
    val name: String,
    val description: String,
    val example: String,
    val pointsBonus: Int
)

object MathQuestGenerator {

    val LEVELS = listOf(
        QuestLevelInfo(
            level = 1,
            name = "Pemanasan",
            description = "2 angka satuan (+ / -)",
            example = "7 + 5 = 12",
            pointsBonus = 10
        ),
        QuestLevelInfo(
            level = 2,
            name = "Pemula",
            description = "2 angka puluhan (+ / -)",
            example = "47 + 38 = 85",
            pointsBonus = 20
        ),
        QuestLevelInfo(
            level = 3,
            name = "Menengah",
            description = "3 angka puluhan (+ / -)",
            example = "45 + 23 - 18 = 50",
            pointsBonus = 35
        ),
        QuestLevelInfo(
            level = 4,
            name = "Tangguh",
            description = "(puluhan × puluhan) + puluhan",
            example = "(12 × 15) + 34 = 214",
            pointsBonus = 55
        ),
        QuestLevelInfo(
            level = 5,
            name = "Master",
            description = "(puluhan × puluhan) + ratusan",
            example = "(23 × 14) + 250 = 572",
            pointsBonus = 75
        ),
        QuestLevelInfo(
            level = 6,
            name = "Legenda",
            description = "(ratusan × puluhan) + ribuan",
            example = "(120 × 34) + 1500 = 5580",
            pointsBonus = 100
        )
    )

    fun getLevelInfo(level: Int): QuestLevelInfo {
        return LEVELS.firstOrNull { it.level == level } ?: LEVELS[0]
    }

    fun generateQuestions(level: Int, count: Int): List<MathQuestion> {
        val safeLevel = level.coerceIn(1, 6)
        val safeCount = count.coerceIn(1, 10)
        return (1..safeCount).map { id ->
            generateSingleQuestion(id, safeLevel)
        }
    }

    fun generateSingleQuestion(id: Int, level: Int): MathQuestion {
        val safeLevel = level.coerceIn(1, 6)
        val (prompt, answer) = when (safeLevel) {
            1 -> generateLevel1()
            2 -> generateLevel2()
            3 -> generateLevel3()
            4 -> generateLevel4()
            5 -> generateLevel5()
            6 -> generateLevel6()
            else -> generateLevel1()
        }

        return MathQuestion(
            id = id,
            prompt = prompt,
            correctAnswer = answer,
            level = safeLevel
        )
    }

    // Level 1: Pemanasan - 2 angka satuan, tambah/kurang (hasil non-negatif)
    private fun generateLevel1(): Pair<String, Int> {
        val isAdd = Random.nextBoolean()
        val a = Random.nextInt(1, 10)
        val b = Random.nextInt(1, 10)
        return if (isAdd) {
            "$a + $b" to (a + b)
        } else {
            val high = maxOf(a, b)
            val low = minOf(a, b)
            "$high - $low" to (high - low)
        }
    }

    // Level 2: Pemula - 2 angka puluhan, tambah/kurang (hasil non-negatif)
    private fun generateLevel2(): Pair<String, Int> {
        val isAdd = Random.nextBoolean()
        val a = Random.nextInt(10, 100)
        val b = Random.nextInt(10, 100)
        return if (isAdd) {
            "$a + $b" to (a + b)
        } else {
            val high = maxOf(a, b)
            val low = minOf(a, b)
            "$high - $low" to (high - low)
        }
    }

    // Level 3: Menengah - 3 angka puluhan, tambah/kurang (hasil non-negatif)
    private fun generateLevel3(): Pair<String, Int> {
        val a = Random.nextInt(35, 100)
        val b = Random.nextInt(10, 80)
        val isOp1Plus = Random.nextBoolean()

        val (step1, op1) = if (isOp1Plus) {
            (a + b) to "+"
        } else {
            val high = maxOf(a, b)
            val low = minOf(a, b)
            (high - low) to "-"
        }

        val isOp2Minus = Random.nextBoolean() && step1 >= 25
        val (op2, cVal) = if (isOp2Minus) {
            val maxC = minOf(step1 - 1, 99)
            val c = Random.nextInt(10, maxOf(11, maxC + 1))
            "-" to c
        } else {
            val c = Random.nextInt(10, 100)
            "+" to c
        }
        val finalAnswer = if (op2 == "-") step1 - cVal else step1 + cVal

        val firstA = if (!isOp1Plus) maxOf(a, b) else a
        val firstB = if (!isOp1Plus) minOf(a, b) else b

        return "$firstA $op1 $firstB $op2 $cVal" to finalAnswer
    }

    // Level 4: Tangguh - (puluhan x puluhan) + puluhan. Contoh: (12 x 15) + 34
    private fun generateLevel4(): Pair<String, Int> {
        val a = Random.nextInt(11, 26)
        val b = Random.nextInt(11, 26)
        val c = Random.nextInt(10, 100)
        val ans = (a * b) + c
        return "($a × $b) + $c" to ans
    }

    // Level 5: Master - (puluhan x puluhan) + ratusan. Contoh: (23 x 14) + 250
    private fun generateLevel5(): Pair<String, Int> {
        val a = Random.nextInt(12, 35)
        val b = Random.nextInt(11, 30)
        val c = Random.nextInt(100, 500)
        val ans = (a * b) + c
        return "($a × $b) + $c" to ans
    }

    // Level 6: Legenda - (ratusan x puluhan) + ribuan. Contoh: (120 x 34) + 1500
    private fun generateLevel6(): Pair<String, Int> {
        val a = Random.nextInt(100, 200)
        val b = Random.nextInt(12, 35)
        val c = Random.nextInt(1000, 3500)
        val ans = (a * b) + c
        return "($a × $b) + $c" to ans
    }
}
