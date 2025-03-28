package com.example.myapplication

import com.reown.android.Core
import com.reown.android.CoreClient
import com.reown.appkit.client.AppKit
import com.reown.appkit.client.Modal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import timber.log.Timber.Forest.tag


object DappDelegate : AppKit.ModalDelegate, CoreClient.CoreDelegate {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _wcEventModels: MutableSharedFlow<Modal.Model?> = MutableSharedFlow()
    val wcEventModels: SharedFlow<Modal.Model?> = _wcEventModels.asSharedFlow()
    private val _coreEvents: MutableSharedFlow<Core.Model> = MutableSharedFlow()
    val coreEvents: SharedFlow<Core.Model> = _coreEvents.asSharedFlow()
    private val _connectionState: MutableSharedFlow<Modal.Model.ConnectionState> = MutableSharedFlow(replay = 1)
    val connectionState: SharedFlow<Modal.Model.ConnectionState> = _connectionState.asSharedFlow()

    var selectedSessionTopic: String? = null
        private set

    init {
        AppKit.setDelegate(this)
        CoreClient.setDelegate(this)
    }

    override fun onConnectionStateChange(state: Modal.Model.ConnectionState) {
        Timber.d("onConnectionStateChange($state)")
        scope.launch {
            _connectionState.emit(state)
        }
    }

    override fun onSessionApproved(approvedSession: Modal.Model.ApprovedSession) {
        selectedSessionTopic = (approvedSession as Modal.Model.ApprovedSession.WalletConnectSession).topic

        scope.launch {
            _wcEventModels.emit(approvedSession)
        }
    }

    override fun onSessionRejected(rejectedSession: Modal.Model.RejectedSession) {
        scope.launch {
            _wcEventModels.emit(rejectedSession)
        }
    }

    override fun onSessionUpdate(updatedSession: Modal.Model.UpdatedSession) {
        scope.launch {
            _wcEventModels.emit(updatedSession)
        }
    }

    override fun onSessionEvent(sessionEvent: Modal.Model.SessionEvent) {
        scope.launch {
            _wcEventModels.emit(sessionEvent)
        }
    }

    override fun onSessionEvent(sessionEvent: Modal.Model.Event) {
        scope.launch {
            _wcEventModels.emit(sessionEvent)
        }
    }

    override fun onSessionDelete(deletedSession: Modal.Model.DeletedSession) {
        deselectAccountDetails()

        scope.launch {
            _wcEventModels.emit(deletedSession)
        }
    }

    override fun onSessionExtend(session: Modal.Model.Session) {
        scope.launch {
            _wcEventModels.emit(session)
        }
    }

    override fun onSessionRequestResponse(response: Modal.Model.SessionRequestResponse) {
        scope.launch {
            _wcEventModels.emit(response)
        }
    }

    override fun onSessionAuthenticateResponse(sessionUpdateResponse: Modal.Model.SessionAuthenticateResponse) {
        if (sessionUpdateResponse is Modal.Model.SessionAuthenticateResponse.Result) {
            selectedSessionTopic = sessionUpdateResponse.session?.topic
        }
        scope.launch {
            _wcEventModels.emit(sessionUpdateResponse)
        }
    }

    override fun onProposalExpired(proposal: Modal.Model.ExpiredProposal) {
        scope.launch {
            _wcEventModels.emit(proposal)
        }
    }

    override fun onRequestExpired(request: Modal.Model.ExpiredRequest) {
        scope.launch {
            _wcEventModels.emit(request)
        }
    }

    fun deselectAccountDetails() {
        selectedSessionTopic = null
    }

    override fun onError(error: Modal.Model.Error) {
        Timber.d(error.throwable.stackTraceToString())
        scope.launch {
            _wcEventModels.emit(error)
        }
    }

    override fun onPairingDelete(deletedPairing: Core.Model.DeletedPairing) {
        //Deprecated - pairings are automatically deleted
    }

    override fun onPairingExpired(expiredPairing: Core.Model.ExpiredPairing) {
        //Deprecated - pairings are automatically expired
    }

    override fun onPairingState(pairingState: Core.Model.PairingState) {
        Timber.d("Dapp pairing state: ${pairingState.isPairingState}")
    }
}