package zone.ien.taptargetcmp

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.abs

private const val DEBUG = false

/** 탭 타깃과 오버레이 콘텐츠 사이의 여백. */
private val TARGET_PADDING = 20.dp

/** 텍스트 블록의 가로 여백. */
private val TEXT_HORIZONTAL_MARGIN = 40.dp

/** 텍스트 블록의 세로 여백. */
internal val TEXT_VERTICAL_MARGIN = 12.dp

/** 바깥 원과 텍스트 사이의 여백. */
private val OUTER_CIRCLE_INTERNAL_MARGIN = 12.dp

/** 텍스트 블록 위에 예약하는 아이콘 영역. */
private val ICON_CONTAINER_SIZE = 72.dp

/** 아이콘 영역과 텍스트 블록 사이의 간격. */
internal val ICON_TEXT_SPACING = 12.dp

/** 탭 타깃 아이콘을 그릴 때 사용하는 크기. */
private val ICON_SIZE = 48.dp

/** 텍스트의 최대 너비. */
private val MAX_TEXT_WIDTH = 360f.dp

/** 제목과 설명 텍스트 사이의 간격. */
internal val TEXT_SPACING = 8.dp

/** 스킵 버튼과 설명 사이의 간격. */
internal val SKIP_BUTTON_TOP_SPACING = 12.dp

/** 스킵 버튼 영역의 최소 높이. */
private val SKIP_BUTTON_HEIGHT = 48.dp

private fun Dp.toPx(density: Density) = with(density) { toPx() }

