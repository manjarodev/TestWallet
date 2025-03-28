package com.example.myapplication

sealed class Events {

    object SessionApproved : Events()

    object SessionRejected : Events()
    object RequestExpired : Events()

    data class SessionAuthenticateApproved(val message: String?) : Events()
    object SessionAuthenticateRejected : Events()

    data class PingSuccess(val topic: String) : Events()

    object PingError : Events()

    object PingLoading : Events()

    object Disconnect : Events()
    data class DisconnectError(val message: String) : Events()

    object DisconnectLoading : Events()

    data class RequestSuccess(val result: String) : Events()

    data class RequestPeerError(val errorMsg: String) : Events()

    data class RequestError(val exceptionMsg: String) : Events()

    object NoAction : Events()

    data class SessionEvent(val name: String, val data: String) : Events()

    object SessionExtend : Events()

    data class ConnectionEvent(val isAvailable: Boolean) : Events()

    object ProposalExpired : Events()
}