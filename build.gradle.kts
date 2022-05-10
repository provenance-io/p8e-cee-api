import net.swiftzer.semver.SemVer

buildscript {
    dependencies {
        classpath("net.swiftzer.semver:semver:1.1.2")
        classpath("com.github.breadmoirai:github-release:2.2.12")
    }
}

plugins {
    kotlin("jvm") version Versions.Kotlin
    id("java")
    id("maven-publish")
    id("java-library")
    id("com.github.breadmoirai.github-release") version "2.2.12"
    id("io.github.nefilim.gradle.semver-plugin") version "0.3.10"
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

    java {
        withJavadocJar()
        withSourcesJar()
    }

    repositories {
        fun figureNexusUsername() = findProperty("nexusUser")?.toString() ?: System.getenv("NEXUS_USER")
        fun figureNexusPassword() = findProperty("nexusPass")?.toString() ?: System.getenv("NEXUS_PASS")

        maven {
            url = uri("https://nexus.figure.com/repository/mirror")
            credentials {
                username = figureNexusUsername()
                password = figureNexusPassword()
            }
        }
        maven {
            url = uri("https://nexus.figure.com/repository/figure")
            credentials {
                username = figureNexusUsername()
                password = figureNexusPassword()
            }
        }

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
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

semver {
    tagPrefix("v")
    initialVersion("0.1.0")
    findProperty("semver.overrideVersion")?.toString()?.let { overrideVersion(it) }
    val semVerModifier = findProperty("semver.modifier")?.toString()?.let { buildVersionModifier(it) } ?: { nextPatch() }
    versionModifier(semVerModifier)
}

/**
 * The code below is a workaround for [gradle/gradle#20016](https://github.com/gradle/gradle/issues/20016) derived from
 * [platform-portal@14319ed/docs/semver.md](https://github.com/FigureTechnologies/platform-portal/blob/14319ed7e97d88a1b8cbb2f2f7708cc0660dc518/docs/semver.md#limiting-dependencies-to-release-versions)
 * to prevent version strings like `"1.0.+"` from resolving to pre-release versions.
 * You can adjust the string set constants or comment the code out entirely to allow pre-release versions to be used.
 */

val invalidQualifiers = setOf("alpha", "beta", "rc", "nightly")
val onlyReleaseArtifacts = setOf("p8e-cee-api")
val whiteListedMavenGroups = setOf("tech.figure", "io.provenance")

configurations.all {
    resolutionStrategy {
        componentSelection {
            all {
                when {
                    (
                        onlyReleaseArtifacts.any { candidate.moduleIdentifier.name.startsWith(it) } &&
                            !candidate.version.toSemVer()?.preRelease.isNullOrEmpty()
                        ) -> {
                        reject("Rejecting prerelease version for OnlyReleaseArtifact[$candidate]")
                    }
                    (
                        whiteListedMavenGroups.none { candidate.group.startsWith(it) } &&
                            invalidQualifiers.any { candidate.version.contains(it) }
                        ) -> {
                        reject("Invalid qualifier versions for $candidate")
                    }
                }
            }
        }
    }
}

fun String?.toSemVer(): SemVer? =
    try {
        this?.let { versionString ->
            SemVer.parse(versionString)
        }
    } catch (e: Exception) {
        project.logger.info("Failed to parse semantic version from string '$this'")
        null
    }


val semVersion = semver.version
allprojects {
    val project = this
    group = "io.provenance.p8e-cee-api"
    version = semVersion

    repositories {
        mavenLocal()
        mavenCentral()
    }
}

val githubTokenValue = findProperty("githubToken")?.toString() ?: System.getenv("GITHUB_TOKEN")
githubRelease {
    token(githubTokenValue)
    owner("provenance-io")
    targetCommitish("main")
    draft(false)
    prerelease(false)
    repo("p8e-cee-api")
    tagName(semver.versionTagName)
    body(changelog())

    overwrite(false)
    dryRun(false)
    apiEndpoint("https://api.github.com")
    client
}
