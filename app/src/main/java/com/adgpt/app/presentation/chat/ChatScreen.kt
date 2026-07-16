package com.adgpt.app.presentation.chat

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AttachFile
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.adgpt.app.domain.model.ChatMessage
import com.adgpt.app.domain.model.MessageRole
import com.adgpt.app.presentation.theme.SoftBlue

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
    onAddAttachment: (String, String) -> Unit,
    onRemoveAttachment: (String) -> Unit,
    onSelectApi: (String) -> Unit,
    onToggleSidebar: () -> Unit,
    onToggleChatMinimized: () -> Unit,
    onToggleTheme: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val colors = appColors(state.darkTheme)
    var dragAmount by remember { mutableFloatStateOf(0f) }
    val fileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            onAddAttachment(it.lastPathSegment?.substringAfterLast('/') ?: "Selected file", "file")
        }
    }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) onAddAttachment("Camera image", "image")
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .pointerInput(state.sidebarCollapsed) {
                detectHorizontalDragGestures(
                    onDragEnd = { dragAmount = 0f },
                    onHorizontalDrag = { change, drag ->
                        dragAmount += drag
                        if (dragAmount > 80f && state.sidebarCollapsed) {
                            onToggleSidebar()
                            dragAmount = 0f
                        }
                        if (dragAmount < -80f && !state.sidebarCollapsed) {
                            onToggleSidebar()
                            dragAmount = 0f
                        }
                        change.consume()
                    }
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            TopBar(
                colors = colors,
                activeModel = state.activeModel,
                onMenuClick = onToggleSidebar
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                if (state.messages.isEmpty()) {
                    EmptyConversation(colors = colors)
                } else {
                    MessageList(
                        messages = state.messages,
                        colors = colors,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            ComposerBar(
                input = state.input,
                attachments = state.attachments,
                savedApis = state.savedApis,
                activeApiId = state.activeApiId,
                sending = state.sending,
                colors = colors,
                onInputChange = onInputChange,
                onSend = onSend,
                onAttachFile = { fileLauncher.launch(arrayOf("*/*")) },
                onCamera = { cameraLauncher.launch(null) },
                onRemoveAttachment = onRemoveAttachment,
                onSelectApi = onSelectApi
            )
        }

        SidebarDrawer(
            visible = !state.sidebarCollapsed,
            state = state,
            colors = colors,
            onClose = onToggleSidebar,
            onApiKeyChange = onApiKeyChange,
            onActivateApi = onActivateApi,
            onClearApi = onClearApi,
            onNewChat = onNewChat,
            onHistoryClick = onHistoryClick,
            onToggleTheme = onToggleTheme,
            onSettingsClick = onSettingsClick
        )
    }
}

@Composable
private fun TopBar(
    colors: ChatColors,
    activeModel: String,
    onMenuClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onMenuClick) {
            Icon(Icons.Rounded.Menu, contentDescription = "Open sidebar", tint = colors.text)
        }
        Text(
            text = "AD-GPT",
            color = colors.text,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.weight(1f))
        Text(activeModel, color = colors.muted, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun EmptyConversation(colors: ChatColors) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "How can I help?",
                color = colors.text,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
            Text("Type below or tap + for files/camera.", color = colors.muted)
        }
    }
}

@Composable
private fun MessageList(
    messages: List<ChatMessage>,
    colors: ChatColors,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(messages, key = { it.id }) { message ->
            MessageBubble(message = message, colors = colors)
        }
    }
}

@Composable
private fun MessageBubble(message: ChatMessage, colors: ChatColors) {
    val isUser = message.role == MessageRole.User
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(if (isUser) 0.82f else 0.9f),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) colors.userBubble else colors.assistantBubble,
                contentColor = if (isUser) colors.userText else colors.text
            ),
            shape = RoundedCornerShape(22.dp)
        ) {
            Text(
                text = message.content,
                modifier = Modifier.padding(14.dp),
                color = if (isUser) colors.userText else colors.text
            )
        }
    }
}

