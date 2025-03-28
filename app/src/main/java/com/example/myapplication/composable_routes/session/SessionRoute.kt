@file:OptIn(ExperimentalFoundationApi::class)

package com.example.myapplication.composable_routes.session

import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.myapplication.Events
import com.example.myapplication.R
//import coil.compose.AsyncImage
//import com.reown.sample.common.ui.WCTopAppBarLegacy
//import com.reown.sample.common.ui.commons.ButtonWithLoader
//import com.reown.sample.common.ui.themedColor
//import com.reown.sample.dapp.R
//import com.reown.sample.dapp.ui.DappSampleEvents
//import com.reown.sample.dapp.ui.navigateToAccount
import com.example.myapplication.Route
import com.example.myapplication.navigateToAccount
import com.example.myapplication.ui.theme.ButtonWithLoader
import com.example.myapplication.ui.theme.WCTopAppBarLegacy
import com.example.myapplication.ui.theme.themedColor

@Composable
fun SessionRoute(
    navController: NavController
) {
    val context = LocalContext.current
    val viewModel: SessionViewModel = viewModel()
    val state by viewModel.uiState.collectAsState()
    var isDisconnectLoading by remember { mutableStateOf(false) }
    var isPingLoading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.sessionEvent.collect { event ->
            when (event) {
                is Events.PingSuccess -> {
                    isPingLoading = false
                    Toast.makeText(context, "Pinged Peer Successfully on Topic: ${event.topic}", Toast.LENGTH_SHORT).show()
                }

                is Events.PingError -> {
                    isPingLoading = false
                    Toast.makeText(context, "Pinged Peer Unsuccessfully", Toast.LENGTH_SHORT).show()
                }

                is Events.PingLoading -> isPingLoading = true
                is Events.Disconnect -> {
                    isDisconnectLoading = false
                    navController.navigate(Route.ChainSelection.path) {
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = true
                        }
                    }
                    Toast.makeText(context, "Disconnected successfully", Toast.LENGTH_SHORT).show()
                }

                is Events.DisconnectError -> {
                    isDisconnectLoading = false
                    Toast.makeText(context, "Error: ${event.message}", Toast.LENGTH_SHORT).show()
                }

                is Events.DisconnectLoading -> isDisconnectLoading = true
                else -> Unit
            }
        }
    }

    SessionScreen(
        uiState = state,
        onSessionClick = navController::navigateToAccount,
        onPingClick = viewModel::ping,
        onDisconnectClick = viewModel::disconnect,
        isDisconnectLoading,
        isPingLoading
    )
}

@Composable
private fun SessionScreen(
    uiState: List<SessionUi>,
    onSessionClick: (String) -> Unit,
    onPingClick: () -> Unit,
    onDisconnectClick: () -> Unit,
    isDisconnectLoading: Boolean,
    isPingLoading: Boolean,
) {
    Column {
        WCTopAppBarLegacy(titleText = "Session Chains",)
        ChainsAction(onPingClick, onDisconnectClick, isDisconnectLoading, isPingLoading)
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                Text(
                    text = "Chains:",
                    modifier = Modifier.padding(8.dp),
                    style = TextStyle(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        color = themedColor(darkColor = 0xFFE5E7E7, lightColor = 0xFF141414)
                    )
                )
            }
            itemsIndexed(uiState) { _, item ->
                SessionChainItem(item, onSessionClick)
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun ChainsAction(
    onPingClick: () -> Unit,
    onDisconnectClick: () -> Unit,
    isDisconnectLoading: Boolean,
    isPingLoading: Boolean
) {
    var isExpanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier
            .padding(8.dp)
            .animateContentSize()
            .clickable { isExpanded = !isExpanded }
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(text = "Actions")
                Image(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier.padding(4.dp),
                    colorFilter = ColorFilter.tint(Color(0xFF3496ff))
                )
            }
            if (isExpanded) {
                SessionActions(
                    onPingClick = onPingClick,
                    onDisconnectClick = onDisconnectClick,
                    isDisconnectLoading,
                    isPingLoading
                )
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.Gray)
                .height(1.dp)
        )
    }
}

@Composable
private fun SessionActions(
    onPingClick: () -> Unit,
    onDisconnectClick: () -> Unit,
    isDisconnectLoading: Boolean,
    isPingLoading: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
    ) {
        val modifier = Modifier.padding(8.dp)
        ButtonWithLoader(text = "Ping", onClick = onPingClick, modifier = modifier, isPingLoading)
        Spacer(modifier = Modifier.width(4.dp))
        ButtonWithLoader(text = "Disconnect", onClick = onDisconnectClick, modifier = modifier, isDisconnectLoading)
    }
}

@Composable
private fun LazyItemScope.SessionChainItem(
    item: SessionUi,
    onSessionClick: (String) -> Unit
) {
    Box(
        modifier = Modifier.animateItemPlacement(),
    ) {
        Column(
            modifier = Modifier
                .clickable { onSessionClick("${item.chainNamespace}:${item.chainReference}:${item.address}") }
                .fillMaxWidth()
                .padding(horizontal = 24.dp, 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(model = item.icon, contentDescription = null, Modifier.size(48.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = item.name,
                    style = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.address,
                style = TextStyle(fontSize = 12.sp),
                maxLines = 1,
                modifier = Modifier.padding(horizontal = 6.dp),
                overflow = TextOverflow.Ellipsis,
            )
        }
        Image(
            imageVector = ImageVector.vectorResource(id = R.drawable.ic_polygon),
            contentDescription = "ForwardIcon",
            modifier = Modifier
                .padding(end = 8.dp)
                .size(12.dp)
                .align(Alignment.CenterEnd),
            colorFilter = ColorFilter.tint(Color(0xFF3496ff))
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.Gray)
                .height(1.dp)
        )
    }
}
