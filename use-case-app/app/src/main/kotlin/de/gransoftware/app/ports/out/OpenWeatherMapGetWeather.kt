package de.gransoftware.app.ports.out

import de.gransoftware.app.usecase.ErrorType
import de.gransoftware.app.usecase.Outcome
import de.gransoftware.app.usecase.Weather
import de.gransoftware.app.usecase.ports.out.GetWeatherOutPort
import kotlinx.coroutines.future.await
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

class OpenWeatherMapGetWeather(private val json: Json) : GetWeatherOutPort {
    private val log = KotlinLogging.logger {}

    @Serializable
    class WeatherWrapper(
        val weather: List<Weather>,
        val main: Main,
        val wind: Wind,
    ) {
        @Serializable
        class Weather(val main: String)

        @Serializable
        class Main(
            val temp: Float,
            @SerialName("feels_like")
            val feelsLike: Float,
            @SerialName("temp_min")
            val minTemperature: Float,
            @SerialName("temp_max")
            val maxTemperature: Float,
            val pressure: Int,
            val humidity: Int
        )

        @Serializable
        class Wind(val speed: Float)
    }

    override suspend operator fun invoke(input: GetWeatherOutPort.Input): Outcome<Weather> {
        val httpClient: HttpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build()
        val requestHead = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create("https://api.openweathermap.org/data/2.5/weather?id=524901&units=metric&appid=${input.apiKey}&lat=${input.coordinates.latitude}&lon=${input.coordinates.longitude}"))
            .build()
        val httpResponse = httpClient.sendAsync(requestHead, HttpResponse.BodyHandlers.ofString()).await()
        return if (httpResponse.statusCode() == 200) {
            val responseBody = httpResponse.body()
            val weather = json.decodeFromString<WeatherWrapper>(responseBody).let {
                Weather(
                    type = it.weather.first().main,
                    temperature = it.main.temp,
                    feelsLike = it.main.feelsLike,
                    minTemperature = it.main.minTemperature,
                    maxTemperature = it.main.maxTemperature,
                    pressure = it.main.pressure,
                    humidity = it.main.humidity,
                    windSpeed = it.wind.speed,
                )
            }
            Outcome.Success(weather)
        } else {
            log.error { "Error while calling OpenWeatherMap PlaceInfo API: ${httpResponse.statusCode()} : ${httpResponse.body()}" }
            return Outcome.Error(ErrorType.PLACE_INFO_API_ERROR)
        }
    }
}