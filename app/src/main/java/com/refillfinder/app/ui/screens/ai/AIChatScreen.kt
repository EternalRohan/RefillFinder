package com.refillfinder.app.ui.screens.ai

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.refillfinder.app.model.ChatMessage
import com.refillfinder.app.ui.theme.*
import com.refillfinder.app.viewmodel.AIChatViewModel
import com.refillfinder.app.viewmodel.ChatEvent
import kotlinx.coroutines.launch
import android.util.Log

/**
 * AIChatScreen — WaterBot
 *
 * Syllabus:
 *  - LaunchedEffect (auto-scroll, event collection)
 *  - SideEffect (Logcat logging on recomposition)
 *  - DisposableEffect (clear resources on leave)
 *  - SharedFlow event collection
 *  - Coroutines with Compose (rememberCoroutineScope)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIChatScreen(
    onNavigateBack: () -> Unit,
    viewModel: AIChatViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // ── SideEffect: runs every recomposition — used for Logcat ───────
    SideEffect {
        Log.d("AIChatScreen", "Recomposed with ${uiState.messages.size} messages")
    }

    // ── LaunchedEffect: auto-scroll when new message arrives ──────────
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    // ── SharedFlow event collection ───────────────────────────────────
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ChatEvent.Error ->
                    snackbarHostState.showSnackbar(event.message, duration = SnackbarDuration.Short)
                is ChatEvent.MessageSent -> { /* scroll handled above */ }
            }
        }
    }

    // ── DisposableEffect: cleanup when screen leaves composition ──────
    DisposableEffect(Unit) {
        Log.d("AIChatScreen", "Screen entered composition")
        onDispose {
            Log.d("AIChatScreen", "Screen left — cleanup complete")
        }
    }

    Scaffold(
        containerColor = BackgroundWhite,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(PrimaryBlue, AccentTeal))),
                            contentAlignment = Alignment.Center
                        ) { Text("💧", fontSize = 18.sp) }
                        Column {
                            Text("WaterBot", fontWeight = FontWeight.Bold,
                                color = OnSurface, fontSize = 16.sp)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(GreenActive)
                                )
                                Text("Online · Campus AI", color = GreenActive, fontSize = 11.sp)
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = OnSurface)
                    }
                },
                actions = {
                    if (uiState.messages.isNotEmpty()) {
                        TextButton(onClick = { viewModel.clearHistory() }) {
                            Text("Clear", color = RedOffline, fontSize = 12.sp)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceWhite,
                    scrolledContainerColor = SurfaceWhite
                )
            )
        },
        bottomBar = {
            Surface(
                color = SurfaceWhite,
                border = BorderStroke(1.dp, BorderLight),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                        .navigationBarsPadding()
                        .imePadding(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.inputText,
                        onValueChange = viewModel::updateInput,
                        placeholder = {
                            Text("Ask WaterBot anything...",
                                color = OnSurfaceSubtle, fontSize = 14.sp)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 4,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { viewModel.sendMessage() }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = BorderLight,
                            focusedTextColor = OnSurface,
                            unfocusedTextColor = OnSurface,
                            cursorColor = PrimaryBlue,
                            focusedContainerColor = BackgroundWhite,
                            unfocusedContainerColor = BackgroundWhite
                        )
                    )
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                brush = if (uiState.inputText.isBlank())
                                    SolidColor(BorderLight)
                                else
                                    Brush.linearGradient(listOf(PrimaryBlue, AccentTeal))
                            )
                            .clickable(enabled = uiState.inputText.isNotBlank()) {
                                viewModel.sendMessage()
                                scope.launch {
                                    if (uiState.messages.isNotEmpty())
                                        listState.animateScrollToItem(uiState.messages.size - 1)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White, strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                Icons.AutoMirrored.Filled.Send,
                                null,
                                tint = if (uiState.inputText.isBlank()) OnSurfaceMuted else Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Suggested prompts when empty
            if (uiState.messages.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Greeting card
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = PrimaryBlueLight,
                        border = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("💧", fontSize = 32.sp)
                            Column {
                                Text("Hi! I'm WaterBot 👋", fontWeight = FontWeight.Bold,
                                    color = PrimaryBlue, fontSize = 15.sp)
                                Text("I know every water cooler across all 60 campus blocks. Ask me anything!",
                                    color = OnSurfaceMuted, fontSize = 12.sp, lineHeight = 18.sp)
                            }
                        }
                    }

                    Text("💡 Try asking:", color = OnSurfaceMuted,
                        fontSize = 12.sp, fontWeight = FontWeight.SemiBold)

                    val suggestions = listOf(
                        "Which floor has a cooler near Block 15, Floor 6?",
                        "How do I report a broken cooler?",
                        "How many coolers are on Floor 3 of Block 30?",
                        "What floors have water coolers?"
                    )
                    suggestions.forEach { suggestion ->
                        Surface(
                            onClick = { viewModel.updateInput(suggestion) },
                            shape = RoundedCornerShape(12.dp),
                            color = SurfaceWhite,
                            border = BorderStroke(1.dp, BorderLight)
                        ) {
                            Text(
                                suggestion, color = OnSurface, fontSize = 13.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }

            // Chat messages
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // key = stable key for performance (UI Performance Optimization)
                items(uiState.messages, key = { it.id }) { message ->
                    ChatBubble(message = message)
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessage) {
    val isUser = message.isFromUser
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(PrimaryBlue, AccentTeal))),
                contentAlignment = Alignment.Center
            ) { Text("💧", fontSize = 14.sp) }
            Spacer(Modifier.width(8.dp))
        }

        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 18.dp, topEnd = 18.dp,
                        bottomStart = if (isUser) 18.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 18.dp
                    )
                )
                .background(
                    if (isUser) Brush.linearGradient(listOf(PrimaryBlue, PrimaryBlueMid))
                    else SolidColor(SurfaceWhite)
                )
                .border(
                    if (isUser) BorderStroke(0.dp, Color.Transparent)
                    else BorderStroke(1.dp, BorderLight),
                    RoundedCornerShape(18.dp)
                )
                .padding(12.dp)
        ) {
            if (message.isLoading) {
                TypingDots()
            } else {
                Text(
                    message.content,
                    color = if (isUser) Color.White else OnSurface,
                    fontSize = 14.sp, lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
private fun TypingDots() {
    val infiniteTransition = rememberInfiniteTransition(label = "typing")
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(3) { i ->
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.3f, targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, delayMillis = i * 200),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dot_$i"
            )
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(OnSurfaceMuted.copy(alpha = alpha))
            )
        }
    }
}
