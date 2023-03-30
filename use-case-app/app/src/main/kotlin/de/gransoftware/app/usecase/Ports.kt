package de.gransoftware.app.usecase

interface GetWeatherOutPort {
    class Input(val apiKey: String, val coordinates: Coordinates)

    suspend operator fun invoke(input: Input): Outcome<Weather>
}

interface GetPlaceInfoOutPort {
    class Input(val apiKey: String, val coordinates: Coordinates)

    suspend operator fun invoke(input: Input): Outcome<PlaceInfo>
}