/** Composable responsible for drawing the tap target. */
@Composable
internal fun TapTarget(
    tapTarget: TapTarget,
    animationKey: Any?,
    skipButton: (@Composable BoxScope.(onSkip: () -> Unit) -> Unit)?,
    onSkip: () -> Unit,
    onComplete: () -> Unit,
) {
    val density = LocalDensity.current
    val containerSize = LocalWindowInfo.current.containerSize
    var canvasCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val screenSizePx = canvasCoordinates?.let { coordinates ->
        Size(coordinates.size.width.toFloat(), coordinates.size.height.toFloat())
    } ?: Size(
        containerSize.width.toFloat(),
        containerSize.height.toFloat()
    )

    var lastTargetBounds by remember { mutableStateOf(Rect.Zero) }
    // For moving targets, the coordinates can change, use a function
    // to always get the latest.
    val getTargetBoundsPx = {
        val targetCoordinates = tapTarget.coordinates
        val canvas = canvasCoordinates
        if (targetCoordinates.isAttached && canvas?.isAttached == true) {
            val bounds = targetBoundsInCanvas(
                targetBoundsInWindow = targetCoordinates.boundsInWindow(),
                canvasBoundsInWindow = canvas.boundsInWindow(),
            )
            lastTargetBounds = bounds
            bounds
        } else if (targetCoordinates.isAttached) {
            val bounds = targetCoordinates.boundsInWindow()
            lastTargetBounds = bounds
            bounds
        } else {
            lastTargetBounds
        }
    }
    val getTargetCenterPx = {
        getTargetBoundsPx().center
    }

    val targetMaxDimensionPx = remember(tapTarget) {
        max(tapTarget.coordinates.size.width, tapTarget.coordinates.size.height)
    }
    val targetRadiusPx = targetMaxDimensionPx / 2 + TARGET_PADDING.toPx(density)
    val targetBounds = getTargetBoundsPx()

    // Whether we are animating in or out.
    var animateIn by remember { mutableStateOf(true) }
    // Whether the user clicked the target.
    var targetClicked by remember { mutableStateOf(false) }
    // Whether the user clicked outside the target.
    var targetCancelled by remember { mutableStateOf(false) }

    val outerCircleScaleAnimatable = remember { Animatable(0f) }
    val textAlphaScaleAnimatable = remember { Animatable(0f) }
    var lastTargetTransform by remember { mutableStateOf(Matrix()) }

    val getTargetTransform = {
        val canvas = canvasCoordinates
        if (tapTarget.targetLayerCoordinates.isAttached && canvas?.isAttached == true) {
            localToCanvasMatrix(
                coordinates = tapTarget.targetLayerCoordinates,
                canvasCoordinates = canvas,
            ).also { lastTargetTransform = it }
        } else {
            lastTargetTransform
        }
    }

    if (targetClicked) {
        // The user tapped the target, notify the target.
        tapTarget.onTargetClick()
        targetClicked = false
    }

    if (
    // We have completed the current target, and are now animating out.
        !animateIn &&
        // The outer circle has finished collapsing.
        outerCircleScaleAnimatable.value <= 0f
    ) {
        // Animate out is complete. We are now switching to the next target and should animate in.
        animateIn = true

        if (targetCancelled) {
            // The user tapped outside the target, notify the target.
            tapTarget.onTargetCancel()
            targetCancelled = false
        }

        // The animation is complete, notify that we are ready for the next target.
        onComplete()
    }

    if (animateIn) {
        AnimateIn(
            key1 = animationKey,
            outerCircleAnimatable = outerCircleScaleAnimatable,
            textAlphaAnimatable = textAlphaScaleAnimatable
        )
    } else {
        AnimateOut(
            key1 = animationKey,
            outerCircleAnimatable = outerCircleScaleAnimatable,
            textAlphaAnimatable = textAlphaScaleAnimatable
        )
    }

    val maxTextWidthPx = MAX_TEXT_WIDTH.toPx(density)
    val textHorizontalMarginPx = TEXT_HORIZONTAL_MARGIN.toPx(density)
    val textVerticalMarginPx = TEXT_VERTICAL_MARGIN.toPx(density)

    val textWidthPx = max(
        0f,
        min(screenSizePx.width, maxTextWidthPx) - textHorizontalMarginPx * 2
    )

    val constraints = Constraints.fixedWidth(textWidthPx.toInt())
    val textMeasurer = rememberTextMeasurer()
    val titleMeasure = tapTarget.title.rememberMeasure(textMeasurer, constraints)
    val descriptionMeasure = tapTarget.description.rememberMeasure(textMeasurer, constraints)

    val textBlockHeightPx = titleMeasure.size.height +
            descriptionMeasure.size.height +
            TEXT_SPACING.toPx(density)

    val iconContainerHeightPx = if (tapTarget.icon != null) {
        ICON_CONTAINER_SIZE.toPx(density) + ICON_TEXT_SPACING.toPx(density)
    } else {
        0f
    }
    val skipButtonContainerHeightPx = if (skipButton != null) {
        SKIP_BUTTON_TOP_SPACING.toPx(density) + SKIP_BUTTON_HEIGHT.toPx(density)
    } else {
        0f
    }
    val contentBlockHeightPx = textBlockHeightPx + iconContainerHeightPx + skipButtonContainerHeightPx
    val contentTopLeft = getTextBlockOffset(
        Size(textWidthPx, contentBlockHeightPx),
        screenSizePx,
        targetBounds,
        textHorizontalMarginPx,
        textVerticalMarginPx
    )
    val textBlockTopLeft = contentTopLeft + Offset(0f, iconContainerHeightPx)
    val skipButtonTopLeft = textBlockTopLeft + Offset(
        x = 0f,
        y = textBlockHeightPx + SKIP_BUTTON_TOP_SPACING.toPx(density),
    )
    val textBlockRect = Rect(
        textBlockTopLeft.x,
        textBlockTopLeft.y,
        textBlockTopLeft.x + textWidthPx,
        textBlockTopLeft.y + textBlockHeightPx
    )

    val contentBlockRect = Rect(
        contentTopLeft.x,
        contentTopLeft.y,
        contentTopLeft.x + textWidthPx,
        contentTopLeft.y + contentBlockHeightPx
    )
    val topLeftRadius = contentBlockRect.topLeft.distanceTo(getTargetCenterPx())
    val topRightRadius = contentBlockRect.topRight.distanceTo(getTargetCenterPx())
    val bottomLeftRadius = contentBlockRect.bottomLeft.distanceTo(getTargetCenterPx())
    val bottomRightRadius = contentBlockRect.bottomRight.distanceTo(getTargetCenterPx())
    val maxRadius = max(
        topLeftRadius,
        topRightRadius,
        bottomLeftRadius,
        bottomRightRadius
    )

    val outerCircleRadiusPx = maxRadius + OUTER_CIRCLE_INTERNAL_MARGIN.toPx(density)

    Overlay(animationKey) {
        TapTargetRenderer(
            tapTarget,
            onTargetCancel = {
                targetCancelled = true
                animateIn = false
            },
            onTargetClick = {
                targetClicked = true
                animateIn = false
            },
            textAlphaProvider = { textAlphaScaleAnimatable.value },
            getTargetCenter = getTargetCenterPx,
            getTargetTransform = getTargetTransform,
            outerCircleScaleProvider = { outerCircleScaleAnimatable.value },
            targetRadius = targetRadiusPx,
            outerCircleRadius = outerCircleRadiusPx,
            iconTopLeft = if (tapTarget.icon != null) contentTopLeft else null,
            iconContainerSizePx = ICON_CONTAINER_SIZE.toPx(density),
            textBlockTopLeft = textBlockTopLeft,
            textBlockWidth = textWidthPx,
            skipButton = skipButton,
            onSkip = onSkip,
            skipButtonTopLeft = skipButtonTopLeft,
            skipButtonWidth = with(density) { textWidthPx.toDp() },
            titleMeasure = titleMeasure,
            descriptionMeasure = descriptionMeasure,
            textBlockRect = textBlockRect,
            canvasCoordinates = canvasCoordinates,
            onCanvasPositioned = { canvasCoordinates = it },
        )
    }
}

