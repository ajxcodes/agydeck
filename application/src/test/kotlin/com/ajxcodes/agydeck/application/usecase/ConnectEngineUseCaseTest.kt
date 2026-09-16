package com.ajxcodes.agydeck.application.usecase

import app.cash.turbine.test
import com.ajxcodes.agydeck.application.port.EngineConnectionEvent
import com.ajxcodes.agydeck.application.port.IAgyEnginePort
import com.ajxcodes.agydeck.domain.error.EngineError
import com.ajxcodes.agydeck.domain.model.AgySession
import com.ajxcodes.agydeck.domain.model.EngineMode
import com.ajxcodes.agydeck.domain.model.SessionId
import com.ajxcodes.agydeck.domain.model.SessionState
import com.ajxcodes.agydeck.domain.model.SessionTitle
import com.ajxcodes.agydeck.domain.model.TargetAddress
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ConnectEngineUseCaseTest {

    private val enginePort = mockk<IAgyEnginePort>()
    private val connectionFlow = MutableSharedFlow<EngineConnectionEvent>()
    private lateinit var useCase: ConnectEngineUseCase

    private val now = Instant.fromEpochMilliseconds(1725000000000L)
    private val testSession = AgySession(
        id = SessionId("session-test-1"),
        title = SessionTitle("CLI Test Session"),
        engineMode = EngineMode.LOCAL_CLI,
        state = SessionState.ACTIVE,
        createdAt = now,
        lastActiveAt = now,
    )

    @BeforeEach
    fun setUp() {
        every { enginePort.connectionEvents } returns connectionFlow
        useCase = ConnectEngineUseCase(enginePort)
    }

    @Test
    fun `invoke with blank target returns failure with EngineError InvalidTarget`() = runTest {
        val result = useCase(EngineMode.LOCAL_CLI, "   ")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is EngineError.InvalidTarget)
        coVerify(exactly = 0) { enginePort.connect(any(), any()) }
    }

    @Test
    fun `invoke with valid target delegates to port and returns session`() = runTest {
        val target = TargetAddress("localhost:50051")
        coEvery { enginePort.connect(EngineMode.LOCAL_CLI, target) } returns Result.success(testSession)

        val result = useCase(EngineMode.LOCAL_CLI, "  localhost:50051  ")

        assertTrue(result.isSuccess)
        assertEquals(testSession, result.getOrNull())
        coVerify(exactly = 1) { enginePort.connect(EngineMode.LOCAL_CLI, target) }
    }

    @Test
    fun `connectionEvents flow forwards events from engine port using turbine`() = runTest {
        useCase.connectionEvents.test {
            val event1 = EngineConnectionEvent.Connected(testSession)
            connectionFlow.emit(event1)
            assertEquals(event1, awaitItem())

            val event2 = EngineConnectionEvent.Disconnected(
                sessionId = testSession.id,
                reason = "Connection closed by peer",
            )
            connectionFlow.emit(event2)
            assertEquals(event2, awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }
}
