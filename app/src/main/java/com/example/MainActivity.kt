package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.api.GeminiApi
import com.example.data.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                WordArenaApp()
            }
        }
    }
}

@Composable
fun WordArenaApp(viewModel: GameViewModel = viewModel()) {
    Scaffold(
        bottomBar = {
            WordArenaBottomBar(
                currentScreen = viewModel.currentScreen,
                onNavigate = { screen -> viewModel.navigateTo(screen) }
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFEF7FF)) // Vibrant Palette Light Background
                .padding(innerPadding)
        ) {
            // Main Content Area with Fade/Slide transitions
            AnimatedContent(
                targetState = viewModel.currentScreen,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                },
                label = "ScreenTransition"
            ) { screen ->
                when (screen) {
                    ActiveScreen.DASHBOARD -> DashboardScreen(viewModel)
                    ActiveScreen.CROSSWORD -> CrosswordScreen(viewModel)
                    ActiveScreen.KAHOOT -> KahootScreen(viewModel)
                    ActiveScreen.DAILY_CHALLENGE -> CoOpDailyScreen(viewModel)
                    ActiveScreen.EDUCATION -> EducationScreen(viewModel)
                }
            }
        }
    }
}

@Composable
fun WordArenaBottomBar(
    currentScreen: ActiveScreen,
    onNavigate: (ActiveScreen) -> Unit
) {
    NavigationBar(
        containerColor = Color(0xFFF3EDF7),
        contentColor = Color(0xFF6750A4),
        tonalElevation = 4.dp,
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .drawBehind {
                drawLine(
                    color = Color(0xFFCAC4D0),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx()
                )
            }
    ) {
        val items = listOf(
            Triple(ActiveScreen.DASHBOARD, Icons.Default.GridView, "Dashboard"),
            Triple(ActiveScreen.CROSSWORD, Icons.Default.GridOn, "Crosswords"),
            Triple(ActiveScreen.KAHOOT, Icons.Default.EmojiEvents, "Arena"),
            Triple(ActiveScreen.DAILY_CHALLENGE, Icons.Default.People, "Daily Co-op"),
            Triple(ActiveScreen.EDUCATION, Icons.Default.School, "Vocab Corner")
        )

        items.forEach { (screen, icon, label) ->
            val selected = currentScreen == screen
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(screen) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (selected) Color(0xFF6750A4) else Color(0xFF49454F)
                    )
                },
                label = {
                    Text(
                        text = label,
                        color = if (selected) Color(0xFF1D1B20) else Color(0xFF49454F),
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 11.sp
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color(0xFFE8DEF8)
                ),
                modifier = Modifier.testTag("nav_tab_${screen.name.lowercase()}")
            )
        }
    }
}

