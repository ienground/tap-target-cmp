# tap-target-cmp
tap-target-compose is a Compose Multiplatform implementation of the [Material Design tap targets](https://m1.material.io/growth-communications/feature-discovery.html#feature-discovery-design), used for feature discovery.

This library was inspired by its View counterpart, [TapTargetView](https://github.com/KeepSafe/TapTargetView).

This library is a forked version of [tap-target-compose](https://github.com/PierfrancescoSoffritti/tap-target-compose/), porting the original Android-only library for Compose Multiplatform. It currently targets Android and iOS, but since there is no separate native implementation, support for other platforms may be added upon pull requests or requests.

|                                   |                               |
|-----------------------------------|-------------------------------|
| ![](/.github/android_example.gif) | ![](/.github/ios_example.gif) |

# Sample app
This library comes with a sample app that shows examples of how to use it.

* [Click here to see the source code of the example](./example/).

:eyes: If you want to know when a new release of the library is published: [watch this repository on GitHub](https://github.com/ienground/tap-target-cmp/watchers).

# Download
The minimum API level supported by this library is API 29.

Add this to your module level `build.gradle` file to start using the library.

```toml
taptarget = { group = "zone.ien.taptargetcmp", name = "taptarget", version = "2.0.0" }
```

```gradle
dependencies {
    implementation(libs.taptarget)
}
```

# Quick start
In order to start using the library you need to wrap your composables in a TapTargetCoordinator

```kotlin
TapTargetCoordinator(showTapTargets = true, onComplete = {}) {
  Surface {
    Button(
      onClick = { },
      modifier = Modifier.tapTarget(
        precedence = 0,
        title = TextDefinition(
          text = "Tap target title",
          textStyle = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSecondaryContainer
        ),
        description = TextDefinition(
          text = "Tap target description",
          textStyle = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSecondaryContainer
        ),
        tapTargetStyle = TapTargetStyle(
          backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
          tapTargetHighlightColor = MaterialTheme.colorScheme.onSecondaryContainer,
          backgroundAlpha = 1f,
        ),
      ),
    ) {
      Text(text = "Click here")
    }
  }
}
```

You can also create a `TapTargetDefinition` and pass it to the modifier:

```kotlin
val tapTargetDefinition = TapTargetDefinition(
  precedence = 1,
  title = TextDefinition(
    text = "Tap target title",
    textStyle = MaterialTheme.typography.titleLarge,
    fontWeight = FontWeight.Bold,
    color = MaterialTheme.colorScheme.onSecondaryContainer
  ),
  description = TextDefinition(
    text = "Tap target description",
    textStyle = MaterialTheme.typography.bodyMedium,
    color = MaterialTheme.colorScheme.onSecondaryContainer
  ),
  tapTargetStyle = TapTargetStyle(
    backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
    tapTargetHighlightColor = MaterialTheme.colorScheme.onSecondaryContainer,
    backgroundAlpha = 1f,
  ),
)

TapTargetCoordinator(showTapTargets = true, onComplete = {}) {
  Surface {
    Button(
      onClick = {  },
      modifier = Modifier.tapTarget(tapTargetDefinition),
    ) {
      Text(text = "Click here")
    }
  }
}
```

The library supports chaining of multiple tap targets, but you can also show only one if that's what you need.

## Skip button

`TapTargetCoordinator`에 `skipButton`을 전달하면 각 활성 타깃의 설명 아래에 현재 안내를 건너뛸 수 있는 UI를 원하는 형태로 구성할 수 있습니다. 슬롯에 전달되는 콜백을 호출하면 남은 모든 타깃을 종료하고 `onComplete`가 호출됩니다. `BoxScope` 수신자를 사용하므로 타깃 콘텐츠 영역 안에서 `Modifier.align`으로 위치를 지정할 수 있습니다.

```kotlin
TapTargetCoordinator(
    showTapTargets = true,
    skipButton = { onSkip ->
        TextButton(
            onClick = onSkip,
            modifier = Modifier.align(Alignment.Center),
        ) {
            Text("Skip")
        }
    },
    onComplete = { },
) {
    // tap-target content
}
```

## Bring Into View

When using tap targets inside a scrollable container, each target can automatically scroll into view when it becomes active. This behavior is controlled by `bringIntoViewEnabled` and `bringIntoViewVerticalOffset` parameters.

Set a default vertical offset at the coordinator level, and optionally override it per target:

```kotlin
TapTargetCoordinator(
    showTapTargets = true,
    bringIntoViewVerticalOffset = 100.dp,  // default offset for all targets
    onComplete = { }
) {
    Button(
        onClick = { },
        modifier = Modifier.tapTarget(
            precedence = 0,
            title = TextDefinition(text = "Title"),
            description = TextDefinition(text = "Description"),
            bringIntoViewVerticalOffset = 160.dp,  // override for this target
        ),
    ) {
        Text(text = "Click here")
    }
}
```

You can also disable bring into view for specific targets:

```kotlin
Modifier.tapTarget(
    precedence = 1,
    title = TextDefinition(text = "Title"),
    description = TextDefinition(text = "Description"),
    bringIntoViewEnabled = false,
)
```

---

For any question feel free to [open an issue on the GitHub repository](https://github.com/ienground/tap-target-cmp/issues).
