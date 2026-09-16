package com.ajxcodes.agydeck.presentation.state

import com.ajxcodes.agydeck.domain.error.DomainError
import com.ajxcodes.agydeck.domain.model.AgySession

sealed interface SessionViewState {
    data object Idle : SessionViewState
    data object Connecting : SessionViewState
    data class Connected(val session: AgySession) : SessionViewState
    data class Error(val error: DomainError) : SessionViewState
}
