package com.ajxcodes.agydeck.presentation.state

import com.ajxcodes.agydeck.domain.error.EngineError
import com.ajxcodes.agydeck.domain.model.AgySession
import com.ajxcodes.agydeck.domain.model.EngineMode
import com.ajxcodes.agydeck.domain.model.SessionId
import com.ajxcodes.agydeck.domain.model.SessionState
import com.ajxcodes.agydeck.domain.model.SessionTitle
import kotlinx.datetime.Instant
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SessionViewStateTest {

    @Test
    fun `states represent different lifecycle moments correctly`() {
        val idle: SessionViewState = SessionViewState.Idle
        assertTrue(idle is SessionViewState.Idle)

        val connecting: SessionViewState = SessionViewState.Connecting
        assertTrue(connecting is SessionViewState.Connecting)

        val now = Instant.fromEpochMilliseconds(1725000000000L)
        val session = AgySession(
            id = SessionId("sess-1"),
            title = SessionTitle("Test"),
            engineMode = EngineMode.LOCAL_CLI,
            state = SessionState.ACTIVE,
            createdAt = now,
            lastActiveAt = now,
        )
        val connected: SessionViewState = SessionViewState.Connected(session)
        assertEquals(session, (connected as SessionViewState.Connected).session)

        val domainError = EngineError.InvalidTarget("Failed to connect")
        val error: SessionViewState = SessionViewState.Error(domainError)
        assertEquals(domainError, (error as SessionViewState.Error).error)
        assertEquals("Invalid target: Failed to connect", (error as SessionViewState.Error).error.message)
    }
}
