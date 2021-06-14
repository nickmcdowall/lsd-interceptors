package com.nickmcdowall.lsd.http.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HttpInteractionMessageTemplatesTest {

    @Test
    void createsExpectedSequenceDiagramInteraction() {
        String output = HttpInteractionMessageTemplates.responseOf("200 OK", "A", "B");

        assertThat(output).isEqualTo("sync 200 OK response from A to B");
    }

    @Test
    void addsRedForClientErrorStatusCodes() {
        String output = HttpInteractionMessageTemplates.responseOf("404 NOT_FOUND", "A", "B");
        
        assertThat(output).isEqualTo("sync 404 NOT_FOUND response from A to B [#red]");
    }
    @Test
    void addsRedForServerErrorStatusCodes() {
        String output = HttpInteractionMessageTemplates.responseOf("500 INTERNAL_SERVER_ERROR", "A", "B");
        
        assertThat(output).isEqualTo("sync 500 INTERNAL_SERVER_ERROR response from A to B [#red]");
    }
}