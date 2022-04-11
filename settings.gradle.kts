rootProject.name = "service-loan-onboarding"

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
