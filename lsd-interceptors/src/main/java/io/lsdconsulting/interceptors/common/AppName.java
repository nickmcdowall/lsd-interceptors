package io.lsdconsulting.interceptors.common;

import com.lsd.diagram.ValidComponentName;
import lombok.Value;

@Value
public class AppName {
    String value;

    public AppName(String value) {
        this.value = ValidComponentName.of(value);
    }
}
