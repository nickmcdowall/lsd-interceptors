package io.lsdconsulting.intercceptors.example.repository;

import io.lsdconsulting.intercceptors.example.entity.Fish;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

@Transactional
@Repository
public class FishRepositoryEntityManager {
    private final EntityManager entityManager;

    public FishRepositoryEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void persist(long id, String name) {
        entityManager.persist(new Fish(id, name));
    }
}
