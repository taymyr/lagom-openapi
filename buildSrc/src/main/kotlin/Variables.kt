import org.gradle.api.Project

val Project.isRelease get() = !this.version.toString().endsWith("SNAPSHOT")

val Project.lagomVersion get() = this.properties["lagomVersion"] as String? ?: Versions.lagom
val Project.scalaBinaryVersion get() = this.properties["scalaBinaryVersion"] as String? ?: Versions.scalaBinary

val Project.ossrhUsername: String? get() = this.properties["ossrhUsername"] as String?
val Project.ossrhPassword: String? get() = this.properties["ossrhPassword"] as String?
