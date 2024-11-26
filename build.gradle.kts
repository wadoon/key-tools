import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.sonarqube.gradle.SonarQubePlugin

plugins {
    id("org.jetbrains.kotlin.jvm") version "2.0.21" apply (false)
    id("org.jetbrains.dokka") version "1.9.20" apply (false)
    `maven-publish`
    `java-library`
    id("com.diffplug.spotless") version "6.25.0" apply false
    id("org.sonarqube") version "6.0.0.5145"

    id("com.github.ben-manes.versions") version "0.51.0"
}

repositories {
    mavenCentral()
}

sonar {
    properties {
        property("sonar.projectKey", "wadoon_key-tools")
        property("sonar.organization", "wadoon")
        property("sonar.host.url", "https://sonarcloud.io")
    }
}


subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.dokka")
    apply(plugin = "maven-publish")
    apply(plugin = "java-library")
    //apply(plugin = "com.diffplug.spotless")

    val plugin by configurations.creating
    configurations {
        implementation.get().extendsFrom(plugin)
    }


    repositories {
        mavenCentral()
        maven { url = uri("https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven") }
    }

    dependencies {
        val implementation by configurations

        val plugin by configurations
        plugin(platform("org.jetbrains.kotlin:kotlin-bom"))
        plugin("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
        plugin("com.github.ajalt:clikt:2.8.0")
        plugin("org.jetbrains:annotations:26.0.1")
        plugin("org.slf4j:slf4j-api:2.0.16")

        //    implementation("org.key-project:key.core")

        val testImplementation by configurations
        testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.3")
        testImplementation("org.junit.jupiter:junit-jupiter-params:5.11.3")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.11.3")
        testImplementation("com.google.truth:truth:1.4.4")
        testImplementation("org.slf4j:slf4j-simple:2.0.16")
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "11"
        }
    }

    tasks.withType<JavaCompile> {
        options.release.set(11)
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        reports.html.required.set(false)
        reports.junitXml.required.set(true)
        testLogging {
            events("passed", "skipped", "failed")
            showExceptions = true
        }
    }

    java {
        withJavadocJar()
        withSourcesJar()
    }

    tasks.withType<Javadoc>() {
        isFailOnError = false
    }

    /*configure<SpotlessExtension> { // if you are using build.gradle.kts, instead of 'spotless {' use:
        // configure<com.diffplug.gradle.spotless.SpotlessExtension> {
        kotlin {
            // by default the target is every '.kt' and '.kts` file in the java sourcesets
            //ktfmt()    // has its own section below
            ktlint().userData(
                mapOf(
                    "disabled_rules" to "no-wildcard-imports",
                    "insert_final_newline" to "true"
                )
            )
            // has its own section below
            //diktat()   // has its own section below
            //prettier() // has its own section below
            licenseHeaderFile("$rootDir/gradle/license_header")  // '/* (C)$YEAR */' // or licenseHeaderFile
        }
        kotlinGradle {
            //target("*.gradle.kts") // default target for kotlinGradle
            //ktlint() // or ktfmt() or prettier()
        }
    }*/



    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                from(components["java"])
                pom {
                    url.set("http://github.com/wadoon/key-tools")
                    licenses {
                        license {
                            name.set("GPLv2+")
                            url.set("https://www.gnu.org/licenses/old-licenses/gpl-2.0.html")
                        }
                    }
                    developers {
                        developer {
                            id.set("weigl")
                            name.set("Alexander Weigl ")
                            email.set("weigl@kit.edu")
                        }
                    }
                    scm {
                        connection.set("scm:git:https://github.com/wadoon/key-tools.git")
                        developerConnection.set("scm:git:git@github.com:wadoon/key-tools.git")
                        url.set("http://github.com/wadoon/key-tools")
                    }
                }
            }
            repositories {
                maven {
                    name = "GitHubPackages"
                    url = uri("https://maven.pkg.github.com/wadoon/key-tools")
                    credentials {
                        username = System.getenv("GITHUB_ACTOR")
                        password = System.getenv("GITHUB_TOKEN")
                    }
                }
            }
        }
    }
}
