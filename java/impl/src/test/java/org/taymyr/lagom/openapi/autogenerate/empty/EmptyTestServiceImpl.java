package org.taymyr.lagom.openapi.autogenerate.empty;

import com.typesafe.config.Config;
import org.jetbrains.annotations.NotNull;
import org.taymyr.lagom.openapi.AbstractOpenAPIService;

public class EmptyTestServiceImpl extends AbstractOpenAPIService implements EmptyTestService {
    public EmptyTestServiceImpl(@NotNull Config config) {
        super(config);
    }
}
