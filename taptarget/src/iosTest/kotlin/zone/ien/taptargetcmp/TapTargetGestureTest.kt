package zone.ien.taptargetcmp

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class TapTargetGestureTest {
    @Test
    fun `누르는 동안 같은 대상이 다시 배치되어도 탭을 취소하지 않는다`() = runComposeUiTest {
        val offset = mutableIntStateOf(0)
        var center = Offset.Zero
        var clicks = 0
        setContent {
            TapTargetCoordinator(showTapTargets = true, modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier.offset { IntOffset(offset.intValue, 0) }
                        .size(160.dp)
                        .onGloballyPositioned { center = it.boundsInRoot().center }
                        .tapTarget(
                            title = TextDefinition(text = "조리 시간"),
                            description = TextDefinition(text = "휠 선택기"),
                            precedence = 0,
                            onTargetClick = { clicks++ },
                        ),
                )
            }
        }
        waitForIdle()
        onRoot().performTouchInput { down(center) }
        runOnIdle { offset.intValue = 1 }
        waitForIdle()
        onRoot().performTouchInput {
            advanceEventTime(100)
            up()
        }
        waitForIdle()
        runOnIdle { assertEquals(1, clicks) }
    }
}
