package com.nickmcdowall.lsd.interceptor.common;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

public class DynamicDestinationNameResolverTest {

    @ParameterizedTest
    @CsvSource(value = {
            "/pricing, pricing",
            "/pricing-service, pricing-service",
            "/pricing-service/add/123, pricing-service",
            "/pricing-service?id=123&type=live, pricing-service"
    })
    void resolveDestinationNameByPath(String path, String name) {
        DestinationNamesMapper dynamicDestinationNameResolver = new RegexResolvingDestinationNameMapper();

        assertThat(dynamicDestinationNameResolver.mapForPath(path)).isEqualTo(name);
    }
}
