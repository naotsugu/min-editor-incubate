plugins {
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":javafx"))
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
    mainModule = "code.editor"
    if (providers.systemProperty("debug").isPresent) {
        applicationDefaultJvmArgs = applicationDefaultJvmArgs.plus(listOf("-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"))
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
