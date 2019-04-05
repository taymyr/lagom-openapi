include("java:api", "java:impl", "scala")

rootProject.name = "lagom-openapi"
project(":java:api").name = "lagom-openapi-java-api"
project(":java:impl").name = "lagom-openapi-java-impl"
project(":scala").name = "lagom-openapi-scala"