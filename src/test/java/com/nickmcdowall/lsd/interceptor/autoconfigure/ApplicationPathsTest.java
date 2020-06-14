package com.nickmcdowall.lsd.interceptor.autoconfigure;

import org.junit.jupiter.api.Test;

import static java.util.List.of;
import static org.assertj.core.api.Assertions.assertThat;

class ApplicationPathsTest {

    @Test
    void additionalPathsCanBeAdded() {
        ApplicationPaths applicationPaths = new ApplicationPaths(of("path1")).addAll(of("path2"));

        assertThat(applicationPaths.stream()).containsExactlyInAnyOrder("path1", "path2");
    }
}