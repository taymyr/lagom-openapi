@file:Suppress("UnstableApiUsage")

plugins {
    scala
    id("cz.alenkacz.gradle.scalafmt") version Versions.`scalafmt-plugin`
    signing
}

dependencies {
    implementation(project(":scala:lagom-openapi-scala-api"))
    implementation(project(":lagom-openapi-core"))
    compileOnly("com.lightbend.lagom", "lagom-scaladsl-server_$scalaBinaryVersion", lagomVersion)

    testImplementation("org.scalatest", "scalatest_$scalaBinaryVersion", Versions.scalatest)
    testImplementation(evaluationDependsOn(":lagom-openapi-core").sourceSets.test.get().output)
}

configurations {
    testCompile.get().extendsFrom(compileOnly.get())
}

val scalaTest by tasks.creating(JavaExec::class) {
    main = "org.scalatest.tools.Runner"
    args = listOf("-R", "$buildDir/classes/scala/test", "-o")
    classpath = sourceSets.test.get().runtimeClasspath
    dependsOn(tasks.testClasses)
}
tasks.test { dependsOn(scalaTest) }
jacoco { applyTo(scalaTest) }

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

signing {
    isRequired = isRelease
    sign(publishing.publications["maven"])
}