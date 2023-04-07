package de.gransoftware.core

import de.gransoftware.core.usecase.Coordinates
import de.gransoftware.core.usecase.ErrorType
import de.gransoftware.core.usecase.GetPlaceInfoUseCase
import de.gransoftware.core.usecase.Outcome
import de.gransoftware.core.usecase.PlaceInfo
import de.gransoftware.core.usecase.ports.GetPlaceInfoInPort
import de.gransoftware.core.usecase.ports.GetPlaceInfoOutPort
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlin.test.Test

class GetPlaceInfoUseCaseTest {
    @Test
    fun `invoke, for supported coordinates returns correct place info`(): Unit = runBlocking {
        val placeInfo = PlaceInfo("Berlin", "Germany")
        val getPlaceInOuPort = mockk<GetPlaceInfoOutPort> {
            coEvery { this@mockk.invoke(any()) } returns Outcome.Success(placeInfo)
        }

        val outcome = GetPlaceInfoUseCase(getPlaceInOuPort).invoke(GetPlaceInfoInPort.Input(Coordinates(0.0F, 0.0F)))

        val placeInfoOutcome = (outcome as Outcome.Success).value
        placeInfoOutcome shouldBe placeInfo
    }

    @Test
    fun `invoke, for unsupported coordinates returns error`(): Unit = runBlocking {
        val getPlaceInOuPort = mockk<GetPlaceInfoOutPort> {
            coEvery { this@mockk.invoke(any()) } returns Outcome.Error(ErrorType.PLACE_INFO_API_ERROR)
        }

        val outcome = GetPlaceInfoUseCase(getPlaceInOuPort).invoke(GetPlaceInfoInPort.Input(Coordinates(0.0F, 0.0F)))

        (outcome as Outcome.Error).errorType shouldBe ErrorType.PLACE_INFO_API_ERROR
    }
}
