import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import com.github.gundy.semver4j.model.Version

plugins {
  alias(libs.plugins.kotlin.jvm)

  alias(libs.plugins.dependencyAnalysis)
  alias(libs.plugins.kotlinter)
  alias(libs.plugins.taskTree)
  alias(libs.plugins.versions)
}

buildscript {
  repositories {
    mavenCentral()
  }
  dependencies {
    classpath(libs.semver4j)
  }
}

group = "uk.org.lidalia"

repositories {
  mavenCentral()
}

kotlin {
  jvmToolchain(17)
}

dependencies {
  testImplementation(platform(libs.kotest.bom))
  testImplementation(libs.kotest.runner.junit5)
  testImplementation(libs.kotest.framework.api)
  testImplementation(libs.kotest.assertions.shared)
}

dependencyLocking {
  lockAllConfigurations()
}

testing {
  suites {
    @Suppress("UnstableApiUsage")
    val test by getting(JvmTestSuite::class) {
      useJUnitJupiter()
    }
  }
}

val test by tasks.named<Test>("test") {
  systemProperty("kotest.framework.classpath.scanning.config.disable", "true")
  systemProperty("kotest.framework.classpath.scanning.autoscan.disable", "true")
}

kotlinter {
  reporters = arrayOf("checkstyle", "plain", "html")
}

tasks {
  check {
    dependsOn("buildHealth")
    dependsOn("installKotlinterPrePushHook")
  }
}

dependencyAnalysis {
  issues {
    // configure for all projects
    all {
      // set behavior for all issue types
      onAny {
        severity("fail")
        exclude("org.jetbrains.kotlin:kotlin-stdlib")
      }
    }
  }
}

tasks.withType<DependencyUpdatesTask> {
  rejectVersionIf {
    candidate.version.isPreRelease()
  }
}

fun String.isPreRelease(): Boolean = try {
  Version.fromString(this).preReleaseIdentifiers.isNotEmpty()
} catch (e: IllegalArgumentException) {
  false
}
