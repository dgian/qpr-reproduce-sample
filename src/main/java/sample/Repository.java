package sample;

import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.quarkus.hibernate.reactive.panache.common.runtime.ReactiveTransactional;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class Repository implements PanacheRepository<Person> {
    private static final Logger log = LoggerFactory.getLogger(Repository.class);

    private final Mutiny.SessionFactory sessionFactory;

    public Repository(Mutiny.SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @ReactiveTransactional
    public Uni<Void> save(Person person) {
        return persist(person)
            .invoke(() -> log.info("Saved person {}", person.getFirstName()))
            .replaceWithVoid();
    }

    public Uni<Void> saveUsingSession(Person person) {
        return sessionFactory
            .withTransaction((session, transaction) -> session.persist(person))
            .invoke(() -> log.info("Saved person {}", person.getFirstName()))
            .replaceWithVoid();
    }
}
