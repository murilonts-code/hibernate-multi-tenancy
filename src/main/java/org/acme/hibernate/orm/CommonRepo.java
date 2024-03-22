package org.acme.hibernate.orm;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CommonRepo implements PanacheRepository<Fruit> {
}
