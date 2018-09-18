package org.taymyr.openapi

import com.lightbend.lagom.javadsl.api.Descriptor

import com.lightbend.lagom.javadsl.api.Service.named
import com.typesafe.config.Config

interface Test1Service : OpenAPIService {
    override fun descriptor(): Descriptor = withOpenAPI(named("test1"))
}
interface Test2Service : OpenAPIService {
    override fun descriptor(): Descriptor = withOpenAPI(named("test2"))
}
interface Test3Service : OpenAPIService {
    override fun descriptor(): Descriptor = withOpenAPI(named("test3"))
}

class Test1ServiceImpl(config: Config) : AbstractOpenAPIService(config), Test1Service
class Test2ServiceImpl(config: Config) : AbstractOpenAPIService(config), Test2Service
class Test3ServiceImpl(config: Config) : AbstractOpenAPIService(config), Test3Service