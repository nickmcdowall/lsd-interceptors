package io.lsdconsulting.intercceptors.example;

import io.lsdconsulting.intercceptors.example.repository.FishRepositoryJpa;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledService {
    private final FishRepositoryJpa fishRepositoryJpa;

    private static int errorCounter = 0;

    public ScheduledService(FishRepositoryJpa fishRepositoryJpa) {
        this.fishRepositoryJpa = fishRepositoryJpa;
    }

    @Scheduled(fixedRate = 200)
    public void countFish() {
        errorCounter++;
        if (errorCounter % 2 == 0)
            throw new RuntimeException("Failing for evens");
        fishRepositoryJpa.count();
    }
}
