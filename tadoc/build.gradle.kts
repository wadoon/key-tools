plugins {
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

application {
    mainClass = "org.key-project.core.doc.AppKt"
}

dependencies {
    implementation("org.key-project:key.core:2.12.3")
    implementation("org.key-project:key.util:2.12.3")

    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.11.0")
    implementation("com.atlassian.commonmark:commonmark:0.17.0")
    implementation("com.atlassian.commonmark:commonmark-ext-gfm-tables:0.17.0")
    implementation("com.atlassian.commonmark:commonmark-ext-autolink:0.17.0")
    implementation("com.atlassian.commonmark:commonmark-ext-gfm-strikethrough:0.17.0")
    implementation("com.atlassian.commonmark:commonmark-ext-ins:0.17.0")
    implementation("com.atlassian.commonmark:commonmark-ext-heading-anchor:0.17.0")
//    compile group: 'org.eclipse.jgit', name: 'org.eclipse.jgit', version: '5.7.0.202003110725-r'
}