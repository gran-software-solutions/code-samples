package de.gransoftware.app.usecase

import de.gransoftware.app.usecase.GetWeatherOutPort.Input

class GetWeatherUseCase(private val getWeather: GetWeatherOutPort) : UseCase<Input, Weather> {

    override suspend fun execute(input: Input): Outcome<Weather> = getWeather(input)
}