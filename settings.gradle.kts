plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "min-editor-incubate"
include("javafx", "app")

include(":piecetable")
project(":piecetable").projectDir = file("piecetable/lib")