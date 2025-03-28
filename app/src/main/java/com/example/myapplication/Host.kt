package com.example.myapplication

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.navigation.material.BottomSheetNavigator
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.ModalBottomSheetLayout
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialNavigationApi::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Host() {
    val snackbarHostState = remember { SnackbarHostState() }
    var isOfflineState: Boolean? by remember { mutableStateOf(null) }
    val sheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )
    // ✅ Set up BottomSheetNavigator and NavController
    val bottomSheetNavigator = remember { BottomSheetNavigator(sheetState) }
    val navController = rememberNavController(bottomSheetNavigator)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        ModalBottomSheetLayout(
            bottomSheetNavigator = bottomSheetNavigator
        ) {
            Box(modifier = Modifier.padding(innerPadding)) {
                DappSampleNavGraph(
                    navController = navController,
                    startDestination = Route.Session.path
                )

                when (isOfflineState) {
                    true -> NoConnectionIndicator()
                    false -> RestoredConnectionIndicator()
                    null -> Unit
                }
            }
        }
    }
}

@Composable
private fun NoConnectionIndicator() {
    var shouldShow by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(3000)
        shouldShow = false
    }

    AnimatedVisibility(visible = shouldShow) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF3496ff))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "No internet connection", color = Color.White)
            Spacer(modifier = Modifier.width(4.dp))
            Image(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_launcher_background),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                colorFilter = ColorFilter.tint(Color.White)
            )
        }
    }
}

@Composable
private fun RestoredConnectionIndicator() {
    var shouldShow by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(3000)
        shouldShow = false
    }

    AnimatedVisibility(visible = shouldShow) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF93c47d))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Network connection is OK", color = Color.White)
            Spacer(modifier = Modifier.width(4.dp))
            Image(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_polygon),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                colorFilter = ColorFilter.tint(Color.White)
            )
        }
    }
}