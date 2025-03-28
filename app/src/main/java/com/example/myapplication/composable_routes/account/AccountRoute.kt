package com.example.myapplication.composable_routes.account

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.myapplication.Events
//import com.example.myapplication.viewModel
import com.example.myapplication.ui.theme.BlueButton
//import com.reown.sample.common.ui.commons.FullScreenLoader
//import com.reown.sample.common.ui.commons.Loader
//import com.reown.sample.dapp.ui.DappSampleEvents
import com.example.myapplication.Route
import com.example.myapplication.ui.theme.FullScreenLoader
import com.example.myapplication.ui.theme.Loader
import timber.log.Timber

@Composable
fun AccountRoute(navController: NavController) {
    val viewModel: AccountViewModel = viewModel()
    val state by viewModel.uiState.collectAsState()
    val awaitResponse by viewModel.awaitResponse.collectAsState(false)
    val showDialog = remember { mutableStateOf(false) }
    val dialogMessage = remember { mutableStateOf("") }
    val errorMessage = remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.fetchAccountDetails()

        viewModel.events.collect { event ->
            when (event) {
                is Events.RequestSuccess -> {
                    dialogMessage.value = "Result: ${event.result}"
                    showDialog.value = true
                }

                is Events.RequestPeerError -> {
                    errorMessage.value = "Error: ${event.errorMsg}"
                    showDialog.value = true
                }

                is Events.RequestError -> {
                    errorMessage.value = "Error: ${event.exceptionMsg}"
                    showDialog.value = true
                }

                is Events.Disconnect -> navController.navigate(Route.ChainSelection.path) {
                    popUpTo(navController.graph.startDestinationId) {
                        inclusive = true
                    }
                }

                else -> Unit
            }
        }
    }

    if (showDialog.value) {
        SimpleResultDialog(
            message = dialogMessage.value,
            error = errorMessage.value,
            onClose = { showDialog.value = false }
        )
    }

    AccountScreen(
        state = state,
        onMethodClick = viewModel::requestMethod,
        awaitResponse = awaitResponse
    )
}

@Composable
fun SimpleResultDialog(
    message: String,
    error: String,
    onClose: () -> Unit
) {
    AlertDialog(
        modifier = Modifier.semantics {
            contentDescription = "result_dialog"
        },
        onDismissRequest = onClose,
        text = {
            if (message.isNotEmpty()) {
                Text(
                    text = message,
                    modifier = Modifier.semantics {
                        contentDescription = "result_message"
                    }
                )
            }
            if (error.isNotEmpty()) {
                Text(
                    text = error,
                    modifier = Modifier.semantics {
                        contentDescription = "result_error"
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onClose,
                modifier = Modifier.semantics {
                    contentDescription = "close_button"
                }
            ) {
                Text("Close")
            }
        }
    )
}


@Composable
private fun AccountScreen(
    state: AccountUi,
    onMethodClick: (String, (Uri) -> Unit) -> Unit,
    awaitResponse: Boolean,
) {
    when (state) {
        AccountUi.Loading -> FullScreenLoader()
        is AccountUi.AccountData -> AccountContent(state, onMethodClick, awaitResponse)
    }
}

@Composable
fun AccountContent(
    state: AccountUi.AccountData,
    onMethodClick: (String, (Uri) -> Unit) -> Unit,
    awaitResponse: Boolean,
) {
    Box {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            ChainData(chain = state)
            Spacer(modifier = Modifier.height(6.dp))
            MethodList(
                methods = state.listOfMethods,
                onMethodClick = onMethodClick
            )
        }

        if (awaitResponse) {
            Loader()
        }
    }
}

@Composable
private fun ChainData(chain: AccountUi.AccountData) {
    Column(
        modifier = Modifier
            .clickable { }
            .fillMaxWidth()
            .padding(horizontal = 24.dp, 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(model = chain.icon, contentDescription = null, Modifier.size(48.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = chain.chainName,
                style = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = chain.account,
            style = TextStyle(fontSize = 12.sp),
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 6.dp),
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun MethodList(
    methods: List<String>,
    onMethodClick: (String, (Uri) -> Unit) -> Unit,
) {
    val context = LocalContext.current
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        itemsIndexed(methods) { _, item ->
            BlueButton(
                text = item,
                onClick = {
                    onMethodClick(item) { uri ->
                        try {
                            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                        } catch (e: Exception) {
                            Timber.tag("AccountRoute").d("Activity not found: $e")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