/** Component that draws the tap target. */
@Composable
private fun TapTargetRenderer(
    tapTarget: TapTarget,
    onTargetClick: () -> Unit,
    onTargetCancel: () -> Unit,
    getTargetCenter: () -> Offset,
    getTargetTransform: () -> Matrix,
    outerCircleScaleProvider: () -> Float,
    textAlphaProvider: () -> Float,
    targetRadius: Float,
    outerCircleRadius: Float,
    iconTopLeft: Offset?,
    iconContainerSizePx: Float,
    textBlockTopLeft: Offset,
    textBlockWidth: Float,
    skipButton: (@Composable BoxScope.(onSkip: () -> Unit) -> Unit)?,
    onSkip: () -> Unit,
    skipButtonTopLeft: Offset,
    skipButtonWidth: Dp,
    titleMeasure: TextLayoutResult,
    descriptionMeasure: TextLayoutResult,
    textBlockRect: Rect,
    canvasCoordinates: LayoutCoordinates?,
    onCanvasPositioned: (LayoutCoordinates) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned(onCanvasPositioned)
                .pointerInput(tapTarget, canvasCoordinates) {
                    detectTapGestures { tapOffset ->
                        when {
                            tapOffset.isOutsideCircle(getTargetCenter(), outerCircleRadius) -> {
                                // The user clicked outside the target
                                onTargetCancel()
                            }

                            tapOffset.isInsideCircle(getTargetCenter(), targetRadius) -> {
                                // The user clicked the target
                                onTargetClick()
                            }
                        }
                    }
                }
        ) {
            drawCircle(
                center = getTargetCenter(),
                radius = outerCircleRadius * outerCircleScaleProvider(),
                color = tapTarget.style.backgroundColor,
                alpha = tapTarget.style.backgroundAlpha,
            )

            withTransform({
                transform(getTargetTransform())
            }) {
                drawLayer(tapTarget.targetLayer)
            }

            drawText(
                textLayoutResult = titleMeasure,
                topLeft = textBlockTopLeft,
                alpha = textAlphaProvider().pow(2)
            )
            drawText(
                textLayoutResult = descriptionMeasure,
                topLeft = textBlockTopLeft.plus(
                    Offset(x = 0f, y = titleMeasure.size.height + TEXT_SPACING.toPx())
                ),
                alpha = textAlphaProvider().pow(2)
            )

            if (DEBUG) {
                // Draw the text block rect to see text bounds.
                drawRect(
                    color = Color.Black.copy(alpha = 0.4f),
                    topLeft = textBlockRect.topLeft,
                    size = Size(textBlockRect.width, textBlockRect.height)
                )
            }
        }

        val icon = tapTarget.icon
        if (iconTopLeft != null && icon != null) {
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = (iconTopLeft.x + (textBlockWidth - iconContainerSizePx) / 2)
                                .roundToInt(),
                            y = iconTopLeft.y.roundToInt(),
                        )
                    }
                    .size(ICON_CONTAINER_SIZE)
                    .graphicsLayer { alpha = textAlphaProvider().pow(2) },
                contentAlignment = androidx.compose.ui.Alignment.Center,
            ) {
                val iconContent: @Composable () -> Unit = {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = tapTarget.style.tapTargetHighlightColor,
                        modifier = Modifier.size(ICON_SIZE),
                    )
                }
                tapTarget.iconWrapper?.invoke(iconContent) ?: iconContent()
            }
        }

        if (skipButton != null) {
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = skipButtonTopLeft.x.roundToInt(),
                            y = skipButtonTopLeft.y.roundToInt(),
                        )
                    }
                    .size(
                        width = skipButtonWidth,
                        height = SKIP_BUTTON_HEIGHT,
                    )
                    .graphicsLayer { alpha = textAlphaProvider().pow(2) },
            ) {
                skipButton.invoke(this, onSkip)
            }
        }
    }
}

