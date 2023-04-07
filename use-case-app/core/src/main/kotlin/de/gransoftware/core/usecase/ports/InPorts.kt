package de.gransoftware.core.usecase.ports

import de.gransoftware.core.usecase.Coordinates
import de.gransoftware.core.usecase.PlaceInfo
import de.gransoftware.core.usecase.UseCase
import de.gransoftware.core.usecase.Weather

interface GetPlaceInfoInPort : UseCase<GetPlaceInfoInPort.Input, PlaceInfo> {
    data class Input(val coordinates: Coordinates)
}

interface GetWeatherInPort : UseCase<GetWeatherInPort.Input, Weather> {
    data class Input(val coordinates: Coordinates)
}