// ==========================================
// SCREEN 1: DASHBOARD / HOMEPAGE
// ==========================================
@Composable
fun DashboardScreen(viewModel: GameViewModel) {
    var topicSelectedByAi by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Header Banner
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEADDFF)),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color(0xFFD0BCFF)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "WORD ARENA",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF21005D),
                                letterSpacing = 2.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Welcome back, Wordsmith!",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF1D1B20)
                            )
                        }
                        // Level Badge
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF6750A4))
                                .wrapContentSize(Alignment.Center)
                        ) {
                            Text("A1", fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 18.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Streak and Resource counters in deep slate pill borders
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricsPill(icon = Icons.Default.Whatshot, value = "${viewModel.streakDays} Days", label = "Streak", tint = Color(0xFFE65100))
                        MetricsPill(icon = Icons.Default.Animation, value = "${viewModel.userXP} XP", label = "Level Points", tint = Color(0xFF0B57D0))
                        MetricsPill(icon = Icons.Default.Savings, value = "${viewModel.coins}", label = "Coins", tint = Color(0xFF86119E))
                    }
                }
            }
        }

        // Word Of The Day Widget
        item {
            val wotd = viewModel.wordOfTheDayList[viewModel.activeWordOfTheDayIndex]
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color(0xFFCAC4D0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFF6750A4), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("WORD OF THE DAY", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6750A4), letterSpacing = 1.sp)
                        }
                        Row {
                            IconButton(onClick = { viewModel.loadWordOfTheDay(-1) }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.ArrowLeft, contentDescription = "Prev", tint = Color(0xFF6750A4))
                            }
                            IconButton(onClick = { viewModel.loadWordOfTheDay(1) }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.ArrowRight, contentDescription = "Next", tint = Color(0xFF6750A4))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(wotd.word, fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color(0xFF1D1B20))
                    Text("${wotd.syllable} • ${wotd.partOfSpeech}", fontSize = 14.sp, color = Color(0xFF6750A4), fontWeight = FontWeight.Bold)
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(wotd.definition, fontSize = 15.sp, color = Color(0xFF49454F))
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF7F2FA), RoundedCornerShape(12.dp))
                            .border(BorderStroke(1.dp, Color(0xFFEADDFF)), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "\"${wotd.example}\"",
                            fontSize = 14.sp,
                            color = Color(0xFF49454F),
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "💡 Etymology: ${wotd.origin}",
                        fontSize = 12.sp,
                        color = Color(0xFF49454F)
                    )
                }
            }
        }

        // Custom AI Game Generator Configurator Call-to-Action
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFD3E3FD)),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.3.dp, Color(0xFF0B57D0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (GeminiApi.isApiKeyConfigured) Color(0xFF0B57D0) else Color(0xFFBA1A1A))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (GeminiApi.isApiKeyConfigured) "GEMINI ENGINE ACTIVE" else "GEMINI OFFLINE MODE (DEFAULT FALLBACKS)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (GeminiApi.isApiKeyConfigured) Color(0xFF0B57D0) else Color(0xFFBA1A1A),
                            letterSpacing = 1.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Transform Any Topic Into a Crossword / Timed Quiz!",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF041E49)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Type any interest (e.g. Space, Plants, Cooking, Anime) and click a module to instantly manufacture an interactive game boards on demand.",
                        fontSize = 13.sp,
                        color = Color(0xFF041E49).copy(alpha = 0.8f),
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    TextField(
                        value = topicSelectedByAi,
                        onValueChange = { topicSelectedByAi = it },
                        placeholder = { Text("Enter custom topic (e.g., Astronomy)", color = Color(0xFF49454F)) },
                        modifier = Modifier.fillMaxWidth().testTag("ai_topic_input"),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFFEF7FF),
                            unfocusedContainerColor = Color(0xFFFEF7FF),
                            focusedTextColor = Color(0xFF1D1B20),
                            unfocusedTextColor = Color(0xFF1D1B20),
                            cursorColor = Color(0xFF6750A4)
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                if (topicSelectedByAi.isNotEmpty()) {
                                    viewModel.generateAIPuzzle(topicSelectedByAi)
                                    viewModel.navigateTo(ActiveScreen.CROSSWORD)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).testTag("gen_crossword_button")
                        ) {
                            Icon(Icons.Default.GridOn, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Make Crossword", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                if (topicSelectedByAi.isNotEmpty()) {
                                    viewModel.startKahootMatch(topicSelectedByAi)
                                    viewModel.navigateTo(ActiveScreen.KAHOOT)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B57D0)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).testTag("gen_quiz_button")
                        ) {
                            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Make Timed Quiz", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Quick Launch Game Hub
        item {
            Text(
                text = "GAME CHAMBERS",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF6750A4),
                letterSpacing = 2.sp
            )
        }

        item {
            GameLaunchCard(
                title = "1. Masterclass Crosswords",
                desc = "Grid intersections testing syntax, spelling, and semantic alignment.",
                icon = Icons.Default.GridOn,
                containerColor = Color(0xFFE8DEF8),
                textColor = Color(0xFF21005D),
                accentColor = Color(0xFF6750A4),
                onClick = {
                    viewModel.loadDefaultCrossword()
                    viewModel.navigateTo(ActiveScreen.CROSSWORD)
                }
            )
        }

        item {
            GameLaunchCard(
                title = "2. Arena Timed Tournament",
                desc = "Multiplayer timed quiz race against 3 aggressive bots. High speed equals major score rewards!",
                icon = Icons.Default.Group,
                containerColor = Color(0xFFFAD8FD),
                textColor = Color(0xFF3B0945),
                accentColor = Color(0xFF86119E),
                onClick = {
                    viewModel.startKahootMatch("")
                    viewModel.navigateTo(ActiveScreen.KAHOOT)
                }
            )
        }

        item {
            GameLaunchCard(
                title = "3. Co-Op Daily Challenge",
                desc = "Global live wordsmithing table. Submit letters together with users globally to meet today's communal milestones.",
                icon = Icons.Default.Forum,
                containerColor = Color(0xFFC2F0E3),
                textColor = Color(0xFF00382D),
                accentColor = Color(0xFF0B845C),
                onClick = { viewModel.navigateTo(ActiveScreen.DAILY_CHALLENGE) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun MetricsPill(icon: ImageVector, value: String, label: String, tint: Color) {
    Box(
        modifier = Modifier
            .background(Color.White, RoundedCornerShape(14.dp))
            .border(BorderStroke(1.dp, Color(0xFFCAC4D0)), RoundedCornerShape(14.dp))
            .padding(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(value, color = Color(0xFF1D1B20), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(label, color = Color(0xFF49454F), fontSize = 10.sp)
            }
        }
    }
}

@Composable
fun GameLaunchCard(
    title: String,
    desc: String,
    icon: ImageVector,
    containerColor: Color,
    textColor: Color,
    accentColor: Color,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.2f))
                    .wrapContentSize(Alignment.Center)
            ) {
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(24.dp))
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = textColor)
                Spacer(modifier = Modifier.height(2.dp))
                Text(desc, fontSize = 13.sp, color = textColor.copy(alpha = 0.8f), lineHeight = 16.sp)
            }

            Icon(Icons.Default.ArrowRight, contentDescription = null, tint = textColor.copy(alpha = 0.6f))
        }
    }
}


