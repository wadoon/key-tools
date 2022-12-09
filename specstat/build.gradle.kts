import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("application")
    id("antlr")
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "com.github.wadoon.keytools"
version = "0.1.0"


dependencies {
    val plugin by configurations
    plugin("org.slf4j:slf4j-simple:2.0.5")
    plugin("com.google.code.gson:gson:2.10")
}

val plugin by configurations

tasks.register<ShadowJar>("miniShadowJar") {
    group = "shadow"
    archiveClassifier.set("mini")
    /*dependencies {
        exclude(dependency("org.key_project:key.core"))
        exclude(dependency("org.key_project:key.util"))
    }*/
    from(sourceSets.getByName("main").output)
    configurations = listOf(plugin)
    manifest {
        this.attributes(
            "Main-Class" to "com.github.wadoon.keytools.SpecStat"
        )
    }
}

application {
    mainClass.set("com.github.wadoon.keytools.SpecStat")
}

run {}
