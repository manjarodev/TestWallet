package com.example.myapplication

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.myapplication.composable_routes.account.AccountRoute
import com.example.myapplication.composable_routes.chain_selection.ChainSelectionRoute
import com.example.myapplication.composable_routes.session.SessionRoute
import com.reown.appkit.ui.appKitGraph

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DappSampleNavGraph(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Your local composables
        composable(Route.ChainSelection.path) {
            ChainSelectionRoute(navController)
        }
        composable(Route.Session.path) {
            SessionRoute(navController)
        }
        composable(
            route = Route.Account.path + "/{$accountArg}",
            arguments = listOf(navArgument(accountArg) { type = NavType.StringType })
        ) {
            AccountRoute(navController)
        }

        appKitGraph(navController = navController)
    }
}

const val accountArg = "accountArg"

fun NavController.navigateToAccount(selectedAccount: String) {
    navigate(Route.Account.path + "/$selectedAccount")
}