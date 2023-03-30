package de.gransoftware.app

import de.gransoftware.app.ports.OpenWeatherMapGetPlaceInfo
import de.gransoftware.app.ports.OpenWeatherMapGetWeather
import de.gransoftware.app.usecase.GetPlaceInfoUseCase
import de.gransoftware.app.usecase.GetWeatherUseCase
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance

private val useCases = DI.Module("useCases") {
    bindSingleton { GetWeatherUseCase(instance()) }
    bindSingleton { GetPlaceInfoUseCase(instance()) }
}

private val outPorts = DI.Module("outPorts") {
    bindSingleton { OpenWeatherMapGetWeather(instance()) }
    bindSingleton { OpenWeatherMapGetPlaceInfo(instance()) }
}

fun configureDependencyInjection() = DI {
    import(useCases)
    import(outPorts)
    bindSingleton { Json { ignoreUnknownKeys = true } }
}