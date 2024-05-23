pluginManagement {
    repositories {
        maven { url = uri("https://androidx.dev/snapshots/builds/11681059/artifacts/repository/") }

        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven { url = uri("https://androidx.dev/snapshots/builds/11670047/artifacts/repository/") }
        google()
        mavenCentral()
    }
}

rootProject.name = "Experiments and tests"
include(":app")
