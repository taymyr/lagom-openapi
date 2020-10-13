package org.taymyr.lagom.javadsl.openapi.file

import com.lightbend.lagom.javadsl.api.Descriptor
import com.lightbend.lagom.javadsl.api.Service
import org.taymyr.lagom.javadsl.openapi.OpenAPIService
import org.taymyr.lagom.javadsl.openapi.withOpenAPI

interface Test4Service : OpenAPIService {
    @JvmDefault
    override fun descriptor(): Descriptor {
        return Service.named("test4").withOpenAPI()
    }
}
