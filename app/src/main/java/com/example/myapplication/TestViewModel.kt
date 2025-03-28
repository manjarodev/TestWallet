package com.example.myapplication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reown.appkit.client.Modal
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.shareIn

class TestViewModel : ViewModel() {
    val events = merge(DappDelegate.wcEventModels, DappDelegate.connectionState)
        .map { event ->
            when (event) {
                is Modal.Model.ConnectionState -> Events.ConnectionEvent(event.isAvailable)
                is Modal.Model.DeletedSession -> Events.Disconnect
                is Modal.Model.Session -> Events.SessionExtend
                is Modal.Model.Error -> Events.RequestError(event.throwable.localizedMessage ?: "Something goes wrong")
                else -> Events.NoAction
            }
        }.shareIn(viewModelScope, SharingStarted.WhileSubscribed())
}