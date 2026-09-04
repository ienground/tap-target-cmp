package zone.ien.taptargetcmp.example

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.Foundation.NSProcessInfo
import platform.UIKit.UIAction
import platform.UIKit.UIButton
import platform.UIKit.UIButtonTypeSystem
import platform.UIKit.UIMenu

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun NativeDropdown(
    text: String,
    options: List<String>,
    modifier: Modifier,
    onOptionSelected: (String) -> Unit,
) {
    val currentOnOptionSelected = rememberUpdatedState(onOptionSelected)

    UIKitView(
        factory = {
            UIButton.buttonWithType(UIButtonTypeSystem).apply {
                showsMenuAsPrimaryAction = true
            }
        },
        modifier = modifier,
        update = { button ->
            button.configuration = nativeGlassButtonConfiguration(text)
            button.menu = UIMenu.menuWithTitle(
                title = "",
                children = options.map { option ->
                    UIAction.actionWithTitle(
                        title = option,
                        image = null,
                        identifier = null,
                    ) { _ -> currentOnOptionSelected.value(option) }
                },
            )
        },
        properties = UIKitInteropProperties(
            isInteractive = true,
            isNativeAccessibilityEnabled = true,
        ),
    )
}

@OptIn(ExperimentalForeignApi::class)
internal fun nativeGlassButtonConfiguration(text: String) =
    if (isLiquidGlassAvailable()) {
        platform.UIKit.UIButtonConfiguration.glassButtonConfiguration()
    } else {
        platform.UIKit.UIButtonConfiguration.tintedButtonConfiguration()
    }.apply {
        title = text
    }

@OptIn(ExperimentalForeignApi::class)
private fun isLiquidGlassAvailable(): Boolean =
    NSProcessInfo.processInfo.operatingSystemVersion.useContents {
        majorVersion >= 26L
    }
