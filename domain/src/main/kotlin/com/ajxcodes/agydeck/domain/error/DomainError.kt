package com.ajxcodes.agydeck.domain.error

import com.ajxcodes.agydeck.domain.model.SessionId
import com.ajxcodes.agydeck.domain.model.SessionState

sealed interface DomainError {
    val message: String
}

sealed class DomainException(override val message: String) : RuntimeException(message), DomainError

sealed class SessionError(message: String) : DomainException(message) {
    data object BlankIdentifier : SessionError("Session id cannot be blank")
    data object BlankTitle : SessionError("Session title cannot be blank")
    data class InvalidTransition(
        val from: SessionState,
        val to: SessionState,
    ) : SessionError("Invalid session state transition from $from to $to")
}

sealed class EngineError(message: String) : DomainException(message) {
    data class SessionNotFound(val sessionId: SessionId) :
        EngineError("Session not found: ${sessionId.value}")
    data class InvalidTarget(val reason: String) :
        EngineError("Invalid target: $reason")
}
