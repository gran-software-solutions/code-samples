package de.gransoftware.app.usecase

import de.gransoftware.app.usecase.ports.out.GetPlaceInfoOutPort
import de.gransoftware.app.usecase.ports.out.GetPlaceInfoOutPort.Input

class GetPlaceInfoUseCase(private val getPlaceInfo: GetPlaceInfoOutPort) : UseCase<Input, PlaceInfo> {

    override suspend operator fun invoke(input: Input): Outcome<PlaceInfo> = getPlaceInfo(input)
}