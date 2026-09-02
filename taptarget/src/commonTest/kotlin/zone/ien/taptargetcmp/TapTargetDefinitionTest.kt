package zone.ien.taptargetcmp

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class TapTargetDefinitionTest {
    @Test
    fun icon_and_wrapper_are_optional() {
        val definition = TapTargetDefinition(
            title = TextDefinition("title"),
            description = TextDefinition("description"),
            precedence = 0,
        )

        assertNull(definition.icon)
        assertNull(definition.iconWrapper)
    }

    @Test
    fun custom_icon_wrapper_can_be_provided() {
        val wrapper: @Composable ((@Composable () -> Unit) -> Unit) = { content -> content() }
        val icon = ImageVector.Builder(
            name = "test",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(0f, 0f)
                lineTo(24f, 0f)
                lineTo(24f, 24f)
                lineTo(0f, 24f)
                close()
            }
        }.build()
        val definition = TapTargetDefinition(
            title = TextDefinition("title"),
            description = TextDefinition("description"),
            precedence = 0,
            icon = icon,
            iconWrapper = wrapper,
        )

        assertNotNull(definition.icon)
        assertNotNull(definition.iconWrapper)
    }
}
