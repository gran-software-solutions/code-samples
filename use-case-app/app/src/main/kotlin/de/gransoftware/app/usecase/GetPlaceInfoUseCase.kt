package de.gransoftware.app.usecase

import de.gransoftware.app.usecase.ports.out.GetPlaceInfoOutPort
import de.gransoftware.app.usecase.ports.out.GetPlaceInfoOutPort.Input

class GetPlaceInfoUseCase(private val getPlaceInfo: GetPlaceInfoOutPort) : UseCase<Input, PlaceInfo> {

    override suspend fun execute(input: Input): Outcome<PlaceInfo> = getPlaceInfo(input)
}