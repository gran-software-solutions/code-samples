package de.gransoftware.app.ports.out

import de.gransoftware.app.usecase.ports.out.GetPlaceInfoOutPort
import de.gransoftware.app.usecase.Outcome
import kotlinx.coroutines.future.await
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import de.gransoftware.app.usecase.PlaceInfo as DomainPlaceInfo

class OpenWeatherMapGetPlaceInfo(private val json: Json) : GetPlaceInfoOutPort {

    @Serializable
    class PlaceInfo(
        val name: String,
        val country: String
    )

    override suspend operator fun invoke(input: GetPlaceInfoOutPort.Input): Outcome<DomainPlaceInfo> {
        val httpClient: HttpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build()
        val requestHead = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create("http://api.openweathermap.org/geo/1.0/reverse?lat=${input.coordinates.latitude}&lon=${input.coordinates.longitude}&limit=1&appid=${input.apiKey}"))
            .build()
        val httpResponse = httpClient.sendAsync(requestHead, HttpResponse.BodyHandlers.ofString()).await()
        return if (httpResponse.statusCode() == 200) {
            val places = json.decodeFromString<List<PlaceInfo>>(httpResponse.body())
            require(places.size == 1) { "Expected exactly one result" }
            Outcome.Success(places.first().let { DomainPlaceInfo(it.name, it.country) })
        } else {
            TODO("Not yet implemented")
        }
    }
}