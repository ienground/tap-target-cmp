package zone.ien.taptargetcmp.example

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun UIKitButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
)
