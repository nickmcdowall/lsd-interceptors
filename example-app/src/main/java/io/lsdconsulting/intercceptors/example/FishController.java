package io.lsdconsulting.intercceptors.example;

import io.lsdconsulting.intercceptors.example.entity.Fish;
import io.lsdconsulting.intercceptors.example.repository.FishRepositoryJpa;
import io.lsdconsulting.intercceptors.example.repository.FishRepositoryEntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static java.util.Objects.isNull;

@RequiredArgsConstructor
@RestController
public class FishController {
    private final FishRepositoryJpa fishRepositoryJpa;
    private final FishRepositoryEntityManager fishRepositoryEntityManager;

    @PostMapping(value = "/fish")
    public void add(@RequestBody NewFishRequest request) {
        fishRepositoryJpa.save(Fish.builder()
                .id(System.currentTimeMillis())
                .name(request.getName())
                .build()
        );
    }

    @DeleteMapping(value = "/fish/{name}")
    public void deleteFishWithName(@PathVariable String name) {
        fishRepositoryJpa.deleteByName(name);
    }

    @GetMapping(value = "/fish/{name}")
    public String getFishWithName(@PathVariable String name) {
        Fish fishByName = fishRepositoryJpa.findFishByName(name);
        if (isNull(fishByName)) {
            throw new FishNotFound();
        }
        return fishByName.toString();
    }

    @PostMapping(value = "/fish/{id}/{name}")
    public void createFish(@PathVariable long id, @PathVariable String name) {
        fishRepositoryEntityManager.persist(id, name);
    }

}
