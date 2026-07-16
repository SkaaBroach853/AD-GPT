package com.adgpt.app.presentation.startup

import android.net.Uri
import androidx.annotation.OptIn
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.launch

private val PremiumEase = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)

@OptIn(UnstableApi::class)
@Composable
fun StartupOverlay(
    videoAssetPath: String?,
    transitionActive: Boolean,
    onVideoFinished: () -> Unit,
    onSkip: () -> Unit
) {
    val context = LocalContext.current
    val overlayAlpha = remember { Animatable(1f) }
    val brightnessAlpha = remember { Animatable(0f) }
    val blur = remember { Animatable(0f) }
    val focusRequester = remember { FocusRequester() }
    val interactionSource = remember { MutableInteractionSource() }
    val player = remember(videoAssetPath) {
        videoAssetPath?.let {
            ExoPlayer.Builder(context)
                .setHandleAudioBecomingNoisy(true)
                .build()
                .apply {
                    setMediaItem(MediaItem.fromUri(Uri.parse("asset:///$it")))
                    playWhenReady = true
                    repeatMode = Player.REPEAT_MODE_OFF
                    prepare()
                }
        }
    }

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) onVideoFinished()
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                onVideoFinished()
            }
        }
        player?.addListener(listener)
        onDispose {
            player?.removeListener(listener)
            player?.release()
        }
    }

    LaunchedEffect(transitionActive) {
        if (transitionActive) {
            val volume = Animatable(player?.volume ?: 1f)
            launch {
                volume.animateTo(0f, tween(300, easing = PremiumEase)) {
                    player?.volume = value
                }
            }
            launch { blur.animateTo(8f, tween(260, easing = PremiumEase)) }
            launch { brightnessAlpha.animateTo(0.28f, tween(240, easing = PremiumEase)) }
            overlayAlpha.animateTo(0f, tween(300, delayMillis = 120, easing = PremiumEase))
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown &&
                    (event.key == Key.Spacebar || event.key == Key.Enter || event.key == Key.Escape)
                ) {
                    onSkip()
                    true
                } else {
                    false
                }
            }
            .clickable(indication = null, interactionSource = interactionSource) { onSkip() }
            .alpha(overlayAlpha.value)
    ) {
        if (player != null) {
            AndroidView(
                factory = {
                    PlayerView(it).apply {
                        useController = false
                        setShutterBackgroundColor(android.graphics.Color.BLACK)
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        this.player = player
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .blur(blur.value.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = brightnessAlpha.value))
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(indication = null, interactionSource = interactionSource) { onSkip() }
        )
    }
}
