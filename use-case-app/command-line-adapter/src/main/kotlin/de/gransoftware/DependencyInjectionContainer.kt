package de.gransoftware

import de.gransoftware.core.usecase.GetPlaceInfoUseCase
import de.gransoftware.core.usecase.GetWeatherUseCase
import de.gransoftware.placeinfo.OpenWeatherMapGetPlaceInfo
import de.gransoftware.weather.OpenWeatherMapGetWeather
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance

private fun useCases() = DI.Module("useCases") {
    bindSingleton { GetWeatherUseCase(instance()) }
    bindSingleton { GetPlaceInfoUseCase(instance()) }
}

private fun outPorts(openWeatherMapApiKey: String) = DI.Module("outPorts") {
    bindSingleton { OpenWeatherMapGetWeather(instance(), openWeatherMapApiKey) }
    bindSingleton { OpenWeatherMapGetPlaceInfo(instance(), openWeatherMapApiKey) }
}

fun configureDependencyInjection(openWeatherMapApiKey: String) = DI {
    import(useCases())
    import(outPorts(openWeatherMapApiKey))
    bindSingleton { Json { ignoreUnknownKeys = true } }
}