internal fun targetBoundsInCanvas(
    targetBoundsInWindow: Rect,
    canvasBoundsInWindow: Rect,
): Rect {
    val canvasOrigin = canvasBoundsInWindow.topLeft
    return Rect(
        left = targetBoundsInWindow.left - canvasOrigin.x,
        top = targetBoundsInWindow.top - canvasOrigin.y,
        right = targetBoundsInWindow.right - canvasOrigin.x,
        bottom = targetBoundsInWindow.bottom - canvasOrigin.y,
    )
}

internal fun localToCanvasMatrix(
    coordinates: LayoutCoordinates,
    canvasCoordinates: LayoutCoordinates,
): Matrix {
    val canvasOrigin = canvasCoordinates.boundsInWindow().topLeft
    return localToWindowMatrix(
        origin = coordinates.localToWindow(Offset.Zero) - canvasOrigin,
        xAxis = coordinates.localToWindow(Offset(1f, 0f)) - canvasOrigin,
        yAxis = coordinates.localToWindow(Offset(0f, 1f)) - canvasOrigin,
    )
}

internal fun localToWindowMatrix(coordinates: LayoutCoordinates): Matrix {
    val origin = coordinates.localToWindow(Offset.Zero)
    val xAxis = coordinates.localToWindow(Offset(1f, 0f))
    val yAxis = coordinates.localToWindow(Offset(0f, 1f))
    return localToWindowMatrix(origin, xAxis, yAxis)
}

internal fun localToWindowMatrix(
    origin: Offset,
    xAxis: Offset,
    yAxis: Offset,
): Matrix = Matrix().apply {
    this[0, 0] = xAxis.x - origin.x
    this[0, 1] = xAxis.y - origin.y
    this[1, 0] = yAxis.x - origin.x
    this[1, 1] = yAxis.y - origin.y
    this[3, 0] = origin.x
    this[3, 1] = origin.y
}

/**
 * Calculates and returns the top left coordinates of the text block.
 * @param textBlockSize The size of the text block.
 * @param targetBounds The target bounds in the window.
 * @param horizontalMargin The horizontal margin between the text block and the screen edge.
 * @param verticalMargin The vertical margin between the target and the content block.
 */
