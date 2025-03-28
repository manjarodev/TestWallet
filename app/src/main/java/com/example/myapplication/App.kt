package com.example.myapplication

import android.app.Application
import android.util.Log
import com.reown.android.BuildConfig
import com.reown.android.Core
import com.reown.android.CoreClient
import com.reown.appkit.client.AppKit
import com.reown.appkit.client.Modal
import com.reown.appkit.presets.AppKitChainsPresets
import timber.log.Timber

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        val projectId = "00e22d83d60e949eb1da800c7abc1a5a"
        val appMetaData = Core.Model.AppMetaData(
            name = "Kotlin Dapp",
            description = "Kotlin Dapp Implementation",
            url = "https://appkit-lab.reown.com",
            icons = listOf("https://gblobscdn.gitbook.com/spaces%2F-LJJeCjcLrr53DcT1Ml7%2Favatar.png?alt=media"),
            redirect = "kotlin-dapp-wc://request",
            appLink = "https://appkit-lab.reown.com/dapp_debug",
            linkMode = true
        )

        CoreClient.initialize(
            application = this,
            projectId = projectId,
            metaData = appMetaData,
        ) {
            Timber.tag("error").e("error")
        }

        AppKit.initialize(Modal.Params.Init(core = CoreClient)) { error ->
            Timber.e(error.throwable.stackTraceToString())
        }

        AppKit.setChains(AppKitChainsPresets.ethChains.values.toList())
    }
}