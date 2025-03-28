package com.example.myapplication.composable_routes.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.DappDelegate
import com.example.myapplication.Events
import com.reown.appkit.client.AppKit
import com.reown.appkit.client.Modal
import com.reown.appkit.client.models.Session
import com.example.myapplication.composable_routes.chain_selection.Chains
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

class SessionViewModel : ViewModel() {

    private val _sessionUI: MutableStateFlow<List<SessionUi>> = MutableStateFlow(getSessions())
    val uiState: StateFlow<List<SessionUi>> = _sessionUI.asStateFlow()

    private val _sessionEvents: MutableSharedFlow<Events> = MutableSharedFlow()
    val sessionEvent: SharedFlow<Events>
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
                        _sessionEvents.emit(Events.Disconnect)
                    }

                    else -> Unit
                }
            }.launchIn(viewModelScope)
    }

    private fun getSessions(topic: String? = null): List<SessionUi> {
        val session = AppKit.getSession() as? Session.WalletConnectSession ?: return emptyList()

        return session.namespaces.values
            .flatMap { it.accounts }
            .filter {
                val (chainNamespace, chainReference, _) = it.split(":")
                Chains.entries.any { chain ->
                    chain.chainNamespace == chainNamespace && chain.chainReference == chainReference
                }
            }
            .map { caip10Account ->
                val (chainNamespace, chainReference, account) = caip10Account.split(":")
                val chain = Chains.entries.first {
                    it.chainNamespace == chainNamespace && it.chainReference == chainReference
                }
                SessionUi(
                    icon = chain.icon,
                    name = chain.name,
                    address = account,
                    chainNamespace = chain.chainNamespace,
                    chainReference = chain.chainReference
                )
            }
    }

    fun ping() {
        viewModelScope.launch { _sessionEvents.emit(Events.PingLoading) }

        try {
            AppKit.ping(object : Modal.Listeners.SessionPing {
                override fun onSuccess(pingSuccess: Modal.Model.Ping.Success) {
                    viewModelScope.launch {
                        _sessionEvents.emit(Events.PingSuccess(pingSuccess.topic))
                    }
                }

                override fun onError(pingError: Modal.Model.Ping.Error) {
                    viewModelScope.launch {
                        _sessionEvents.emit(Events.PingError)
                    }
                }
            })
        } catch (e: Exception) {
            viewModelScope.launch {
                _sessionEvents.emit(Events.PingError)
            }
        }
    }

    fun disconnect() {
        try {
            viewModelScope.launch { _sessionEvents.emit(Events.DisconnectLoading) }
            AppKit.disconnect(
                onSuccess = {
                    DappDelegate.deselectAccountDetails()
                    viewModelScope.launch {
                        _sessionEvents.emit(Events.Disconnect)
                    }
                },
                onError = { throwable: Throwable ->
                    throwable.printStackTrace()
                    viewModelScope.launch {
                        _sessionEvents.emit(
                            Events.DisconnectError(
                                throwable.message
                                    ?: "Unknown error, please try again or contact support"
                            )
                        )
                    }
                })

        } catch (e: Exception) {
            viewModelScope.launch {
                _sessionEvents.emit(
                    Events.DisconnectError(
                        e.message ?: "Unknown error, please try again or contact support"
                    )
                )
            }
        }
    }
}