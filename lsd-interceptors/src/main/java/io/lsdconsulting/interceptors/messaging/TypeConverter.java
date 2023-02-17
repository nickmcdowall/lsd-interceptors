package io.lsdconsulting.interceptors.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import lsd.format.json.ObjectMapperCreator;

public class TypeConverter {
    static String convertToString(Object payload) {
        String payloadString;
        if (payload instanceof String) {
            payloadString = (String) payload;
        } else if (payload instanceof byte[]) {
            payloadString = new String((byte[]) payload);
        } else {
            try {
                payloadString = ObjectMapperCreator.create().writeValueAsString(payload);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        return payloadString;
    }
}
