package de.gransoftware.core.usecase

import de.gransoftware.core.usecase.ports.GetPlaceInfoInPort
import de.gransoftware.core.usecase.ports.GetPlaceInfoOutPort

class GetPlaceInfoUseCase(private val getPlaceInfo: GetPlaceInfoOutPort) : GetPlaceInfoInPort {

    override suspend operator fun invoke(input: GetPlaceInfoInPort.Input): Outcome<PlaceInfo> = getPlaceInfo(
        GetPlaceInfoOutPort.Input(input.coordinates)
    )
}