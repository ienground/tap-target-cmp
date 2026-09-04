package zone.ien.taptargetcmp.example

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIAction
import platform.UIKit.UIButton
import platform.UIKit.UIButtonTypeSystem
import platform.UIKit.UIControlEventTouchUpInside

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun UIKitButton(
    text: String,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val currentOnClick = rememberUpdatedState(onClick)

    UIKitView(
        factory = {
            UIButton.buttonWithType(UIButtonTypeSystem).apply {
                configuration = nativeGlassButtonConfiguration(text)
                addAction(
                    action = UIAction.actionWithHandler { currentOnClick.value() },
                    forControlEvents = UIControlEventTouchUpInside,
                )
            }
        },
        modifier = modifier,
        update = { button ->
            button.configuration = nativeGlassButtonConfiguration(text)
        },
        properties = UIKitInteropProperties(
            isInteractive = true,
            isNativeAccessibilityEnabled = true,
        ),
    )
}
