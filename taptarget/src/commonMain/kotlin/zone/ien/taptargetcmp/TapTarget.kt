package zone.ien.taptargetcmp

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
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

/**
 * Top-level composable that orchestrates tap target discovery overlays.
 *
 * Wrap your content inside this composable and mark elements with [TapTargetScope.tapTarget]
 * to show tap targets over them.
 *
 * @param showTapTargets Whether to show the tap targets overlay.
 * @param modifier Modifier applied to the root container.
 * @param onComplete Called when all tap targets have been dismissed.
 * @param onTargetChanged Called with the precedence of the newly active target.
 * @param state Mutable state holder for the coordinator. Useful for resetting or inspecting state.
 * @param contentAlignment Alignment of the content within the coordinator.
 * @param bringIntoViewVerticalOffset Default vertical offset applied when scrolling a target into view. Can be overridden per target.
 * @param skipButton 활성 타깃 콘텐츠에 표시할 선택적 컴포저블. 전달받은 콜백을 호출하면 남은 타깃을
 *   건너뛰고 [onComplete]를 호출한다. [BoxScope] 수신자를 통해 [Modifier.align]으로 위치를 지정할 수 있다.
 * @param content The composable content that contains tap-target-marked elements.
 */
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
    skipButton: (@Composable BoxScope.(onSkip: () -> Unit) -> Unit)? = null,
    content: @Composable TapTargetScope.() -> Unit,
) {
    val tapTargetScope = remember(state, bringIntoViewVerticalOffset) { TapTargetScope(state, bringIntoViewVerticalOffset) }
    val density = LocalDensity.current
    var completionHandled by remember(state) { mutableStateOf(false) }
    val complete = {
        if (!completionHandled) {
            completionHandled = true
            onComplete()
        }
    }

    LaunchedEffect(state.currentTargetIndex) {
        val target = state.currentTarget
        if (target != null) {
            onTargetChanged(target.precedence)
            if (target.bringIntoViewEnabled && target.coordinates.isAttached) {
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

            // 원본 위젯 위에 오버레이를 한 번만 그린다. 타깃 위젯 자체는 오버레이에서 다시 그린다.
            if (showTapTargets) {
                val currentTapTarget = state.currentTarget
                if (currentTapTarget != null) {
                    TapTarget(
                        tapTarget = currentTapTarget,
                        animationKey = state.currentTargetIndex,
                        skipButton = skipButton,
                        onSkip = {
                            if (state.skipToEnd()) {
                                complete()
                            }
                        },
                        onComplete = {
                            state.currentTargetIndex++
                            if (state.currentTargetIndex >= state.tapTargets.size) {
                                complete()
                            }
                        }
                    )
                }
            }
        }
    }
}

/**
 * Scope receiver for [TapTargetCoordinator] content, providing the [Modifier.tapTarget] extensions
 * to mark composables as tap target anchors.
 */
class TapTargetScope internal constructor(
    private val state: TapTargetCoordinatorState,
    internal val defaultBringIntoViewVerticalOffset: Dp = 0.dp,
) {

    /**
     * Marks this composable as a tap target anchor.
     *
     * The tap target overlay will point to this element when its [precedence] is active.
     *
     * @param title Title text shown in the overlay.
     * @param description Description text shown in the overlay.
     * @param precedence Deterministic ordering key. Lower values are shown first.
     * @param tapTargetStyle Visual style for the tap target (colors, alpha).
     * @param onTargetClick Called when the user taps the highlighted target area.
     * @param onTargetCancel Called when the user taps outside the target.
     * @param bringIntoViewEnabled Whether to automatically scroll this element into view when it becomes active.
     * @param bringIntoViewVerticalOffset Vertical offset (in Dp) above the element when scrolling into view.
     *   Falls back to [TapTargetScope.defaultBringIntoViewVerticalOffset] when `null`.
     * @param bringIntoViewRequester Optional custom [BringIntoViewRequester]. If `null`, one is created automatically.
     * @param icon 제목과 설명 위에 표시할 선택적 아이콘.
     * @param iconWrapper [icon]을 감싸는 선택적 컴포저블. `null`이면 적용하지 않는다.
     */
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
        icon: ImageVector? = null,
        iconWrapper: (@Composable (content: @Composable () -> Unit) -> Unit)? = null,
    ): Modifier {
        val targetModifier = this
        return Modifier.composed {
            val requester = bringIntoViewRequester ?: remember { BringIntoViewRequester() }
            val targetLayer = rememberGraphicsLayer()
            val offset = bringIntoViewVerticalOffset ?: defaultBringIntoViewVerticalOffset
            Modifier
                .drawWithContent {
                    val contentDrawScope = this@drawWithContent
                    targetLayer.record {
                        contentDrawScope.drawContent()
                    }
                    drawContent()
                }
                .then(targetModifier)
                .onGloballyPositioned { layoutCoordinates ->
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
                        icon = icon,
                        iconWrapper = iconWrapper,
                        targetLayer = targetLayer,
                    )
                }
                .bringIntoViewRequester(requester)
        }
    }

    /**
     * Marks this composable as a tap target anchor using a pre-configured [TapTargetDefinition].
     *
     * @param tapTargetDefinition The definition holding all tap target configuration.
     */
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
            icon = tapTargetDefinition.icon,
            iconWrapper = tapTargetDefinition.iconWrapper,
        )
    }
}

