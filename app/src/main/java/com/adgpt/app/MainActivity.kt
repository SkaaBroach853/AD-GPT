package com.adgpt.app

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import com.adgpt.app.presentation.ADGPTApp
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setBackgroundDrawableResource(android.R.color.black)
        enableEdgeToEdge(
            statusBarStyle = androidx.activity.SystemBarStyle.dark(Color.BLACK),
            navigationBarStyle = androidx.activity.SystemBarStyle.dark(Color.BLACK)
        )
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent { ADGPTApp() }
    }
}