// TODO(issue#3) the entire screen size is used to position the text block,
//  therefore it might overlap the status bar.
internal fun getTextBlockOffset(
    textBlockSize: Size,
    screenSize: Size,
    targetBounds: Rect,
    horizontalMargin: Float,
    verticalMargin: Float
): Offset {
    val centeredX = (screenSize.width - textBlockSize.width) / 2
    val maxX = max(horizontalMargin, screenSize.width - horizontalMargin - textBlockSize.width)
    val xOffset = centeredX.coerceIn(horizontalMargin, maxX)

    // The Y coordinate of the text block, if positioned above the target.
    val yTop = targetBounds.top - textBlockSize.height - verticalMargin
    // The Y coordinate of the text block, if positioned below the target.
    val yBottom = targetBounds.bottom + verticalMargin

    val maxYOffset = (screenSize.height - textBlockSize.height).coerceAtLeast(0f)
    val validYOffsets = listOf(yTop, yBottom).filter { it in 0f..maxYOffset }
    val yOffset = if (validYOffsets.isNotEmpty()) {
        val screenCenterY = screenSize.height / 2
        validYOffsets.minBy { abs(it + textBlockSize.height / 2 - screenCenterY) }
    } else {
        val preferredYOffset = if (yTop > 0) yTop else yBottom
        preferredYOffset.coerceIn(0f, maxYOffset)
    }

    return Offset(xOffset, yOffset)
}

@Composable
private fun AnimateIn(
    key1: Any?,
    outerCircleAnimatable: Animatable<Float, AnimationVector1D>,
    textAlphaAnimatable: Animatable<Float, AnimationVector1D>
) {
    LaunchedEffect(key1) {
        // Outer circle
        launch {
            outerCircleAnimatable.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 300,
                    easing = FastOutSlowInEasing,
                ),
            )
        }

        // Text alpha
        launch {
            textAlphaAnimatable.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    delayMillis = 200,
                    durationMillis = 300,
                    easing = FastOutSlowInEasing,
                ),
            )
        }
    }
}

@Composable
private fun AnimateOut(
    key1: Any?,
    outerCircleAnimatable: Animatable<Float, AnimationVector1D>,
    textAlphaAnimatable: Animatable<Float, AnimationVector1D>
) {
    LaunchedEffect(key1) {
        // Outer circle
        launch {
            outerCircleAnimatable.animateTo(
                targetValue = 0f,
                animationSpec = tween(
                    durationMillis = 300,
                    easing = FastOutSlowInEasing,
                ),
            )
        }

        // Text alpha
        launch {
            textAlphaAnimatable.animateTo(
                targetValue = 0f,
                animationSpec = tween(
                    durationMillis = 300,
                    easing = FastOutSlowInEasing,
                ),
            )
        }
    }
}

/**
 * A composable that is drawn as an overlay over the entire screen.
 * @param key The key used to know when to remove the overlay.
 * @param content The content of the overlay.
 */

@Composable
internal fun Overlay(
    key: Any?,
    content: @Composable () -> Unit
) {
    // key가 변경되면 Popup이 재구성됨
    key(key) {
        Popup(
            properties = PopupProperties(
                focusable = true,
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
                clippingEnabled = false,
            )
        ) {
            content()
        }
    }
}

@Composable
private fun TextDefinition.rememberMeasure(
    textMeasurer: TextMeasurer,
    constraints: Constraints
): TextLayoutResult {
    return remember(
        text,
        constraints,
        style.color,
        style.fontSize,
        style.fontWeight,
        style.textAlign,
        style.lineHeight,
        style.fontFamily,
        style.textDecoration,
        style.fontStyle,
        style.letterSpacing
    ) {
        textMeasurer.measure(
            AnnotatedString(text),
            constraints = constraints,
            style = TextStyle(
                color = style.color,
                fontSize = style.fontSize,
                fontWeight = style.fontWeight,
                textAlign = style.textAlign.takeUnless { it == TextAlign.Unspecified } ?: TextAlign.Center,
                lineHeight = style.lineHeight,
                fontFamily = style.fontFamily,
                textDecoration = style.textDecoration,
                fontStyle = style.fontStyle,
                letterSpacing = style.letterSpacing
            )
        )
    }
}
