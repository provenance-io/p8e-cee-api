val ktlint: Configuration by configurations.creating

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.detekt)
    `java-library`
    `maven-publish`
}

java.sourceCompatibility = JavaVersion.VERSION_11

dependencies {
    ktlint(libs.ktlint)

    listOf(
        libs.protobuf.java.util,
        libs.p8eScope.contract.base,
        libs.p8eScope.contract.proto,
        libs.p8eScope.objectStore.client,
        libs.kotlin.coroutines.reactor,
        libs.jackson.databind,
        libs.assetClassification.client,
        libs.assetClassification.verifier,
        libs.provenance.keyAccessLib,
    ).forEach { dependency ->
        implementation(dependency)
    }

    implementation(libs.provenance.metadataAssetModel) {
        version {
            strictly(libs.versions.metadataAssetModel.get())
        }
    }
}

java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "io.provenance.p8e-cee-api"
            artifactId = project.name

            from(components["java"])

            pom {
                name.set("Provenance p8e-cee-api data classes")
                description.set("Provenance p8e-cee-api data classes")
                url.set("https://provenance.io")

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }

                developers {
                    developer {
                        id.set("cworsnop-figure")
                        name.set("Cody Worsnop")
                        email.set("cworsnop@figure.com")
                    }
                }

                scm {
                    connection.set("git@github.com:provenance-io/p8e-cee-api.git")
                    developerConnection.set("git@github.com:provenance-io/p8e-cee-api.git")
                    url.set("https://github.com/provenance-io/p8e-cee-api")
                }
            }
        }
    }

    signing {
        sign(publishing.publications["maven"])
    }

    tasks.javadoc {
        if(JavaVersion.current().isJava9Compatible) {
            (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
        }
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

detekt {
    toolVersion = libs.versions.detekt.get()
    buildUponDefaultConfig = true
    config = files("${rootDir.path}/detekt.yml")
    input = files("src/main/kotlin", "src/test/kotlin")
}
