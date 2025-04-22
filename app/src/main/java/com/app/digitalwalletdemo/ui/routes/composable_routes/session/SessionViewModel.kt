package com.app.digitalwalletdemo.ui.routes.composable_routes.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.digitalwalletdemo.domain.DappDelegate
import com.app.digitalwalletdemo.ui.DappSampleEvents
import com.reown.appkit.client.AppKit
import com.reown.appkit.client.Modal
import com.reown.appkit.client.models.Session
import com.app.digitalwalletdemo.common.Chains
import com.app.digitalwalletdemo.common.tag
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber

class SessionViewModel : ViewModel() {

    private val _sessionUI: MutableStateFlow<List<SessionUi>> = MutableStateFlow(getSessions())
    val uiState: StateFlow<List<SessionUi>> = _sessionUI.asStateFlow()

    private val _sessionEvents: MutableSharedFlow<DappSampleEvents> = MutableSharedFlow()
    val sessionEvent: SharedFlow<DappSampleEvents>
        get() = _sessionEvents.asSharedFlow()

    init {
        DappDelegate.wcEventModels
            .filterNotNull()
            .onEach { event ->
                when (event) {
                    is Modal.Model.UpdatedSession -> {
                        _sessionUI.value = getSessions(event.topic)
                    }

                    is Modal.Model.DeletedSession -> {
                        _sessionEvents.emit(DappSampleEvents.Disconnect)
                    }

                    else -> Unit
                }
            }.launchIn(viewModelScope)
    }

    private fun getSessions(topic: String? = null): List<SessionUi> {
        return (AppKit.getSession() as Session.WalletConnectSession).namespaces.values
            .flatMap { it.accounts }
            .filter {
                val (chainNamespace, chainReference, account) = it.split(":")
                val chain = Chains.values().find { chain ->
                    chain.chainNamespace == chainNamespace && chain.chainReference == chainReference
                }
                chain != null
            }
            .map { caip10Account ->
                val (chainNamespace, chainReference, account) = caip10Account.split(":")
                val chain = Chains.values().first { chain ->
                    chain.chainNamespace == chainNamespace && chain.chainReference == chainReference
                }
                SessionUi(
                    chain.icon,
                    chain.name,
                    account,
                    chain.chainNamespace,
                    chain.chainReference
                )
            }
    }

    fun ping() {
        viewModelScope.launch { _sessionEvents.emit(DappSampleEvents.PingLoading) }

        try {
            AppKit.ping(object : Modal.Listeners.SessionPing {
                override fun onSuccess(pingSuccess: Modal.Model.Ping.Success) {
                    viewModelScope.launch {
                        _sessionEvents.emit(DappSampleEvents.PingSuccess(pingSuccess.topic))
                    }
                }

                override fun onError(pingError: Modal.Model.Ping.Error) {
                    viewModelScope.launch {
                        _sessionEvents.emit(DappSampleEvents.PingError)
                    }
                }
            })
        } catch (e: Exception) {
            viewModelScope.launch {
                _sessionEvents.emit(DappSampleEvents.PingError)
            }
        }
    }

    fun disconnect() {
        try {
            viewModelScope.launch { _sessionEvents.emit(DappSampleEvents.DisconnectLoading) }
            AppKit.disconnect(
                onSuccess = {
                    DappDelegate.deselectAccountDetails()
                    viewModelScope.launch {
                        _sessionEvents.emit(DappSampleEvents.Disconnect)
                    }
                },
                onError = { throwable: Throwable ->
                    Timber.tag(tag(this)).e(throwable.stackTraceToString())
//                    Firebase.crashlytics.recordException(throwable)
                    viewModelScope.launch {
                        _sessionEvents.emit(
                            DappSampleEvents.DisconnectError(
                                throwable.message
                                    ?: "Unknown error, please try again or contact support"
                            )
                        )
                    }
                })

        } catch (e: Exception) {
            viewModelScope.launch {
                _sessionEvents.emit(
                    DappSampleEvents.DisconnectError(
                        e.message ?: "Unknown error, please try again or contact support"
                    )
                )
            }
        }
    }
}