package com.kori.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.kori.app.app.OidcIntentBus
import com.kori.app.app.KoriApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent?.let(OidcIntentBus::publish)
        setContent {
            KoriApp()
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        OidcIntentBus.publish(intent)
    }
}
