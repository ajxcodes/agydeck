package com.ajxcodes.agydeck.application.port

import com.ajxcodes.agydeck.domain.error.DomainError
import com.ajxcodes.agydeck.domain.model.AgySession
import com.ajxcodes.agydeck.domain.model.EngineMode
import com.ajxcodes.agydeck.domain.model.SessionId
import com.ajxcodes.agydeck.domain.model.TargetAddress
import kotlinx.coroutines.flow.Flow

sealed interface EngineConnectionEvent {
    data class Connected(val session: AgySession) : EngineConnectionEvent
    data class Disconnected(val sessionId: SessionId, val reason: String) : EngineConnectionEvent
    data class Error(val error: DomainError) : EngineConnectionEvent
}

interface IAgyEnginePort {
    val connectionEvents: Flow<EngineConnectionEvent>
    suspend fun connect(engineMode: EngineMode, target: TargetAddress): Result<AgySession>
    suspend fun disconnect(sessionId: SessionId): Result<Unit>
}
