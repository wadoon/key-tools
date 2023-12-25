plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.github.wadoon.keytools"
version = "0.9"

val plugin by configurations

tasks.getByName<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    configurations = listOf(plugin)
}


dependencies {
    implementation("org.key-project:key.core:2.12.2")
    implementation("org.key-project:key.util:2.12.2")
    implementation("org.key-project:key.ui:2.12.2")
    implementation("org.slf4j:slf4j-api:2.0.9")

    testImplementation("org.slf4j:slf4j-simple:2.0.9")

    val plugin by configurations
    plugin("com.atlassian.commonmark:commonmark:0.17.0")
    plugin("com.atlassian.commonmark:commonmark-ext-gfm-tables:0.17.0")
    plugin("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    plugin("com.fasterxml.jackson.core:jackson-databind:2.16.0")
    plugin("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.16.0")
    plugin("com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.16.1")
    plugin("org.ocpsoft.prettytime:prettytime:5.0.7.Final")
}