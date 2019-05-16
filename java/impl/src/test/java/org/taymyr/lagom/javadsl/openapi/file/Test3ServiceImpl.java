package org.taymyr.lagom.javadsl.openapi.file;

import com.typesafe.config.Config;
import org.jetbrains.annotations.NotNull;
import org.taymyr.lagom.javadsl.openapi.AbstractOpenAPIService;

public class Test3ServiceImpl extends AbstractOpenAPIService implements Test3Service {
    public Test3ServiceImpl(@NotNull Config config) {
        super(config);
    }
}
