package com.ajxcodes.agydeck.domain.model

import com.ajxcodes.agydeck.domain.error.EngineError
import com.ajxcodes.agydeck.domain.error.SessionError
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@JvmInline
value class SessionId(val value: String) {
    init {
        if (value.isBlank()) {
            throw SessionError.BlankIdentifier
        }
    }

    override fun toString(): String = value

    companion object {
        fun generate(): SessionId = SessionId(UUID.randomUUID().toString())
    }
}

@Serializable
@JvmInline
value class SessionTitle(val value: String) {
    init {
        if (value.isBlank()) {
            throw SessionError.BlankTitle
        }
    }

    override fun toString(): String = value
}

@Serializable
@JvmInline
value class TargetAddress(val value: String) {
    init {
        if (value.isBlank()) {
            throw EngineError.InvalidTarget("Target address cannot be blank")
        }
    }

    override fun toString(): String = value
}
