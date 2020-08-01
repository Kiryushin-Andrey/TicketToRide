package core

enum class ConnectionState {
    NotConnected,
    Connected,
    Reconnecting,
    CannotJoin,
    CannotConnect
}