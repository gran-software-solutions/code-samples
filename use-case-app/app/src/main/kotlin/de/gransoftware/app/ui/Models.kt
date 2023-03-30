package de.gransoftware.app.ui

import kotlinx.serialization.Serializable

@Serializable
class WeatherAndCity(
    val weather: Weather,
    val placeInfo: PlaceInfo
) {
    @Serializable
    class PlaceInfo(
        val name: String,
        val country: String
    )

    @Serializable
    class Weather(
        val type: String,
        val temperature: Float,
        val feelsLike: Float,
        val minTemperature: Float,
        val maxTemperature: Float,
        val pressure: Int,
        val humidity: Int,
        val windSpeed: Float,
    )
}