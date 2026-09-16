package com.ajxcodes.agydeck.application.usecase

import com.ajxcodes.agydeck.application.port.EngineConnectionEvent
import com.ajxcodes.agydeck.application.port.IAgyEnginePort
import com.ajxcodes.agydeck.domain.model.AgySession
import com.ajxcodes.agydeck.domain.model.EngineMode
import com.ajxcodes.agydeck.domain.model.TargetAddress
import kotlinx.coroutines.flow.Flow

class ConnectEngineUseCase(
    private val enginePort: IAgyEnginePort,
) {
    val connectionEvents: Flow<EngineConnectionEvent> = enginePort.connectionEvents

    suspend operator fun invoke(engineMode: EngineMode, rawTarget: String): Result<AgySession> {
        val targetAddress = runCatching { TargetAddress(rawTarget.trim()) }.getOrElse {
            return Result.failure(it)
        }
        return enginePort.connect(engineMode, targetAddress)
    }

    suspend operator fun invoke(
        engineMode: EngineMode,
        targetAddress: TargetAddress,
    ): Result<AgySession> {
        return enginePort.connect(engineMode, targetAddress)
    }
}