@Composable
private fun ComposerBar(
    input: String,
    attachments: List<AttachmentUi>,
    savedApis: List<SavedApiUi>,
    activeApiId: String?,
    sending: Boolean,
    colors: ChatColors,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    onAttachFile: () -> Unit,
    onCamera: () -> Unit,
    onRemoveAttachment: (String) -> Unit,
    onSelectApi: (String) -> Unit
) {
    var apiMenuOpen by remember { androidx.compose.runtime.mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (attachments.isNotEmpty()) {
            AttachmentStrip(attachments = attachments, colors = colors, onRemoveAttachment = onRemoveAttachment)
        }
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(30.dp),
            color = colors.composer,
            border = BorderStroke(1.dp, colors.border)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onAttachFile) {
                    Icon(Icons.Rounded.Add, contentDescription = "Add files", tint = colors.text)
                }
                IconButton(onClick = onCamera) {
                    Icon(Icons.Rounded.CameraAlt, contentDescription = "Camera", tint = colors.text)
                }
                TextField(
                    value = input,
                    onValueChange = onInputChange,
                    placeholder = { Text("Message AD-GPT", color = colors.muted) },
                    modifier = Modifier.weight(1f),
                    textStyle = TextStyle(color = colors.text),
                    minLines = 1,
                    maxLines = 4,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = SoftBlue
                    )
                )
                Box {
                    IconButton(onClick = { apiMenuOpen = true }) {
                        Icon(Icons.Rounded.SmartToy, contentDescription = "Choose API or model", tint = colors.text)
                    }
                    DropdownMenu(expanded = apiMenuOpen, onDismissRequest = { apiMenuOpen = false }) {
                        if (savedApis.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("Add API in sidebar first") },
                                onClick = { apiMenuOpen = false }
                            )
                        } else {
                            savedApis.forEach { api ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(api.providerName)
                                            Text(api.model, style = MaterialTheme.typography.bodySmall)
                                            Text(api.maskedKey, style = MaterialTheme.typography.bodySmall)
                                        }
                                    },
                                    leadingIcon = {
                                        if (api.id == activeApiId) Text("✓") else Icon(Icons.Rounded.SmartToy, contentDescription = null)
                                    },
                                    onClick = {
                                        apiMenuOpen = false
                                        onSelectApi(api.id)
                                    }
                                )
                            }
                        }
                    }
                }
                IconButton(
                    onClick = onSend,
                    enabled = (input.isNotBlank() || attachments.isNotEmpty()) && !sending
                ) {
                    Icon(
                        Icons.Rounded.Send,
                        contentDescription = "Send",
                        tint = if (input.isNotBlank() || attachments.isNotEmpty()) SoftBlue else colors.muted
                    )
                }
            }
        }
    }
}

@Composable
private fun AttachmentStrip(
    attachments: List<AttachmentUi>,
    colors: ChatColors,
    onRemoveAttachment: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        attachments.forEach { attachment ->
            Surface(
                color = colors.card,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, colors.border),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.AttachFile, contentDescription = null, tint = colors.text, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        attachment.name,
                        color = colors.text,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(attachment.type, color = colors.muted, style = MaterialTheme.typography.bodySmall)
                    IconButton(onClick = { onRemoveAttachment(attachment.id) }) {
                        Icon(Icons.Rounded.Close, contentDescription = "Remove attachment", tint = colors.muted)
                    }
                }
            }
        }
    }
}

@Composable
private fun SidebarDrawer(
    visible: Boolean,
    state: ChatUiState,
    colors: ChatColors,
    onClose: () -> Unit,
    onApiKeyChange: (String) -> Unit,
    onActivateApi: () -> Unit,
    onClearApi: () -> Unit,
    onNewChat: () -> Unit,
    onHistoryClick: (String) -> Unit,
    onToggleTheme: () -> Unit,
    onSettingsClick: () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInHorizontally { -it },
        exit = fadeOut() + slideOutHorizontally { -it }
    ) {
        Row(Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .width(318.dp)
                    .fillMaxHeight()
                    .background(colors.sidebar)
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Menu", color = colors.text, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = onClose) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close sidebar", tint = colors.text)
                    }
                }

                AddApiCard(state, colors, onApiKeyChange, onActivateApi, onClearApi)

                Button(
                    onClick = onNewChat,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SoftBlue, contentColor = Color.Black)
                ) {
                    Text("New Chat")
                }

                SidebarAction(Icons.Rounded.Search, "Search chats", colors) {}
                SidebarAction(
                    icon = if (state.darkTheme) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                    title = if (state.darkTheme) "Light mode" else "Dark mode",
                    colors = colors,
                    onClick = onToggleTheme
                )
                SidebarAction(Icons.Rounded.Settings, "Settings", colors, onSettingsClick)

                Text("Chats", color = colors.muted, style = MaterialTheme.typography.labelLarge)
                if (state.history.isEmpty()) {
                    Text("No chats yet", color = colors.muted)
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(state.history, key = { it.id }) { item ->
                            HistoryRow(item, colors) { onHistoryClick(item.id) }
                        }
                    }
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(Color.Black.copy(alpha = 0.35f))
                    .clickable(onClick = onClose)
            )
        }
    }
}

