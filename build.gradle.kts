plugins {
    kotlin("jvm") version Versions.kotlin apply false
    id("io.codearte.nexus-staging") version Versions.`nexus-staging`
    jacoco
    base
}

allprojects {
    repositories {
        mavenCentral()
        jcenter()
    }
}

subprojects {
    group = "org.taymyr.lagom"
    version = "1.1.0-SNAPSHOT"

    apply<JacocoPlugin>()

    jacoco {
        toolVersion = Versions.jacoco
    }
}

val jacocoAggregateMerge by tasks.creating(JacocoMerge::class) {
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    executionData(
        project(":lagom-openapi-core").buildDir.absolutePath + "/jacoco/test.exec",
        project(":java:lagom-openapi-java-impl").buildDir.absolutePath + "/jacoco/test.exec",
        project(":scala:lagom-openapi-scala-impl").buildDir.absolutePath + "/jacoco/scalaTest.exec"
    )
    dependsOn(
        ":lagom-openapi-core:test",
        ":java:lagom-openapi-java-impl:test",
        ":scala:lagom-openapi-scala-impl:test"
    )
}

@Suppress("UnstableApiUsage")
val jacocoAggregateReport by tasks.creating(JacocoReport::class) {
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    executionData(jacocoAggregateMerge.destinationFile)
    reports {
        xml.isEnabled = true
    }
    additionalClassDirs(files(subprojects.flatMap { project ->
        listOf("scala", "kotlin").map { project.buildDir.path + "/classes/$it/main" }
    }))
    additionalSourceDirs(files(subprojects.flatMap { project ->
        listOf("scala", "kotlin").map { project.file("src/main/$it").absolutePath }
    }))
    dependsOn(jacocoAggregateMerge)
}

tasks.check { finalizedBy(jacocoAggregateReport) }

nexusStaging {
    packageGroup = "org.taymyr"
    username = ossrhUsername
    password = ossrhPassword
}

tasks.wrapper {
    distributionType = Wrapper.DistributionType.ALL
}