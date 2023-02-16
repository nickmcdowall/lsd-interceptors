package io.lsdconsulting.interceptors.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import lsd.format.PrettyPrinter;
import lsd.format.json.ObjectMapperCreator;
import org.springframework.messaging.Message;

public class PayloadRetriever {
    static String getPayload(Message<?> message) {
        String payload;
        if (message.getPayload() instanceof String) {
            payload = PrettyPrinter.prettyPrint((String) message.getPayload());
        } else if (message.getPayload() instanceof byte[]) {
            payload = PrettyPrinter.prettyPrint(new String((byte[]) message.getPayload()));
        } else {
            try {
                payload = ObjectMapperCreator.create().writeValueAsString(message.getPayload());
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        return payload;
    }
}
