plugins {
    application
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

val ktor_version = "2.2.1"

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("io.ktor:ktor-server-call-logging:$ktor_version")
    implementation("io.ktor:ktor-server-sessions:$ktor_version")
    implementation("io.ktor:ktor-server-auto-head-response:$ktor_version")
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("io.ktor:ktor-server-html-builder:$ktor_version")
    implementation("io.ktor:ktor-server-status-pages:$ktor_version")
    implementation("io.ktor:ktor-server-default-headers:$ktor_version")
    implementation("io.ktor:ktor-server-swagger:$ktor_version")
    implementation("io.ktor:ktor-server-cors:$ktor_version")
    implementation("io.ktor:ktor-server-compression:$ktor_version")
    implementation("io.ktor:ktor-server-data-conversion:$ktor_version")

    implementation("io.github.microutils:kotlin-logging:3.0.4")
    implementation("org.slf4j:slf4j-simple:2.0.5")

    implementation("org.key_project:key.core:2.10.0")
    implementation("org.key_project:key.util:2.10.0")
    testImplementation("io.ktor:ktor-server-test-host-jvm:2.2.1")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:1.7.22")
}

repositories {
    mavenCentral()
}

application {
    mainClass.set("org.key_project.web.Server")
}
