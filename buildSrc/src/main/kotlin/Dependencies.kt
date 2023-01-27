import org.gradle.api.plugins.ObjectConfigurationAction
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.ScriptHandlerScope
import org.gradle.kotlin.dsl.exclude
import org.gradle.plugin.use.PluginDependenciesSpec

object Versions {
    // Branch targets for internal deps
    const val Master = "master-+"
    const val Develop = "develop-+"

    const val Kotlin = "1.6.21"
    const val KotlinCoroutines = "1.6.2"
    const val Protobuf = "3.20.1"
    const val SpringBoot = "2.5.6"
    const val KotlinLogging = "2.0.11"
    const val Reactor = "3.4.9"
    const val Jackson = "2.12.5"
    const val Redisson = "3.16.0"
    const val Ktlint = "0.45.2"
    const val Detekt = "1.18.1"
    const val Hamkrest = "1.8.0.1"
    const val Mockk = "1.12.0"
    const val Kotest = "5.3.0"
    const val KotestExtensionsArrow = "1.2.+"
    const val KotestSpring = "1.1.1"
    const val KotlinFaker = "1.7.1"
    const val SpringMockk = "3.0.1"
    const val Swagger = "1.6.2"
    const val AssetModel = "0.1.16"
    const val P8eScope = "0.6.2"
    const val ProvenanceHdWallet = "0.1.15"
    const val ProvenanceClient = "1.1.1"
    const val Unirest = "3.13.6"
    const val KeyAccessLib = "0.3.0"
    const val LoanPackage = "0.6.1"
    const val Grpc = "1.45.0"
    const val ProvenanceProto = "1.11.1"
    const val Reflections = "0.9.10"
    const val NexusPublishing = "1.1.0"
    const val BouncyCastle = "1.70"
    const val OpenApi = "1.5.13"
    const val TestContainer = "1.3.3"
    const val AssetClassification = "3.6.1"
    const val OsGateway = "3.3.0"
}

object Plugins { // please keep this sorted in sections

    // Kotlin
    val Kotlin = PluginSpec("kotlin", Versions.Kotlin)

    // 3rd Party
    val Detekt = PluginSpec("io.gitlab.arturbosch.detekt", Versions.Detekt)
    val Idea = PluginSpec("idea")
    val Protobuf = PluginSpec("com.google.protobuf", "0.8.16")
    val SpringBoot = PluginSpec("org.springframework.boot", Versions.SpringBoot)
    val SpringDependencyManagement = PluginSpec("io.spring.dependency-management", "1.0.11.RELEASE")
    val NexusPublishing = PluginSpec("io.github.gradle-nexus.publish-plugin", Versions.NexusPublishing)
}

object Dependencies {
    object OpenApi {
        val WebFluxSupport = DependencySpec("org.springdoc:springdoc-openapi-webflux-ui", Versions.OpenApi)
        val KotlinSupport = DependencySpec("org.springdoc:springdoc-openapi-kotlin", Versions.OpenApi)
    }
    // Kotlin
    object Kotlin {
        val AllOpen = DependencySpec("org.jetbrains.kotlin:kotlin-allopen", Versions.Kotlin)
        val Reflect = DependencySpec("org.jetbrains.kotlin:kotlin-reflect", Versions.Kotlin)
        val StdlbJdk8 = DependencySpec("org.jetbrains.kotlin:kotlin-stdlib-jdk8", Versions.Kotlin)
        val StdlbCommon = DependencySpec("org.jetbrains.kotlin:kotlin-stdlib-common", Versions.Kotlin)
        val CoroutinesCoreJvm = DependencySpec(
            "org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm",
            Versions.KotlinCoroutines
        )
        val CoroutinesReactor = DependencySpec(
            "org.jetbrains.kotlinx:kotlinx-coroutines-reactor",
            Versions.KotlinCoroutines
        )
        val CoroutinesJdk8 = DependencySpec(
            "org.jetbrains.kotlinx:kotlinx-coroutines-jdk8",
            Versions.KotlinCoroutines
        )
        val CoroutinesTest = DependencySpec(
            "org.jetbrains.kotlinx:kotlinx-coroutines-test",
            Versions.KotlinCoroutines
        )
    }

    object AssetClassification {
        val Client = DependencySpec("tech.figure.classification.asset:ac-client", Versions.AssetClassification)
        val Verifier = DependencySpec("tech.figure.classification.asset:ac-verifier", Versions.AssetClassification)
    }

