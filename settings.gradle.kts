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
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
<<<<<<< HEAD
=======
        gradlePluginPortal()
>>>>>>> a069341194d7ff5844d7cd893c81f7cc4c5d0971
    }
}

rootProject.name = "doanck"
include(":app")
<<<<<<< HEAD
 
=======
>>>>>>> a069341194d7ff5844d7cd893c81f7cc4c5d0971
