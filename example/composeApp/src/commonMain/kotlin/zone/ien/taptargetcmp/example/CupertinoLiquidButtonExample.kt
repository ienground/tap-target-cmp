package zone.ien.taptargetcmp.example

import androidx.compose.animation.Animatable as ColorAnimatable
import androidx.compose.animation.core.Animatable as FloatAnimatable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCoerceAtMost
import androidx.compose.ui.util.lerp
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import zone.ien.hig.CupertinoButtonSize
import zone.ien.hig.CupertinoLiquidButtonColors
import zone.ien.hig.CupertinoLiquidButtonDefaults.glassButtonColors
import zone.ien.hig.ExperimentalCupertinoApi
import zone.ien.hig.LocalContentColor
import zone.ien.hig.theme.CupertinoTheme
import zone.ien.hig.utils.InteractiveHighlight
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sign
import kotlin.math.sin
import kotlin.math.tanh

@OptIn(ExperimentalCupertinoApi::class)
@Composable
internal fun CupertinoLiquidButtonExample(
    text: String,
    modifier: Modifier = Modifier,
    backdrop: LayerBackdrop,
    onClick: () -> Unit,
) {
//    CupertinoLiquidButton(
//        onClick = onClick,
//        modifier = modifier,
//        isInteractive = false,
//        isBackgroundAdaptive = false,
//        backdrop = backdrop,
//    ) {
//        Text(text)
//    }

    val enabled: Boolean = true
    val size: CupertinoButtonSize = CupertinoButtonSize.Regular
    val colors: CupertinoLiquidButtonColors = glassButtonColors()
    val shape: Shape = size.shape(CupertinoTheme.shapes)
    val contentPadding: PaddingValues = size.contentPadding
    val interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
    val isBackgroundAdaptive: Boolean = true
    val isInteractive: Boolean = true

    val animationScope = rememberCoroutineScope()

    val tintColor by colors.tintColor(enabled)
    val surfaceColor by colors.surfaceColor(enabled)
    val contentColor by colors.contentColor(enabled)

    val lightTintColor by colors.tintColor(enabled, isDark = false)
    val lightSurfaceColor by colors.surfaceColor(enabled, isDark = false)
    val lightContentColor by colors.contentColor(enabled, isDark = false)
    val darkTintColor by colors.tintColor(enabled, isDark = true)
    val darkSurfaceColor by colors.surfaceColor(enabled, isDark = true)
    val darkContentColor by colors.contentColor(enabled, isDark = true)

    val interactiveHighlight = remember(animationScope) { InteractiveHighlight(animationScope = animationScope) }

    val isLightTheme = !isSystemInDarkTheme()
    val graphicsLayer = rememberGraphicsLayer()
    val targetGraphicsLayer = rememberGraphicsLayer()

    val luminanceAnimation = remember(enabled) { FloatAnimatable(if (isLightTheme) 1f else 0f) }
    val tintColorAnimation = remember(enabled) { ColorAnimatable(if (isLightTheme) lightTintColor else darkTintColor) }
    val surfaceColorAnimation = remember(enabled) { ColorAnimatable(if (isLightTheme) lightSurfaceColor else darkSurfaceColor) }
    val contentColorAnimation = remember(enabled) { ColorAnimatable(if (isLightTheme) lightContentColor else darkContentColor) }

    Box(modifier = Modifier.fillMaxWidth()) {
        Box(modifier = modifier.height(48.dp)) {
            Canvas(Modifier.fillMaxSize()) {
                drawLayer(targetGraphicsLayer)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .drawWithContent {
                    targetGraphicsLayer.record {
                        this@drawWithContent.drawContent()
                    }
                    drawContent()
                }
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = { shape },
                    effects = {
                        val l = (luminanceAnimation.value * 2f - 1f).let { sign(it) * it * it }
                        vibrancy()
                        if (isBackgroundAdaptive) {
                            blur(
                                if (l > 0f) lerp(8.dp.toPx(), 16.dp.toPx(), l)
                                else lerp(8.dp.toPx(), 2.dp.toPx(), -l)
                            )
                        } else {
                            blur(2.dp.toPx())
                        }
                        lens(12.dp.toPx(), 24.dp.toPx())
                    },
                    layerBlock = if (enabled && isInteractive) {
                        {
                            val width = this.size.width
                            val height = this.size.height

                            val progress = interactiveHighlight.pressProgress
                            val scale = lerp(1f, 1f + 4.dp.toPx() / height, progress)

                            val maxOffset = this.size.minDimension
                            val initialDerivative = 0.05f
                            val offset = interactiveHighlight.offset

                            translationX = maxOffset * tanh(initialDerivative * offset.x / maxOffset)
                            translationY = maxOffset * tanh(initialDerivative * offset.y / maxOffset)

                            val maxDragScale = 4.dp.toPx() / height
                            val offsetAngle = atan2(offset.y, offset.x)

                            scaleX = scale + maxDragScale * abs(cos(offsetAngle) * offset.x / this.size.maxDimension) * (width / height).fastCoerceAtMost(1f)
                            scaleY = scale + maxDragScale * abs(sin(offsetAngle) * offset.y / this.size.maxDimension) * (height / width).fastCoerceAtMost(1f)
                        }
                    } else {
                        null
                    },
                    onDrawSurface = {
                        if (isBackgroundAdaptive) {
                            if (tintColorAnimation.value.isSpecified) {
                                drawRect(tintColorAnimation.value, blendMode = BlendMode.Hue)
                                drawRect(tintColorAnimation.value.copy(alpha = 0.75f))
                            }
                            if (surfaceColorAnimation.value.isSpecified) {
                                drawRect(surfaceColorAnimation.value)
                            }
                        } else {
                            if (tintColor.isSpecified) {
                                drawRect(tintColor, blendMode = BlendMode.Hue)
                                drawRect(tintColor.copy(alpha = 0.75f))
                            }
                            if (surfaceColor.isSpecified) {
                                drawRect(surfaceColor)
                            }
                        }
                    },
                    onDrawBackdrop = { drawBackdrop ->
                        drawBackdrop()
                        graphicsLayer.record { drawBackdrop() }
                    }
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = if (enabled) LocalIndication.current else null,
                    enabled = enabled,
                    role = Role.Button,
                    onClick = onClick
                )
                .then(
                    if (enabled) {
                        Modifier
                            .then(interactiveHighlight.modifier)
                            .then(interactiveHighlight.gestureModifier)
                    } else {
                        Modifier
                    }
                )
                .height(48.dp)
                .padding(contentPadding),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CompositionLocalProvider(
                LocalContentColor provides if (isBackgroundAdaptive) contentColorAnimation.value else contentColor,
            ) {
                Text(text = text)
            }
        }

    }
}
