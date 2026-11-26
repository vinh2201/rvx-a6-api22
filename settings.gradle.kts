rootProject.name = "revanced-patches-android6"

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        maven {
            name = "githubPackages"
            url = uri("https://maven.pkg.github.com/revanced/revanced-patches-gradle-plugin")
            // Provided by the CI via ORG_GRADLE_PROJECT_githubPackagesUsername/Password
            // (or ~/.gradle/gradle.properties: githubPackagesUsername / githubPackagesPassword).
            credentials(PasswordCredentials::class)
        }
    }
}

plugins {
    // Same plugin version as the official ReVanced patches (patcher 22.x).
    id("app.revanced.patches") version "1.0.0-dev.10"
}

settings {
    extensions {
        // Namespace given to every extension module by the plugin.
        defaultNamespace = "app.revanced.extension"

        // Must resolve to an absolute path (not relative),
        // otherwise the extensions in subfolders will fail to find the proguard config.
        proguardFiles(rootProject.projectDir.resolve("extensions/proguard-rules.pro").toString())
    }
}

include(":patches:stub")
