// https://docs.gradle.org/current/userguide/kotlin_dsl.html#sec:kotlin-dsl_plugin

// used for the kotlin-dsl
buildscript {
  repositories {
    mavenCentral()
  }
}

plugins {
  `kotlin-dsl`
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

// For everything else
repositories {
  mavenCentral()
}
