package com.example.myapplication.composable_routes.account

import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.DappDelegate
import com.example.myapplication.Events
import com.example.myapplication.composable_routes.chain_selection.Chains
import com.example.myapplication.composable_routes.chain_selection.getEthSendTransaction
import com.example.myapplication.composable_routes.chain_selection.getEthSignBody
import com.example.myapplication.composable_routes.chain_selection.getEthSignTypedData
import com.example.myapplication.composable_routes.chain_selection.getPersonalSignBody
import com.example.myapplication.accountArg
import com.reown.appkit.client.AppKit
import com.reown.appkit.client.Modal
import com.reown.appkit.client.models.Session
import com.reown.appkit.client.models.request.Request
import com.example.myapplication.composable_routes.chain_selection.getSolanaSignAndSendParams
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

class AccountViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val selectedAccountInfo = checkNotNull(savedStateHandle.get<String>(accountArg))

    private val _uiState: MutableStateFlow<AccountUi> = MutableStateFlow(AccountUi.Loading)
    val uiState: StateFlow<AccountUi> = _uiState.asStateFlow()

    private val _awaitResponse: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val awaitResponse: StateFlow<Boolean> = _awaitResponse.asStateFlow()

    private val _events: MutableSharedFlow<Events> = MutableSharedFlow()
    val events: SharedFlow<Events>
        get() = _events.asSharedFlow()

    init {
        DappDelegate.wcEventModels
            .filterNotNull()
            .onEach { walletEvent ->
                when (walletEvent) {
                    is Modal.Model.UpdatedSession -> fetchAccountDetails()
                    is Modal.Model.SessionRequestResponse -> {
                        val request = when (walletEvent.result) {
                            is Modal.Model.JsonRpcResponse.JsonRpcResult -> {
                                _awaitResponse.value = false
                                val successResult = (walletEvent.result as Modal.Model.JsonRpcResponse.JsonRpcResult)
                                Events.RequestSuccess(successResult.result)
                            }

                            is Modal.Model.JsonRpcResponse.JsonRpcError -> {
                                _awaitResponse.value = false
                                val errorResult = (walletEvent.result as Modal.Model.JsonRpcResponse.JsonRpcError)
                                Events.RequestPeerError("Error Message: ${errorResult.message}\n Error Code: ${errorResult.code}")
                            }
                        }

                        _events.emit(request)
                    }

                    is Modal.Model.ExpiredRequest -> {
                        _awaitResponse.value = false
                        _events.emit(Events.RequestError("Request expired"))
                    }

                    is Modal.Model.DeletedSession -> {
                        _events.emit(Events.Disconnect)
                    }

                    else -> Unit
                }
            }
            .launchIn(viewModelScope)
    }

    fun requestMethod(method: String, sendSessionRequestDeepLink: (Uri) -> Unit) {
        (uiState.value as? AccountUi.AccountData)?.let { currentState ->
            try {
                _awaitResponse.value = true

                val (_, _, account) = currentState.selectedAccount.split(":")
                val params: String = when {
                    method.equals("personal_sign", true) -> getPersonalSignBody(account)
                    method.equals("eth_sign", true) -> getEthSignBody(account)
                    method.equals("eth_sendTransaction", true) -> getEthSendTransaction(account)
                    method.equals("eth_signTypedData", true) -> getEthSignTypedData(account)
                    method.equals("solana_signAndSendTransaction", true) -> getSolanaSignAndSendParams()
                    else -> "[]"
                }
                val requestParams = Request(
                    method = method,
                    params = params, // stringified JSON
                )

                AppKit.request(requestParams,
                    onSuccess = { _ ->
                        (AppKit.getSession() as Session.WalletConnectSession).redirect?.toUri()?.let { deepLinkUri -> sendSessionRequestDeepLink(deepLinkUri) }
                    },
                    onError = {
                        viewModelScope.launch {
                            _awaitResponse.value = false
                            _events.emit(Events.RequestError(it.localizedMessage ?: "Error trying to send request"))
                        }
                    })
            } catch (e: Exception) {
                viewModelScope.launch {
                    _awaitResponse.value = false
                    _events.emit(Events.RequestError(e.localizedMessage ?: "Error trying to send request"))
                }
            }
        }
    }

    fun fetchAccountDetails() {
        val (chainNamespace, chainReference, account) = selectedAccountInfo.split(":")
        val chainDetails = Chains.values().first {
            it.chainNamespace == chainNamespace && it.chainReference == chainReference
        }

        val listOfMethodsByChainId: List<String> =
            (AppKit.getSession() as Session.WalletConnectSession).namespaces
                .filter { (namespaceKey, _) -> namespaceKey == chainDetails.chainId }
                .flatMap { (_, namespace) -> namespace.methods }


        val listOfMethodsByNamespace: List<String> =
            (AppKit.getSession() as Session.WalletConnectSession).namespaces
                .filter { (namespaceKey, _) -> namespaceKey == chainDetails.chainNamespace }
                .flatMap { (_, namespace) -> namespace.methods }

        viewModelScope.launch {
            _uiState.value = AccountUi.AccountData(
                icon = chainDetails.icon,
                chainName = chainDetails.chainName,
                account = account,
                listOfMethods = listOfMethodsByChainId.ifEmpty { listOfMethodsByNamespace },
                selectedAccount = selectedAccountInfo
            )
        }
    }
}