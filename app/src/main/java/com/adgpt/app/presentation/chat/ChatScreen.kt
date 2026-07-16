@file:OptIn(ExperimentalLayoutApi::class)

package com.adgpt.app.presentation.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.UploadFile
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.adgpt.app.domain.model.ChatMessage
import com.adgpt.app.domain.model.MessageRole
import com.adgpt.app.presentation.theme.ElectricBlue
import com.adgpt.app.presentation.theme.PanelBlack
import com.adgpt.app.presentation.theme.SoftBlue
import com.adgpt.app.presentation.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun ChatScreen(
    state: ChatUiState,
    readyForEntrance: Boolean,
    onInputChange: (String) -> Unit,
    onApiKeyChange: (String) -> Unit,
    onActivateApi: () -> Unit,
    onClearApi: () -> Unit,
    onSend: () -> Unit,
    onNewChat: () -> Unit,
    onHistoryClick: (String) -> Unit,
    onQuickPrompt: (String) -> Unit,
    onToggleSidebar: () -> Unit,
    onToggleChatMinimized: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    var composerMenuOpen by remember { mutableStateOf(false) }
    val inputScale by animateFloatAsState(
        targetValue = if (readyForEntrance) 1f else 0.98f,
        animationSpec = entranceTween(delay = 240, duration = 520),
        label = "inputScale"
    )

    LaunchedEffect(readyForEntrance) {
        if (readyForEntrance) {
            delay(250)
            runCatching { focusRequester.requestFocus() }
        }
    }

    Row(Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = readyForEntrance,
            enter = fadeIn(entranceTween(duration = 520)) +
                slideInHorizontally(animationSpec = tween(580, easing = EntranceEase)) { -it / 2 }
        ) {
            if (state.sidebarCollapsed) {
                CollapsedSidebar(onToggleSidebar = onToggleSidebar, onNewChat = onNewChat)
            } else {
                WorkspaceSidebar(
                    state = state,
                    onApiKeyChange = onApiKeyChange,
                    onActivateApi = onActivateApi,
                    onClearApi = onClearApi,
                    onNewChat = onNewChat,
                    onHistoryClick = onHistoryClick,
                    onToggleSidebar = onToggleSidebar,
                    onSettingsClick = onSettingsClick
                )
            }
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
                TopBar(
                    state = state,
                    onToggleChatMinimized = onToggleChatMinimized,
                    onSettingsClick = onSettingsClick
                )
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
                if (state.chatMinimized) {
                    MinimizedChatCard(
                        messageCount = state.messages.size,
                        onRestore = onToggleChatMinimized,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    MessageList(
                        messages = state.messages,
                        onQuickPrompt = onQuickPrompt,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .scale(inputScale)
                    .alpha(if (readyForEntrance) 1f else 0f)
            ) {
                Box {
                    IconButton(onClick = { composerMenuOpen = true }) {
                        Icon(Icons.Rounded.Add, contentDescription = "Add attachment or action")
                    }
                    DropdownMenu(
                        expanded = composerMenuOpen,
                        onDismissRequest = { composerMenuOpen = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Attach file") },
                            leadingIcon = { Icon(Icons.Rounded.UploadFile, contentDescription = null) },
                            onClick = { composerMenuOpen = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Use prompt template") },
                            leadingIcon = { Icon(Icons.Rounded.AutoAwesome, contentDescription = null) },
                            onClick = {
                                composerMenuOpen = false
                                onQuickPrompt("Help me think through this step by step.")
                            }
                        )
                    }
                }
                Spacer(Modifier.width(8.dp))
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
private fun WorkspaceSidebar(
    state: ChatUiState,
    onApiKeyChange: (String) -> Unit,
    onActivateApi: () -> Unit,
    onClearApi: () -> Unit,
    onNewChat: () -> Unit,
    onHistoryClick: (String) -> Unit,
    onToggleSidebar: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(304.dp)
            .fillMaxHeight()
            .background(Color.Black.copy(alpha = 0.52f))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(SoftBlue, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("AD", color = Color.Black, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("AD-GPT", fontWeight = FontWeight.SemiBold)
                Text("AI workspace", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onToggleSidebar) {
                Icon(Icons.Rounded.KeyboardArrowLeft, contentDescription = "Collapse sidebar", tint = TextSecondary)
            }
        }

        AddApiCard(
            state = state,
            onApiKeyChange = onApiKeyChange,
            onActivateApi = onActivateApi,
            onClearApi = onClearApi
        )

        Button(
            onClick = onNewChat,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SoftBlue, contentColor = Color.Black)
        ) {
            Icon(Icons.Rounded.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("New Chat")
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = Color.White.copy(alpha = 0.07f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Rounded.Search, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Search chats", color = TextSecondary)
            }
        }

        SidebarSectionTitle(icon = Icons.Rounded.History, title = "Chat History")

        if (state.history.isEmpty()) {
            EmptyHistoryCard()
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.history, key = { it.id }) { item ->
                    HistoryRow(
                        item = item,
                        selected = item.id == state.selectedHistoryId,
                        onClick = { onHistoryClick(item.id) }
                    )
                }
            }
        }

        HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

        SidebarOption(Icons.Rounded.AutoAwesome, "Model", state.activeModel)
        SidebarOption(Icons.Rounded.Folder, "Knowledge", "Files and context")
        SidebarOption(Icons.Rounded.Tune, "Tools", "Provider, API, cache")

        OutlinedButton(
            onClick = onSettingsClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Rounded.Settings, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Open Settings")
        }
    }
}

@Composable
private fun CollapsedSidebar(
    onToggleSidebar: () -> Unit,
    onNewChat: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(76.dp)
            .fillMaxHeight()
            .background(Color.Black.copy(alpha = 0.58f))
            .padding(vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(SoftBlue, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("AD", color = Color.Black, fontWeight = FontWeight.Bold)
        }
        IconButton(onClick = onToggleSidebar) {
            Icon(Icons.Rounded.KeyboardArrowRight, contentDescription = "Expand sidebar")
        }
        IconButton(onClick = onNewChat) {
            Icon(Icons.Rounded.Add, contentDescription = "New chat")
        }
        Icon(Icons.Rounded.History, contentDescription = "History", tint = TextSecondary)
    }
}

@Composable
private fun AddApiCard(
    state: ChatUiState,
    onApiKeyChange: (String) -> Unit,
    onActivateApi: () -> Unit,
    onClearApi: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SoftBlue.copy(alpha = 0.10f)),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, SoftBlue.copy(alpha = 0.28f))
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Key, contentDescription = null, tint = SoftBlue, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Add API", fontWeight = FontWeight.SemiBold)
            }
            OutlinedTextField(
                value = state.apiKeyInput,
                onValueChange = onApiKeyChange,
                placeholder = { Text("Paste API key") },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            ProviderStatusRow(state)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onActivateApi,
                    enabled = state.detectedProvider != null && state.apiStatus != ApiStatus.Active,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Activate")
                }
                OutlinedButton(
                    onClick = onClearApi,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Clear")
                }
            }
        }
    }
}

