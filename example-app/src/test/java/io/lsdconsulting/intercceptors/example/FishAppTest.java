package io.lsdconsulting.intercceptors.example;

import com.lsd.core.LsdContext;
import io.lsdconsulting.junit5.LsdExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.lsd.core.domain.ParticipantType.ACTOR;
import static com.lsd.core.domain.ParticipantType.PARTICIPANT;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;

@ImportAutoConfiguration({FeignAutoConfiguration.class})
@SpringBootTest(webEnvironment = DEFINED_PORT, classes = FishApp.class)
@ActiveProfiles("test")
@Import({
        TestConfig.class
})
@ExtendWith(LsdExtension.class)
public class FishAppTest {

    @Autowired
    private FishClient fishClient;

    private final LsdContext lsdContext = LsdContext.getInstance();

    @Test
    void triggerVariousInteractions() {
        fishClient.post(new NewFishRequest("nick"));
        fishClient.createFish(System.currentTimeMillis(), "eric");
        safely(() -> fishClient.getFishWithName("ted"));
        safely(() -> fishClient.getFishWithName("nick"));
        safely(() -> fishClient.getFishByName("nick"));
        fishClient.deleteByName("eric");
        safely((Object) -> fishClient.post(new NewFishRequest("jon")));
        safely((Object) -> fishClient.post(new NewFishRequest("jon")));
        safely((Object) -> fishClient.createFish(System.currentTimeMillis(), "jon"));
    }

    private void safely(Supplier<?> supplier) {
        try {
            supplier.get();
        } catch (Exception e) {
            //Allow for test
        }
    }

    private void safely(Consumer<?> consumer
    ) {
        try {
            consumer.accept(null);
        } catch (Exception e) {
            //Allow for test
        }
    }

    @PostConstruct
    public void participants() {
        lsdContext.addParticipants(List.of(
                ACTOR.called("User"),
                PARTICIPANT.called("FishApp")
        ));
    }
}
