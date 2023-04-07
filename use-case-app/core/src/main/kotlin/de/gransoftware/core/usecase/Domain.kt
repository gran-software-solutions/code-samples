package de.gransoftware.core.usecase

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

class PlaceInfo(
    val name: String,
    val country: String
)

data class Coordinates(val latitude: Float, val longitude: Float)