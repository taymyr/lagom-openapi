package org.taymyr.lagom.openapi

import io.swagger.v3.core.util.RefUtils
import io.swagger.v3.oas.models.media.Schema

class RefSchema(name: String) : Schema<Any>() {
    init {
        this.`$ref` = RefUtils.constructRef(name)
    }
}
