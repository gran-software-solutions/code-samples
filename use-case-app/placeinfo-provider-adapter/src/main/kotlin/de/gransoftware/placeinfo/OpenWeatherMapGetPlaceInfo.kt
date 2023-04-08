package de.gransoftware.placeinfo

import de.gransoftware.core.usecase.ErrorType
import de.gransoftware.core.usecase.Outcome
import de.gransoftware.core.usecase.ports.GetPlaceInfoOutPort
import kotlinx.coroutines.future.await
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import de.gransoftware.core.usecase.PlaceInfo as DomainPlaceInfo

class OpenWeatherMapGetPlaceInfo(
    private val json: Json,
    private val apiKey: String,
    private val httpClient: HttpClient,
) : GetPlaceInfoOutPort {
    private val log = KotlinLogging.logger {}

    @Serializable
    class PlaceInfo(
        val name: String,
        val country: String
    )

    override suspend operator fun invoke(input: GetPlaceInfoOutPort.Input): Outcome<DomainPlaceInfo> {
        val httpResponse = httpClient.sendAsync(buildRequest(input), HttpResponse.BodyHandlers.ofString()).await()
        return if (httpResponse.statusCode() == 200) {
            val places = json.decodeFromString<List<PlaceInfo>>(httpResponse.body())
            require(places.size == 1) { "Expected exactly one result" }
            Outcome.Success(places.first().let { DomainPlaceInfo(it.name, it.country) })
        } else {
            log.error { "Error while calling OpenWeatherMap Weather API: ${httpResponse.statusCode()} : ${httpResponse.body()}" }
            return Outcome.Error(ErrorType.WEATHER_API_ERROR)
        }
    }

    private fun buildRequest(input: GetPlaceInfoOutPort.Input) =
        HttpRequest.newBuilder()
            .GET()
            .uri(URI.create("http://api.openweathermap.org/geo/1.0/reverse?lat=${input.coordinates.latitude}&lon=${input.coordinates.longitude}&limit=1&appid=$apiKey"))
            .build()
}