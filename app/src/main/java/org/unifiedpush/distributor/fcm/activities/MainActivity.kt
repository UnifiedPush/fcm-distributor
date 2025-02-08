package org.unifiedpush.distributor.fcm.activities

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.unifiedpush.distributor.fcm.EventBus
import org.unifiedpush.distributor.fcm.Migrations
import org.unifiedpush.distributor.fcm.activities.ui.MainUi
import org.unifiedpush.distributor.fcm.activities.ui.theme.AppTheme
import org.unifiedpush.distributor.fcm.services.RestartWorker
import org.unifiedpush.distributor.fcm.utils.TAG

class MainActivity : ComponentActivity() {
    private var viewModel: MainViewModel? = null
    private var jobs: MutableList<Job> = emptyList<Job>().toMutableList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RestartWorker.startPeriodic(this)
        Migrations(this).run()

        enableEdgeToEdge()

        setContent {
            val viewModel =
                viewModel {
                    MainViewModel(this@MainActivity)
                }.also {
                    viewModel = it
                }
            AppTheme {
                MainUi(viewModel)
            }
            subscribeActions()
        }
    }

    private fun subscribeActions() {
        Log.d(TAG, "Subscribing to actions")
        jobs += CoroutineScope(Dispatchers.IO).launch {
            EventBus.subscribe<AppAction> { it.handle(this@MainActivity) }
        }
        jobs += CoroutineScope(Dispatchers.IO).launch {
            EventBus.subscribe<UiAction> {
                it.handle { type ->
                    when (type) {
                        UiAction.Action.RefreshRegistrations -> viewModel?.refreshRegistrations(
                            this@MainActivity
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        Log.d(TAG, "Resumed")
        viewModel?.refreshRegistrations(this)
        super.onResume()
    }

    override fun onDestroy() {
        Log.d(TAG, "Destroy")
        jobs.removeAll {
            it.cancel()
            true
        }
        super.onDestroy()
    }
}
