package de.gransoftware

import com.charleskorn.kaml.Yaml
import com.github.ajalt.clikt.completion.CompletionCandidates
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.float
import de.gransoftware.core.usecase.Coordinates
import de.gransoftware.core.usecase.Outcome
import de.gransoftware.core.usecase.PlaceInfo
import de.gransoftware.core.usecase.UseCaseExecutor
import de.gransoftware.core.usecase.Weather
import de.gransoftware.core.usecase.ports.GetPlaceInfoInPort
import de.gransoftware.core.usecase.ports.GetWeatherInPort
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.kodein.di.direct
import org.kodein.di.instance

class WeatherCommand : CliktCommand() {
    private val latitude: Float by option("-l", "--latitude", help = "Latitude").float().required()
    private val longitude: Float by option("-L", "--longitude", help = "Longitude").float().required()
    private val openWeatherMapApiKey by option(
        "-k",
        "--openweathermap-api-key",
        help = "OpenWeatherMap API key"
    ).required()

    private val format by option(
        "-f",
        "--format",
        help = "Output format",
        completionCandidates = CompletionCandidates.Fixed("json", "yaml")
    ).required()

    private lateinit var getPlaceInfoInPort: GetPlaceInfoInPort
    private lateinit var getWeatherInPort: GetWeatherInPort

    override fun run() = runBlocking<Unit> {
        val di = configureDependencyInjection(openWeatherMapApiKey)
        getPlaceInfoInPort = di.direct.instance<GetPlaceInfoInPort>()
        getWeatherInPort = di.direct.instance<GetWeatherInPort>()
        runCatching {
            async { getWeather() }.await() to async { getPlaceInfo() }.await()
        }.onSuccess { (weatherOutcome, placeInfoOutcome) ->
            if (weatherOutcome is Outcome.Success && placeInfoOutcome is Outcome.Success) {
                displayWeatherAndPlaceInfo(weatherOutcome.value, placeInfoOutcome.value)
            } else {
                echo(
                    err = true,
                    message = "Error, not all information could be retrieved: ${(weatherOutcome as? Outcome.Error)?.errorType} | ${(placeInfoOutcome as? Outcome.Error)?.errorType}"
                )
                return@onSuccess
            }
        }.onFailure { echo(err = true, message = "Error: ${it.message}") }
    }

    private fun displayWeatherAndPlaceInfo(
        weather: Weather,
        placeInfo: PlaceInfo
    ) {
        val weatherAndPlaceInfo = WeatherAndCity(weather.let {
            WeatherAndCity.Weather(
                it.type,
                it.temperature,
                it.feelsLike,
                it.minTemperature,
                it.maxTemperature,
                it.pressure,
                it.humidity,
                it.windSpeed
            )
        }, placeInfo.let { WeatherAndCity.PlaceInfo(it.name, it.country) })
        when (format) {
            "json" -> echo(Json.encodeToString(weatherAndPlaceInfo))
            "yaml" -> echo(Yaml.default.encodeToString(weatherAndPlaceInfo))
            else -> echo(err = true, message = "Unknown format: $format")
        }
    }

    private suspend fun getPlaceInfo() = UseCaseExecutor.execute(
        useCase = getPlaceInfoInPort,
        toContext = { },
        toInput = { GetPlaceInfoInPort.Input(Coordinates(latitude, longitude)) },
        toResponse = { it: Outcome<PlaceInfo> -> it }
    )

    private suspend fun getWeather() = UseCaseExecutor.execute(
        useCase = getWeatherInPort,
        toContext = { },
        toInput = { GetWeatherInPort.Input(Coordinates(latitude, longitude)) },
        toResponse = { it: Outcome<Weather> -> it }
    )
}

fun main(args: Array<String>) = WeatherCommand().main(args)