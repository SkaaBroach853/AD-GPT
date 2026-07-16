package com.adgpt.app.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.adgpt.app.presentation.theme.CarbonBlack
import com.adgpt.app.presentation.theme.PanelBlack
import com.adgpt.app.presentation.theme.TextSecondary

@Composable
fun SettingsRoute(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val reduceMotion by viewModel.reduceMotion.collectAsState()
    val playIntroOnStart by viewModel.playIntroOnStart.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CarbonBlack)
            .padding(24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
            }
            Text("Settings", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(24.dp))
        SettingsSwitchCard(
            title = "Play intro on start",
            subtitle = "Shows the startup video before AD-GPT opens.",
            checked = playIntroOnStart,
            onCheckedChange = viewModel::setPlayIntroOnStart
        )
        Spacer(Modifier.height(12.dp))
        SettingsSwitchCard(
            title = "Reduce motion",
            subtitle = "Softens interface animations for accessibility.",
            checked = reduceMotion,
            onCheckedChange = viewModel::setReduceMotion
        )
    }
}

@Composable
private fun SettingsSwitchCard(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = PanelBlack),
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(22.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Medium)
                Text(subtitle, color = TextSecondary)
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}
