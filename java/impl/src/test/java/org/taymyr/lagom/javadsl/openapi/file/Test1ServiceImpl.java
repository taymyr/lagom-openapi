package org.taymyr.lagom.javadsl.openapi.file;

import com.typesafe.config.Config;
import org.jetbrains.annotations.NotNull;
import org.taymyr.lagom.javadsl.openapi.AbstractOpenAPIService;

public class Test1ServiceImpl extends AbstractOpenAPIService implements Test1Service {
    public Test1ServiceImpl(@NotNull Config config) {
        super(config);
    }
}
