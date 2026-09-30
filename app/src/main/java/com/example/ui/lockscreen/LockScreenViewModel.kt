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

    private val _isAntiUninstallMode = MutableStateFlow(false)
    val isAntiUninstallMode: StateFlow<Boolean> = _isAntiUninstallMode.asStateFlow()

    private val _questions = MutableStateFlow<List<MathQuestion>>(emptyList())
    val questions: StateFlow<List<MathQuestion>> = _questions.asStateFlow()

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()

    private val _enteredAnswer = MutableStateFlow("")
    val enteredAnswer: StateFlow<String> = _enteredAnswer.asStateFlow()

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

    fun initialize(packageName: String, appName: String, level: Int, isAntiUninstall: Boolean = false) {
        targetPackageName = packageName
        targetAppName = appName
        _questLevel.value = if (isAntiUninstall) 6 else level.coerceIn(1, 6)
        _isAntiUninstallMode.value = isAntiUninstall

        viewModelScope.launch {
            val qCount = if (isAntiUninstall) 3 else settingsRepository.questionsPerQuest.first()
            val bypassLimit = settingsRepository.dailyBypassLimit.first()
            _dailyBypassLimit.value = bypassLimit

            val usedBypass = focusRepository.getDailyBypassUsed()
            _dailyBypassUsed.value = usedBypass

            _questions.value = MathQuestGenerator.generateQuestions(_questLevel.value, qCount)
            _currentQuestionIndex.value = 0
            _enteredAnswer.value = ""
            _isAnswerWrong.value = false
            _questCompleted.value = false
        }
    }

    fun appendDigit(digit: String) {
        if (_enteredAnswer.value.length < 8) {
            _enteredAnswer.value += digit
            _isAnswerWrong.value = false
        }
    }

    fun deleteDigit() {
        if (_enteredAnswer.value.isNotEmpty()) {
            _enteredAnswer.value = _enteredAnswer.value.dropLast(1)
            _isAnswerWrong.value = false
        }
    }

    fun clearDigits() {
        _enteredAnswer.value = ""
        _isAnswerWrong.value = false
    }

    fun submitAnswer(onResult: (isCorrect: Boolean, isFinished: Boolean) -> Unit) {
        val currentQ = _questions.value.getOrNull(_currentQuestionIndex.value) ?: return
        val enteredInt = _enteredAnswer.value.toIntOrNull()

        if (enteredInt == currentQ.correctAnswer) {
            _isAnswerWrong.value = false
            if (_currentQuestionIndex.value + 1 < _questions.value.size) {
                _currentQuestionIndex.value += 1
                _enteredAnswer.value = ""
                onResult(true, false)
            } else {
                // All questions finished!
                viewModelScope.launch {
                    if (_isAntiUninstallMode.value) {
                        focusRepository.startAntiUninstallCountdown()
                        _earnedPoints.value = 150
                    } else {
                        val points = focusRepository.recordUnlockSuccess(
                            packageName = targetPackageName,
                            appName = targetAppName,
                            method = "MATH_QUEST",
                            level = _questLevel.value,
                            note = "Lolos Quest MTK Level ${_questLevel.value}"
                        )
                        _earnedPoints.value = points
                    }
                    _questCompleted.value = true
                    onResult(true, true)
                }
            }
        } else {
            // Wrong answer! Trigger shake and regenerate a new question for this question index
            _isAnswerWrong.value = true
            val newQ = MathQuestGenerator.generateSingleQuestion(currentQ.id, _questLevel.value)
            val updatedList = _questions.value.toMutableList()
            updatedList[_currentQuestionIndex.value] = newQ
            _questions.value = updatedList
            _enteredAnswer.value = ""
            onResult(false, false)
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
