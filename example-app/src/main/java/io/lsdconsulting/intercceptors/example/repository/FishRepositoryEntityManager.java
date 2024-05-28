package io.lsdconsulting.intercceptors.example.repository;

import io.lsdconsulting.intercceptors.example.entity.Fish;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

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
