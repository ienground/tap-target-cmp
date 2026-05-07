// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
  alias(libs.plugins.android.kotlin.multiplatform.library) apply false
  alias(libs.plugins.kotlin.multiplatform) apply false
  alias(libs.plugins.serialization) apply false
  alias(libs.plugins.vanniktech.mavenPublish) apply false
  alias(libs.plugins.android.lint) apply false
  alias(libs.plugins.compose.compiler) apply false
}
val isReleaseVersion = !version.toString().endsWith("SNAPSHOT")

subprojects {
  plugins.withId("com.vanniktech.maven.publish") {
    configure<com.vanniktech.maven.publish.MavenPublishBaseExtension> {
      publishToMavenCentral(automaticRelease = true)

      // Artifact ID만 각 프로젝트의 이름으로 자동 설정
      group = "zone.ien.taptargetcmp"
      version = libs.versions.lib.version.name.get()
      println("${group} ${project.name} ${version}")

      coordinates(group.toString(), project.name, version.toString())

      pom {
        name = project.name
        description = "A Compose Multiplatform implementation of Material Design tap targets, for feature discovery."
        inceptionYear = "2026"
        url = "https://github.com/ienground/tap-target-cmp"
        licenses {
          license {
            name = "Apache-2.0"
            url = "https://www.apache.org/licenses/LICENSE-2.0"
          }
        }
        developers {
          developer {
            id = "ienground"
            name = "Ericano Rhee"
            url = "https://www.ien.zone"
          }
        }
        scm {
          url = "https://github.com/ienground/tap-target-cmp.git"
          connection = "scm:git:https://github.com/ienground/tap-target-cmp.git"
          developerConnection = "scm:git:https://github.com/ienground/tap-target-cmp.git"
        }
      }

      val isPublishingToMavenLocal = gradle.startParameter.taskNames.any { it.contains("publishToMavenLocal", ignoreCase = true) }
      val isSnapshot = version.toString().endsWith("SNAPSHOT")

      if (!isSnapshot && !isPublishingToMavenLocal) {
        signAllPublications()
      }
    }
  }
}