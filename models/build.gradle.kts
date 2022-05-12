val ktlint: Configuration by configurations.creating

plugins {
    Plugins.Detekt.addTo(this)
    `java-library`
    `maven-publish`
}

java.sourceCompatibility = JavaVersion.VERSION_11

dependencies {
    ktlint(Dependencies.Ktlint.toDependencyNotation())

    listOf(
        Dependencies.Protobuf.JavaUtil,
        Dependencies.P8eScope.ContractProto,
        Dependencies.P8eScope.ContractBase,
        Dependencies.Provenance.AssetModel,
        Dependencies.P8eScope.OsClient,
        Dependencies.Kotlin.CoroutinesReactor,
        Dependencies.Jackson.Databind,
    ).forEach { dep ->
        dep.implementation(this)
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
    toolVersion = Versions.Detekt
    buildUponDefaultConfig = true
    config = files("${rootDir.path}/detekt.yml")
    input = files("src/main/kotlin", "src/test/kotlin")
}
