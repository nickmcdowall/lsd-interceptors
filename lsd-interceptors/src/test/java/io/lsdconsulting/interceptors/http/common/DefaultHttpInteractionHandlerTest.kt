package io.lsdconsulting.interceptors.http.common

import com.lsd.core.LsdContext
import com.lsd.core.domain.Message
import com.lsd.core.domain.MessageType
import com.lsd.core.domain.ParticipantType
import com.lsd.core.domain.SequenceEvent
import io.lsdconsulting.interceptors.common.HeaderKeys
import io.lsdconsulting.interceptors.http.naming.DestinationNameMappings
import io.lsdconsulting.interceptors.http.naming.SourceNameMappings
import io.mockk.every
import io.mockk.slot
import io.mockk.spyk
import io.mockk.verify
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Duration

internal class DefaultHttpInteractionHandlerTest {
    private val serviceNameHeaders = mapOf(
        HeaderKeys.TARGET_NAME.key() to "bob",
        HeaderKeys.SOURCE_NAME.key() to "juliet",
    )
    private val sourceNameMapping = SourceNameMappings { "andrea" }
    private val destinationNameMapping = DestinationNameMappings { "bren" }
    private val messageSlot = slot<SequenceEvent>()
    private val lsdContext = spyk<LsdContext>()
    private val handler = DefaultHttpInteractionHandler(lsdContext, sourceNameMapping, destinationNameMapping)
    private val bob = ParticipantType.PARTICIPANT.called("bob")
    private val juliet = ParticipantType.PARTICIPANT.called("juliet")
    private val bren = ParticipantType.PARTICIPANT.called("bren")
    private val andrea = ParticipantType.PARTICIPANT.called("andrea")

    @BeforeEach
    fun setup() {
        every { lsdContext.capture(capture(messageSlot), any()) } returns Unit
    }

    @Test
    fun usesTestStateToLogRequest() {
        handler.handleRequest("GET", emptyMap(), "/path", "{\"type\":\"request\"}")

        verify { lsdContext.capture(any(), any()) }
        val (_, from, to, label, type, _, data) = extractFirstMessageFromCaptor()

        assertThat(from).isEqualTo(andrea)
        assertThat(to).isEqualTo(bren)
        assertThat(label).isEqualTo("GET /path")
        assertThat(type).isEqualTo(MessageType.SYNCHRONOUS)
        assertThat(data.toString())
            .contains("<p>{\n  &quot;type&quot;: &quot;request&quot;\n}</p>")
    }

    @Test
    fun usesTestStateToLogResponse() {
        handler.handleResponse("200 OK", emptyMap(), emptyMap(), "/path", "response body", Duration.ofMillis(5))

        verify { lsdContext.capture(any(), any()) }
        val (_, from, to, label, type, _, data, duration) = extractFirstMessageFromCaptor()

        assertThat(from).isEqualTo(bren)
        assertThat(to).isEqualTo(andrea)
        assertThat(label).isEqualTo("200 OK (5ms)")
        assertThat(type).isEqualTo(MessageType.SYNCHRONOUS_RESPONSE)
        assertThat(data.toString()).contains("<p>response body</p")
        assertThat(duration).isEqualTo(Duration.ofMillis(5))
    }

    @Test
    fun headerValuesForSourceAndDestinationArePreferredWhenLoggingRequest() {
        handler.handleRequest("GET", serviceNameHeaders, "/path", "")

        verify { lsdContext.capture(any(), any()) }
        val (_, from, to, label, type, _, data) = extractFirstMessageFromCaptor()

        assertThat(from).isEqualTo(juliet)
        assertThat(to).isEqualTo(bob)
        assertThat(label).isEqualTo("GET /path")
        assertThat(type).isEqualTo(MessageType.SYNCHRONOUS)
        assertThat(data.toString())
            .contains("Source-Name: juliet")
            .contains("Target-Name: bob")
    }

    @Test
    fun headerValuesForSourceAndDestinationArePreferredWhenLoggingResponse() {
        handler.handleResponse("200 OK", serviceNameHeaders, emptyMap(), "/path", "response body", Duration.ofMillis(3))

        verify { lsdContext.capture(any(), any()) }
        val (_, from, to, label, type, _, data, duration) = extractFirstMessageFromCaptor()

        assertThat(from).isEqualTo(bob)
        assertThat(to).isEqualTo(juliet)
        assertThat(label).isEqualTo("200 OK (3ms)")
        assertThat(type).isEqualTo(MessageType.SYNCHRONOUS_RESPONSE)
        assertThat(duration).isEqualTo(Duration.ofMillis(3))
        assertThat(data.toString())
            .contains("<h3>Request Headers</h3>")
            .contains("Target-Name: bob")
            .contains("Source-Name: juliet")
            .contains("<p>response body</p>")
    }

    private fun extractFirstMessageFromCaptor(): Message =
        messageSlot.captured
            .takeIf { obj: SequenceEvent -> Message::class.java.isInstance(obj) }
            ?.let { obj: SequenceEvent -> Message::class.java.cast(obj) }
            ?: throw Exception()
}
