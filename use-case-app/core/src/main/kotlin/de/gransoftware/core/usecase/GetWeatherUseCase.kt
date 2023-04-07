package de.gransoftware.core.usecase

import de.gransoftware.core.usecase.ports.GetWeatherInPort
import de.gransoftware.core.usecase.ports.GetWeatherOutPort
import de.gransoftware.core.usecase.ports.GetWeatherOutPort.Input

class GetWeatherUseCase(private val getWeather: GetWeatherOutPort) : GetWeatherInPort {
    override suspend fun invoke(input: GetWeatherInPort.Input): Outcome<Weather> {
        return getWeather(Input(input.coordinates))
    }
}