@Composable
private fun AddApiCard(
    state: ChatUiState,
    colors: ChatColors,
    onApiKeyChange: (String) -> Unit,
    onActivateApi: () -> Unit,
    onClearApi: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = colors.card),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Key, contentDescription = null, tint = colors.text, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Add API", color = colors.text, fontWeight = FontWeight.SemiBold)
            }
            OutlinedTextField(
                value = state.apiKeyInput,
                onValueChange = onApiKeyChange,
                placeholder = { Text("Paste API key", color = colors.muted) },
                textStyle = TextStyle(color = colors.text),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Text(providerStatusText(state), color = providerStatusColor(state, colors), style = MaterialTheme.typography.bodySmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onActivateApi,
                    enabled = state.detectedProvider != null && state.apiStatus != ApiStatus.Active,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                ) { Text("Activate") }
                OutlinedButton(onClick = onClearApi, shape = RoundedCornerShape(14.dp)) {
                    Text("Clear", color = colors.text)
                }
            }
        }
    }
}

@Composable
private fun SidebarAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    colors: ChatColors,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = colors.card,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, colors.border)
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = colors.text, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Text(title, color = colors.text)
        }
    }
}

@Composable
private fun HistoryRow(item: ChatHistoryItem, colors: ChatColors, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = colors.card,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, colors.border)
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.History, contentDescription = null, tint = colors.muted, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(10.dp))
            Column {
                Text(item.title, color = colors.text, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(item.subtitle, color = colors.muted, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

private data class ChatColors(
    val background: Color,
    val text: Color,
    val muted: Color,
    val composer: Color,
    val sidebar: Color,
    val card: Color,
    val border: Color,
    val userBubble: Color,
    val assistantBubble: Color,
    val userText: Color
)

private fun appColors(dark: Boolean): ChatColors =
    if (dark) {
        ChatColors(
            background = Color(0xFF05060A),
            text = Color.White,
            muted = Color(0xFFB8C0D0),
            composer = Color(0xFF11141D),
            sidebar = Color(0xFF080A10),
            card = Color(0xFF141824),
            border = Color.White.copy(alpha = 0.10f),
            userBubble = SoftBlue,
            assistantBubble = Color.White.copy(alpha = 0.08f),
            userText = Color.Black
        )
    } else {
        ChatColors(
            background = Color(0xFFF7F8FB),
            text = Color(0xFF111827),
            muted = Color(0xFF5D6678),
            composer = Color.White,
            sidebar = Color.White,
            card = Color(0xFFF0F2F6),
            border = Color.Black.copy(alpha = 0.10f),
            userBubble = Color(0xFF111827),
            assistantBubble = Color(0xFFE9EDF5),
            userText = Color.White
        )
    }

private fun providerStatusText(state: ChatUiState): String =
    when (state.apiStatus) {
        ApiStatus.Idle -> "Paste a key to detect provider"
        ApiStatus.Detected -> "Detected ${state.detectedProvider?.name}"
        ApiStatus.Active -> "Active: ${state.activeProvider?.name} • ${state.activeModel}"
        ApiStatus.Invalid -> "Unknown key format"
    }

private fun providerStatusColor(state: ChatUiState, colors: ChatColors): Color =
    when (state.apiStatus) {
        ApiStatus.Active -> SoftBlue
        ApiStatus.Invalid -> Color(0xFFFF6B6B)
        else -> colors.muted
    }
