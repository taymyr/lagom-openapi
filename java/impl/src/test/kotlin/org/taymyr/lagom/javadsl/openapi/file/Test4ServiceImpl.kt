package org.taymyr.lagom.javadsl.openapi.file

import com.typesafe.config.Config
import org.taymyr.lagom.javadsl.openapi.AbstractOpenAPIService

class Test4ServiceImpl(config: Config) : AbstractOpenAPIService(config), Test4Service
