rootProject.name = "p8e-cee-api"

pluginManagement {
    // builds before buildSrc extensions are available :(
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

include(
    "service"
)
