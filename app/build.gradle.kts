plugins {
    application
    id("org.openjfx.javafxplugin") version "0.1.0"
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(22)
    }
}

application {
    mainClass = "code.editor.App"
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

javafx {
    version = "22"
    modules("javafx.base", "javafx.graphics")
}