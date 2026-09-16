package com.ajxcodes.agydeck.infrastructure.adapter

import com.ajxcodes.agydeck.domain.error.EngineError
import com.ajxcodes.agydeck.domain.model.EngineMode
import com.ajxcodes.agydeck.domain.model.TargetAddress
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class InMemoryAgyEngineAdapterTest {

    private val fixedInstant = Instant.fromEpochMilliseconds(1725000000000L)
    private val adapter = InMemoryAgyEngineAdapter(clock = { fixedInstant })

    @Test
    fun `connect creates session and returns success`() = runTest {
        val result = adapter.connect(EngineMode.LOCAL_CLI, TargetAddress("local-process"))
        assertTrue(result.isSuccess)
        val session = result.getOrNull()
        assertEquals("local-process", session?.title?.value)
        assertEquals(EngineMode.LOCAL_CLI, session?.engineMode)
        assertEquals(fixedInstant, session?.createdAt)
    }

    @Test
    fun `disconnect removes session and repeated disconnect fails with SessionNotFound`() = runTest {
        val sessionResult = adapter.connect(
            EngineMode.REMOTE_SSH,
            TargetAddress("ssh://example.com"),
        )
        val session = sessionResult.getOrThrow()

        val disconnectResult = adapter.disconnect(session.id)
        assertTrue(disconnectResult.isSuccess)

        val secondDisconnect = adapter.disconnect(session.id)
        assertTrue(secondDisconnect.isFailure)
        val error = secondDisconnect.exceptionOrNull()
        assertTrue(error is EngineError.SessionNotFound)
        assertEquals(session.id, (error as EngineError.SessionNotFound).sessionId)
    }
}
