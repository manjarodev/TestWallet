pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://jitpack.io") }
//        maven { url = uri("https://mvnrepository.com/artifact/com.reown/walletkit/1.2.0") }


    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
//        maven { url = uri("https://mvnrepository.com/artifact/com.reown/walletkit/1.2.0") }
    }
}

rootProject.name = "DigitalWalletDemo"
include(":app")
