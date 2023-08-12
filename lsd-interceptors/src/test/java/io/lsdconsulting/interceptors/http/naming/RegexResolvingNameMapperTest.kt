package io.lsdconsulting.interceptors.http.naming

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class RegexResolvingNameMapperTest {

    @ParameterizedTest
    @CsvSource(value = ["/pricing, pricing", "/pricing-service, pricing_service", "/pricing-service/add/123, pricing_service", "/pricing-service?id=123&type=live, pricing_service"])
    fun resolveDestinationNameByPath(path: String, name: String) {
        val nameMapper: DestinationNameMappings = RegexResolvingNameMapper()
        assertThat(nameMapper.mapForPath(path)).isEqualTo(name)
    }
}
