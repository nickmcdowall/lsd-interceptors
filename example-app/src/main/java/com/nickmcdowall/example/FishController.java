package com.nickmcdowall.example;

import com.nickmcdowall.example.entity.Fish;
import com.nickmcdowall.example.repository.FishRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

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
    
    @GetMapping(value = "/fish/{name}")
    public String getFishWithName(@PathVariable String name) {
        Fish fishByName = fishRepository.findFishByName(name);
        if (Objects.isNull(fishByName)) {
            throw new RuntimeException("Fish not found!");
        }
        return fishByName.toString();
    }

}
