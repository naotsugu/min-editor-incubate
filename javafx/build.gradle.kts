import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform;

plugins {
    `java-library`
    id("org.gradlex.extra-java-module-info") version "1.8"
}

repositories {
    mavenCentral()
}

val os   = DefaultNativePlatform.getCurrentOperatingSystem()
val arch = DefaultNativePlatform.getCurrentArchitecture()
val artifact = when {
    os.isMacOsX  && arch.isArm64 -> "mac-aarch64"
    os.isMacOsX  && arch.isAmd64 -> "mac"
    os.isLinux   && arch.isArm64 -> "linux-aarch64"
    os.isLinux   && arch.isAmd64 -> "linux"
    os.isWindows && arch.isAmd64 -> "win"
    else -> throw Error("Unsupported OS: $os, ARCH: $arch")
}

dependencies {
    api("org.openjfx:javafx-graphics:22:${artifact}")
    api("org.openjfx:javafx-base:22:${artifact}")
//    testImplementation(libs.junit.jupiter)
//    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(22)
    }
}
//tasks.named<Test>("test") {
//    useJUnitPlatform()
//}

extraJavaModuleInfo {
    module("org.openjfx:javafx-graphics", "javafx.graphics") {
        patchRealModule()
        requires("java.desktop")
        requires("java.xml")
        requires("jdk.unsupported")

        requiresTransitive("javafx.base")

        exports("javafx.animation")
        exports("javafx.application")
        exports("javafx.concurrent")
        exports("javafx.css")
        exports("javafx.css.converter")
        exports("javafx.geometry")
        exports("javafx.print")
        exports("javafx.scene")
        exports("javafx.scene.canvas")
        exports("javafx.scene.effect")
        exports("javafx.scene.image")
        exports("javafx.scene.input")
        exports("javafx.scene.layout")
        exports("javafx.scene.paint")
        exports("javafx.scene.robot")
        exports("javafx.scene.shape")
        exports("javafx.scene.text")
        exports("javafx.scene.transform")
        exports("javafx.stage")

        exports("com.sun.glass.ui",
            "javafx.media",
            "javafx.swing",
            "javafx.web")
        exports("com.sun.glass.utils",
            "javafx.media",
            "javafx.web")
        exports("com.sun.javafx.application",
            "java.base",
            "javafx.controls",
            "javafx.swing",
            "javafx.web")
        exports("com.sun.javafx.css",
            "javafx.controls")
        exports("com.sun.javafx.cursor",
            "javafx.swing")
        exports("com.sun.javafx.embed",
            "javafx.swing")
        exports("com.sun.javafx.font",
            "javafx.web",
            "code.javafx")
        exports("com.sun.javafx.geom",
            "javafx.controls",
            "javafx.media",
            "javafx.swing",
            "javafx.web",
            "code.javafx")
        exports("com.sun.javafx.geom.transform",
            "javafx.controls",
            "javafx.media",
            "javafx.swing",
            "javafx.web",
            "code.javafx")
        exports("com.sun.javafx.iio",
            "javafx.web")
        exports("com.sun.javafx.menu",
            "javafx.controls")
        exports("com.sun.javafx.scene",
            "javafx.controls",
            "javafx.media",
            "javafx.swing",
            "javafx.web")
        exports("com.sun.javafx.scene.input",
            "javafx.controls",
            "javafx.swing",
            "javafx.web")
        exports("com.sun.javafx.scene.layout",
            "javafx.controls",
            "javafx.web")
        exports("com.sun.javafx.scene.text",
            "javafx.controls",
            "javafx.web",
            "code.javafx")
        exports("com.sun.javafx.scene.traversal",
            "javafx.controls",
            "javafx.web")
        exports("com.sun.javafx.sg.prism",
            "javafx.media",
            "javafx.swing",
            "javafx.web")
        exports("com.sun.javafx.stage",
            "javafx.controls",
            "javafx.swing")
        exports("com.sun.javafx.text",
            "javafx.web")
        exports("com.sun.javafx.tk",
            "javafx.controls",
            "javafx.media",
            "javafx.swing",
            "javafx.web",
            "code.javafx")
        exports("com.sun.javafx.util",
            "javafx.controls",
            "javafx.fxml",
            "javafx.media",
            "javafx.swing",
            "javafx.web")
        exports("com.sun.prism",
            "javafx.media",
            "javafx.web")
        exports("com.sun.prism.image",
            "javafx.web")
        exports("com.sun.prism.paint",
            "javafx.web")
        exports("com.sun.scenario.effect",
            "javafx.web")
        exports("com.sun.scenario.effect.impl",
            "javafx.web")
        exports("com.sun.scenario.effect.impl.prism",
            "javafx.web")
    }
}