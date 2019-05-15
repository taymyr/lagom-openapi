val ossrhUsername: String? by project
val ossrhPassword: String? by project

plugins {
    kotlin("jvm") version Versions.kotlin apply false
    id("io.codearte.nexus-staging") version Versions.`nexus-staging`
}

allprojects {
    repositories {
        mavenCentral()
        jcenter()
    }
}

subprojects {
    group = "org.taymyr.lagom"
    version = "1.0.0-SNAPSHOT"
}

nexusStaging {
    packageGroup = "org.taymyr"
    username = ossrhUsername
    password = ossrhPassword
}