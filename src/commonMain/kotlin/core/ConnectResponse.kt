package core

import kotlinx.serialization.Serializable

@Serializable
sealed class ConnectResponse<out T> {

    @Serializable
    object Success : ConnectResponse<Nothing>()

    @Serializable
    object CannotConnect : ConnectResponse<Nothing>()

    @Serializable
    class CannotJoin<out T>(val reason: T) : ConnectResponse<T>()
}