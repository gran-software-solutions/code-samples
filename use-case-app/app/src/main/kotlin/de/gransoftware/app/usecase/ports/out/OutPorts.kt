package de.gransoftware.app.usecase.ports.out

import de.gransoftware.app.usecase.Coordinates
import de.gransoftware.app.usecase.Outcome
import de.gransoftware.app.usecase.PlaceInfo
import de.gransoftware.app.usecase.Weather

interface GetWeatherOutPort {
    class Input(val apiKey: String, val coordinates: Coordinates)

    suspend operator fun invoke(input: Input): Outcome<Weather>
}

interface GetPlaceInfoOutPort {
    class Input(val apiKey: String, val coordinates: Coordinates)

    suspend operator fun invoke(input: Input): Outcome<PlaceInfo>
}
