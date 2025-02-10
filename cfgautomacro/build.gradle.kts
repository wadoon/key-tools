import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.github.wadoon.keytools"
version = "0.1"

val plugin by configurations

dependencies {
    implementation("org.key-project:key.core:2.12.3")
    implementation("org.key-project:key.util:2.12.3")
}

tasks.register<ShadowJar>("miniShadowJar") {
    group = "shadow"
    archiveClassifier.set("mini")
    from(sourceSets.getByName("main").output)
    configurations = listOf(plugin)
}