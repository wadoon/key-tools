import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("application")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.github.wadoon.keytools"
version = "1.4.0"


dependencies {
    implementation("org.key-project:key.core:2.12.2")
    implementation("org.key-project:key.util:2.12.2")

    val plugin by configurations
    plugin("org.slf4j:slf4j-simple:2.0.13")
    plugin("com.google.code.gson:gson:2.11.0")
}

val plugin by configurations

tasks.register<ShadowJar>("miniShadowJar") {
    group = "shadow"
    archiveClassifier.set("mini")
    /*dependencies {
        exclude(dependency("org.key-project:key.core"))
        exclude(dependency("org.key-project:key.util"))
    }*/
    from(sourceSets.getByName("main").output)
    configurations = listOf(plugin)
    manifest {
        this.attributes(
            "Main-Class" to "de.uka.ilkd.key.CheckerKt"
        )
    }
}

application {
    mainClass = "de.uka.ilkd.key.CheckerKt"
}


run {}
