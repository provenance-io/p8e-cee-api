plugins {
    kotlin("jvm") version Versions.Kotlin
    id("java")
    id("maven-publish")
    id("java-library")
    id("io.github.gradle-nexus.publish-plugin") version Versions.NexusPublishing
    signing
}

subprojects {
    apply {
        Plugins.Kotlin.addTo(this)
        Plugins.Idea.addTo(this)
        plugin("java")
        plugin("signing")
    }

    repositories {
        mavenLocal()
        mavenCentral()
    }
}

allprojects {
    java {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf(
                "-Xjsr305=strict",
                "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
                "-Xskip-prerelease-check", // To support loan package contracts dependency using context receivers
            )
            jvmTarget = "11"
            apiVersion = "1.6"
            languageVersion = "1.6"
            allWarningsAsErrors = true
        }
    }
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri(RepositoryLocations.Sonatype))
            snapshotRepositoryUrl.set(uri(RepositoryLocations.SonatypeSnapshot))
            username.set(findProject("ossrhUsername")?.toString() ?: System.getenv("OSSRH_USERNAME"))
            password.set(findProject("ossrhPassword")?.toString() ?: System.getenv("OSSRH_PASSWORD"))
            stagingProfileId.set("3180ca260b82a7") // prevents querying for the staging profile id, performance optimization
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

allprojects {
    group = "io.provenance.p8e-cee-api"

    repositories {
        mavenLocal()
        mavenCentral()
    }
}
