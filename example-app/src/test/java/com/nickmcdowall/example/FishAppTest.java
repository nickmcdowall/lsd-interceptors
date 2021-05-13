package com.nickmcdowall.example;

import com.googlecode.yatspec.junit.SequenceDiagramExtension;
import com.googlecode.yatspec.junit.WithParticipants;
import com.googlecode.yatspec.sequence.Participant;
import com.googlecode.yatspec.state.givenwhenthen.TestState;
import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static com.googlecode.yatspec.sequence.Participants.ACTOR;
import static com.googlecode.yatspec.sequence.Participants.PARTICIPANT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;
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
    void saveAndFind() {
        fishClient.post(new NewFishRequest("nick"));

        fishClient.getFishWithName("nick");

        assertThat(fishClient.getFishWithName("nick")).contains("nick");
    }

    @Test
    void saveAndDeleteFind() {
        fishClient.post(new NewFishRequest("ted"));
        fishClient.getFishWithName("ted");

        fishClient.deleteByName("ted");

        try {
            fishClient.getFishWithName("ted");
            fail("Fish should have been deleted causing a 404 FishNotFound");
        } catch (FeignException.NotFound e) {
            //expected
        }
    }

    @Test
    void preventDuplicates() {
        try {
            fishClient.post(new NewFishRequest("jon"));
            
            fishClient.getFishWithName("jon");

            fishClient.post(new NewFishRequest("jon"));
        } catch (FeignException.InternalServerError e) {
            //expected
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
