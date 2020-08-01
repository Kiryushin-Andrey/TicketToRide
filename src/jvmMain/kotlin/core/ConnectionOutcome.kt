package core

sealed class ConnectionOutcome<out TConnection, out TCannotJoinReason> {
    class Success<TConnection>(val connection: TConnection) : ConnectionOutcome<TConnection, Nothing>()
    class CannotConnect<TConnection> : ConnectionOutcome<TConnection, Nothing>()
    class CannotJoin<TCannotJoinReason>(val reason: TCannotJoinReason) : ConnectionOutcome<Nothing, TCannotJoinReason>()
}