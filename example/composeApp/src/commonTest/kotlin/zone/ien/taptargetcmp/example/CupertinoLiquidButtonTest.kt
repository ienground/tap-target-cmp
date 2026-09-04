package zone.ien.taptargetcmp.example

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kyant.backdrop.backdrops.LayerBackdrop
import kotlin.test.Test
import kotlin.test.assertNotNull

class CupertinoLiquidButtonTest {
    @Test
    fun cupertino_liquid_button_example_exposes_the_hig_click_contract() {
        val button: @Composable (String, Modifier, LayerBackdrop, () -> Unit) -> Unit =
            ::CupertinoLiquidButtonExample

        assertNotNull(button)
    }
}
