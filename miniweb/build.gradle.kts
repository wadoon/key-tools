plugins {
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

val ktor_version = "1.6.7"

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("io.ktor:ktor-server-netty:3.1.3")
    implementation("io.ktor:ktor-html-builder:1.6.8")
    implementation("io.github.microutils:kotlin-logging:4.0.0-beta-2")
    implementation("org.slf4j:slf4j-simple:2.0.17")

    implementation("org.key-project:key.core:2.12.3")
    implementation("org.key-project:key.util:2.12.3")
}

repositories {
    mavenCentral()
}

application {
    mainClass.set("org.key-project.web.Server")
}
