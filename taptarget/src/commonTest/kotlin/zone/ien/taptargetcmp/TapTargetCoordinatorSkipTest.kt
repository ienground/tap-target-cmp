package zone.ien.taptargetcmp

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class TapTargetCoordinatorSkipTest {
    @Test
    fun coordinator_accepts_custom_skip_button_content() {
        val skipButton: @Composable BoxScope.(onSkip: () -> Unit) -> Unit = { _ -> }

        val coordinator: @Composable () -> Unit = {
            TapTargetCoordinator(
                showTapTargets = true,
                skipButton = skipButton,
            ) { }
        }

        assertNotNull(coordinator)
    }

    @Test
    fun skipping_moves_progress_to_the_end_only_when_a_target_is_active() {
        assertEquals(3, skipTargetIndex(currentIndex = 1, targetCount = 3))
        assertNull(skipTargetIndex(currentIndex = 3, targetCount = 3))
    }
}
