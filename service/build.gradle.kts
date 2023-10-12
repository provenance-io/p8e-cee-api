val ktlint: Configuration by configurations.creating

plugins {
    alias(libs.plugins.detekt)
    alias(libs.plugins.jib)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependencyManagement)
    kotlin("plugin.spring") version "1.8.22"
}

java.sourceCompatibility = JavaVersion.VERSION_17

dependencyManagement {
    applyMavenExclusions(false)
}

dependencies {
    ktlint(libs.ktlint)

    implementation(project(":models"))
    testImplementation(project(":models"))

    listOf(
        libs.kotlin.allOpen,
        libs.kotlin.reflect,
        libs.kotlin.stdLib.jdk8,
        libs.kotlin.stdLib.common,
        libs.kotlin.coroutines.coreJvm,
        libs.kotlin.coroutines.jdk8,
        libs.kotlin.coroutines.reactor,
        libs.springBoot.starter,
        libs.springBoot.starter.actuator,
        libs.springBoot.starter.devTools,
        libs.springBoot.starter.jetty,
        libs.springBoot.starter.security,
        libs.springBoot.starter.validation,
        libs.assetClassification.client,
        libs.assetClassification.verifier,
        libs.bouncyCastle,
        libs.bouncyCastle.provider,
        libs.grpc.protobuf,
        libs.grpc.stub,
        libs.jackson.databind,
        libs.jackson.datatype,
        libs.jackson.kotlinModule,
        libs.jackson.protobuf,
        libs.jakarta.servlet,
        libs.kong.unirest,
        libs.kotlin.logging,
        libs.objectStore.gateway,
        libs.p8eScope.encryption,
        libs.p8eScope.objectStore.client,
        libs.p8eScope.sdk,
        libs.protobuf.java.util,
        libs.provenance.client,
        libs.provenance.hdWallet,
        libs.provenance.hdWallet.bip39,
        libs.provenance.keyAccessLib,
        libs.provenance.loanPackage,
        libs.provenance.protoKotlin,
        libs.reactor.core, // Needed to play nice with WebFlux Coroutines stuff
        libs.reflections,
        libs.springdoc.openApi.kotlinSupport,
        libs.springdoc.openApi.webFluxSupport,
        libs.springfox.swagger,
        libs.springfox.swagger.ui,
        libs.swagger.annotations,
    ).forEach { dependency ->
        implementation(dependency)
    }

    implementation(libs.springBoot.starter.webflux) {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-tomcat")
    }

    implementation(libs.provenance.metadataAssetModel) {
        version {
            strictly(libs.versions.metadataAssetModel.get())
        }
    }

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    listOf(
        libs.hamkrest,
        libs.kotest,
        libs.kotest.assertions,
        libs.kotest.assertions.arrow,
        libs.kotest.property,
        libs.kotest.spring,
        libs.kotlin.coroutines.test,
        libs.kotlin.faker,
        libs.mockk,
        libs.spring.mockk,
        libs.testContainers.core,
    ).forEach { testDependency ->
        testImplementation(testDependency)
    }

    testImplementation(libs.springBoot.starter.test) {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
        exclude(group = "org.mockito", module = "mockito-core")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

tasks.register<JavaExec>("ktlint") {
    group = "verification"
    description = "Check Kotlin code style."
    classpath = ktlint
    mainClass.set("com.pinterest.ktlint.Main")
    args("src/**/*.kt")
}

tasks.named("check") {
    dependsOn(tasks.named("ktlint"))
}

tasks.register<JavaExec>("ktlintFormat") {
    group = "formatting"
    description = "Fix Kotlin code style deviations."
    classpath = ktlint
    mainClass.set("com.pinterest.ktlint.Main")
    args("-F", "src/**/*.kt")
}

tasks.jar {
  enabled = false
}

tasks.bootRun.configure {
    systemProperty("spring.profiles.active", System.getenv("SPRING_PROFILES_ACTIVE") ?: "development")
}

detekt {
   toolVersion = libs.versions.detekt.get()
   buildUponDefaultConfig = true
   config = files("${rootDir.path}/detekt.yml")
   source = files("src/main/kotlin", "src/test/kotlin")
}

val latestTag: String? = "latest".takeIf {
    System.getenv("GITHUB_REF_NAME")
        ?.takeIf { it.isNotBlank() }
        ?.let { githubRefName -> setOf("main").any { githubRefName.endsWith(it) } }
        ?: false
}

tasks.named("jib") {
    dependsOn(tasks.named("bootJar"))
}

tasks.named("jibDockerBuild") {
    dependsOn(tasks.named("bootJar"))
}

val jarFileName = "service.jar"
val mainClassName = "io.provenance.api.ApplicationKt"

jib {
    from {
        image = "azul/zulu-openjdk:17.0.8.1"
    }
    to {
        auth {
            username = System.getenv("JIB_AUTH_USERNAME") ?: "_json_key"
            password = System.getenv("JIB_AUTH_PASSWORD") ?: "nopass"
        }
        image = System.getenv("DOCKER_IMAGE_NAME") ?: "provenanceio/${rootProject.name}"
        tags = System.getenv("DOCKER_IMAGE_TAGS")?.run {
            split(",").toSet()
        } ?: setOfNotNull(rootProject.version.toString(), latestTag)
    }
    extraDirectories {
        paths {
            path {
                setFrom(file("src/main/jib/"))
                into = "/"
                includes.set(listOf("service-configure"))
            }
            path {
                setFrom(file("build/libs"))
                into = "/"
                includes.set(listOf(jarFileName))
            }
        }
        permissions = mapOf("/service-configure" to "755", jarFileName to "755")
    }
    container {
        mainClass = mainClassName
        ports = listOf("8080")
        entrypoint = listOf("/bin/bash", "-c", "--", "/service-configure /$jarFileName")
        creationTime = "USE_CURRENT_TIMESTAMP"
    }
}
