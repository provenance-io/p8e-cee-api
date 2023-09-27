val ktlint: Configuration by configurations.creating

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.detekt)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependencyManagement)
    kotlin("plugin.spring") version "1.8.10"
}

java.sourceCompatibility = JavaVersion.VERSION_11

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
        libs.springBoot.starter.jetty,
        libs.springBoot.starter.devTools,
        libs.springBoot.starter.security,
        libs.springBoot.starter.validation,
        libs.reactor.core, // Needed to play nice with WebFlux Coroutines stuff
        libs.kotlin.logging,
        libs.springfox.swagger,
        libs.springfox.swagger.ui,
        libs.swagger.annotations,
        libs.p8eScope.encryption,
        libs.p8eScope.objectStore.client,
        libs.p8eScope.sdk,
        libs.provenance.keyAccessLib,
        libs.provenance.hdWallet,
        libs.provenance.hdWallet.bip39,
        libs.provenance.client,
        libs.provenance.protoKotlin,
        libs.provenance.loanPackage,
        libs.jackson.databind,
        libs.jackson.datatype,
        libs.jackson.protobuf,
        libs.jackson.kotlinModule,
        libs.protobuf.java.util,
        libs.kong.unirest,
        libs.grpc.protobuf,
        libs.grpc.stub,
        libs.reflections,
        libs.bouncyCastle,
        libs.bouncyCastle.provider,
        libs.springdoc.openApi.webFluxSupport,
        libs.springdoc.openApi.kotlinSupport,
        libs.assetClassification.client,
        libs.assetClassification.verifier,
        libs.objectStore.gateway,
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
        libs.mockk,
        libs.kotlin.faker,
        libs.kotlin.coroutines.test,
        libs.spring.mockk,
        libs.kotest,
        libs.kotest.assertions,
        libs.kotest.assertions.arrow,
        libs.kotest.property,
        libs.kotest.spring,
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
