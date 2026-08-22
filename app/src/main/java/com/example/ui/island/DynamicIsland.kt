package com.example.ui.island

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.model.IslandEvent
import com.example.data.model.IslandSettings
import com.example.data.model.IslandState

@Composable
fun DynamicIsland(
    event: IslandEvent?,
    state: IslandState,
    settings: IslandSettings,
    onExpand: () -> Unit,
    onCollapse: () -> Unit,
    onDismiss: () -> Unit,
    onToggleMusic: () -> Unit = {},
    onNextMusic: () -> Unit = {},
    onPrevMusic: () -> Unit = {},
    onPauseTimer: () -> Unit = {},
    onResumeTimer: () -> Unit = {},
    onResetTimer: () -> Unit = {},
    onToggleMuteCall: () -> Unit = {},
    onToggleSpeakerCall: () -> Unit = {},
    onEndCall: () -> Unit = {},
    onPauseDownload: () -> Unit = {},
    onResumeDownload: () -> Unit = {},
    onCancelDownload: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (state == IslandState.Hidden) {
        return
    }

    val isExpanded = state == IslandState.Expanded || state == IslandState.Interactive
    val isDismissing = state == IslandState.Dismissing

    // Target Dimensions
    val targetWidth: Dp = when {
        isDismissing -> 0.dp
        isExpanded -> 356.dp
        else -> settings.islandWidthDp.dp
    }

    val targetCornerRadius: Dp = when {
        isExpanded -> 28.dp
        else -> settings.islandCornerRadiusDp.dp
    }

    val animatedWidth by animateDpAsState(
        targetValue = targetWidth,
        animationSpec = IslandSprings.IslandSizeSpring,
        label = "island_width"
    )

    val animatedCornerRadius by animateDpAsState(
        targetValue = targetCornerRadius,
        animationSpec = IslandSprings.IslandSizeSpring,
        label = "island_corner_radius"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isDismissing) 0f else 1f,
        animationSpec = IslandSprings.SnappySpring,
        label = "island_alpha"
    )

    val scale by animateFloatAsState(
        targetValue = if (isDismissing) 0.7f else 1f,
        animationSpec = IslandSprings.SmoothSpring,
        label = "island_scale"
    )

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Surface(
            modifier = Modifier
                .graphicsLayer {
                    this.alpha = alpha
                    this.scaleX = scale
                    this.scaleY = scale
                }
                .width(animatedWidth)
                .then(
                    if (!isExpanded) {
                        Modifier.height(settings.islandHeightDp.dp)
                    } else {
                        Modifier.wrapContentHeight()
                    }
                )
                .shadow(
                    elevation = if (isExpanded) 16.dp else 8.dp,
                    shape = RoundedCornerShape(animatedCornerRadius),
                    spotColor = Color(0x99000000),
                    ambientColor = Color(0x66000000)
                )
                .clip(RoundedCornerShape(animatedCornerRadius))
                .background(Color(0xFF000000))
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0x33FFFFFF),
                            Color(0x11FFFFFF),
                            Color(0x05000000)
                        )
                    ),
                    shape = RoundedCornerShape(animatedCornerRadius)
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) {
                    if (isExpanded) onCollapse() else onExpand()
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        // Swipe up to dismiss
                        if (dragAmount.y < -15f) {
                            onDismiss()
                        } else if (dragAmount.y > 15f && !isExpanded) {
                            onExpand()
                        }
                    }
                }
                .testTag("dynamic_island_container"),
            color = Color(0xFF000000),
            shape = RoundedCornerShape(animatedCornerRadius)
        ) {
            if (isExpanded) {
                ExpandedIsland(
                    event = event,
                    onDismiss = onDismiss,
                    onToggleMusic = onToggleMusic,
                    onNextMusic = onNextMusic,
                    onPrevMusic = onPrevMusic,
                    onPauseTimer = onPauseTimer,
                    onResumeTimer = onResumeTimer,
                    onResetTimer = onResetTimer,
                    onToggleMuteCall = onToggleMuteCall,
                    onToggleSpeakerCall = onToggleSpeakerCall,
                    onEndCall = onEndCall,
                    onPauseDownload = onPauseDownload,
                    onResumeDownload = onResumeDownload,
                    onCancelDownload = onCancelDownload
                )
            } else {
                CompactIsland(event = event)
            }
        }
    }
}