// ==========================================
// SCREEN 2: INTERACTIVE CROSSWORD GAME
// ==========================================
@Composable
fun CrosswordScreen(viewModel: GameViewModel) {
    val puzzle = viewModel.activeCrossword
    val crosswordStatus = viewModel.crosswordFeedbackMessage

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Upper Title Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.navigateTo(ActiveScreen.DASHBOARD) }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF6750A4))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                Text(puzzle.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1D1B20), textAlign = TextAlign.Center)
                Text("Topic: ${puzzle.topic}", fontSize = 13.sp, color = Color(0xFF6750A4), fontWeight = FontWeight.Bold)
            }
            IconButton(onClick = { viewModel.loadDefaultCrossword() }) {
                Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = Color(0xFF6750A4))
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (viewModel.isLoadingCrossword) {
            Column(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(color = Color(0xFF6750A4))
                Spacer(modifier = Modifier.height(14.dp))
                Text(crosswordStatus, color = Color(0xFF49454F), textAlign = TextAlign.Center, fontSize = 14.sp)
            }
        } else {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.SpaceBetween) {
                // Crossword Grid Section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .background(Color.White, RoundedCornerShape(24.dp))
                        .border(BorderStroke(1.dp, Color(0xFFCAC4D0)), RoundedCornerShape(24.dp))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CrosswordGrid(
                        puzzle = puzzle,
                        selectedRow = viewModel.selectedRow,
                        selectedCol = viewModel.selectedCol,
                        onCellClick = { r, c -> viewModel.selectCell(r, c) }
                    )
                }

                // Clue Card & Controls
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp)
                ) {
                    val activeClue = puzzle.clues.find { it.id == viewModel.activeClueId }
                    if (activeClue != null) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8DEF8)),
                            border = BorderStroke(1.2.dp, Color(0xFFEADDFF)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${activeClue.id} (${activeClue.length} Letters)",
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF21005D),
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "Direction: ${activeClue.direction}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF49454F)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = activeClue.clue,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1D1B20)
                                )
                            }
                        }
                    }

                    if (crosswordStatus.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF7F2FA), RoundedCornerShape(10.dp))
                                .border(BorderStroke(1.dp, Color(0xFFEADDFF)))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = crosswordStatus,
                                color = Color(0xFF1D1B20),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                // Custom On-Screen Keyboard so user does not struggle typing!
                OnScreenKeyboard(
                    onKeyInput = { k -> viewModel.enterLetterInSelectedCell(k) },
                    onDelete = { viewModel.enterLetterInSelectedCell("") }
                )

                // Tool Strip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.revealSelectedCell() },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF6750A4)),
                        border = BorderStroke(1.dp, Color(0xFF6750A4)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Reveal Cell", fontSize = 12.sp)
                    }

                    Button(
                        onClick = { viewModel.solvePuzzle() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Solve Puzzle", color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun CrosswordGrid(
    puzzle: CrosswordPuzzle,
    selectedRow: Int,
    selectedCol: Int,
    onCellClick: (Int, Int) -> Unit
) {
    val size = puzzle.size
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        for (r in 0 until size) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (c in 0 until size) {
                    val cell = puzzle.cells.find { it.row == r && it.col == c }
                    if (cell != null) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (cell.isBlack) Color(0xFF21005D)
                                    else if (selectedRow == r && selectedCol == c) Color(0xFFEADDFF)
                                    else Color.White
                                )
                                .border(
                                    BorderStroke(
                                        width = if (selectedRow == r && selectedCol == c) 2.dp else 1.dp,
                                        color = if (selectedRow == r && selectedCol == c) Color(0xFF6750A4) else Color(0xFFEADDFF)
                                    ),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .clickable {
                                    if (!cell.isBlack) {
                                        onCellClick(r, c)
                                    }
                                }
                                .testTag("cell_${r}_${c}")
                        ) {
                            if (!cell.isBlack) {
                                // Cell clue position number
                                if (cell.number > 0) {
                                    Text(
                                        text = cell.number.toString(),
                                        fontSize = 11.sp,
                                        color = Color(0xFF6750A4),
                                        modifier = Modifier
                                            .align(Alignment.TopStart)
                                            .padding(2.dp)
                                    )
                                }
                                // Entered Letter
                                Text(
                                    text = cell.enteredChar,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (selectedRow == r && selectedCol == c) Color(0xFF6750A4) else Color(0xFF1D1B20),
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }
                    } else {
                        // Empty Black cell placeholder
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .aspectRatio(1f)
                                .background(Color(0xFF21005D))
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OnScreenKeyboard(onKeyInput: (String) -> Unit, onDelete: () -> Unit) {
    val row1 = listOf("Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P")
    val row2 = listOf("A", "S", "D", "F", "G", "H", "J", "K", "L")
    val row3 = listOf("Z", "X", "C", "V", "B", "N", "M")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Keyboard Row 1
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            row1.forEach { key ->
                KeyboardKey(key = key, onClick = { onKeyInput(key) })
            }
        }
        // Keyboard Row 2
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            row2.forEach { key ->
                KeyboardKey(key = key, onClick = { onKeyInput(key) })
            }
        }
        // Keyboard Row 3
        Row(
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Delete Key
            Box(
                modifier = Modifier
                    .size(width = 44.dp, height = 36.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFEADDFF))
                    .clickable { onDelete() }
                    .wrapContentSize(Alignment.Center)
            ) {
                Icon(Icons.Default.Backspace, contentDescription = "Delete", tint = Color(0xFF6750A4), modifier = Modifier.size(16.dp))
            }

            row3.forEach { key ->
                KeyboardKey(key = key, onClick = { onKeyInput(key) })
            }
        }
    }
}

