package com.adgpt.app.presentation.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.adgpt.app.domain.model.ChatMessage
import com.adgpt.app.domain.model.MessageRole
import com.adgpt.app.presentation.theme.PanelBlack
import com.adgpt.app.presentation.theme.SoftBlue
import com.adgpt.app.presentation.theme.TextSecondary

@Composable
fun ChatScreen(
    state: ChatUiState,
    readyForEntrance: Boolean,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val inputScale by animateFloatAsState(
        targetValue = if (readyForEntrance) 1f else 0.98f,
        animationSpec = entranceTween(delay = 240, duration = 520),
        label = "inputScale"
    )

    LaunchedEffect(readyForEntrance) {
        if (readyForEntrance) focusRequester.requestFocus()
    }

    Row(Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = readyForEntrance,
            enter = fadeIn(entranceTween(duration = 520)) +
                slideInHorizontally(animationSpec = tween(580, easing = EntranceEase)) { -it / 2 }
        ) {
            Sidebar(onSettingsClick = onSettingsClick)
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(24.dp)
        ) {
            AnimatedVisibility(
                visible = readyForEntrance,
                enter = fadeIn(entranceTween(delay = 120)) +
                    slideInVertically(animationSpec = tween(560, delayMillis = 80, easing = EntranceEase)) { -24 }
            ) {
                TopBar(onSettingsClick = onSettingsClick)
            }

            Spacer(Modifier.height(20.dp))

            AnimatedVisibility(
                visible = readyForEntrance,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                enter = fadeIn(entranceTween(delay = 180)) +
                    slideInVertically(animationSpec = tween(620, delayMillis = 120, easing = EntranceEase)) { 36 }
            ) {
                MessageList(
                    messages = state.messages,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(Modifier.height(18.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .scale(inputScale)
                    .alpha(if (readyForEntrance) 1f else 0f)
            ) {
                OutlinedTextField(
                    value = state.input,
                    onValueChange = onInputChange,
                    placeholder = { Text("Ask AD-GPT…") },
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester),
                    singleLine = false,
                    minLines = 1,
                    maxLines = 5
                )
                Spacer(Modifier.width(12.dp))
                Button(
                    onClick = onSend,
                    enabled = state.input.isNotBlank() && !state.sending,
                    shape = CircleShape,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(Icons.Rounded.Send, contentDescription = "Send")
                }
            }
        }
    }
}

@Composable
private fun Sidebar(onSettingsClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(92.dp)
            .fillMaxHeight()
            .background(Color.Black.copy(alpha = 0.34f))
            .padding(vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(SoftBlue, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = Color.Black)
            }
            Spacer(Modifier.height(28.dp))
            Icon(Icons.Rounded.History, contentDescription = "History", tint = TextSecondary)
        }
        IconButton(onClick = onSettingsClick) {
            Icon(Icons.Rounded.Settings, contentDescription = "Settings", tint = TextSecondary)
        }
    }
}

@Composable
private fun TopBar(onSettingsClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text("AD-GPT", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
            Text("Premium AI workspace", color = TextSecondary)
        }
        IconButton(onClick = onSettingsClick) {
            Icon(Icons.Rounded.Settings, contentDescription = "Settings")
        }
    }
}

@Composable
private fun MessageList(messages: List<ChatMessage>, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = PanelBlack.copy(alpha = 0.76f)),
        shape = RoundedCornerShape(32.dp)
    ) {
        if (messages.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(28.dp), contentAlignment = Alignment.Center) {
                Text("Your AD-GPT session is warmed up and ready.", color = TextSecondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages, key = { it.id }) { message ->
                    MessageBubble(message)
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(message: ChatMessage) {
    val isUser = message.role == MessageRole.User
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) SoftBlue else Color.White.copy(alpha = 0.08f),
                contentColor = if (isUser) Color.Black else Color.White
            ),
            shape = RoundedCornerShape(22.dp),
            modifier = Modifier.fillMaxWidth(if (isUser) 0.72f else 0.82f)
        ) {
            Text(message.content, modifier = Modifier.padding(16.dp))
        }
    }
}
