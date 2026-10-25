package com.luismi.adviento

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import com.luismi.adviento.platform.AndroidServices
import kotlinx.coroutines.CompletableDeferred

class MainActivity : ComponentActivity() {
    private val navigator = AppNavigator()
    private var pendingPermission: CompletableDeferred<Boolean>? = null
    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            pendingPermission?.complete(granted)
            pendingPermission = null
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val services = AndroidServices.get(applicationContext)
        AndroidServices.notifier(applicationContext).permissionRequester = {
            CompletableDeferred<Boolean>().also {
                pendingPermission = it
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }.await()
        }

        onBackPressedDispatcher.addCallback(this) {
            if (!navigator.back()) {
                isEnabled = false
                onBackPressedDispatcher.onBackPressed()
                isEnabled = true
            }
        }

        setContent { App(services, navigator) }
    }

    override fun onDestroy() {
        AndroidServices.notifier(applicationContext).permissionRequester = null
        if (isFinishing) AndroidServices.get(applicationContext).audioPlayer.stop()
        super.onDestroy()
    }
}