@Composable
fun KeyboardKey(key: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(width = 30.dp, height = 36.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFFE8DEF8))
            .clickable { onClick() }
            .wrapContentSize(Alignment.Center)
            .testTag("key_$key")
    ) {
        Text(
            text = key,
            color = Color(0xFF21005D),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}


// ==========================================
// SCREEN 3: KAHOOT TIMED MULTIPLAYER TOURNEY
// ==========================================
@Composable
fun KahootScreen(viewModel: GameViewModel) {
    when (viewModel.kahootState) {
        KahootState.IDLE -> {
            // Welcome screen of competitive arena
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = Color(0xFF86119E),
                    modifier = Modifier.size(80.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "ARENA BUZZER MATCH",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF1D1B20),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Enter a live speed competition against 3 aggressive AI bots. Correctness rules, but speed gains points! Standard 15 second timers.",
                    fontSize = 15.sp,
                    color = Color(0xFF49454F),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Optional custom topic
                var topicInput by remember { mutableStateOf("") }
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEADDFF)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Custom AI Topic Match:", fontWeight = FontWeight.Bold, color = Color(0xFF21005D), fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        TextField(
                            value = topicInput,
                            onValueChange = { topicInput = it },
                            placeholder = { Text("What topic? (e.g. Science, Slang)", color = Color(0xFF49454F)) },
                            modifier = Modifier.fillMaxWidth().testTag("kahoot_topic_input"),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedTextColor = Color(0xFF1D1B20),
                                unfocusedTextColor = Color(0xFF1D1B20),
                                cursorColor = Color(0xFF6750A4)
                            ),
                            singleLine = true
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { viewModel.startKahootMatch(topicInput) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("start_kahoot_button")
                ) {
                    Text("LOBBY & ENTER ARENA RUN", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                }
            }
        }

        KahootState.GETTING_READY -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(color = Color(0xFF86119E))
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Assembling dynamic AI quiz questions...",
                    fontSize = 18.sp,
                    color = Color(0xFF1D1B20),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Awaiting Gemini compiler and scoreboard initialization.",
                    fontSize = 13.sp,
                    color = Color(0xFF49454F),
                    textAlign = TextAlign.Center
                )
            }
        }

        KahootState.QUESTION_ACTIVE, KahootState.REVEALED -> {
            val isRevealed = viewModel.kahootState == KahootState.REVEALED
            val isUserCorrect = viewModel.userSelectedOptionIndex == viewModel.activeQuestion.correctIndex

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header Status Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (viewModel.isAiTopicKahoot) "AI MONSTERS ROUND" else "QUESTION ${viewModel.currentQuestionIndex + 1} of 7",
                            color = Color(0xFF6750A4),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "Score: ${viewModel.userKahootScore} pts",
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1D1B20),
                            fontSize = 16.sp
                        )
                    }

                    // Ticking circular countdown timer
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(
                                if (viewModel.timerSecondsRemaining > 10) Color(0xFF00875A)
                                else if (viewModel.timerSecondsRemaining > 5) Color(0xFFD97706)
                                else Color(0xFFBA1A1A)
                            )
                            .wrapContentSize(Alignment.Center)
                    ) {
                        Text(
                            text = viewModel.timerSecondsRemaining.toString(),
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            fontSize = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Big Clue Question Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEADDFF)),
                    border = BorderStroke(1.dp, Color(0xFFD0BCFF)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = viewModel.activeQuestion.question,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF21005D),
                            textAlign = TextAlign.Center,
                            lineHeight = 26.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Scoreboard of contestants (Bottom view during play)
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Contender: You
                    item {
                        ContenderPill(
                            name = "You",
                            emoji = "🤠",
                            score = viewModel.userKahootScore,
                            status = if (isRevealed) (if (isUserCorrect) "CORRECT" else "WRONG") else (if (viewModel.isUserAnswerLocked) "LOCKED" else "THINKING")
                        )
                    }
                    items(viewModel.bots) { bot ->
                        ContenderPill(
                            name = bot.name,
                            emoji = bot.avatarEmoji,
                            score = bot.score,
                            status = if (isRevealed) (if (bot.lastAnswerCorrect) "CORRECT" else "WRONG") else "LOCKED"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Option Choice Buttons (4 Quadrants grid or rows)
                if (!isRevealed) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val rowColors = listOf(Color(0xFFEF4444), Color(0xFF3B82F6), Color(0xFFF59E0B), Color(0xFF10B981))
                        val icons = listOf(Icons.Default.ChangeHistory, Icons.Default.Diamond, Icons.Default.Circle, Icons.Default.Square)

                        for (i in 0 until 4) {
                            val optionText = viewModel.activeQuestion.options.getOrNull(i).orEmpty()
                            Button(
                                onClick = { viewModel.lockKahootAnswer(i) },
                                colors = ButtonDefaults.buttonColors(containerColor = rowColors[i]),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp)
                                    .testTag("choice_button_$i")
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(icons[i], contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = optionText,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Revealed Round explanation details
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Success Badge Card
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isUserCorrect) Color(0xFF00875A) else Color(0xFFBA1A1A)
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isUserCorrect) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = if (isUserCorrect) "Spot on! That's Correct." else "Unfortunate! Wrong Option.",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "The correct answer is: ${viewModel.activeQuestion.options[viewModel.activeQuestion.correctIndex]}",
                                        fontSize = 13.sp,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }

                        // Etymology Explanation Card
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F2FA)),
                            border = BorderStroke(1.dp, Color(0xFFEADDFF)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Educational Insight:", fontWeight = FontWeight.Bold, color = Color(0xFF6750A4), fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(viewModel.activeQuestion.explanation, fontSize = 14.sp, color = Color(0xFF1D1B20), lineHeight = 18.sp)
                            }
                        }

                        // Next button
                        Button(
                            onClick = { viewModel.nextKahootQuestion() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text("Next Clue", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }

        KahootState.PODIUM -> {
            // High Scores Podium stands
            val standings = (listOf(Pair("You", viewModel.userKahootScore)) + viewModel.bots.map { Pair(it.name, it.score) })
                .sortedByDescending { it.second }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFF86119E), modifier = Modifier.size(64.dp))
                    Text("TOURNAMENT RESULTS", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color(0xFF1D1B20))
                    Text("Podium Finishers", fontSize = 14.sp, color = Color(0xFF6750A4), fontWeight = FontWeight.Bold)
                }

                // Graphical Podium Stands
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .padding(horizontal = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    // 2nd Place (Left)
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val second = standings.getOrNull(1)
                        if (second != null) {
                            Text(second.first, fontWeight = FontWeight.Bold, color = Color(0xFF1D1B20), fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("${second.second} pts", color = Color(0xFF6750A4), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .background(Color(0xFFE8DEF8), RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                                .wrapContentSize(Alignment.Center)
                        ) {
                            Text("2", color = Color(0xFF21005D), fontWeight = FontWeight.Black, fontSize = 36.sp)
                        }
                    }

                    // 1st Place (Center - Tallest)
                    Column(
                        modifier = Modifier.weight(1.2f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("👑", fontSize = 22.sp)
                        val first = standings.getOrNull(0)
                        if (first != null) {
                            Text(first.first, fontWeight = FontWeight.ExtraBold, color = Color(0xFF86119E), fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("${first.second} pts", color = Color(0xFFE65100), fontSize = 12.sp, fontWeight = FontWeight.Black)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(170.dp)
                                .background(Color(0xFFFFD8E4), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                                .wrapContentSize(Alignment.Center)
                        ) {
                            Text("1", color = Color(0xFF31111D), fontWeight = FontWeight.Black, fontSize = 48.sp)
                        }
                    }

                    // 3rd Place (Right)
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val third = standings.getOrNull(2)
                        if (third != null) {
                            Text(third.first, fontWeight = FontWeight.Bold, color = Color(0xFF1D1B20), fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("${third.second} pts", color = Color(0xFF0B845C), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .background(Color(0xFFC2F0E3), RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                                .wrapContentSize(Alignment.Center)
                        ) {
                            Text("3", color = Color(0xFF00382D), fontWeight = FontWeight.Black, fontSize = 28.sp)
                        }
                    }
                }

                // Action buttons
                Button(
                    onClick = { viewModel.navigateTo(ActiveScreen.DASHBOARD) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Exit to Dashboard", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ContenderPill(name: String, emoji: String, score: Int, status: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFCAC4D0)),
        modifier = Modifier
            .width(130.dp)
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(emoji, fontSize = 18.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(name, fontWeight = FontWeight.Bold, color = Color(0xFF1D1B20), fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("$score pts", color = Color(0xFF6750A4), fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .background(
                        when (status) {
                            "CORRECT" -> Color(0xFF00875A)
                            "WRONG" -> Color(0xFFBA1A1A)
                            "LOCKED" -> Color(0xFF0B57D0).copy(alpha = 0.2f)
                            else -> Color(0xFF49454F)
                        },
                        RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(status, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}


// ==========================================
// SCREEN 4: COLLABORATIVE DAILY CHALLENGE
// ==========================================
@Composable
fun CoOpDailyScreen(viewModel: GameViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Co-Op Header Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.navigateTo(ActiveScreen.DASHBOARD) }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF6750A4))
            }
            Text("COLLABORATIVE DAILY CHALLENGE", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color(0xFF1D1B20))
            Icon(Icons.Default.People, contentDescription = null, tint = Color(0xFF0B845C))
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Community Global Goal Status Bar Progress
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFC2F0E3)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "COMMUNAL GLOBAL GOAL TODAY:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00382D),
                    letterSpacing = 1.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Build 10,000 pts with daily pool", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color(0xFF00382D))
                    Text("${viewModel.coOpProgressPoints} / 10,000 pts", color = Color(0xFF0B845C), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Progress Bar
                LinearProgressIndicator(
                    progress = { viewModel.coOpProgressPoints.toFloat() / viewModel.maxCoOpGoal },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = Color(0xFF0B845C),
                    trackColor = Color.White.copy(alpha = 0.5f)
                )

                Spacer(modifier = Modifier.height(6.dp))
                Text("Contributors Active: 4,812 online builders • 6h remaining", fontSize = 11.sp, color = Color(0xFF00382D).copy(alpha = 0.7f))
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // The Letters Wheel board
        Text("TODAY'S LETTER WHEEL POOL:", fontWeight = FontWeight.Bold, color = Color(0xFF6750A4), fontSize = 12.sp)
        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            items(viewModel.dailyLetters) { letter ->
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE8DEF8))
                        .border(BorderStroke(1.5.dp, Color(0xFF6750A4)), CircleShape)
                        .clickable { viewModel.userDailyEnteredWord += letter }
                        .wrapContentSize(Alignment.Center)
                ) {
                    Text(letter, color = Color(0xFF21005D), fontWeight = FontWeight.Black, fontSize = 20.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Word Composer Field
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = viewModel.userDailyEnteredWord,
                onValueChange = { viewModel.userDailyEnteredWord = it },
                placeholder = { Text("Build a word using pool...", color = Color(0xFF49454F)) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("coop_word_input"),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Color(0xFF1D1B20),
                    unfocusedTextColor = Color(0xFF1D1B20),
                    cursorColor = Color(0xFF6750A4)
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Submit Button
            Button(
                onClick = { viewModel.submitCoOpWord() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("coop_submit_button")
            ) {
                Text("Submit", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Live columns splitting User Submissions and Simulated Active Global Feed
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Left Column: User Submissions
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(Color.White, RoundedCornerShape(14.dp))
                    .border(BorderStroke(1.dp, Color(0xFFCAC4D0)), RoundedCornerShape(14.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "MY DAILY ADDS:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6750A4)
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (viewModel.userSubmittedDailyWords.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No entries built.", color = Color(0xFF49454F), fontSize = 12.sp)
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(viewModel.userSubmittedDailyWords.toList()) { word ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(word, fontWeight = FontWeight.Bold, color = Color(0xFF1D1B20), fontSize = 13.sp)
                                Text("+${word.length * 2} pts", color = Color(0xFF0B845C), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Right Column: Live Global Feed Simulator
            Column(
                modifier = Modifier
                    .weight(1.2f)
                    .background(Color.White, RoundedCornerShape(14.dp))
                    .border(BorderStroke(1.dp, Color(0xFFCAC4D0)), RoundedCornerShape(14.dp))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00875A))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LIVE ACTIVITY FEED:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6750A4)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(viewModel.coOpFeedList, key = { it.id }) { msg ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF7F2FA), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "@${msg.username}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (msg.username == "You") Color(0xFF00875A) else Color(0xFF0B57D0)
                                    )
                                    Text(text = "Just now", fontSize = 9.sp, color = Color(0xFF49454F))
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "submitted '${msg.word}' (+${msg.score} pts)",
                                    fontSize = 12.sp,
                                    color = Color(0xFF1D1B20)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


// ==========================================
// SCREEN 5: VOCAB CORNER & EDUCATIONAL MINDGAMES
// ==========================================
@Composable
fun EducationScreen(viewModel: GameViewModel) {
    var activeEducTab by remember { mutableStateOf(0) } // 0: Search lookup, 1: Anagrams minigame

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Education Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.navigateTo(ActiveScreen.DASHBOARD) }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF6750A4))
            }
            Text("VOCABULARY CORNER", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color(0xFF1D1B20))
            Icon(Icons.Default.School, contentDescription = null, tint = Color(0xFF0B845C))
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Switch Tabs: AI Search Lookup vs Anagram Solvers
        TabRow(
            selectedTabIndex = activeEducTab,
            containerColor = Color(0xFFF3EDF7),
            contentColor = Color(0xFF6750A4)
        ) {
            Tab(selected = activeEducTab == 0, onClick = { activeEducTab = 0 }) {
                Text("AI Lesson Search", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, color = if (activeEducTab == 0) Color(0xFF6750A4) else Color(0xFF49454F))
            }
            Tab(selected = activeEducTab == 1, onClick = { activeEducTab = 1 }) {
                Text("Anagram Puzzles", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, color = if (activeEducTab == 1) Color(0xFF6750A4) else Color(0xFF49454F))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (activeEducTab) {
            0 -> {
                // Look up custom etymological definitions with Gemini
                Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "Etymological Vocabulary Search:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6750A4)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            value = viewModel.searchedWord,
                            onValueChange = { viewModel.searchedWord = it },
                            placeholder = { Text("E.g. Episodic, Eloquent", color = Color(0xFF49454F)) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("words_lookup_input"),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedTextColor = Color(0xFF1D1B20),
                                unfocusedTextColor = Color(0xFF1D1B20),
                                cursorColor = Color(0xFF6750A4)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        IconButton(
                            onClick = { viewModel.searchWordWithAI() },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF6750A4))
                                .testTag("search_word_button")
                        ) {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
                        }
                    }

                    if (viewModel.isSearchingWord) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = Color(0xFF86119E))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Analyzing phonetic etymology details with AI...", color = Color(0xFF49454F), fontSize = 13.sp)
                            }
                        }
                    } else {
                        val result = viewModel.searchResultInsight
                        if (result != null) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.2.dp, Color(0xFFEADDFF)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(18.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(result.word, fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color(0xFF1D1B20))
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFFEADDFF), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(result.partOfSpeech, color = Color(0xFF6750A4), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Text(result.syllable, fontSize = 13.sp, color = Color(0xFF49454F), fontWeight = FontWeight.Medium)
                                    Spacer(modifier = Modifier.height(10.dp))

                                    Text("Definition:", fontWeight = FontWeight.Bold, color = Color(0xFF6750A4), fontSize = 13.sp)
                                    Text(result.definition, fontSize = 14.sp, color = Color(0xFF1D1B20), lineHeight = 18.sp)
                                    Spacer(modifier = Modifier.height(10.dp))

                                    Text("Example Sentence:", fontWeight = FontWeight.Bold, color = Color(0xFF6750A4), fontSize = 13.sp)
                                    Text("\"${result.example}\"", fontSize = 13.sp, color = Color(0xFF49454F), fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text("Etymological History:", fontWeight = FontWeight.Bold, color = Color(0xFF6750A4), fontSize = 13.sp)
                                    Text(result.origin, fontSize = 13.sp, color = Color(0xFF1D1B20))
                                    Spacer(modifier = Modifier.height(10.dp))

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFFFFD8E4), RoundedCornerShape(10.dp))
                                            .padding(12.dp)
                                    ) {
                                        Text("💡 Trivia Point: ${result.funFact}", fontSize = 12.sp, color = Color(0xFF31111D), lineHeight = 16.sp)
                                    }
                                }
                            }
                        } else if (viewModel.searchError.isNotEmpty()) {
                            Text(viewModel.searchError, color = Color(0xFFBA1A1A), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                        } else {
                            // Instructional placeholder empty state
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.MenuBook, contentDescription = null, tint = Color(0xFFCAC4D0), modifier = Modifier.size(64.dp))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Ready to explore word derivations.\nEntries fetched offline or generated dynamically via Gemini.",
                                    color = Color(0xFF49454F),
                                    textAlign = TextAlign.Center,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }

            // Anagram solver minigame view
            1 -> {
                val challenge = viewModel.currentAnagramChallenge
                val validatorMsg = viewModel.anagramValidationMessage
                val solved = viewModel.isAnagramSolved

                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "UNSCRAMBLE THE LETTERS:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6750A4),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        // Large glowing scrambled blocks
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(vertical = 10.dp)
                        ) {
                            challenge.scrambled.forEach { char ->
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFEADDFF))
                                        .border(BorderStroke(1.dp, Color(0xFF6750A4)), RoundedCornerShape(8.dp))
                                        .wrapContentSize(Alignment.Center)
                                ) {
                                    Text(
                                        text = char.toString(),
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF21005D),
                                        fontSize = 18.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Educational clue
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F2FA)),
                            border = BorderStroke(1.dp, Color(0xFFEADDFF)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("CLUE / MEANING:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6750A4))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(challenge.clue, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1D1B20))
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Answer input box
                        TextField(
                            value = viewModel.userAnagramInput,
                            onValueChange = { viewModel.userAnagramInput = it },
                            placeholder = { Text("What is correct sorting order?", color = Color(0xFF49454F)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("anagram_input_field"),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedTextColor = Color(0xFF1D1B20),
                                unfocusedTextColor = Color(0xFF1D1B20),
                                cursorColor = Color(0xFF6750A4)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        if (validatorMsg.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = validatorMsg,
                                color = if (solved) Color(0xFF00875A) else Color(0xFFBA1A1A),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    // Bottom validation controls
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { viewModel.submitAnagramGuess() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).height(48.dp)
                        ) {
                            Text("Unscramble Guess", color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { viewModel.nextAnagramPuzzle() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEADDFF)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).height(48.dp)
                        ) {
                            Text("Next Anagram", color = Color(0xFF21005D), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
