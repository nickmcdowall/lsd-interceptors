package com.nickmcdowall.lsd.http.naming;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

public class RegexResolvingNameMapperTest {

    @ParameterizedTest
    @CsvSource(value = {
            "/pricing, pricing",
            "/pricing-service, pricing_service",
            "/pricing-service/add/123, pricing_service",
            "/pricing-service?id=123&type=live, pricing_service"
    })
    void resolveDestinationNameByPath(String path, String name) {
        DestinationNameMappings nameMapper = new RegexResolvingNameMapper();

        assertThat(nameMapper.mapForPath(path)).isEqualTo(name);
    }
}
