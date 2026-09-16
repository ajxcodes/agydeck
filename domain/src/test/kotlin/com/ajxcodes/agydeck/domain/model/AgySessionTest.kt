package com.ajxcodes.agydeck.domain.model

import com.ajxcodes.agydeck.domain.error.EngineError
import com.ajxcodes.agydeck.domain.error.SessionError
import kotlinx.datetime.Instant
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class AgySessionTest {

    private val now = Instant.fromEpochMilliseconds(1725000000000L)
    private val later = Instant.fromEpochMilliseconds(1725000060000L)

    @Test
    fun `session creation succeeds with valid value objects`() {
        val session = AgySession(
            id = SessionId("sess-100"),
            title = SessionTitle("Agent Session"),
            engineMode = EngineMode.LOCAL_CLI,
            createdAt = now,
            lastActiveAt = now,
        )

        assertEquals("sess-100", session.id.value)
        assertEquals("Agent Session", session.title.value)
        assertEquals(SessionState.INITIALIZING, session.state)
        assertFalse(session.isActive)
    }

    @Test
    fun `sessionId throws SessionError BlankIdentifier when blank`() {
        assertThrows<SessionError.BlankIdentifier> {
            SessionId("   ")
        }
    }

    @Test
    fun `sessionTitle throws SessionError BlankTitle when blank`() {
        assertThrows<SessionError.BlankTitle> {
            SessionTitle("")
        }
    }

    @Test
    fun `targetAddress throws EngineError InvalidTarget when blank`() {
        assertThrows<EngineError.InvalidTarget> {
            TargetAddress("  ")
        }
    }

    @Test
    fun `sessionId generate produces non blank identifier`() {
        val generated = SessionId.generate()
        assertTrue(generated.value.isNotBlank())
        assertNotNull(generated.toString())
    }

    @Test
    fun `transitionTo valid states updates state and lastActiveAt`() {
        val session = AgySession(
            id = SessionId("sess-1"),
            title = SessionTitle("Test Session"),
            engineMode = EngineMode.LOCAL_CLI,
            createdAt = now,
            lastActiveAt = now,
        )

        val activeSession = session.transitionTo(SessionState.ACTIVE, later)
        assertEquals(SessionState.ACTIVE, activeSession.state)
        assertTrue(activeSession.isActive)
        assertEquals(later, activeSession.lastActiveAt)

        val pausedSession = activeSession.transitionTo(SessionState.PAUSED, later)
        assertEquals(SessionState.PAUSED, pausedSession.state)
        assertFalse(pausedSession.isActive)

        val terminatedSession = pausedSession.transitionTo(SessionState.TERMINATED, later)
        assertEquals(SessionState.TERMINATED, terminatedSession.state)
    }

    @Test
    fun `transitionTo invalid state throws SessionError InvalidTransition`() {
        val session = AgySession(
            id = SessionId("sess-1"),
            title = SessionTitle("Test Session"),
            engineMode = EngineMode.LOCAL_CLI,
            createdAt = now,
            lastActiveAt = now,
        ).transitionTo(SessionState.TERMINATED, later)

        val error = assertThrows<SessionError.InvalidTransition> {
            session.transitionTo(SessionState.ACTIVE, later)
        }
        assertEquals(SessionState.TERMINATED, error.from)
        assertEquals(SessionState.ACTIVE, error.to)
    }
}
