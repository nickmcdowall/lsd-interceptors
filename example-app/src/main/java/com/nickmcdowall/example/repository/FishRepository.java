package com.nickmcdowall.example.repository;

import com.nickmcdowall.example.entity.Fish;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FishRepository extends CrudRepository<Fish, String> {
    Fish findFishByName(String name);

    long countFishByName(String name);
}
