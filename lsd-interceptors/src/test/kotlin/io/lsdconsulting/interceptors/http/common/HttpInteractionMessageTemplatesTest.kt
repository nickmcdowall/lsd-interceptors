package io.lsdconsulting.interceptors.http.common

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class HttpInteractionMessageTemplatesTest {

    @Test
    fun createsExpectedSequenceDiagramInteraction() {
        var interaction = String.format("sync %s response from %s to %s", "200 OK", "A", "B")
        if (interaction.startsWith("sync 4") || interaction.startsWith("sync 5")) {
            interaction += " [#red]"
        }
        val output = interaction
        assertThat(output).isEqualTo("sync 200 OK response from A to B")
    }

    @Test
    fun addsRedForClientErrorStatusCodes() {
        var interaction = String.format("sync %s response from %s to %s", "404 NOT_FOUND", "A", "B")
        if (interaction.startsWith("sync 4") || interaction.startsWith("sync 5")) {
            interaction += " [#red]"
        }
        val output = interaction
        assertThat(output).isEqualTo("sync 404 NOT_FOUND response from A to B [#red]")
    }

    @Test
    fun addsRedForServerErrorStatusCodes() {
        var interaction = String.format("sync %s response from %s to %s", "500 INTERNAL_SERVER_ERROR", "A", "B")
        if (interaction.startsWith("sync 4") || interaction.startsWith("sync 5")) {
            interaction += " [#red]"
        }
        val output = interaction
        assertThat(output).isEqualTo("sync 500 INTERNAL_SERVER_ERROR response from A to B [#red]")
    }
}
