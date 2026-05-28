package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.GeminiApi
import com.example.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.random.Random

enum class ActiveScreen {
    DASHBOARD,
    CROSSWORD,
    KAHOOT,
    DAILY_CHALLENGE,
    EDUCATION
}

enum class KahootState {
    IDLE,
    GETTING_READY,
    QUESTION_ACTIVE,
    REVEALED,
    PODIUM
}

// Bot player definition for Kahoot
data class KahootPlayer(
    val name: String,
    val avatarEmoji: String,
    var score: Int = 0,
    val accuracy: Double, // 0.0 to 1.0
    val responseSpeedMinS: Double,
    val responseSpeedMaxS: Double,
    var lastAnswerCorrect: Boolean = false,
    var lastAnswerTimeS: Double = 0.0,
    var selectedOptionIndex: Int = -1
)

// Collaborative Submission feed item
data class FeedMessage(
    val id: String = UUID.randomUUID().toString(),
    val username: String,
    val word: String,
    val score: Int,
    val timestamp: String = "Just now"
)

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "GameViewModel"
    private val prefs = application.getSharedPreferences("word_arena_prefs", Context.MODE_PRIVATE)

    // Primary State Holders
    var currentScreen by mutableStateOf(ActiveScreen.DASHBOARD)
        private set

    // User Metrics
    var streakDays by mutableStateOf(1)
        private set
    var userXP by mutableStateOf(150)
        private set
    var coins by mutableStateOf(50)
        private set

    // --- CROSSWORD STATE ---
    var activeCrossword by mutableStateOf<CrosswordPuzzle>(Dictionary.defaultCrossword)
        private set
    var isLoadingCrossword by mutableStateOf(false)
        private set
    var crosswordFeedbackMessage by mutableStateOf("")
        private set
    var isCrosswordSolved by mutableStateOf(false)
        private set
    var customCrosswordTopic by mutableStateOf("")
    var activeClueId by mutableStateOf("1A")
        private set
    var selectedRow by mutableStateOf(0)
        private set
    var selectedCol by mutableStateOf(0)
        private set

    // --- KAHOOT ARENA STATE ---
    var kahootState by mutableStateOf(KahootState.IDLE)
        private set
    var currentQuestionIndex by mutableStateOf(0)
        private set
    var activeQuestion by mutableStateOf<TriviaQuestion>(Dictionary.triviaQuestions[0])
        private set
    var timerSecondsRemaining by mutableStateOf(15)
        private set
    var userSelectedOptionIndex by mutableStateOf(-1)
        private set
    var isUserAnswerLocked by mutableStateOf(false)
        private set
    var isAiTopicKahoot by mutableStateOf(false)
    var customKahootTopic by mutableStateOf("")
    var isLoadingKahootQuestion by mutableStateOf(false)

    // Player List (User is always index 0 conceptually, but we compile a leaderboard dynamically)
    var userKahootScore by mutableStateOf(0)
        private set
    var bots by mutableStateOf(listOf<KahootPlayer>())
        private set

    private var kahootTimerJob: Job? = null

    // --- COLLABORATIVE DAILY CHALLENGE STATE ---
    val dailyLetters = listOf("A", "E", "S", "T", "R", "M", "P") // Fixed Daily pool
    var coOpProgressPoints by mutableStateOf(4250) // Global community points out of 10000
    val maxCoOpGoal = 10000
    var userDailyEnteredWord by mutableStateOf("")
    var userSubmittedDailyWords by mutableStateOf(setOf<String>())
        private set
    var coOpFeedList by mutableStateOf(listOf<FeedMessage>())
        private set

    private var activeCoOpSimulateJob: Job? = null

    // --- EDUCATION STATE ---
    var activeWordOfTheDayIndex by mutableStateOf(0)
        private set
    val wordOfTheDayList = Dictionary.wordsOfTheDay
    var searchedWord by mutableStateOf("")
    var searchResultInsight by mutableStateOf<WordOfTheDay?>(null)
        private set
    var isSearchingWord by mutableStateOf(false)
        private set
    var searchError by mutableStateOf("")
        private set

    // --- ANAGRAMS STATE ---
    var currentAnagramIndex by mutableStateOf(0)
        private set
    var currentAnagramChallenge by mutableStateOf(Dictionary.anagrams[0])
        private set
    var userAnagramInput by mutableStateOf("")
    var anagramValidationMessage by mutableStateOf("")
        private set
    var isAnagramSolved by mutableStateOf(false)
        private set

    // Shared preferences keys
    init {
        loadProgress()
        setupSimulationFeed()
        resetAnagramPuzzle()
    }

    fun navigateTo(screen: ActiveScreen) {
        currentScreen = screen
        // Stop jobs if navigating away from specific contexts if needed
        if (screen != ActiveScreen.DAILY_CHALLENGE) {
            // keep it simple, feed can run or stop
        }
    }

    private fun loadProgress() {
        streakDays = prefs.getInt("streak_days", 3)
        userXP = prefs.getInt("user_xp", 180)
        coins = prefs.getInt("coins", 75)
        val dailyWords = prefs.getStringSet("daily_words", emptySet()) ?: emptySet()
        userSubmittedDailyWords = dailyWords
    }

    private fun saveProgress() {
        prefs.edit().apply {
            putInt("streak_days", streakDays)
            putInt("user_xp", userXP)
            putInt("coins", coins)
            putStringSet("daily_words", userSubmittedDailyWords)
            apply()
        }
    }

    fun earnXP(amount: Int) {
        userXP += amount
        saveProgress()
    }

    fun earnCoins(amount: Int) {
        coins += amount
        saveProgress()
    }

    // --- CROSSWORD FUNCTIONS ---
    fun selectCell(row: Int, col: Int) {
        val cell = activeCrossword.cells.find { it.row == row && it.col == col }
        if (cell != null && !cell.isBlack) {
            selectedRow = row
            selectedCol = col
            // Find a clue associated with this cell
            val associatedClue = activeCrossword.clues.find { clue ->
                if (clue.direction == "Across") {
                    row == clue.row && col >= clue.col && col < clue.col + clue.length
                } else {
                    col == clue.col && row >= clue.row && row < clue.row + clue.length
                }
            }
            if (associatedClue != null) {
                activeClueId = associatedClue.id
            }
        }
    }

    fun selectClue(clue: CrosswordClue) {
        activeClueId = clue.id
        selectedRow = clue.row
        selectedCol = clue.col
    }

    fun enterLetterInSelectedCell(charStr: String) {
        val upperChar = charStr.uppercase()
        val index = activeCrossword.cells.indexOfFirst { it.row == selectedRow && it.col == selectedCol }
        if (index != -1) {
            val cell = activeCrossword.cells[index]
            if (!cell.isBlack) {
                cell.enteredChar = upperChar
                // Trigger recomposition by re-assigning ActiveCrossword
                activeCrossword = activeCrossword.copy(cells = ArrayList(activeCrossword.cells))

                // Auto-advance to the next cell in the direction of the clue
                advanceCursor()
                checkCrosswordCompletion()
            }
        }
    }

    private fun advanceCursor() {
        val currentClue = activeCrossword.clues.find { it.id == activeClueId } ?: return
        if (currentClue.direction == "Across") {
            // Find cell on the right
            val nextCol = selectedCol + 1
            if (nextCol < currentClue.col + currentClue.length) {
                selectedCol = nextCol
            }
        } else {
            // Find cell below
            val nextRow = selectedRow + 1
            if (nextRow < currentClue.row + currentClue.length) {
                selectedRow = nextRow
            }
        }
    }

    fun checkCrosswordCompletion() {
        var allCorrect = true
        var isAnyEmpty = false

        for (cell in activeCrossword.cells) {
            if (!cell.isBlack) {
                if (cell.enteredChar.isEmpty()) {
                    isAnyEmpty = true
                    allCorrect = false
                } else if (cell.enteredChar[0] != cell.targetChar) {
                    allCorrect = false
                }
            }
        }

        if (allCorrect) {
            isCrosswordSolved = true
            crosswordFeedbackMessage = "Spectacular! You've solved the puzzle completely! 🎉 (+50 XP)"
            earnXP(50)
            earnCoins(15)
        } else if (!isAnyEmpty) {
            crosswordFeedbackMessage = "The spelling is complete but there is a slight error somewhere. Verify your answers! 🔍"
        } else {
            crosswordFeedbackMessage = ""
        }
    }

    fun revealSelectedCell() {
        val index = activeCrossword.cells.indexOfFirst { it.row == selectedRow && it.col == selectedCol }
        if (index != -1) {
            val cell = activeCrossword.cells[index]
            if (!cell.isBlack && cell.enteredChar != cell.targetChar.toString()) {
                cell.enteredChar = cell.targetChar.toString()
                activeCrossword = activeCrossword.copy(cells = ArrayList(activeCrossword.cells))
                crosswordFeedbackMessage = "Revealed target letter!"
                checkCrosswordCompletion()
            }
        }
    }

    fun solvePuzzle() {
        for (cell in activeCrossword.cells) {
            if (!cell.isBlack) {
                cell.enteredChar = cell.targetChar.toString()
            }
        }
        activeCrossword = activeCrossword.copy(cells = ArrayList(activeCrossword.cells))
        isCrosswordSolved = true
        crosswordFeedbackMessage = "Puzzle completed. Keep learning! 📖"
    }

    fun loadDefaultCrossword() {
        activeCrossword = Dictionary.defaultCrossword
        isCrosswordSolved = false
        crosswordFeedbackMessage = ""
        activeClueId = "1A"
        selectedRow = 0
        selectedCol = 0
    }

    fun generateAIPuzzle(topic: String) {
        if (topic.isEmpty()) return
        viewModelScope.launch {
            isLoadingCrossword = true
            crosswordFeedbackMessage = "Connecting with Gemini to construct a custom grid on '$topic'..."
            val puzzle = GeminiApi.generateCustomCrossword(topic)
            if (puzzle != null) {
                activeCrossword = puzzle
                isCrosswordSolved = false
                crosswordFeedbackMessage = "AI Puzzle successfully constructed! Start typing. 🧩"
                // select first cell
                val firstEmptyCell = puzzle.cells.firstOrNull { !it.isBlack }
                if (firstEmptyCell != null) {
                    selectedRow = firstEmptyCell.row
                    selectedCol = firstEmptyCell.col
                }
                activeClueId = puzzle.clues.firstOrNull()?.id ?: "1A"
            } else {
                crosswordFeedbackMessage = "Failed to synchronize AI grid. Playing local puzzle."
                loadDefaultCrossword()
            }
            isLoadingCrossword = false
        }
    }

    // --- KAHOOT COMPETITIVE ARENA ---
    fun startKahootMatch(topic: String = "") {
        isAiTopicKahoot = topic.isNotEmpty()
        viewModelScope.launch {
            if (isAiTopicKahoot) {
                isLoadingKahootQuestion = true
                kahootState = KahootState.GETTING_READY
                val question = GeminiApi.generateTriviaQuestion(topic)
                if (question != null) {
                    activeQuestion = question
                } else {
                    activeQuestion = Dictionary.triviaQuestions.random()
                }
                isLoadingKahootQuestion = false
            } else {
                currentQuestionIndex = 0
                activeQuestion = Dictionary.triviaQuestions[0]
            }

            userKahootScore = 0
            // Setup bots
            bots = listOf(
                KahootPlayer("WordWiz 🤖", "⚡", score = 0, accuracy = 0.85, responseSpeedMinS = 1.2, responseSpeedMaxS = 4.0),
                KahootPlayer("WordWeaver 🧙‍♂️", "🌟", score = 0, accuracy = 0.65, responseSpeedMinS = 2.5, responseSpeedMaxS = 8.0),
                KahootPlayer("Lexi_Cat 🐱", "🐾", score = 0, accuracy = 0.50, responseSpeedMinS = 3.0, responseSpeedMaxS = 12.0)
            )

            launchKahootQuestion()
        }
    }

    private fun launchKahootQuestion() {
        kahootState = KahootState.QUESTION_ACTIVE
        userSelectedOptionIndex = -1
        isUserAnswerLocked = false
        timerSecondsRemaining = 15

        // Simulate bots selecting answers async
        simulateBotsAnswering()

        // Setup ticking timer
        kahootTimerJob?.cancel()
        kahootTimerJob = viewModelScope.launch {
            while (timerSecondsRemaining > 0 && !isUserAnswerLocked) {
                delay(1000)
                timerSecondsRemaining--
            }
            if (!isUserAnswerLocked) {
                // Auto-lock if out of time
                lockKahootAnswer(-1)
            }
        }
    }

    private fun simulateBotsAnswering() {
        for (bot in bots) {
            // Determine choice
            val willBeCorrect = Random.nextDouble() < bot.accuracy
            bot.selectedOptionIndex = if (willBeCorrect) {
                activeQuestion.correctIndex
            } else {
                // Pick a wrong option
                val wrongOptions = (0..3).filter { it != activeQuestion.correctIndex }
                wrongOptions.random()
            }
            bot.lastAnswerCorrect = (bot.selectedOptionIndex == activeQuestion.correctIndex)
            bot.lastAnswerTimeS = Random.nextDouble(bot.responseSpeedMinS, bot.responseSpeedMaxS)
        }
    }

    fun lockKahootAnswer(optionIndex: Int) {
        if (isUserAnswerLocked) return
        userSelectedOptionIndex = optionIndex
        isUserAnswerLocked = true
        kahootTimerJob?.cancel()

        // Calculate points based on remaining time
        val responseSpeed = 15.0 - timerSecondsRemaining
        val isUserCorrect = (optionIndex == activeQuestion.correctIndex)

        // Calculate final round scores
        viewModelScope.launch {
            delay(1000) // Small countdown dramatic suspense pause!
            kahootState = KahootState.REVEALED

            // Add user score
            if (isUserCorrect) {
                val speedBonus = (1000 - (responseSpeed * 50)).toInt().coerceIn(400, 1000)
                userKahootScore += speedBonus
            }

            // Update bot scores
            bots = bots.map { bot ->
                var scoreAdd = 0
                if (bot.lastAnswerCorrect && bot.lastAnswerTimeS < 15.0) {
                    scoreAdd = (1000 - (bot.lastAnswerTimeS * 50)).toInt().coerceIn(400, 1000)
                }
                bot.copy(score = bot.score + scoreAdd)
            }
        }
    }

    fun nextKahootQuestion() {
        if (isAiTopicKahoot) {
            // AI mode is a single rich custom boss-mode challenge, end it
            kahootState = KahootState.PODIUM
            earnXP(userKahootScore / 10)
            earnCoins(10)
            return
        }

        val nextIndex = currentQuestionIndex + 1
        if (nextIndex < Dictionary.triviaQuestions.size) {
            currentQuestionIndex = nextIndex
            activeQuestion = Dictionary.triviaQuestions[nextIndex]
            launchKahootQuestion()
        } else {
            // End of pre-built questions, reveal podium
            kahootState = KahootState.PODIUM
            val rewardXP = userKahootScore / 10
            earnXP(rewardXP)
            earnCoins(userKahootScore / 100)
        }
    }

    // --- COLLABORATIVE DAILY CHALLENGE ---
    private fun setupSimulationFeed() {
        coOpFeedList = listOf(
            FeedMessage(username = "word_hunter", word = "STREAM", score = 12),
            FeedMessage(username = "lexicon_king", word = "STREET", score = 10),
            FeedMessage(username = "jess_alpha", word = "TEAMS", score = 8),
            FeedMessage(username = "vocab_beast", word = "SMART", score = 10)
        )

        activeCoOpSimulateJob?.cancel()
        activeCoOpSimulateJob = viewModelScope.launch {
            val names = listOf("brave_speller", "coder_bee", "alpha_writer", "riddle_guy", "bookworm_42", "galaxy_mind", "epic_lexis")
            val possibleWords = listOf(
                Pair("STEAM", 6), Pair("ASTRA", 8), Pair("MIST", 5), Pair("STAT", 4),
                Pair("MEAT", 4), Pair("TEAM", 4), Pair("EAST", 4), Pair("TAME", 4),
                Pair("MATE", 4), Pair("STAR", 4), Pair("STADIUM", 14), Pair("SMART", 10),
                Pair("SEAT", 4), Pair("TART", 4), Pair("MASTER", 12), Pair("STREET", 10)
            )

            while (true) {
                delay(Random.nextLong(6000, 11000)) // other online users submit words periodically
                val randomWord = possibleWords.random()
                val score = randomWord.second
                val randomUser = names.random()

                // Insert into feed
                val newFeedItem = FeedMessage(
                    username = randomUser,
                    word = randomWord.first,
                    score = score,
                    timestamp = "Just now"
                )
                
                coOpFeedList = (listOf(newFeedItem) + coOpFeedList).take(15)
                coOpProgressPoints = (coOpProgressPoints + score).coerceAtMost(maxCoOpGoal)
            }
        }
    }

    fun submitCoOpWord() {
        val word = userDailyEnteredWord.trim().uppercase()
        userDailyEnteredWord = ""
        if (word.isEmpty()) return

        // Validate dictionary
        val inDictionary = Dictionary.validDictionary.contains(word)
        
        // Validate letters
        val lettersMap = dailyLetters.groupingBy { it }.eachCount().toMutableMap()
        var validLetters = true
        for (char in word) {
            val count = lettersMap[char.toString()] ?: 0
            if (count > 0) {
                lettersMap[char.toString()] = count - 1
            } else {
                validLetters = false
                break
            }
        }

        if (userSubmittedDailyWords.contains(word)) {
            // Already submitted
            return
        }

        if (validLetters && inDictionary) {
            val wordScore = word.length * 2
            userSubmittedDailyWords = userSubmittedDailyWords + word
            coOpProgressPoints = (coOpProgressPoints + wordScore).coerceAtMost(maxCoOpGoal)
            
            val userFeedMsg = FeedMessage(
                username = "You",
                word = word,
                score = wordScore,
                timestamp = "Just now"
            )
            coOpFeedList = listOf(userFeedMsg) + coOpFeedList
            earnXP(wordScore * 2)
            earnCoins(1)
            saveProgress()
        }
    }

    // --- ANAGRAMS CORNER ---
    fun submitAnagramGuess() {
        val guess = userAnagramInput.trim().uppercase()
        if (guess == currentAnagramChallenge.solution) {
            isAnagramSolved = true
            anagramValidationMessage = "Perfect Unscramble! 🎉 (+25 XP)"
            earnXP(25)
            earnCoins(5)
        } else {
            anagramValidationMessage = "Double check the letters. Try again! 🤔"
        }
    }

    fun nextAnagramPuzzle() {
        val nextIndex = (currentAnagramIndex + 1) % Dictionary.anagrams.size
        currentAnagramIndex = nextIndex
        currentAnagramChallenge = Dictionary.anagrams[nextIndex]
        resetAnagramPuzzle()
    }

    private fun resetAnagramPuzzle() {
        userAnagramInput = ""
        anagramValidationMessage = ""
        isAnagramSolved = false
    }

    // --- WORD SEARCH INSIGHTS (EDUCATION) ---
    fun loadWordOfTheDay(increment: Int) {
        val size = wordOfTheDayList.size
        activeWordOfTheDayIndex = (activeWordOfTheDayIndex + increment + size) % size
    }

    fun searchWordWithAI() {
        val word = searchedWord.trim()
        if (word.isEmpty()) return
        viewModelScope.launch {
            isSearchingWord = true
            searchResultInsight = null
            searchError = ""

            // If API key is configured, use AI to generate rich custom lesson cards!
            if (GeminiApi.isApiKeyConfigured) {
                val result = GeminiApi.generateVocabInsight(word)
                if (result != null) {
                    searchResultInsight = result
                    earnXP(10)
                } else {
                    searchError = "Unable to process word insight. Check connection."
                }
            } else {
                // Offline fallback - search in existing lists
                val localWord = Dictionary.wordsOfTheDay.find { it.word.equals(word, ignoreCase = true) }
                if (localWord != null) {
                    searchResultInsight = localWord
                } else {
                    // Generate a simulated educational card for any word
                    searchResultInsight = WordOfTheDay(
                        word = word.lowercase().replaceFirstChar { it.uppercase() },
                        syllable = word.map { "$it" }.joinToString("-").lowercase(),
                        partOfSpeech = "Noun",
                        definition = "A custom word searched inside Word Arena dictionary explorer.",
                        example = "The student searched for '$word' using the Word Arena educational lookup tool.",
                        origin = "From English dictionary records.",
                        funFact = "Every single word holds historical, cultural, and phonetic treasures. Looking it up adds to your lexical capacity!"
                    )
                }
            }
            isSearchingWord = false
        }
    }

    fun clearSearch() {
        searchedWord = ""
        searchResultInsight = null
        searchError = ""
    }

    override fun onCleared() {
        super.onCleared()
        kahootTimerJob?.cancel()
        activeCoOpSimulateJob?.cancel()
    }
}
