val ossrhUsername: String? by project
val ossrhPassword: String? by project

plugins {
    kotlin("jvm") version "1.3.30" apply false
    id("io.codearte.nexus-staging") version "0.20.0"
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