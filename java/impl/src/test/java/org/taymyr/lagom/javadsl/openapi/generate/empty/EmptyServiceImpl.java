package org.taymyr.lagom.javadsl.openapi.generate.empty;

import com.typesafe.config.Config;
import org.jetbrains.annotations.NotNull;
import org.taymyr.lagom.javadsl.openapi.AbstractOpenAPIService;

public class EmptyServiceImpl extends AbstractOpenAPIService implements EmptyService {
    public EmptyServiceImpl(@NotNull Config config) {
        super(config);
    }
}
