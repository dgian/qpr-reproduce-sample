package sample;

import io.smallrye.mutiny.Uni;
import io.vertx.core.json.Json;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class Consumer {

    Logger log = LoggerFactory.getLogger(Consumer.class);

    private final Repository repository;

    public Consumer(Repository repository) {
        this.repository = repository;
    }

    @Incoming("persons")
    public Uni<Void> consume(String payload) {
        return Uni.createFrom()
            .item(payload)
            .map(json -> Json.decodeValue(json, Person.class))
            .flatMap(repository::save)
            .onFailure().invoke(t -> log.error(t.getMessage(), t));
    }

    @Incoming("persons-session")
    public Uni<Void> consumeUsingSession(String payload) {
        return Uni.createFrom()
            .item(payload)
            .map(json -> Json.decodeValue(json, Person.class))
            .flatMap(repository::saveUsingSession)
            .onFailure().invoke(t -> log.error(t.getMessage(), t));
    }
}
