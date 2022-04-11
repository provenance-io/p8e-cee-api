import org.gradle.api.plugins.ObjectConfigurationAction
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.ScriptHandlerScope
import org.gradle.kotlin.dsl.exclude
import org.gradle.plugin.use.PluginDependenciesSpec

object Versions {
    // Branch targets for internal deps
    const val Master = "master-+"
    const val Develop = "develop-+"

    const val ProvenanceCore = Master
    const val StreamData = Master

    const val Kotlin = "1.5.31"
    const val KotlinCoroutines = "1.5.2"
    const val Protobuf = "3.18.1"
    const val Kafka = "2.7.0"
    const val SpringBoot = "2.5.6"
    const val KotlinLogging = "2.0.11"
    const val Reactor = "3.4.9"
    const val Jackson = "2.12.5"
    const val Redisson = "3.16.0"
    const val Flyaway = "8.0.2"
    const val Ktlint = "0.42.1"
    const val Detekt = "1.18.1"
    const val Hamkrest = "1.8.0.1"
    const val Mockk = "1.12.0"
    const val Kotest = "5.2.+"
    const val KotestExtensionsArrow = "1.2.+"
    const val KotlinFaker = "1.7.1"
    const val SpringMockk = "3.0.1"
    const val Swagger = "1.6.2"
    const val AssetModel = "0.1.2"
    const val P8eScope = "0.4.9"
    const val ProvenancePbc = Master
    const val ProvenanceProtobuf = Master
    const val WalletPbClient = Develop
    const val ProvenanceHdWallet = "0.1.15"
    const val ProvenanceClient = "1.0.5"
    const val Unirest = "3.13.6"
    const val KeyAccessLib = "0.2.+"
}

object Plugins { // please keep this sorted in sections

    // Kotlin
    val Kotlin = PluginSpec("kotlin", Versions.Kotlin)

    // 3rd Party
    val Detekt = PluginSpec("io.gitlab.arturbosch.detekt", Versions.Detekt)
    val Flyway = PluginSpec("org.flywaydb.flyway", Versions.Flyaway)
    val Idea = PluginSpec("idea")
    val Protobuf = PluginSpec("com.google.protobuf", "0.8.16")
    val SpringBoot = PluginSpec("org.springframework.boot", Versions.SpringBoot)
    val SpringDependencyManagement = PluginSpec("io.spring.dependency-management", "1.0.11.RELEASE")
}

object Dependencies {
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

    object P8eScope {
        val Encryption = DependencySpec("io.provenance.scope:encryption", Versions.P8eScope)
        val OsClient = DependencySpec("io.provenance.scope:os-client", Versions.P8eScope)
        val Sdk = DependencySpec("io.provenance.scope:sdk", Versions.P8eScope)
        val Util = DependencySpec("io.provenance.scope:util", Versions.P8eScope)
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

    // Apache Kafka
    object Kafka {
        val Clients = DependencySpec("org.apache.kafka:kafka-clients", Versions.Kafka)
    }

    object Kong {
        val Unirest = DependencySpec("com.konghq:unirest-java", Versions.Unirest)
    }

    object Jackson {
        val Databind = DependencySpec(
            "com.fasterxml.jackson.core:jackson-databind",
            "2.12.+"
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
        val CoreKcache = DependencySpec("io.provenance:core-kcache", Versions.ProvenanceCore)
        val CoreKcacheSpring = DependencySpec("io.provenance:core-kcache-spring", Versions.ProvenanceCore)
        val CoreKafkaAggregator = DependencySpec("io.provenance:core-kafka-aggregator", Versions.ProvenanceCore)
        val CoreKafkaAggregatorSpring =
            DependencySpec(
                "io.provenance:core-kafka-aggregator-spring",
                Versions.ProvenanceCore,
                exclude = listOf(
                    "org.springframework.boot:spring-boot-starter-web",
                    "org.springframework.boot:spring-boot-starter-security"
                )
            )
        val CoreLogging = DependencySpec("io.provenance:core-logging", Versions.ProvenanceCore)
        val AssetModel = DependencySpec("io.provenance.model:metadata-asset-model", Versions.AssetModel)
        object Client {
            val GrpcClientKotlin = DependencySpec("io.provenance.client:pb-grpc-client-kotlin", Versions.ProvenanceClient)
        }

        object HdWallet {
            val HdWallet = DependencySpec("io.provenance.hdwallet:hdwallet", Versions.ProvenanceHdWallet)
            val HdWalletBase58 = DependencySpec("io.provenance.hdwallet:hdwallet-base58", Versions.ProvenanceHdWallet)
            val HdWalletBech32 = DependencySpec("io.provenance.hdwallet:hdwallet-bech32", Versions.ProvenanceHdWallet)
            val HdWalletBip32 = DependencySpec("io.provenance.hdwallet:hdwallet-bip32", Versions.ProvenanceHdWallet)
            val HdWalletBip39 = DependencySpec("io.provenance.hdwallet:hdwallet-bip39", Versions.ProvenanceHdWallet)
            val HdWalletBip44 = DependencySpec("io.provenance.hdwallet:hdwallet-bip44", Versions.ProvenanceHdWallet)
            val HdWalletEc = DependencySpec("io.provenance.hdwallet:hdwallet-ec", Versions.ProvenanceHdWallet)
            val HdWalletSigner = DependencySpec("io.provenance.hdwallet:hdwallet-signer", Versions.ProvenanceHdWallet)
            val HdWalletCommon = DependencySpec("io.provenance.hdwallet:hdwallet-common", Versions.ProvenanceHdWallet)
        }
    }

    val KotlinLogging = DependencySpec("io.github.microutils:kotlin-logging-jvm", Versions.KotlinLogging)

    val Ktlint = DependencySpec("com.pinterest:ktlint", Versions.Ktlint)
    val Mockk = DependencySpec("io.mockk:mockk", Versions.Mockk)
    val Kotest = DependencySpec("io.kotest:kotest-runner-junit5-jvm", Versions.Kotest)
    val KotestAssertions = DependencySpec("io.kotest:kotest-assertions-core-jvm", Versions.Kotest)
    val KotestAssertionsArrow = DependencySpec("io.kotest.extensions:kotest-assertions-arrow", Versions.KotestExtensionsArrow)
    val Hamkrest = DependencySpec("com.natpryce:hamkrest", Versions.Hamkrest)
    val Redisson = DependencySpec("org.redisson:redisson", Versions.Redisson)
    val SpringMockk = DependencySpec("com.ninja-squad:springmockk", Versions.SpringMockk)
    val KotlinFaker = DependencySpec("io.github.serpro69:kotlin-faker", Versions.KotlinFaker)

    object Swagger {
        val Annotations = DependencySpec("io.swagger:swagger-annotations", Versions.Swagger)
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