    object P8eScope {
        val Encryption = DependencySpec("io.provenance.scope:encryption", Versions.P8eScope)
        val OsClient = DependencySpec("io.provenance.scope:os-client", Versions.P8eScope)
        val Sdk = DependencySpec("io.provenance.scope:sdk", Versions.P8eScope)
        val Util = DependencySpec("io.provenance.scope:util", Versions.P8eScope)
        val ContractBase = DependencySpec("io.provenance.scope:contract-base", Versions.P8eScope)
        val ContractProto = DependencySpec("io.provenance.scope:contract-proto", Versions.P8eScope)
        val OsGateway = DependencySpec("tech.figure.objectstore.gateway:client", Versions.OsGateway)
    }

    // Spring Boot
    object SpringBoot {
        val Starter = DependencySpec("org.springframework.boot:spring-boot-starter")
        val StarterWebFlux = DependencySpec(
            name = "org.springframework.boot:spring-boot-starter-webflux",
            exclude = listOf("org.springframework.boot:spring-boot-starter-tomcat")
        )
        val StarterJetty = DependencySpec("org.springframework.boot:spring-boot-starter-jetty")
        val StarterActuator = DependencySpec("org.springframework.boot:spring-boot-starter-actuator")
        val StarterDevTools = DependencySpec("org.springframework.boot:spring-boot-devtools")
        val StarterSecurity = DependencySpec("org.springframework.boot:spring-boot-starter-security")
        val StarterValidation = DependencySpec("org.springframework.boot:spring-boot-starter-validation")
        val Swagger = DependencySpec(    "io.springfox:springfox-boot-starter", "3.0.0")
        val SwaggerUI = DependencySpec(    "io.springfox:springfox-swagger-ui", "3.0.0")

        val StarterTest =
            DependencySpec(
                name = "org.springframework.boot:spring-boot-starter-test",
                exclude = listOf(
                    "org.junit.vintage:junit-vintage-engine",
                    "org.mockito:mockito-core"
                )
            )
    }

    // Project Reactor
    object Reactor {
        // https://github.com/reactor/reactor-core
        val Core = DependencySpec("io.projectreactor:reactor-core", Versions.Reactor)
    }

    // Protobuf
    object Protobuf {
        val Java = DependencySpec("com.google.protobuf:protobuf-java", Versions.Protobuf)
        val JavaUtil = DependencySpec("com.google.protobuf:protobuf-java-util", Versions.Protobuf)
    }

    object Kong {
        val Unirest = DependencySpec("com.konghq:unirest-java", Versions.Unirest)
    }

    object Jackson {
        val Databind = DependencySpec(
            "com.fasterxml.jackson.core:jackson-databind",
            "2.12.6.1"
        )
        val Datatype = DependencySpec(
            "com.fasterxml.jackson.datatype:jackson-datatype-jsr310",
            "2.12.+"
        )
        val KotlinModule = DependencySpec(
            "com.fasterxml.jackson.module:jackson-module-kotlin",
            Versions.Jackson
        )
        val Hubspot = DependencySpec(
            "com.hubspot.jackson:jackson-datatype-protobuf",
            "0.9.9-jackson2.9-proto3"
        )
    }

    object Provenance {
        val KeyAccessLib = DependencySpec("io.provenance.originator-key-access-lib:lib", Versions.KeyAccessLib)
        val ProtoKotlin = DependencySpec("io.provenance:proto-kotlin", Versions.ProvenanceProto)
        val AssetModel = DependencySpec("io.provenance.model:metadata-asset-model", Versions.AssetModel)
        val LoanPackage = DependencySpec("io.provenance.loan-package:contract", Versions.LoanPackage)

        object Client {
            val GrpcClientKotlin = DependencySpec("io.provenance.client:pb-grpc-client-kotlin", Versions.ProvenanceClient)
        }

        object HdWallet {
            val HdWallet = DependencySpec("io.provenance.hdwallet:hdwallet", Versions.ProvenanceHdWallet)
            val HdWalletBip39 = DependencySpec("io.provenance.hdwallet:hdwallet-bip39", Versions.ProvenanceHdWallet)
        }
    }

