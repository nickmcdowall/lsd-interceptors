package io.lsdconsulting.interceptors.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;

import static lsd.format.json.ObjectMapperCreatorKt.createObjectMapper;

public class TypeConverter {
    static String convertToString(Object payload) {
        String payloadString;
        if (payload instanceof String) {
            payloadString = (String) payload;
        } else if (payload instanceof byte[]) {
            payloadString = new String((byte[]) payload);
        } else {
            try {
                payloadString = createObjectMapper().writeValueAsString(payload);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        return payloadString;
    }
}
