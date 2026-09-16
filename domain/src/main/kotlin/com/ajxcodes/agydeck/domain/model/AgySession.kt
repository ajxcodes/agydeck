package com.ajxcodes.agydeck.domain.model

import com.ajxcodes.agydeck.domain.error.SessionError
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
enum class SessionState {
    INITIALIZING,
    ACTIVE,
    PAUSED,
    DISCONNECTED,
    TERMINATED,
}

@Serializable
enum class EngineMode {
    MICROVM_DOCKER,
    REMOTE_SSH,
    LOCAL_CLI,
}

@Serializable
data class AgySession(
    val id: SessionId,
    val title: SessionTitle,
    val engineMode: EngineMode,
    val state: SessionState = SessionState.INITIALIZING,
    val createdAt: Instant,
    val lastActiveAt: Instant,
) {
    val isActive: Boolean
        get() = state == SessionState.ACTIVE

    fun transitionTo(newState: SessionState, timestamp: Instant): AgySession {
        if (!canTransition(state, newState)) {
            throw SessionError.InvalidTransition(from = state, to = newState)
        }
        return copy(state = newState, lastActiveAt = timestamp)
    }

    companion object {
        fun canTransition(from: SessionState, to: SessionState): Boolean {
            if (from == to) return true
            return when (from) {
                SessionState.INITIALIZING -> to in setOf(
                    SessionState.ACTIVE,
                    SessionState.DISCONNECTED,
                    SessionState.TERMINATED,
                )
                SessionState.ACTIVE -> to in setOf(
                    SessionState.PAUSED,
                    SessionState.DISCONNECTED,
                    SessionState.TERMINATED,
                )
                SessionState.PAUSED -> to in setOf(
                    SessionState.ACTIVE,
                    SessionState.DISCONNECTED,
                    SessionState.TERMINATED,
                )
                SessionState.DISCONNECTED -> to in setOf(
                    SessionState.INITIALIZING,
                    SessionState.ACTIVE,
                    SessionState.TERMINATED,
                )
                SessionState.TERMINATED -> false
            }
        }
    }
}