    object Grpc {
        val Protobuf = DependencySpec("io.grpc:grpc-protobuf", Versions.Grpc)
        val Stub = DependencySpec("io.grpc:grpc-stub", Versions.Grpc)
    }

    val KotlinLogging = DependencySpec("io.github.microutils:kotlin-logging-jvm", Versions.KotlinLogging)

    val Ktlint = DependencySpec("com.pinterest:ktlint", Versions.Ktlint)
    val Mockk = DependencySpec("io.mockk:mockk", Versions.Mockk)
    val Kotest = DependencySpec("io.kotest:kotest-runner-junit5-jvm", Versions.Kotest)
    val KotestAssertions = DependencySpec("io.kotest:kotest-assertions-core-jvm", Versions.Kotest)
    val KotestAssertionsArrow = DependencySpec("io.kotest.extensions:kotest-assertions-arrow", Versions.KotestExtensionsArrow)
    val KotestSpring = DependencySpec("io.kotest.extensions:kotest-extensions-spring", Versions.KotestSpring)
    val KotestProperty = DependencySpec("io.kotest:kotest-property", Versions.Kotest)
    val Hamkrest = DependencySpec("com.natpryce:hamkrest", Versions.Hamkrest)
    val Redisson = DependencySpec("org.redisson:redisson", Versions.Redisson)
    val SpringMockk = DependencySpec("com.ninja-squad:springmockk", Versions.SpringMockk)
    val KotlinFaker = DependencySpec("io.github.serpro69:kotlin-faker", Versions.KotlinFaker)
    object Swagger {
        val Annotations = DependencySpec("io.swagger:swagger-annotations", Versions.Swagger)
    }

    val Reflections = DependencySpec("org.reflections:reflections", Versions.Reflections)

    val BouncyCastleProvider = DependencySpec("org.bouncycastle:bcprov-jdk15on", Versions.BouncyCastle)
    val BouncyCastle = DependencySpec("org.bouncycastle:bcpkix-jdk15on", Versions.BouncyCastle)

    object TestContainers {
        val Core = DependencySpec("io.kotest.extensions:kotest-extensions-testcontainers", Versions.TestContainer)
    }
}

data class PluginSpec(
    val id: String,
    val version: String = ""
) {
    fun addTo(scope: PluginDependenciesSpec) {
        scope.also {
            it.id(id).version(version.takeIf { v -> v.isNotEmpty() })
        }
    }

    fun addTo(action: ObjectConfigurationAction) {
        action.plugin(this.id)
    }
}

data class DependencySpec(
    val name: String,
    val version: String = "",
    val isChanging: Boolean = false,
    val exclude: List<String> = emptyList()
) {
    fun plugin(scope: PluginDependenciesSpec) {
        scope.apply {
            id(name).version(version.takeIf { it.isNotEmpty() })
        }
    }

    fun classpath(scope: ScriptHandlerScope) {
        val spec = this
        with(scope) {
            dependencies {
                classpath(spec.toDependencyNotation())
            }
        }
    }

    fun api(handler: DependencyHandlerScope) {
        val spec = this
        with(handler) {
            "api".invoke(spec.toDependencyNotation()) {
                isChanging = spec.isChanging
                spec.exclude.forEach { excludeDependencyNotation ->
                    val (group, module) = excludeDependencyNotation.split(":", limit = 2)
                    this.exclude(group = group, module = module)
                }
            }
        }
    }

    fun implementation(handler: DependencyHandlerScope) {
        val spec = this
        with(handler) {
            "implementation".invoke(spec.toDependencyNotation()) {
                isChanging = spec.isChanging
                spec.exclude.forEach { excludeDependencyNotation ->
                    val (group, module) = excludeDependencyNotation.split(":", limit = 2)
                    this.exclude(group = group, module = module)
                }
            }
        }
    }

    fun testImplementation(handler: DependencyHandlerScope) {
        val spec = this
        with(handler) {
            "testImplementation".invoke(spec.toDependencyNotation()) {
                isChanging = spec.isChanging
                spec.exclude.forEach { excludeDependencyNotation ->
                    val (group, module) = excludeDependencyNotation.split(":", limit = 2)
                    this.exclude(group = group, module = module)
                }
            }
        }
    }

    fun toDependencyNotation(): String =
        listOfNotNull(
            name,
            version.takeIf { it.isNotEmpty() }
        ).joinToString(":")
}
