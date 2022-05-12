val ktlint: Configuration by configurations.creating

plugins {
    Plugins.Detekt.addTo(this)
    Plugins.SpringBoot.addTo(this)
    Plugins.SpringDependencyManagement.addTo(this)
    kotlin("plugin.spring") version Versions.Kotlin
}

java.sourceCompatibility = JavaVersion.VERSION_11

dependencyManagement {
    applyMavenExclusions(false)
}

dependencies {
    ktlint(Dependencies.Ktlint.toDependencyNotation())
    implementation(project(":models"))

    listOf(
        Dependencies.Kotlin.AllOpen,
        Dependencies.Kotlin.Reflect,
        Dependencies.Kotlin.StdlbJdk8,
        Dependencies.Kotlin.StdlbCommon,
        Dependencies.Kotlin.CoroutinesCoreJvm,
        Dependencies.Kotlin.CoroutinesJdk8,
        Dependencies.Kotlin.CoroutinesReactor,
        Dependencies.SpringBoot.Starter,
        Dependencies.SpringBoot.StarterActuator,
        Dependencies.SpringBoot.StarterWebFlux,
        Dependencies.SpringBoot.StarterJetty,
        Dependencies.SpringBoot.StarterDevTools,
        Dependencies.SpringBoot.StarterSecurity,
        Dependencies.SpringBoot.StarterValidation,
        // Needed to play nice with WebFlux Coroutines stuff
        Dependencies.Reactor.Core,
        Dependencies.KotlinLogging,
        Dependencies.SpringBoot.Swagger,
        Dependencies.SpringBoot.SwaggerUI,
        Dependencies.Swagger.Annotations,
        Dependencies.P8eScope.Encryption,
        Dependencies.P8eScope.OsClient,
        Dependencies.P8eScope.Sdk,
        Dependencies.P8eScope.Util,
        Dependencies.Provenance.AssetModel,
        Dependencies.Provenance.KeyAccessLib,
        Dependencies.Provenance.HdWallet.HdWallet,
        Dependencies.Provenance.HdWallet.HdWalletBip39,
        Dependencies.Provenance.Client.GrpcClientKotlin,
        Dependencies.Provenance.ProtoKotlin,
        Dependencies.Provenance.LoanPackage,
        Dependencies.Jackson.Databind,
        Dependencies.Jackson.Datatype,
        Dependencies.Jackson.Hubspot,
        Dependencies.Jackson.KotlinModule,
        Dependencies.Protobuf.JavaUtil,
        Dependencies.Kong.Unirest,
        Dependencies.Grpc.Protobuf,
        Dependencies.Grpc.Stub,
        Dependencies.Reflections,
        Dependencies.BouncyCastle,
        Dependencies.BouncyCastleProvider,
        Dependencies.OpenApi.WebFluxSupport,
        Dependencies.OpenApi.KotlinSupport,

    ).forEach { dep ->
        dep.implementation(this)
    }

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    listOf(
        Dependencies.Hamkrest,
        Dependencies.Mockk,
        Dependencies.KotlinFaker,
        Dependencies.Kotlin.CoroutinesTest,
        Dependencies.SpringMockk,
        Dependencies.SpringBoot.StarterTest,
        Dependencies.Kotest,
        Dependencies.KotestAssertions,
        Dependencies.KotestAssertionsArrow,
    ).forEach { testDep ->
        testDep.testImplementation(this)
    }
}

tasks.withType<Test> {
    useJUnitPlatform {
        includeEngines("junit-jupiter")
    }
    testLogging {
        events("passed", "skipped", "failed")
    }
}

tasks.register<JavaExec>("ktlint") {
    group = "verification"
    description = "Check Kotlin code style."
    classpath = ktlint
    main = "com.pinterest.ktlint.Main"
    args("src/**/*.kt")
}

tasks.named("check") {
    dependsOn(tasks.named("ktlint"))
}

tasks.register<JavaExec>("ktlintFormat") {
    group = "formatting"
    description = "Fix Kotlin code style deviations."
    classpath = ktlint
    main = "com.pinterest.ktlint.Main"
    args("-F", "src/**/*.kt")
}

tasks.jar {
  enabled = false
}

tasks.bootRun.configure {
    systemProperty("spring.profiles.active", System.getenv("SPRING_PROFILES_ACTIVE") ?: "development")
}

detekt {
   toolVersion = Versions.Detekt
   buildUponDefaultConfig = true
   config = files("${rootDir.path}/detekt.yml")
   input = files("src/main/kotlin", "src/test/kotlin")
}
