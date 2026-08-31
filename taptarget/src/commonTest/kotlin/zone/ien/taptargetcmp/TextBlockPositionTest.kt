package zone.ien.taptargetcmp

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import kotlin.test.Test
import kotlin.test.assertEquals

class TextBlockPositionTest {
    @Test
    fun text_block_is_centered_horizontally() {
        val offset = getTextBlockOffset(
            textBlockSize = Size(width = 300f, height = 100f),
            screenSize = Size(width = 1_000f, height = 1_000f),
            targetCenter = Offset(500f, 800f),
            targetRadius = 50f,
            horizontalMargin = 40f,
            verticalMargin = 40f,
        )

        assertEquals(350f, offset.x)
        assertEquals(610f, offset.y)
    }
}
