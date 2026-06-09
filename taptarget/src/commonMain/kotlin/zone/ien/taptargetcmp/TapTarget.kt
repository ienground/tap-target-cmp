package zone.ien.taptargetcmp

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TapTargetCoordinator(
    showTapTargets: Boolean,
    modifier: Modifier = Modifier,
    onComplete: () -> Unit = { },
    onTargetChanged: (Int) -> Unit = {},
    state: TapTargetCoordinatorState = remember { TapTargetCoordinatorState() },
    contentAlignment: Alignment = Alignment.Center,
    bringIntoViewVerticalOffset: Dp = 0.dp,
    content: @Composable TapTargetScope.() -> Unit,
) {
    val tapTargetScope = remember(state, bringIntoViewVerticalOffset) { TapTargetScope(state, bringIntoViewVerticalOffset) }

    val density = LocalDensity.current

    LaunchedEffect(state.currentTargetIndex) {
        val target = state.currentTarget
        if (target != null) {
            onTargetChanged(target.precedence)
            if (target.bringIntoViewEnabled) {
                val offsetPx = with(density) { target.bringIntoViewVerticalOffset.toPx() }
                val size = target.coordinates.size
                target.bringIntoViewRequester.bringIntoView(
                    Rect(
                        offset = Offset(0f, -offsetPx),
                        size = Size(size.width.toFloat(), size.height.toFloat())
                    )
                )
            }
        }
    }

    CompositionLocalProvider(LocalTapTargetScope provides tapTargetScope) {
        Box(
            contentAlignment = contentAlignment,
            modifier = modifier
        ) {
            tapTargetScope.content()

            if (showTapTargets) {
                val currentTapTarget = state.currentTarget
                if (currentTapTarget != null) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        TapTarget(
                            tapTarget = currentTapTarget,
                            onComplete = {
                                state.currentTargetIndex++
                                if (state.currentTargetIndex >= state.tapTargets.size) {
                                    onComplete()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

class TapTargetScope internal constructor(
    private val state: TapTargetCoordinatorState,
    internal val defaultBringIntoViewVerticalOffset: Dp = 0.dp,
) {

    @OptIn(ExperimentalFoundationApi::class)
    fun Modifier.tapTarget(
        title: TextDefinition,
        description: TextDefinition,
        precedence: Int,
        tapTargetStyle: TapTargetStyle = TapTargetStyle.Default,
        onTargetClick: () -> Unit = { },
        onTargetCancel: () -> Unit = { },
        bringIntoViewEnabled: Boolean = true,
        bringIntoViewVerticalOffset: Dp? = null,
        bringIntoViewRequester: BringIntoViewRequester? = null,
    ): Modifier = composed {
        val requester = bringIntoViewRequester ?: remember { BringIntoViewRequester() }
        val offset = bringIntoViewVerticalOffset ?: defaultBringIntoViewVerticalOffset
        onGloballyPositioned { layoutCoordinates ->
            state.tapTargets[precedence] = TapTarget(
                precedence = precedence,
                coordinates = layoutCoordinates,
                title = title,
                description = description,
                style = tapTargetStyle,
                onTargetClick = onTargetClick,
                onTargetCancel = onTargetCancel,
                bringIntoViewEnabled = bringIntoViewEnabled,
                bringIntoViewVerticalOffset = offset,
                bringIntoViewRequester = requester,
            )
        }
            .bringIntoViewRequester(requester)
    }

    fun Modifier.tapTarget(tapTargetDefinition: TapTargetDefinition): Modifier {
        return tapTarget(
            tapTargetDefinition.title,
            tapTargetDefinition.description,
            tapTargetDefinition.precedence,
            tapTargetDefinition.tapTargetStyle,
            tapTargetDefinition.onTargetClick,
            tapTargetDefinition.onTargetCancel,
            tapTargetDefinition.bringIntoViewEnabled,
            tapTargetDefinition.bringIntoViewVerticalOffset,
        )
    }
}

val LocalTapTargetScope = staticCompositionLocalOf<TapTargetScope?> { null }

fun Modifier.ifTapTarget(definition: TapTargetDefinition?): Modifier = composed {
    val scope = LocalTapTargetScope.current
    if (scope != null && definition != null) {
        with(scope) { this@composed.tapTarget(definition) }
    } else {
        this
    }
}

data class TapTargetDefinition(
    val title: TextDefinition,
    val description: TextDefinition,
    val precedence: Int,
    val tapTargetStyle: TapTargetStyle = TapTargetStyle.Default,
    val onTargetClick: () -> Unit = { },
    val onTargetCancel: () -> Unit = { },
    val bringIntoViewEnabled: Boolean = true,
    val bringIntoViewVerticalOffset: Dp? = null,
)

class TapTargetCoordinatorState internal constructor() {
    internal val tapTargets = mutableStateMapOf<Int, TapTarget>()
    internal var currentTargetIndex by mutableIntStateOf(0)
    val currentTarget: TapTarget?
        get() = tapTargets.keys.sorted().getOrNull(currentTargetIndex)?.let { tapTargets[it] }
}

class TapTarget internal constructor(
    val precedence: Int,
    val title: TextDefinition,
    val description: TextDefinition,
    val coordinates: LayoutCoordinates,
    val style: TapTargetStyle = TapTargetStyle.Default,
    val onTargetClick: () -> Unit,
    val onTargetCancel: () -> Unit,
    val bringIntoViewEnabled: Boolean = true,
    val bringIntoViewVerticalOffset: Dp = 0.dp,
    @Suppress("EXPERIMENTAL_API_USAGE")
    val bringIntoViewRequester: BringIntoViewRequester = BringIntoViewRequester(),
)

data class TextDefinition(
    val text: String,
    internal val textStyle: TextStyle = TextStyle.Default,
    internal val color: Color = Color.Unspecified,
    internal val fontSize: TextUnit = TextUnit.Unspecified,
    internal val fontStyle: FontStyle? = null,
    internal val fontWeight: FontWeight? = null,
    internal val fontFamily: FontFamily? = null,
    internal val letterSpacing: TextUnit = TextUnit.Unspecified,
    internal val textDecoration: TextDecoration? = null,
    internal val textAlign: TextAlign? = null,
    internal val lineHeight: TextUnit = TextUnit.Unspecified,
) {
    val style = textStyle.merge(
        TextStyle(
            color = color,
            fontSize = fontSize,
            fontWeight = fontWeight,
            textAlign = textAlign ?: TextAlign.Unspecified,
            lineHeight = lineHeight,
            fontFamily = fontFamily,
            textDecoration = textDecoration,
            fontStyle = fontStyle,
            letterSpacing = letterSpacing
        )
    )
}

data class TapTargetStyle(
    val backgroundColor: Color = Color.Blue,
    val backgroundAlpha: Float = 1f,
    val tapTargetHighlightColor: Color = Color.White,
) {
    companion object {
        val Default = TapTargetStyle()
    }
}