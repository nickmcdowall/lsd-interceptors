package io.lsdconsulting.interceptors.common;

import com.lsd.core.domain.ComponentName;
import lombok.Value;

@Value
public class AppName {
    String value;

    public AppName(String value) {
        this.value = new ComponentName(value).getName();
    }
}