/**
 * CompositionLocal holding the current [TapTargetScope], used by [Modifier.ifTapTarget].
 */
val LocalTapTargetScope = staticCompositionLocalOf<TapTargetScope?> { null }

/**
 * Conditionally applies [TapTargetScope.tapTarget] only when inside a [TapTargetCoordinator]
 * and [definition] is non-null. Otherwise this modifier is a no-op.
 *
 * @param definition The tap target definition, or `null` to skip.
 */
fun Modifier.ifTapTarget(definition: TapTargetDefinition?): Modifier = composed {
    val scope = LocalTapTargetScope.current
    if (scope != null && definition != null) {
        with(scope) { this@composed.tapTarget(definition) }
    } else {
        this
    }
}

/**
 * Pre-configured definition of a tap target, reusable across multiple [TapTargetScope.tapTarget] calls.
 *
 * @param title Title text shown in the overlay.
 * @param description Description text shown in the overlay.
 * @param precedence Deterministic ordering key. Lower values are shown first.
 * @param tapTargetStyle Visual style for the tap target.
 * @param onTargetClick Called when the user taps the highlighted area.
 * @param onTargetCancel Called when the user taps outside the target.
 * @param bringIntoViewEnabled Whether to auto-scroll this element into view when active.
 * @param bringIntoViewVerticalOffset Vertical offset when scrolling into view. `null` uses the coordinator default.
 * @param icon 제목과 설명 위에 표시할 선택적 아이콘.
 * @param iconWrapper [icon]을 감싸는 선택적 컴포저블. `null`이면 적용하지 않는다.
 */
data class TapTargetDefinition(
    val title: TextDefinition,
    val description: TextDefinition,
    val precedence: Int,
    val tapTargetStyle: TapTargetStyle = TapTargetStyle.Default,
    val onTargetClick: () -> Unit = { },
    val onTargetCancel: () -> Unit = { },
    val bringIntoViewEnabled: Boolean = true,
    val bringIntoViewVerticalOffset: Dp? = null,
    val icon: ImageVector? = null,
    val iconWrapper: (@Composable (content: @Composable () -> Unit) -> Unit)? = null,
)

/**
 * Mutable state holder for [TapTargetCoordinator].
 *
 * Useful for inspecting the currently active target or resetting state programmatically.
 *
 * @property currentTarget The currently active [TapTarget], or `null` if none is active.
 */
class TapTargetCoordinatorState internal constructor() {
    internal val tapTargets = mutableStateMapOf<Int, TapTarget>()
    internal var currentTargetIndex by mutableIntStateOf(0)
    val currentTarget: TapTarget?
        get() = tapTargets.keys.sorted().getOrNull(currentTargetIndex)?.let { tapTargets[it] }

    internal fun skipToEnd(): Boolean {
        val skippedIndex = skipTargetIndex(currentTargetIndex, tapTargets.size) ?: return false
        currentTargetIndex = skippedIndex
        return true
    }
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
    val icon: ImageVector? = null,
    val iconWrapper: (@Composable (content: @Composable () -> Unit) -> Unit)? = null,
    internal val targetLayer: GraphicsLayer,
    @Suppress("EXPERIMENTAL_API_USAGE")
    val bringIntoViewRequester: BringIntoViewRequester = BringIntoViewRequester(),
)

/**
 * Text content definition for tap target title and description.
 *
 * Provides a convenient way to style text without creating a full [TextStyle] manually.
 *
 * @param text The text content.
 * @property style The resolved [TextStyle] merging all provided properties into [TextStyle.Default].
 */
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

/**
 * Visual style configuration for a tap target overlay.
 *
 * @param backgroundColor Background color of the outer circle and text block area.
 * @param backgroundAlpha Alpha value for the background color (0.0 – 1.0).
 * @param tapTargetHighlightColor 오버레이의 선택적 아이콘에 적용할 색상.
 */
data class TapTargetStyle(
    val backgroundColor: Color = Color.Blue,
    val backgroundAlpha: Float = 1f,
    val tapTargetHighlightColor: Color = Color.White,
) {
    companion object {
        val Default = TapTargetStyle()
    }
}
