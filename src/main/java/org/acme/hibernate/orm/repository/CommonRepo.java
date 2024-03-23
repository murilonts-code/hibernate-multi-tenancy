package org.acme.hibernate.orm.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.hibernate.orm.model.Fruit;

@ApplicationScoped
public class CommonRepo implements PanacheRepository<Fruit> {
}
