package com.ajxcodes.agydeck.infrastructure.adapter

import com.ajxcodes.agydeck.application.port.EngineConnectionEvent
import com.ajxcodes.agydeck.application.port.IAgyEnginePort
import com.ajxcodes.agydeck.domain.error.EngineError
import com.ajxcodes.agydeck.domain.model.AgySession
import com.ajxcodes.agydeck.domain.model.EngineMode
import com.ajxcodes.agydeck.domain.model.SessionId
import com.ajxcodes.agydeck.domain.model.SessionState
import com.ajxcodes.agydeck.domain.model.SessionTitle
import com.ajxcodes.agydeck.domain.model.TargetAddress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

class InMemoryAgyEngineAdapter(
    private val clock: () -> Instant = { Clock.System.now() },
) : IAgyEnginePort {

    private val events = MutableSharedFlow<EngineConnectionEvent>(
        extraBufferCapacity = DEFAULT_EVENT_BUFFER_CAPACITY,
    )
    override val connectionEvents: Flow<EngineConnectionEvent> = events.asSharedFlow()

    private val activeSessions = mutableMapOf<SessionId, AgySession>()

    override suspend fun connect(engineMode: EngineMode, target: TargetAddress): Result<AgySession> {
        val now = clock()
        val sessionId = SessionId("$SESSION_ID_PREFIX${System.nanoTime()}")
        val session = AgySession(
            id = sessionId,
            title = SessionTitle(target.value),
            engineMode = engineMode,
            state = SessionState.ACTIVE,
            createdAt = now,
            lastActiveAt = now,
        )
        activeSessions[sessionId] = session
        events.emit(EngineConnectionEvent.Connected(session))
        return Result.success(session)
    }

    override suspend fun disconnect(sessionId: SessionId): Result<Unit> {
        val session = activeSessions.remove(sessionId)
            ?: return Result.failure(EngineError.SessionNotFound(sessionId))
        events.emit(
            EngineConnectionEvent.Disconnected(
                sessionId = session.id,
                reason = "Session ${session.id.value} closed",
            ),
        )
        return Result.success(Unit)
    }

    companion object {
        const val DEFAULT_EVENT_BUFFER_CAPACITY: Int = 16
        const val SESSION_ID_PREFIX: String = "session-"
    }
}
