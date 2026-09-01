package zone.ien.taptargetcmp

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TextBlockPositionTest {
    @Test
    fun text_block_is_centered_horizontally() {
        val offset = getTextBlockOffset(
            textBlockSize = Size(width = 300f, height = 100f),
            screenSize = Size(width = 1_000f, height = 1_000f),
            targetBounds = Rect(left = 450f, top = 750f, right = 550f, bottom = 850f),
            horizontalMargin = 40f,
            verticalMargin = 40f,
        )

        assertEquals(350f, offset.x)
        assertEquals(610f, offset.y)
    }

    @Test
    fun text_block_stays_inside_the_screen_when_below_target_does_not_fit() {
        val offset = getTextBlockOffset(
            textBlockSize = Size(width = 200f, height = 280f),
            screenSize = Size(width = 300f, height = 300f),
            targetBounds = Rect(left = 120f, top = 0f, right = 180f, bottom = 40f),
            horizontalMargin = 40f,
            verticalMargin = 8f,
        )

        assertEquals(20f, offset.y)
        assertTrue(offset.y + 280f <= 300f)
    }

    @Test
    fun skip_button_keeps_a_readable_gap_from_the_text() {
        assertEquals(12.dp, SKIP_BUTTON_TOP_SPACING)
    }

    @Test
    fun icon_keeps_a_readable_gap_from_the_text() {
        assertEquals(12.dp, ICON_TEXT_SPACING)
    }

    @Test
    fun title_keeps_a_readable_gap_from_the_description() {
        assertEquals(8.dp, TEXT_SPACING)
    }

    @Test
    fun content_keeps_a_readable_gap_from_the_target() {
        assertEquals(12.dp, TEXT_VERTICAL_MARGIN)
    }

    @Test
    fun text_block_prefers_the_side_closer_to_the_screen_center() {
        val offset = getTextBlockOffset(
            textBlockSize = Size(width = 200f, height = 100f),
            screenSize = Size(width = 300f, height = 1_000f),
            targetBounds = Rect(left = 100f, top = 300f, right = 200f, bottom = 400f),
            horizontalMargin = 40f,
            verticalMargin = 8f,
        )

        assertEquals(408f, offset.y)
    }
}
