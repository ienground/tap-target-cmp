package zone.ien.taptargetcmp

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit

@Composable
fun TapTargetCoordinator(
  showTapTargets: Boolean,
  modifier: Modifier = Modifier,
  onComplete: () -> Unit = { },
  state: TapTargetCoordinatorState = remember { TapTargetCoordinatorState() },
  contentAlignment: Alignment = Alignment.Center,
  content: @Composable TapTargetScope.() -> Unit,
) {
  val scope = remember(state) { TapTargetScope(state) }

  Box(
    contentAlignment = contentAlignment,
    modifier = modifier
  ) {
    scope.content()

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

class TapTargetScope internal constructor(private val state: TapTargetCoordinatorState) {

  fun Modifier.tapTarget(
    title: TextDefinition,
    description: TextDefinition,
    precedence: Int,
    tapTargetStyle: TapTargetStyle = TapTargetStyle.Default,
    onTargetClick: () -> Unit = { },
    onTargetCancel: () -> Unit = { },
  ): Modifier {
    return onGloballyPositioned { layoutCoordinates ->
      state.tapTargets[precedence] = TapTarget(
        precedence = precedence,
        coordinates = layoutCoordinates,
        title = title,
        description = description,
        style = tapTargetStyle,
        onTargetClick = onTargetClick,
        onTargetCancel = onTargetCancel,
      )
    }
  }

  fun Modifier.tapTarget(tapTargetDefinition: TapTargetDefinition): Modifier {
    return tapTarget(
      tapTargetDefinition.title,
      tapTargetDefinition.description,
      tapTargetDefinition.precedence,
      tapTargetDefinition.tapTargetStyle,
      tapTargetDefinition.onTargetClick,
      tapTargetDefinition.onTargetCancel
    )
  }
}

data class TapTargetDefinition(
  val title: TextDefinition,
  val description: TextDefinition,
  val precedence: Int,
  val tapTargetStyle: TapTargetStyle = TapTargetStyle.Default,
  val onTargetClick: () -> Unit = { },
  val onTargetCancel: () -> Unit = { },
)

class TapTargetCoordinatorState internal constructor() {
  internal val tapTargets = mutableStateMapOf<Int, TapTarget>()
  internal var currentTargetIndex by mutableIntStateOf(0)

  val currentTarget get() = tapTargets[currentTargetIndex]
}

class TapTarget internal constructor(
  val precedence: Int,
  val title: TextDefinition,
  val description: TextDefinition,
  val coordinates: LayoutCoordinates,
  val style: TapTargetStyle = TapTargetStyle.Default,
  val onTargetClick: () -> Unit,
  val onTargetCancel: () -> Unit,
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