@Composable
private fun ProviderStatusRow(state: ChatUiState) {
    val text = when (state.apiStatus) {
        ApiStatus.Idle -> "Paste a key to detect provider"
        ApiStatus.Detected -> "Detected ${state.detectedProvider?.name}"
        ApiStatus.Active -> "Active: ${state.activeProvider?.name} • ${state.activeModel}"
        ApiStatus.Invalid -> "Unknown key format"
    }
    val color = when (state.apiStatus) {
        ApiStatus.Active -> SoftBlue
        ApiStatus.Invalid -> Color(0xFFFF9B9B)
        else -> TextSecondary
    }
    Text(text, color = color, style = MaterialTheme.typography.bodySmall)
}

@Composable
private fun EmptyHistoryCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.06f)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("No chats yet", fontWeight = FontWeight.Medium)
            Text(
                "Start a conversation and your recent prompts will appear here.",
                color = TextSecondary,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun HistoryRow(
    item: ChatHistoryItem,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = if (selected) SoftBlue.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.055f),
        border = BorderStroke(1.dp, if (selected) SoftBlue.copy(alpha = 0.42f) else Color.White.copy(alpha = 0.06f))
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.PushPin, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text(item.title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Medium)
            }
            Text(item.subtitle, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun SidebarSectionTitle(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(title, color = TextSecondary, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun SidebarOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(Color.White.copy(alpha = 0.07f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(10.dp))
        Column {
            Text(title, fontWeight = FontWeight.Medium)
            Text(subtitle, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun TopBar(
    state: ChatUiState,
    onToggleChatMinimized: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text("AD-GPT", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
            Text(
                "${state.activeProvider?.name ?: "Local demo"} • ${state.activeModel} • intro video enabled",
                color = TextSecondary
            )
        }
        Row {
            IconButton(onClick = onToggleChatMinimized) {
                Icon(Icons.Rounded.MoreHoriz, contentDescription = "Minimize or restore chat")
            }
            IconButton(onClick = onSettingsClick) {
                Icon(Icons.Rounded.Settings, contentDescription = "Settings")
            }
        }
    }
}

@Composable
private fun MinimizedChatCard(
    messageCount: Int,
    onRestore: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = PanelBlack.copy(alpha = 0.76f)),
        shape = RoundedCornerShape(32.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Conversation minimized", style = MaterialTheme.typography.headlineSmall)
            Text("$messageCount messages hidden", color = TextSecondary)
            Spacer(Modifier.height(18.dp))
            Button(onClick = onRestore, shape = RoundedCornerShape(18.dp)) {
                Text("Restore Chat")
            }
        }
    }
}

@Composable
private fun MessageList(
    messages: List<ChatMessage>,
    onQuickPrompt: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = PanelBlack.copy(alpha = 0.76f)),
        shape = RoundedCornerShape(32.dp)
    ) {
        if (messages.isEmpty()) {
            EmptyConversation(onQuickPrompt = onQuickPrompt)
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
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
private fun EmptyConversation(onQuickPrompt: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .background(SoftBlue.copy(alpha = 0.16f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = SoftBlue, modifier = Modifier.size(32.dp))
        }
        Spacer(Modifier.height(18.dp))
        Text("What should AD-GPT help with today?", style = MaterialTheme.typography.headlineSmall)
        Text("Choose a starter or type below.", color = TextSecondary)
        Spacer(Modifier.height(22.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickPrompt("Draft a launch plan", onQuickPrompt)
            QuickPrompt("Summarize an idea", onQuickPrompt)
            QuickPrompt("Create app settings", onQuickPrompt)
            QuickPrompt("Design a provider layer", onQuickPrompt)
        }
    }
}

@Composable
private fun QuickPrompt(
    text: String,
    onQuickPrompt: (String) -> Unit
) {
    OutlinedButton(
        onClick = { onQuickPrompt(text) },
        shape = RoundedCornerShape(18.dp)
    ) {
        Text(text)
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
