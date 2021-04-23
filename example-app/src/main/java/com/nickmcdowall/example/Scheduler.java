package com.nickmcdowall.example;

import com.nickmcdowall.example.repository.FishRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class Scheduler {

    private final FishRepository fishRepository;

    @Scheduled(fixedRate = 2)
    void run() {
        fishRepository.findFishByName("nick");
    }
}
