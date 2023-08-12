package io.lsdconsulting.interceptors.messaging

import com.lsd.core.LsdContext
import com.lsd.core.domain.MessageType
import com.lsd.core.domain.ParticipantType
import io.lsdconsulting.interceptors.common.HeaderKeys
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.spyk
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.junit.jupiter.api.Test
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.MessageHeaders
import java.nio.charset.StandardCharsets

internal class EventConsumerInterceptorTest {
    private val messageSlot = slot<com.lsd.core.domain.Message>()
    private val lsdContext = spyk<LsdContext>()
    private val message = spyk<Message<ByteArray>>()

    private val underTest = EventConsumerInterceptor(lsdContext)

    @Test
    fun logInteraction() {
        every { lsdContext.capture(capture(messageSlot))} returns Unit
        every { message.payload } returns "{\"key\":\"value\"}".toByteArray(StandardCharsets.UTF_8)
        every { message.headers } returns
            MessageHeaders(
                mapOf<String, Any>(
                    HeaderKeys.SOURCE_NAME.key() to "Source",
                    HeaderKeys.TARGET_NAME.key() to "Target"
                )
            )

        underTest.preSend(message, mockk<MessageChannel>())
        val (_, from, to, label, type, _, data) = messageSlot.captured
        assertThat(from).isEqualTo(ParticipantType.PARTICIPANT.called("Source"))
        assertThat(to).isEqualTo(ParticipantType.PARTICIPANT.called("Target"))
        assertThat(label).isEqualTo("Consume event")
        assertThat(type).isEqualTo(MessageType.ASYNCHRONOUS)
        assertThat(data.toString())
            .contains("timestamp: " + message.headers["timestamp"])
            .contains("Source-Name: Source")
            .contains("Target-Name: Target")
            .contains("id: " + message.headers["id"])
    }
}