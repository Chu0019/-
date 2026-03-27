package com.airport.quiz

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel

class QuizViewModel : ViewModel() {
    var questions by mutableStateOf(emptyList<QuizQuestion>())
        private set
        
    var selectedAnswers = mutableStateListOf<Int?>()
        private set
        
    var currentIndex by mutableStateOf(0)
    var showResults by mutableStateOf(false)
    var isStarted by mutableStateOf(false)
    var score by mutableStateOf(0)
    
    data class QuizQuestion(
        val id: Int,
        val q: String,
        val shuffledOptions: List<String>,
        val correctIndex: Int,
        val image: String?
    )
    
    fun startQuiz() {
        val selectedQuestions = questionBank.shuffled().take(20)
        
        val newQuestions = selectedQuestions.map { q ->
            val shuffledOptions = q.options.shuffled()
            val correctIndex = shuffledOptions.indexOf(q.options[q.answer])
            QuizQuestion(
                id = q.id,
                q = q.q,
                shuffledOptions = shuffledOptions,
                correctIndex = correctIndex.coerceAtLeast(0),
                image = q.image
            )
        }
        
        questions = newQuestions
        selectedAnswers.clear()
        selectedAnswers.addAll(List(20) { null })
        
        currentIndex = 0
        score = 0
        showResults = false
        isStarted = true
    }
    
    fun submit() {
        var calculatedScore = 0
        questions.forEachIndexed { index, q ->
            if (selectedAnswers[index] == q.correctIndex) {
                calculatedScore++
            }
        }
        score = calculatedScore
        showResults = true
    }
    
    fun setAnswer(index: Int) {
        selectedAnswers[currentIndex] = index
    }
}
