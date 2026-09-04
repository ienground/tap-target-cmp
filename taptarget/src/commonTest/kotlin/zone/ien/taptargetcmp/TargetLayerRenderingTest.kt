package zone.ien.taptargetcmp

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TargetLayerRenderingTest {
    @Test
    fun target_bounds_are_converted_from_window_to_canvas_coordinates() {
        val targetBoundsInWindow = Rect(110f, 220f, 210f, 320f)
        val canvasBoundsInWindow = Rect(10f, 20f, 510f, 820f)

        assertEquals(
            Rect(100f, 200f, 200f, 300f),
            targetBoundsInCanvas(targetBoundsInWindow, canvasBoundsInWindow),
        )
    }

    @Test
    fun target_layer_is_not_recorded_when_overlay_is_hidden() {
        assertFalse(
            shouldRecordTargetLayer(
                showTapTargets = false,
                activePrecedence = 0,
                targetPrecedence = 0,
            )
        )
    }

    @Test
    fun target_layer_is_recorded_only_for_the_active_target() {
        assertTrue(
            shouldRecordTargetLayer(
                showTapTargets = true,
                activePrecedence = 1,
                targetPrecedence = 1,
            )
        )
        assertFalse(
            shouldRecordTargetLayer(
                showTapTargets = true,
                activePrecedence = 1,
                targetPrecedence = 2,
            )
        )
    }

    @Test
    fun local_to_window_matrix_preserves_translation_and_transform() {
        val matrix = localToWindowMatrix(
            origin = Offset(10f, 20f),
            xAxis = Offset(12f, 20f),
            yAxis = Offset(10f, 24f),
        )

        assertEquals(Offset(16f, 32f), matrix.map(Offset(3f, 3f)))
    }

    @Test
    fun local_to_window_matrix_preserves_rotation() {
        val matrix = localToWindowMatrix(
            origin = Offset(100f, 100f),
            xAxis = Offset(100f, 101f),
            yAxis = Offset(99f, 100f),
        )

        assertEquals(Offset(97f, 102f), matrix.map(Offset(2f, 3f)))
    }
}
