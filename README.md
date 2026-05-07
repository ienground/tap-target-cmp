# tap-target-cmp
tap-target-compose is a Compose Multiplatform implementation of the [Material Design tap targets](https://m1.material.io/growth-communications/feature-discovery.html#feature-discovery-design), used for feature discovery.

This library was inspired by its View counterpart, [TapTargetView](https://github.com/KeepSafe/TapTargetView).L:

|     |     |
| --- | --- |
| ![](/.github/tap-target-image.gif) | [![](/.github/green-stash.gif)](https://github.com/Pool-Of-Tears/GreenStash) |

# Sample app
This library comes with a sample app that shows examples of how to use it.

* [Click here to see the source code of the example](./example/).

:eyes: If you want to know when a new release of the library is published: [watch this repository on GitHub](https://github.com/ienground/tap-target-cmp/watchers).

# Download
The minimum API level supported by this library is API 29.

Add this to your module level `build.gradle` file to start using the library.

```toml
taptarget = { group = "zone.ien.taptargetcmp", name = "taptarget", version.ref = "{version}"}
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

---

For any question feel free to [open an issue on the GitHub repository](https://github.com/ienground/tap-target-cmp/issues).