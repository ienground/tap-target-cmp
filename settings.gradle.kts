enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
  repositories {
    google()
    mavenCentral()
    mavenLocal()
    gradlePluginPortal()
  }
}
dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    google()
    mavenCentral()
    mavenLocal()
  }
}

rootProject.name = "tap-target-cmp"
include(":example")
include(":example:composeApp")
include(":example:androidApp")
include(":example:iosApp")
include(":taptarget")
