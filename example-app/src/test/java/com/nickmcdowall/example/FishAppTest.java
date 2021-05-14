package com.nickmcdowall.example;

import com.googlecode.yatspec.junit.SequenceDiagramExtension;
import com.googlecode.yatspec.junit.WithParticipants;
import com.googlecode.yatspec.sequence.Participant;
import com.googlecode.yatspec.state.givenwhenthen.TestState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.googlecode.yatspec.sequence.Participants.ACTOR;
import static com.googlecode.yatspec.sequence.Participants.PARTICIPANT;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;

@ImportAutoConfiguration({FeignAutoConfiguration.class})
@SpringBootTest(webEnvironment = DEFINED_PORT, classes = FishApp.class)
@ActiveProfiles("test")
@Import({
        TestConfig.class
})
@ExtendWith(SequenceDiagramExtension.class)
public class FishAppTest implements WithParticipants {

    @Autowired
    private FishClient fishClient;

    @Autowired
    private TestState testState;

    @Test
    void triggerVariousInteractions() {
        fishClient.post(new NewFishRequest("nick"));
        fishClient.createFish(System.currentTimeMillis(), "eric");
        safely(() -> fishClient.getFishWithName("ted"));
        safely(() -> fishClient.getFishWithName("nick"));
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

    @Override
    public List<Participant> participants() {
        return List.of(
                ACTOR.create("User"),
                PARTICIPANT.create("FishApp")
        );
    }
}
