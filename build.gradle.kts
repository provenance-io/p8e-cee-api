plugins {
    kotlin("jvm") version Versions.Kotlin
    id("java")
    id("maven-publish")
}

allprojects {
    val project = this
    group = "com.figure.onboarding"
    version = this.findProperty("artifactVersion")?.toString()
        ?: "1.0-snapshot"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply {
        Plugins.Kotlin.addTo(this)
        Plugins.Idea.addTo(this)
        plugin("java")
    }

    repositories {
        mavenCentral()
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        sourceCompatibility = "1.8"
        targetCompatibility = "1.8"

        kotlinOptions {
            freeCompilerArgs = listOf(
                "-Xjsr305=strict",
                "-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
            )
            jvmTarget = "11"
            allWarningsAsErrors = true
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}
