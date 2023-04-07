package de.gransoftware.core.usecase.ports

import de.gransoftware.core.usecase.Coordinates
import de.gransoftware.core.usecase.Outcome
import de.gransoftware.core.usecase.PlaceInfo
import de.gransoftware.core.usecase.Weather

interface GetWeatherOutPort {
    class Input(val coordinates: Coordinates)

    suspend operator fun invoke(input: Input): Outcome<Weather>
}

interface GetPlaceInfoOutPort {
    class Input(val coordinates: Coordinates)

    suspend operator fun invoke(input: Input): Outcome<PlaceInfo>
}
