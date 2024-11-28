package com.example.sarah_malik_hangman

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GuessTheWordGame()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuessTheWordGame() {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    var wordToGuess by rememberSaveable { mutableStateOf("ANDROID") }
    var revealedWord by rememberSaveable { mutableStateOf("_".repeat(wordToGuess.length)) }
    var guessedLetters by rememberSaveable { mutableStateOf(mutableSetOf<Char>()) }
    var remainingGuesses by rememberSaveable { mutableStateOf(6) }
    var hintCount by rememberSaveable { mutableStateOf(0) }
    var isGameOver by rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current

    fun resetGame() {
        wordToGuess = listOf("ANDROID", "KOTLIN", "COMPOSE", "DEVELOPER").random()
        revealedWord = "_".repeat(wordToGuess.length)
        guessedLetters.clear()
        remainingGuesses = 6
        hintCount = 0
        isGameOver = false
    }

    fun useHint() {
        if (isGameOver) return // Prevent hints after the game ends
        if (remainingGuesses <= 1) {
            Toast.makeText(context, "Hint not available", Toast.LENGTH_SHORT).show()
            return
        }

        when (hintCount) {
            0 -> {
                Toast.makeText(context, "Hint: It's a programming-related term!", Toast.LENGTH_SHORT).show()
            }
            1 -> {
                val incorrectLetters = ('A'..'Z').filter { it !in wordToGuess && it !in guessedLetters }
                val lettersToDisable = incorrectLetters.take(incorrectLetters.size / 2)
                guessedLetters.addAll(lettersToDisable)
            }
            2 -> {
                val vowels = listOf('A', 'E', 'I', 'O', 'U')
                vowels.forEach { vowel ->
                    if (vowel in wordToGuess) {
                        wordToGuess.forEachIndexed { index, char ->
                            if (char == vowel) {
                                val revealedArray = revealedWord.toCharArray()
                                revealedArray[index] = vowel
                                revealedWord = String(revealedArray)
                            }
                        }
                    }
                }
            }
        }
        hintCount++
        remainingGuesses--
        if (remainingGuesses == 0) isGameOver = true
    }

    fun onLetterClick(letter: Char) {
        if (isGameOver) return // Prevent letter selection after the game ends
        if (letter in wordToGuess) {
            wordToGuess.forEachIndexed { index, char ->
                if (char == letter) {
                    val revealedArray = revealedWord.toCharArray()
                    revealedArray[index] = letter
                    revealedWord = String(revealedArray)
                }
            }
        } else {
            remainingGuesses--
        }
        guessedLetters.add(letter)

        if (remainingGuesses == 0) {
            isGameOver = true
        }

        // Check if the player won
        if (!revealedWord.contains("_")) {
            isGameOver = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Guess the Word Game") })
        }
    ) { padding ->
        if (isLandscape) {
            // Landscape layout with left and right columns
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Left Column: Hangman Visual and Buttons
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    HangmanDrawing(remainingGuesses)

                    Text(
                        text = "Word: $revealedWord",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = "Guesses Remaining: $remainingGuesses",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    if (isGameOver) {
                        Text(
                            text = if (revealedWord.contains("_")) "You Lost! The word was $wordToGuess." else "You Won! Congratulations!",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(onClick = { useHint() }, enabled = !isGameOver) {
                            Text("Hint (Used: $hintCount)")
                        }
                        Button(onClick = { resetGame() }) {
                            Text("New Game")
                        }
                    }
                }

                // Right Column: Letter Bank
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LetterGrid(
                        guessedLetters = guessedLetters,
                        onLetterClick = ::onLetterClick,
                        isGameOver = isGameOver
                    )
                }
            }
        } else {
            // Portrait layout
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Text(
                    text = "Word: $revealedWord",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(16.dp)
                )

                Text(
                    text = "Guesses Remaining: $remainingGuesses",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                HangmanDrawing(remainingGuesses)

                Spacer(modifier = Modifier.height(16.dp))

                LetterGrid(
                    guessedLetters = guessedLetters,
                    onLetterClick = ::onLetterClick,
                    isGameOver = isGameOver
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (isGameOver) {
                    Text(
                        text = if (revealedWord.contains("_")) "You Lost! The word was $wordToGuess." else "You Won! Congratulations!",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

                Button(onClick = { resetGame() }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Text("New Game")
                }
            }
        }
    }
}

@Composable
fun HangmanDrawing(remainingGuesses: Int) {
    Canvas(modifier = Modifier.size(150.dp)) {
        drawLine(Color.Black, Offset(50f, 180f), Offset(150f, 180f), strokeWidth = 8f)
        drawLine(Color.Black, Offset(100f, 180f), Offset(100f, 50f), strokeWidth = 8f)
        drawLine(Color.Black, Offset(100f, 50f), Offset(140f, 50f), strokeWidth = 8f)
        drawLine(Color.Black, Offset(140f, 50f), Offset(140f, 70f), strokeWidth = 4f)

        if (remainingGuesses <= 5) drawCircle(Color.Red, 20f, Offset(140f, 90f))
        if (remainingGuesses <= 4) drawLine(Color.Red, Offset(140f, 110f), Offset(140f, 150f), strokeWidth = 4f)
        if (remainingGuesses <= 3) drawLine(Color.Red, Offset(140f, 120f), Offset(120f, 140f), strokeWidth = 4f)
        if (remainingGuesses <= 2) drawLine(Color.Red, Offset(140f, 120f), Offset(160f, 140f), strokeWidth = 4f)
        if (remainingGuesses <= 1) drawLine(Color.Red, Offset(140f, 150f), Offset(130f, 170f), strokeWidth = 4f)
        if (remainingGuesses == 0) drawLine(Color.Red, Offset(140f, 150f), Offset(150f, 170f), strokeWidth = 4f)
    }
}

@Composable
fun LetterGrid(guessedLetters: Set<Char>, onLetterClick: (Char) -> Unit, isGameOver: Boolean) {
    val letters = ('A'..'Z').toList()
    LazyVerticalGrid(
        columns = GridCells.Fixed(6),
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = spacedBy(8.dp),
        horizontalArrangement = spacedBy(8.dp)
    ) {
        items(letters) { letter ->
            Button(
                onClick = { onLetterClick(letter) },
                enabled = letter !in guessedLetters && !isGameOver
            ) {
                Text(letter.toString())
            }
        }
    }
}