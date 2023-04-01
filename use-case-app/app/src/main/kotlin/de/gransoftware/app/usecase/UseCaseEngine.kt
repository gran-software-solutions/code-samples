package de.gransoftware.app.usecase

interface UseCase<in Input, out T : Any> {
    suspend operator fun invoke(input: Input): Outcome<T>
}

enum class ErrorType {
    UNKNOWN
}

sealed class Outcome<out T : Any> {
    data class Success<out T : Any>(val value: T) : Outcome<T>()
    data class Error(val errorType: ErrorType, val exception: Throwable? = null) : Outcome<Nothing>()
}

object UseCaseExecutor {
    // some params - some response
    suspend fun <Request, Response, Input, T : Any> execute(
        useCase: UseCase<Input, T>,
        toContext: suspend () -> Request,
        toInput: (Request) -> Input,
        toResponse: (Outcome<T>) -> Response
    ) =
        toContext.invoke()
            .let(toInput)
            .let { useCase(it) }
            .let(toResponse)

    // some params - no response
    suspend fun <Request, Input> execute(
        useCase: UseCase<Input, Unit>,
        toContext: () -> Request,
        toInput: (Request) -> Input
    ) = execute(useCase, toContext, toInput) {}

    // no params - no response
    suspend fun execute(useCase: UseCase<Unit, Unit>) =
        execute(useCase, { }) { }

    // no params - some response
    suspend fun <Response, T : Any> execute(
        useCase: UseCase<Unit, T>,
        toResponse: (Outcome<T>) -> Response
    ) =
        execute(useCase, {}, { }, toResponse)
}
