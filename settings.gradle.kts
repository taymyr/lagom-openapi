include("core", "java:api", "java:impl", "scala:api", "scala:impl")

rootProject.name = "lagom-openapi"
project(":core").name = "lagom-openapi-core"
project(":java:api").name = "lagom-openapi-java-api"
project(":java:impl").name = "lagom-openapi-java-impl"
project(":scala:api").name = "lagom-openapi-scala-api"
project(":scala:impl").name = "lagom-openapi-scala-impl"