package de.gransoftware.app

import com.charleskorn.kaml.Yaml
import com.github.ajalt.clikt.completion.CompletionCandidates
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.float
import de.gransoftware.app.ui.WeatherAndCity
import de.gransoftware.app.usecase.Coordinates
import de.gransoftware.app.usecase.GetPlaceInfoOutPort
import de.gransoftware.app.usecase.GetWeatherOutPort
import de.gransoftware.app.usecase.Outcome
import de.gransoftware.app.usecase.PlaceInfo
import de.gransoftware.app.usecase.UseCaseExecutor
import de.gransoftware.app.usecase.Weather
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.kodein.di.direct
import org.kodein.di.instance

class App {
    val greeting: String
        get() {
            return "Hello World!"
        }
}

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

    override fun run() = runBlocking<Unit> {
        val di = configureDependencyInjection()
        runCatching {
            val weatherOutcome = async {
                UseCaseExecutor.execute(
                    useCase = di.direct.instance(),
                    toContext = { },
                    toInput = { GetWeatherOutPort.Input(openWeatherMapApiKey, Coordinates(latitude, longitude)) },
                    toResponse = { it: Outcome<Weather> -> it }
                )
            }
            val placeInfoOutcome = async {
                UseCaseExecutor.execute(
                    useCase = di.direct.instance(),
                    toContext = { },
                    toInput = { GetPlaceInfoOutPort.Input(openWeatherMapApiKey, Coordinates(latitude, longitude)) },
                    toResponse = { it: Outcome<PlaceInfo> -> it }
                )
            }
            weatherOutcome.await() to placeInfoOutcome.await()
        }.onSuccess { (weatherOutcome, placeInfoOutcome) ->
            if (weatherOutcome is Outcome.Error || placeInfoOutcome is Outcome.Error) {
                echo(
                    err = true,
                    message = "Error, not all information could be retrieved: ${(weatherOutcome as? Outcome.Error)?.errorType} | ${(placeInfoOutcome as? Outcome.Error)?.errorType}"
                )
                return@onSuccess
            }

            val (weather, placeInfo) = weatherOutcome as Outcome.Success<Weather> to placeInfoOutcome as Outcome.Success<PlaceInfo>

            when (format) {
                "json" -> {
                    val weatherAndCity = WeatherAndCity(weather.value.let {
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
                    }, placeInfo.value.let { WeatherAndCity.City(it.name, it.country) })
                    echo(Json.encodeToString(weatherAndCity))
                }

                "yaml" -> {
                    val weatherAndCity = WeatherAndCity(weather.value.let {
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
                    }, placeInfo.value.let { WeatherAndCity.City(it.name, it.country) })
                    echo(Yaml.default.encodeToString(weatherAndCity))
                }

                else -> echo(err = true, message = "Unknown format: $format")
            }

        }.onFailure { echo(err = true, message = "Error: ${it.message}") }
    }
}

fun main(args: Array<String>) {
    WeatherCommand().main(args)
}
