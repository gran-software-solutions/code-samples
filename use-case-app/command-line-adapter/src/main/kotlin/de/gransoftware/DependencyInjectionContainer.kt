package de.gransoftware

import de.gransoftware.core.usecase.GetPlaceInfoUseCase
import de.gransoftware.core.usecase.GetWeatherUseCase
import de.gransoftware.placeinfo.OpenWeatherMapGetPlaceInfo
import de.gransoftware.weather.OpenWeatherMapGetWeather
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import java.net.http.HttpClient
import java.time.Duration

private fun useCases() = DI.Module("useCases") {
    bindSingleton { GetWeatherUseCase(instance()) }
    bindSingleton { GetPlaceInfoUseCase(instance()) }
}

private fun outPorts(openWeatherMapApiKey: String) = DI.Module("outPorts") {
    bindSingleton { OpenWeatherMapGetWeather(instance(), openWeatherMapApiKey, instance()) }
    bindSingleton { OpenWeatherMapGetPlaceInfo(instance(), openWeatherMapApiKey, instance()) }
}

fun configureDependencyInjection(openWeatherMapApiKey: String) = DI {
    import(useCases())
    import(outPorts(openWeatherMapApiKey))
    bindSingleton { Json { ignoreUnknownKeys = true } }
    bindSingleton {
        HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build()
    }
}