package com.example.ui.lockscreen

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.judge.AiApiJudge
import com.example.data.judge.JudgeResult
import com.example.data.judge.OfflineFallbackJudge
import com.example.data.judge.UnlockJudge
import com.example.data.math.MathQuestGenerator
import com.example.data.math.MathQuestion
import com.example.data.repository.FocusRepository
import com.example.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed interface ReasonState {
    object Idle : ReasonState
    object Loading : ReasonState
    data class Success(val result: JudgeResult) : ReasonState
    data class Rejected(val result: JudgeResult) : ReasonState
    data class Error(val message: String) : ReasonState
}

class LockScreenViewModel(application: Application) : AndroidViewModel(application) {

    private val focusRepository = FocusRepository(application)
    private val settingsRepository = SettingsRepository(application)
    private val aiJudge: UnlockJudge = AiApiJudge(application)
    private val fallbackJudge: UnlockJudge = OfflineFallbackJudge()

    private val _questLevel = MutableStateFlow(1)
    val questLevel: StateFlow<Int> = _questLevel.asStateFlow()

    private val _questions = MutableStateFlow<List<MathQuestion>>(emptyList())
    val questions: StateFlow<List<MathQuestion>> = _questions.asStateFlow()

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()

    private val _selectedAnswer = MutableStateFlow<Int?>(null)
    val selectedAnswer: StateFlow<Int?> = _selectedAnswer.asStateFlow()

    private val _isAnswerWrong = MutableStateFlow(false)
    val isAnswerWrong: StateFlow<Boolean> = _isAnswerWrong.asStateFlow()

    private val _questCompleted = MutableStateFlow(false)
    val questCompleted: StateFlow<Boolean> = _questCompleted.asStateFlow()

    private val _earnedPoints = MutableStateFlow(0)
    val earnedPoints: StateFlow<Int> = _earnedPoints.asStateFlow()

    private val _dailyBypassUsed = MutableStateFlow(0)
    val dailyBypassUsed: StateFlow<Int> = _dailyBypassUsed.asStateFlow()

    private val _dailyBypassLimit = MutableStateFlow(3)
    val dailyBypassLimit: StateFlow<Int> = _dailyBypassLimit.asStateFlow()

    private val _reasonState = MutableStateFlow<ReasonState>(ReasonState.Idle)
    val reasonState: StateFlow<ReasonState> = _reasonState.asStateFlow()

    private var targetPackageName: String = ""
    private var targetAppName: String = ""

    fun initialize(packageName: String, appName: String, level: Int) {
        targetPackageName = packageName
        targetAppName = appName
        _questLevel.value = level

        viewModelScope.launch {
            val qCount = settingsRepository.questionsPerQuest.first()
            val bypassLimit = settingsRepository.dailyBypassLimit.first()
            _dailyBypassLimit.value = bypassLimit

            val usedBypass = focusRepository.getDailyBypassUsed()
            _dailyBypassUsed.value = usedBypass

            _questions.value = MathQuestGenerator.generateQuestions(level, qCount)
            _currentQuestionIndex.value = 0
            _selectedAnswer.value = null
            _questCompleted.value = false
        }
    }

    fun selectAnswer(answer: Int) {
        _selectedAnswer.value = answer
        _isAnswerWrong.value = false
    }

    fun submitAnswer(onAllFinished: () -> Unit) {
        val currentQ = _questions.value.getOrNull(_currentQuestionIndex.value) ?: return
        val selected = _selectedAnswer.value ?: return

        if (selected == currentQ.correctAnswer) {
            _isAnswerWrong.value = false
            if (_currentQuestionIndex.value + 1 < _questions.value.size) {
                _currentQuestionIndex.value += 1
                _selectedAnswer.value = null
            } else {
                // All questions finished!
                viewModelScope.launch {
                    val points = focusRepository.recordUnlockSuccess(
                        packageName = targetPackageName,
                        appName = targetAppName,
                        method = "MATH_QUEST",
                        level = _questLevel.value,
                        note = "Lolos Quest MTK Level ${_questLevel.value}"
                    )
                    _earnedPoints.value = points
                    _questCompleted.value = true
                    onAllFinished()
                }
            }
        } else {
            _isAnswerWrong.value = true
        }
    }

    fun evaluateReason(reasonText: String, onSuccessUnlock: () -> Unit) {
        if (_dailyBypassUsed.value >= _dailyBypassLimit.value) {
            _reasonState.value = ReasonState.Error(
                "Batas harian buka dengan alasan (${_dailyBypassLimit.value}) sudah habis. Silakan selesaikan Quest MTK."
            )
            return
        }

        viewModelScope.launch {
            _reasonState.value = ReasonState.Loading

            // 1. Try AI Judge
            val aiResult = aiJudge.evaluateReason(targetAppName, targetPackageName, reasonText)

            val finalResult = if (aiResult.isSuccess) {
                aiResult.getOrThrow()
            } else {
                // Fallback to offline judge
                val offlineResult = fallbackJudge.evaluateReason(targetAppName, targetPackageName, reasonText)
                offlineResult.getOrDefault(
                    JudgeResult(
                        approved = false,
                        comment = "Koneksi offline dan API tidak merespons. Silakan selesaikan Quest Matematika untuk membuka.",
                        isFallback = true
                    )
                )
            }

            if (finalResult.approved) {
                val pts = focusRepository.recordUnlockSuccess(
                    packageName = targetPackageName,
                    appName = targetAppName,
                    method = "AI_REASON",
                    level = 1,
                    note = reasonText
                )
                _earnedPoints.value = pts
                _dailyBypassUsed.value += 1
                _reasonState.value = ReasonState.Success(finalResult)
                onSuccessUnlock()
            } else {
                focusRepository.recordUnlockFailure(
                    packageName = targetPackageName,
                    appName = targetAppName,
                    method = "AI_REASON",
                    note = reasonText
                )
                _reasonState.value = ReasonState.Rejected(finalResult)
            }
        }
    }

    fun resetReasonState() {
        _reasonState.value = ReasonState.Idle
    }
}
