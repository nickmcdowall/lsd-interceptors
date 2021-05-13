package com.nickmcdowall.example;

import com.nickmcdowall.example.entity.Fish;
import com.nickmcdowall.example.repository.FishRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static java.util.Objects.isNull;

@RequiredArgsConstructor
@RestController
public class FishController {
    private final FishRepository fishRepository;

    @PostMapping(value = "/fish")
    public void add(@RequestBody NewFishRequest request) {
        fishRepository.save(Fish.builder()
                .id(System.currentTimeMillis())
                .name(request.getName())
                .build()
        );
    }

    @DeleteMapping(value = "/fish/{name}")
    public void deleteFishWithName(@PathVariable String name) {
        fishRepository.deleteByName(name);
    }

    @GetMapping(value = "/fish/{name}")
    public String getFishWithName(@PathVariable String name) {
        Fish fishByName = fishRepository.findFishByName(name);
        if (isNull(fishByName)) {
            throw new FishNotFound();
        }
        return fishByName.toString();
    }

}
