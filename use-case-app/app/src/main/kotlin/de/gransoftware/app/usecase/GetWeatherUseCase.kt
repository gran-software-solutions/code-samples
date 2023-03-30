package de.gransoftware.app.usecase

import de.gransoftware.app.usecase.ports.out.GetWeatherOutPort
import de.gransoftware.app.usecase.ports.out.GetWeatherOutPort.Input

class GetWeatherUseCase(private val getWeather: GetWeatherOutPort) : UseCase<Input, Weather> {

    override suspend fun execute(input: Input): Outcome<Weather> = getWeather(input)
}