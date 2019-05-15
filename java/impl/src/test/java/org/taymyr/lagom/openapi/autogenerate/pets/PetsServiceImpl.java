package org.taymyr.lagom.openapi.autogenerate.pets;

import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.typesafe.config.Config;
import org.jetbrains.annotations.NotNull;
import org.taymyr.lagom.openapi.AbstractOpenAPIService;

import java.util.Optional;

public class PetsServiceImpl extends AbstractOpenAPIService implements PetsService {

    public PetsServiceImpl(@NotNull Config config) {
        super(config);
    }

    @Override
    public ServiceCall<NotUsed, Pets> listPets(Optional<Integer> limit) {
        return null;
    }

    @Override
    public ServiceCall<Pet, NotUsed> createPets() {
        return null;
    }

    @Override
    public ServiceCall<NotUsed, Pets> showPetById(String petId) {
        return null;
    }

}
