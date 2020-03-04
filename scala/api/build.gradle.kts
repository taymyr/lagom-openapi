plugins {
    scala
    id("cz.alenkacz.gradle.scalafmt") version Versions.`scalafmt-plugin`
    id("de.marcphilipp.nexus-publish") version Versions.`nexus-publish`
    signing
}

dependencies {
    compileOnly("com.lightbend.lagom", "lagom-scaladsl-api_$scalaBinaryVersion", lagomVersion)
    compileOnly("io.swagger.core.v3", "swagger-annotations", Versions.swagger)
}

val sourcesJar by tasks.creating(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

val scalaDocJar by tasks.creating(Jar::class) {
    archiveClassifier.set("javadoc")
    from(tasks.scaladoc)
}

tasks.check { dependsOn(tasks.checkScalafmtAll) }

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "${project.name}_$scalaBinaryVersion"
            from(components["java"])
            artifact(sourcesJar)
            artifact(scalaDocJar)
            pom(Publishing.pom)
        }
    }
}

@Suppress("UnstableApiUsage")
signing {
    isRequired = isRelease
    sign(publishing.publications["maven"])
}