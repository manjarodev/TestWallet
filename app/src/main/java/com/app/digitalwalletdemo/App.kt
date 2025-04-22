package com.app.digitalwalletdemo

import android.app.Application
import android.util.Log
import com.reown.android.Core
import com.reown.android.CoreClient
import com.reown.appkit.client.AppKit
import com.reown.appkit.client.Modal
import com.reown.appkit.presets.AppKitChainsPresets


class App : Application() {


    override fun onCreate() {
        super.onCreate()


        val appMetaData = Core.Model.AppMetaData(
            name = "Kotlin Dapp",
            description = "Kotlin Dapp Implementation",
            url = "https://appkit-lab.reown.com",
            icons = listOf("https://gblobscdn.gitbook.com/spaces%2F-LJJeCjcLrr53DcT1Ml7%2Favatar.png?alt=media"),
            redirect = "kotlin-dapp-wc://request",
            linkMode = true
        )

        CoreClient.initialize(
            application = this,
            projectId = "00e22d83d60e949eb1da800c7abc1a5a",
            metaData = appMetaData,
        ) {
            Log.e("mmTAG", "${it.throwable}")
        }

        AppKit.initialize(Modal.Params.Init(core = CoreClient)) { error ->
            Log.e("mmTAG", "${error.throwable}")
        }

        AppKit.setChains(AppKitChainsPresets.ethChains.values.toList())

//        val authParams = Modal.Model.AuthPayloadParams(
//            chains = AppKitChainsPresets.ethChains.values.toList().map { it.id },
//            domain = "sample.kotlin.modal",
//            uri = "https://web3inbox.com/all-apps",
//            nonce = randomBytes(12).bytesToHex(),
//            statement = "I accept the Terms of Service: https://yourDappDomain.com/",
//            methods = EthUtils.ethMethods
//        )
//        AppKit.setAuthRequestParams(authParams)

//        FirebaseAppDistribution.getInstance().updateIfNewReleaseAvailable()

    }

}