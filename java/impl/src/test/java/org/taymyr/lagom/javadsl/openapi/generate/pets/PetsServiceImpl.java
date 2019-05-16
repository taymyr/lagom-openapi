package org.taymyr.lagom.javadsl.openapi.generate.pets;

import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.typesafe.config.Config;
import org.jetbrains.annotations.NotNull;
import org.taymyr.lagom.javadsl.openapi.AbstractOpenAPIService;

import java.util.List;
import java.util.Optional;

public class PetsServiceImpl extends AbstractOpenAPIService implements PetsService {

    public PetsServiceImpl(@NotNull Config config) {
        super(config);
    }

    @Override
    public ServiceCall<NotUsed, List<Pet>> find(List<String> tags, Optional<Integer> limit) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public ServiceCall<NewPet, Pet> create() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public ServiceCall<NotUsed, Pet> findBy(Long id) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public ServiceCall<NotUsed, NotUsed> delete(Long id) {
        throw new UnsupportedOperationException("Not implemented");
    }

}
