import org.jetbrains.dokka.DokkaConfiguration
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType
import java.net.URL

val isReleaseVersion = !version.toString().endsWith("SNAPSHOT")

val ossrhUsername: String? by project
val ossrhPassword: String? by project

object Versions {
    const val scalaBinary = "2.12"
    const val lagom = "1.4.12" // "1.5.1"
    const val ktlint = "0.32.0"
    const val `kotlin-logging` = "1.6.10"
    const val config4k = "0.4.1"
    const val `lagom-extensions` = "0.1.0"
    const val swagger = "2.0.7"
    const val jacoco = "0.8.2"
    const val junit5 = "5.3.2"
    const val assertj = "3.11.1"
    const val `json-unit` = "2.6.1"
}

val lagomVersion = project.properties["lagomVersion"] as String? ?: Versions.lagom
val scalaBinaryVersion = project.properties["scalaBinaryVersion"] as String? ?: Versions.scalaBinary

plugins {
    kotlin("jvm") version "1.3.30"
    id("org.jetbrains.dokka") version "0.9.18"
    id("org.jlleitschuh.gradle.ktlint") version "8.0.0"
    id("de.marcphilipp.nexus-publish") version "0.2.0"
    signing
    jacoco
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
    implementation(project(":java:lagom-openapi-java-api"))
    implementation("io.github.microutils", "kotlin-logging", Versions.`kotlin-logging`)
    implementation("io.swagger.core.v3", "swagger-core", Versions.swagger)
    implementation("io.swagger.core.v3", "swagger-integration", Versions.swagger)
    implementation("io.github.config4k", "config4k", Versions.config4k)
    implementation("com.lightbend.lagom", "lagom-javadsl-server_$scalaBinaryVersion", lagomVersion)
    implementation("org.taymyr.lagom", "lagom-extensions-java_$scalaBinaryVersion", Versions.`lagom-extensions`)

    testImplementation("org.junit.jupiter", "junit-jupiter-api", Versions.junit5)
    testImplementation("org.junit.jupiter", "junit-jupiter-params", Versions.junit5)
    testImplementation("org.junit.jupiter", "junit-jupiter-engine", Versions.junit5)
    testImplementation("org.assertj", "assertj-core", Versions.assertj)
    testImplementation("net.javacrumbs.json-unit", "json-unit-assertj", Versions.`json-unit`)
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
    finalizedBy("jacocoTestReport")
}

jacoco {
    toolVersion = Versions.jacoco
}
tasks.jacocoTestReport {
    reports {
        xml.isEnabled = true
    }
}

val sourcesJar by tasks.creating(Jar::class) {
    classifier = "sources"
    from(sourceSets.main.get().allSource)
}

val dokkaJar by tasks.creating(Jar::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    classifier = "javadoc"
    from(tasks.dokka)
}

tasks.dokka {
    outputFormat = "javadoc"
    outputDirectory = "$buildDir/javadoc"
    jdkVersion = 8
    reportUndocumented = true
    impliedPlatforms = mutableListOf("JVM")
    externalDocumentationLink(delegateClosureOf<DokkaConfiguration.ExternalDocumentationLink.Builder> {
        url = URL("https://www.lagomframework.com/documentation/1.4.x/java/api/")
    })
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "${project.name}_$scalaBinaryVersion"
            from(components["java"])
            artifact(sourcesJar)
            artifact(dokkaJar)
            pom {
                name.set("Taymyr: OpenAPI Java API")
                description.set("OpenAPI module for Lagom framework")
                url.set("https://taymyr.org")
                organization {
                    name.set("Digital Economy League")
                    url.set("https://www.digitalleague.ru/")
                }
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("taymyr")
                        name.set("Taymyr Contributors")
                        email.set("contributors@taymyr.org")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/taymyr/lagom-openapi.git")
                    developerConnection.set("scm:git:https://github.com/taymyr/lagom-openapi.git")
                    url.set("https://github.com/taymyr/lagom-openapi")
                    tag.set("HEAD")
                }
            }
        }
    }
}

signing {
    isRequired = isReleaseVersion
    sign(publishing.publications["maven"])
}