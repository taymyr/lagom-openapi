[![Gitter](https://img.shields.io/badge/chat-gitter-purple.svg)](https://gitter.im/taymyr/taymyr)
[![Gitter_RU](https://img.shields.io/badge/chat-russian%20channel-purple.svg)](https://gitter.im/taymyr/taymyr_ru)
[![Build Status](https://travis-ci.org/taymyr/lagom-openapi.svg?branch=master)](https://travis-ci.org/taymyr/lagom-openapi)
[![codecov](https://codecov.io/gh/taymyr/lagom-openapi/branch/master/graph/badge.svg)](https://codecov.io/gh/taymyr/lagom-openapi)
[![Maven](https://img.shields.io/maven-central/v/org.taymyr.lagom/lagom-openapi-core_2.12.svg)](https://search.maven.org/search?q=a:lagom-openapi-*2.12%20AND%20g:org.taymyr.lagom)

# [OpenAPI/Swagger](https://swagger.io/specification/) module for [Lagom](https://www.lagomframework.com)

The Lagom OpenAPI module has two common features:
 
* Generation OpenAPI 3.X specification for the Lagom service by annotations. (How to use with [Scala DSL](#11-generate-scala-dsl)/[Java DSL](#12-generate-java-dsl))
* Publishing static OpenAPI 3.X/Swagger 2.X specification from the classpath. (How to use with [Scala DSL](#21-static-scala-dsl)/[Java DSL](#22-static-java-dsl))

Also, you can see how to generate OpenAPI Specification for Lagom service on demo projects ([Java/Maven example](https://github.com/taymyr/lagom-samples/blob/master/openapi/java/README.md), [Scala/Sbt example](https://github.com/taymyr/lagom-samples/blob/master/openapi/scala/README.md)).

## Versions compatibility

| Lagom OpenAPI | OpenAPI / Swagger | Lagom           | Scala          |
|---------------|-------------------|-----------------|----------------|
| 1.0.+         | 2.0.7+            | 1.4.+ <br> 1.5.+| 2.11 <br> 2.12 |

# How to use

## 1.1 Generate (Scala DSL)

### 1.1.1 Dependencies

You need to add next dependencies to the _API_ module of Lagom service:

```scala
val swaggerAnnotations = "io.swagger.core.v3" % "swagger-annotations" % "2.0.7"
val lagomOpenapiApi = "org.taymyr.lagom" %% "lagom-openapi-scala-api" % lagomOpenapiVersion

lazy val `lagom-service-api` = (project in file("api"))
  .settings(
    libraryDependencies ++= Seq(
      ...
      swaggerAnnotations,
      lagomOpenapiApi
    )
  )
```

and next dependencies to the _Implementation_ module of Lagom service:

```scala
val lagomOpenapiImpl = "org.taymyr.lagom" %% "lagom-openapi-scala-impl" % lagomOpenapiVersion

lazy val `lagom-service-impl` = (project in file("impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      ...
      lagomOpenapiImpl
    )
  )
```

### 1.1.2 Service Descriptor

Necessarily add `OpenAPIDefinition` annotation to Lagom service descriptor and extend descriptor by `OpenAPIService` trait:

```scala
@OpenAPIDefinition(
  info = new Info(
    version = "1.0.0",
    title = "My Service"
  ),
  ...
)
trait MyService extends OpenAPIService with Service {
  ...
}
```

Then you can use OpenAPI annotations for the methods of your service. For more information about annotations see the [official wiki](https://github.com/swagger-api/swagger-core/wiki/Swagger-2.X---Annotations).

```scala
@Operation(...)
@Tag(...)
...
def method: ServiceCall[_, _]
```

In conclusion, you must add the route for OpenAPI specification. You can do that with helper function `withOpenAPI` (default route is `/_<service_name>/openapi`)

```scala
override def descriptor: Descriptor = withOpenAPI(
  named("service")
    ...
)
```

or use a custom route

```scala
override def descriptor: Descriptor = named("service")
    .withCalls(
      ...
      pathCall("/custom/route", openapi)
    )
```

### 1.1.3 Service implementation

Extend your service by the `OpenAPIServiceImpl` trait:

```scala
class MyServiceImpl(override val config: Config)
    extends MyService
    with OpenAPIServiceImpl {
      ...
}
```

### 1.1.4 Conclusion

Now you can run service and get OpenAPI specification by a sent HTTP request to the registered route. For example by default:

```bash
curl "http://localhost:9000/_<service_name>/openapi"
``` 

## 1.2 Generate (Java DSL)

### 1.2.1 Dependencies

You need to add next dependencies to the _API_ module of Lagom service:

**Maven**

```xml
<dependencies>
    <dependency>
        <groupId>org.taymyr.lagom</groupId>
        <artifactId>lagom-openapi-java-api_${scala.binary.version}</artifactId>
        <version>${lagom.openapi.version}</version>
    </dependency>
    <dependency>
        <groupId>io.swagger.core.v3</groupId>
        <artifactId>swagger-annotations</artifactId>
        <version>2.0.7</version>
    </dependency>
</dependencies>
```

**Sbt**

```scala
val swaggerAnnotations = "io.swagger.core.v3" % "swagger-annotations" % "2.0.7"
val lagomOpenapiApi = "org.taymyr.lagom" %% "lagom-openapi-java-api" % lagomOpenapiVersion

lazy val `lagom-service-api` = (project in file("api"))
  .settings(
    libraryDependencies ++= Seq(
      ...
      swaggerAnnotations,
      lagomOpenapiApi
    )
  )
```

and next dependencies to the _Implementation_ module of Lagom service:

**Maven**

```xml
<dependencies>
    <dependency>
        <groupId>org.taymyr.lagom</groupId>
        <artifactId>lagom-openapi-java-impl_${scala.binary.version}</artifactId>
        <version>${lagom.openapi.version}</version>
    </dependency>
</dependencies>
```

**Sbt**

```scala
val lagomOpenapiImpl = "org.taymyr.lagom" %% "lagom-openapi-java-impl" % lagomOpenapiVersion

lazy val `lagom-service-impl` = (project in file("impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      ...
      lagomOpenapiImpl
    )
  )
```

### 1.2.2 Service Descriptor

Necessarily add `OpenAPIDefinition` annotation to Lagom service descriptor and extend descriptor by `OpenAPIService` interface:

```java
@OpenAPIDefinition(
  info = @Info(
    version = "1.0.0",
    title = "My Service"
  ),
  ...
)
public interface MyService extends OpenAPIService {
  ...
}
```

Then you can use OpenAPI annotations for the methods of your service. For more information about annotations see the [official wiki](https://github.com/swagger-api/swagger-core/wiki/Swagger-2.X---Annotations).

```java
@Operation(...)
@Tag(...)
...
ServiceCall<_, _> method();
```

In conclusion, you must add the route for OpenAPI specification. You can do that with helper function `withOpenAPI` (default route is `/_<service_name>/openapi`)

```java
@Override
default Descriptor descriptor() {
  return withOpenAPI(
    named("service")
      ...
  );
}
```

or use a custom route

```java
@Override
default Descriptor descriptor() {
  return named("service")
    .withCalls(
      ...
      pathCall("/custom/route", this::openapi)
    );
}
```

### 1.2.3 Service implementation

Extend your service by the abstract `AbstractOpenAPIService`:

```java
public class MyServiceImpl extends AbstractOpenAPIService implements MyService {
      ...
}
```

### 1.2.4 Conclusion

Now you can run service and get OpenAPI specification by a sent HTTP request to the registered route. For example by default:

```bash
curl "http://localhost:9000/_<service_name>/openapi"
``` 

## 2.1 Static (Scala DSL)

### 2.1.1 Dependencies

You need to add next dependencies to the _API_ module of Lagom service:

```scala
val lagomOpenapiApi = "org.taymyr.lagom" %% "lagom-openapi-scala-api" % lagomOpenapiVersion

lazy val `lagom-service-api` = (project in file("api"))
  .settings(
    libraryDependencies ++= Seq(
      ...
      lagomOpenapiApi
    )
  )
```

and next dependencies to the _Implementation_ module of Lagom service:

```scala
val lagomOpenapiImpl = "org.taymyr.lagom" %% "lagom-openapi-scala-impl" % lagomOpenapiVersion

lazy val `lagom-service-impl` = (project in file("impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      ...
      lagomOpenapiImpl
    )
  )
```

### 2.1.2 Service Descriptor

Extend Lagom service descriptor by `OpenAPIService` trait:

```scala
trait MyService extends OpenAPIService with Service {
  ...
}
```

Add OpenAPI/Swagger specification file to classpath (typically `src/main/resources` folder) of _API_ module. By default, the file should be named `<service_name>.[yml|yaml|json]`.

```yaml
openapi: 3.0.0
info:
  title: My Service
  version: 1.0.0
paths:
  ...
```

Also, you can change filename using `openapi.file` configuration in `application.conf`.

```HOCON
openapi.file = foobar.yml
```

In conclusion, you must add the route for OpenAPI specification. You can do that with helper function `withOpenAPI` (default route is `/_<service_name>/openapi`)

```scala
override def descriptor: Descriptor = withOpenAPI(
  named("service")
    ...
)
```

or use a custom route

```scala
override def descriptor: Descriptor = named("service")
    .withCalls(
      ...
      pathCall("/custom/route", openapi)
    )
```

### 2.1.3 Service implementation

Extend your service by the `OpenAPIServiceImpl` trait:

```scala
class MyServiceImpl(override val config: Config)
    extends MyService
    with OpenAPIServiceImpl {
      ...
}
```

### 2.1.4 Conclusion

Now you can run service and get OpenAPI specification by a sent HTTP request to the registered route. For example by default:

```bash
curl "http://localhost:9000/_<service_name>/openapi"
``` 


## 2.2 Static (Java DSL)

### 2.2.1 Dependencies

You need to add next dependencies to the _API_ module of Lagom service:

**Maven**

```xml
<dependencies>
    <dependency>
        <groupId>org.taymyr.lagom</groupId>
        <artifactId>lagom-openapi-java-api_${scala.binary.version}</artifactId>
        <version>${lagom.openapi.version}</version>
    </dependency>
</dependencies>
```

**Sbt**

```scala
val lagomOpenapiApi = "org.taymyr.lagom" %% "lagom-openapi-java-api" % lagomOpenapiVersion

lazy val `lagom-service-api` = (project in file("api"))
  .settings(
    libraryDependencies ++= Seq(
      ...
      lagomOpenapiApi
    )
  )
```

and next dependencies to the _Implementation_ module of Lagom service:

**Maven**

```xml
<dependencies>
    <dependency>
        <groupId>org.taymyr.lagom</groupId>
        <artifactId>lagom-openapi-java-impl_${scala.binary.version}</artifactId>
        <version>${lagom.openapi.version}</version>
    </dependency>
</dependencies>
```

**Sbt**

```scala
val lagomOpenapiImpl = "org.taymyr.lagom" %% "lagom-openapi-java-impl" % lagomOpenapiVersion

lazy val `lagom-service-impl` = (project in file("impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      ...
      lagomOpenapiImpl
    )
  )
```

### 2.2.2 Service Descriptor

Extend Lagom service descriptor by `OpenAPIService` interface:

```java
public interface MyService extends OpenAPIService {
  ...
}
```

Add OpenAPI/Swagger specification file to classpath (typically `src/main/resources` folder) of _API_ module. By default, the file should be named `<service_name>.[yml|yaml|json]`.

```yaml
openapi: 3.0.0
info:
  title: My Service
  version: 1.0.0
paths:
  ...
```

Also, you can change filename using `openapi.file` configuration in `application.conf`.

```HOCON
openapi.file = foobar.yml
```

In conclusion, you must add the route for OpenAPI specification. You can do that with helper function `withOpenAPI` (default route is `/_<service_name>/openapi`)

```java
@Override
default Descriptor descriptor() {
  return withOpenAPI(
    named("service")
      ...
  );
}
```

or use a custom route

```java
@Override
default Descriptor descriptor() {
  return named("service")
    .withCalls(
      ...
      pathCall("/custom/route", this::openapi)
    );
}
```

### 2.2.3 Service implementation

Extend your service by the abstract `AbstractOpenAPIService`:

```java
public class MyServiceImpl extends AbstractOpenAPIService implements MyService {
      ...
}
```

### 2.2.4 Conclusion

Now you can run service and get OpenAPI specification by a sent HTTP request to the registered route. For example by default:

```bash
curl "http://localhost:9000/_<service_name>/openapi"
``` 

## Contributions

Contributions are very welcome.

## License

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this project except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0.

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
