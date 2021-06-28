package io.lsdconsulting.intercceptors.example.repository;

import io.lsdconsulting.intercceptors.example.entity.Fish;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

@Transactional
@Repository
@RequiredArgsConstructor
public class FishRepositoryEntityManager {
    private final EntityManager entityManager;

    public void persist(long id, String name) {
        entityManager.persist(Fish.builder().id(id).name(name).build());
    }
}
