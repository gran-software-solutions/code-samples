plugins {
    id("org.jetbrains.kotlin.jvm") version "1.8.10"
    kotlin("plugin.serialization") version "1.8.10"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
    implementation("com.github.ajalt.clikt:clikt:3.5.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
    implementation("org.kodein.di:kodein-di:7.19.0")
    implementation("com.charleskorn.kaml:kaml:0.53.0")
    implementation(project(":core"))
    implementation(project(":weather-provider-adapter"))
    implementation(project(":placeinfo-provider-adapter"))
}

application {
    mainClass.set("de.gransoftware.MainKt")
}