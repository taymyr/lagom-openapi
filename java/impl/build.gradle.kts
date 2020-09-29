@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType
import java.net.URL

plugins {
    kotlin("jvm")
    id("org.jetbrains.dokka") version Versions.dokka
    id("org.jlleitschuh.gradle.ktlint") version Versions.`ktlint-plugin`
    id("io.freefair.lombok") version Versions.lombok
    signing
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions.jvmTarget = "1.8"
compileKotlin.kotlinOptions.freeCompilerArgs += listOf("-Xjvm-default=enable", "-Xjsr305=strict")

val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions.jvmTarget = "1.8"
compileTestKotlin.kotlinOptions.freeCompilerArgs += listOf("-Xjvm-default=enable", "-Xjsr305=strict")

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    implementation(project(":lagom-openapi-core"))
    api(project(":java:lagom-openapi-java-api"))
    api("io.github.microutils", "kotlin-logging", Versions.`kotlin-logging`)
    compileOnly("com.lightbend.lagom", "lagom-javadsl-server_$scalaBinaryVersion", lagomVersion)

    testImplementation(evaluationDependsOn(":lagom-openapi-core").sourceSets.test.get().output)
    testImplementation("org.junit.jupiter", "junit-jupiter-api", Versions.junit5)
    testImplementation("org.junit.jupiter", "junit-jupiter-params", Versions.junit5)
    testImplementation("org.junit.jupiter", "junit-jupiter-engine", Versions.junit5)
    testImplementation("org.assertj", "assertj-core", Versions.assertj)
    testImplementation("net.javacrumbs.json-unit", "json-unit-assertj", Versions.`json-unit`)
}

configurations {
    testCompile.get().extendsFrom(compileOnly.get())
}

ktlint {
    version.set(Versions.ktlint)
    outputToConsole.set(true)
    reporters.set(setOf(ReporterType.CHECKSTYLE))
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

val sourcesJar by tasks.creating(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

val dokkaJar by tasks.creating(Jar::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    archiveClassifier.set("javadoc")
    from(tasks.dokka)
}

tasks.dokka {
    outputFormat = "javadoc"
    outputDirectory = "$buildDir/javadoc"
    configuration {
        jdkVersion = 8
        reportUndocumented = false
        externalDocumentationLink {
            url = URL("https://www.lagomframework.com/documentation/1.6.x/java/api/")
        }
    }
    impliedPlatforms = mutableListOf("JVM")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "${project.name}_$scalaBinaryVersion"
            from(components["java"])
            artifact(sourcesJar)
            artifact(dokkaJar)
            pom(Publishing.pom)
        }
    }
}

signing {
    isRequired = isRelease
    sign(publishing.publications["maven"])
}