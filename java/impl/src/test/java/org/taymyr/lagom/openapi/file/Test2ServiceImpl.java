package org.taymyr.lagom.openapi.file;

import com.typesafe.config.Config;
import org.jetbrains.annotations.NotNull;
import org.taymyr.lagom.openapi.AbstractOpenAPIService;

public class Test2ServiceImpl extends AbstractOpenAPIService implements Test2Service {
    public Test2ServiceImpl(@NotNull Config config) {
        super(config);
    }
}
