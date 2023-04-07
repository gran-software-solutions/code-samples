package de.gransoftware.core

import de.gransoftware.core.usecase.Coordinates
import de.gransoftware.core.usecase.ErrorType
import de.gransoftware.core.usecase.GetWeatherUseCase
import de.gransoftware.core.usecase.Outcome
import de.gransoftware.core.usecase.Weather
import de.gransoftware.core.usecase.ports.GetWeatherInPort
import de.gransoftware.core.usecase.ports.GetWeatherOutPort
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlin.test.Test

class GetWeatherUseCaseTest {
    @Test
    fun `invoke, for supported coordinates returns correct place info`(): Unit = runBlocking {
        val weather = Weather(
            type = "Sunny",
            temperature = 20.0F,
            windSpeed = 10.0F,
            feelsLike = 20.0F,
            minTemperature = 10.0F,
            maxTemperature = 30.0F,
            pressure = 1000,
            humidity = 50,
        )
        val getWeatherOutPort = mockk<GetWeatherOutPort> {
            coEvery { this@mockk.invoke(any()) } returns Outcome.Success(
                weather
            )
        }

        val outcome = GetWeatherUseCase(getWeatherOutPort).invoke(GetWeatherInPort.Input(Coordinates(0.0F, 0.0F)))

        val weatherOutcome = (outcome as Outcome.Success).value
        weatherOutcome shouldBe weather
    }

    @Test
    fun `invoke, for unsupported coordinates returns error`(): Unit = runBlocking {
        val getWeatherOutPort = mockk<GetWeatherOutPort> {
            coEvery { this@mockk.invoke(any()) } returns Outcome.Error(ErrorType.WEATHER_API_ERROR)
        }

        val outcome = GetWeatherUseCase(getWeatherOutPort).invoke(GetWeatherInPort.Input(Coordinates(0.0F, 0.0F)))

        val errorType = (outcome as Outcome.Error).errorType
        errorType shouldBe ErrorType.WEATHER_API_ERROR
    }
}
