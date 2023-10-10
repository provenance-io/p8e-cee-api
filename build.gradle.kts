plugins {
    kotlin("jvm") version "1.8.22"
    id("java")
    id("maven-publish")
    id("java-library")
    alias(libs.plugins.nexus.publishing)
    signing
}

subprojects {
    apply {
        plugin("kotlin")
        plugin("idea")
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf(
                "-Xjsr305=strict",
                "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            )
            jvmTarget = "17"
            apiVersion = "1.8"
            languageVersion = "1.8"
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
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

allprojects {
    group = "io.provenance.p8e-cee-api"

    repositories {
        mavenLocal()
        mavenCentral()
    }
}
