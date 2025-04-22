package com.app.digitalwalletdemo.ui


import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.app.digitalwalletdemo.common.ui.theme.WCSampleAppTheme
import com.app.digitalwalletdemo.ui.routes.host.DappSampleHost
import com.reown.appkit.client.AppKit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DappActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WCSampleAppTheme {
                DappSampleHost()
            }
        }

        if (intent?.dataString?.contains("wc_ev") == true) {
            AppKit.handleDeepLink(intent.dataString ?: "") {
                lifecycleScope.launch(Dispatchers.Main) {
                    Toast.makeText(
                        this@DappActivity,
                        "Error dispatching envelope: ${it.throwable.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        if (intent.dataString?.contains("wc_ev") == true) {
            AppKit.handleDeepLink(intent.dataString ?: "") {
                lifecycleScope.launch(Dispatchers.Main) {
                    Toast.makeText(
                        this@DappActivity,
                        "Error dispatching envelope: ${it.throwable.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}