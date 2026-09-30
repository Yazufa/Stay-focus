package com.example.data.math

import kotlin.random.Random

data class MathQuestion(
    val id: Int,
    val prompt: String,
    val options: List<Int>,
    val correctAnswer: Int,
    val level: Int
)

object MathQuestGenerator {

    fun generateQuestions(level: Int, count: Int): List<MathQuestion> {
        val safeLevel = level.coerceIn(1, 5)
        val safeCount = count.coerceIn(1, 10)
        return (1..safeCount).map { id ->
            generateSingleQuestion(id, safeLevel)
        }
    }

    private fun generateSingleQuestion(id: Int, level: Int): MathQuestion {
        val (prompt, answer) = when (level) {
            1 -> generateLevel1()
            2 -> generateLevel2()
            3 -> generateLevel3()
            4 -> generateLevel4()
            5 -> generateLevel5()
            else -> generateLevel1()
        }

        val options = generateOptions(answer, level)
        return MathQuestion(
            id = id,
            prompt = prompt,
            options = options,
            correctAnswer = answer,
            level = level
        )
    }

    // Level 1: tambah/kurang 1 angka satuan (1..9)
    private fun generateLevel1(): Pair<String, Int> {
        val isAdd = Random.nextBoolean()
        val a = Random.nextInt(2, 10)
        val b = Random.nextInt(1, 10)
        return if (isAdd) {
            "$a + $b" to (a + b)
        } else {
            val high = maxOf(a, b)
            val low = minOf(a, b)
            "$high - $low" to (high - low)
        }
    }

    // Level 2: tambah/kurang 2 angka puluhan (10..99)
    private fun generateLevel2(): Pair<String, Int> {
        val isAdd = Random.nextBoolean()
        val a = Random.nextInt(12, 90)
        val b = Random.nextInt(11, 80)
        return if (isAdd) {
            "$a + $b" to (a + b)
        } else {
            val high = maxOf(a, b)
            val low = minOf(a, b)
            "$high - $low" to (high - low)
        }
    }

    // Level 3: tambah/kurang 3 angka
    private fun generateLevel3(): Pair<String, Int> {
        val a = Random.nextInt(15, 60)
        val b = Random.nextInt(10, 40)
        val c = Random.nextInt(5, 25)
        val op1 = if (Random.nextBoolean()) "+" else "-"
        val op2 = if (Random.nextBoolean()) "+" else "-"

        val step1 = if (op1 == "+") a + b else a - b
        val finalAnswer = if (op2 == "+") step1 + c else (step1 - c).coerceAtLeast(0)
        val actualOp2 = if (op2 == "-" && step1 - c < 0) "+" else op2
        val correctedFinal = if (actualOp2 == "+") step1 + c else step1 - c

        return "$a $op1 $b $actualOp2 $c" to correctedFinal
    }

    // Level 4: perkalian dasar
    private fun generateLevel4(): Pair<String, Int> {
        val isDoubleDigit = Random.nextBoolean()
        return if (isDoubleDigit) {
            val a = Random.nextInt(11, 20)
            val b = Random.nextInt(3, 9)
            "$a × $b" to (a * b)
        } else {
            val a = Random.nextInt(4, 12)
            val b = Random.nextInt(4, 12)
            "$a × $b" to (a * b)
        }
    }

    // Level 5: campuran + - x : (ekstrem)
    private fun generateLevel5(): Pair<String, Int> {
        return when (Random.nextInt(3)) {
            0 -> {
                // (A × B) - (C ÷ D)
                val d = Random.nextInt(2, 6)
                val cMultiplier = Random.nextInt(2, 8)
                val c = d * cMultiplier
                val a = Random.nextInt(6, 15)
                val b = Random.nextInt(3, 8)
                val ans = (a * b) - (c / d)
                "($a × $b) - ($c ÷ $d)" to ans
            }
            1 -> {
                // A × B + C - D
                val a = Random.nextInt(5, 14)
                val b = Random.nextInt(4, 9)
                val c = Random.nextInt(10, 40)
                val d = Random.nextInt(5, 25)
                val ans = (a * b) + c - d
                "$a × $b + $c - $d" to ans
            }
            else -> {
                // (A + B) × C - (D ÷ E)
                val e = Random.nextInt(2, 5)
                val dMul = Random.nextInt(2, 6)
                val d = e * dMul
                val a = Random.nextInt(4, 12)
                val b = Random.nextInt(3, 9)
                val c = Random.nextInt(2, 6)
                val ans = ((a + b) * c) - (d / e)
                "($a + $b) × $c - ($d ÷ $e)" to ans
            }
        }
    }

    private fun generateOptions(correctAnswer: Int, level: Int): List<Int> {
        val options = mutableSetOf(correctAnswer)
        val maxDelta = when (level) {
            1 -> 4
            2 -> 15
            3 -> 20
            4 -> 25
            else -> 40
        }

        var attempts = 0
        while (options.size < 4 && attempts < 50) {
            attempts++
            val delta = Random.nextInt(1, maxDelta + 1)
            val fake = if (Random.nextBoolean()) correctAnswer + delta else (correctAnswer - delta)
            if (fake != correctAnswer) {
                options.add(fake)
            }
        }

        while (options.size < 4) {
            options.add(correctAnswer + options.size * 3)
        }

        return options.toList().shuffled()
    }
}
