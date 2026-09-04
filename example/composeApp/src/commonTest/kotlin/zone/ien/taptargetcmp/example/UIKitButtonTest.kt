package zone.ien.taptargetcmp.example

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlin.test.Test
import kotlin.test.assertNotNull

class UIKitButtonTest {
    @Test
    fun uikit_button_exposes_a_composable_click_contract() {
        val button: @Composable (String, Modifier, () -> Unit) -> Unit = ::UIKitButton

        assertNotNull(button)
    }
}
