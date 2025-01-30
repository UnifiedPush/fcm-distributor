package org.unifiedpush.distributor.fcm.activities

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.unifiedpush.distributor.fcm.Distributor
import org.unifiedpush.distributor.fcm.EventBus

class AppAction(private val action: Action) {
    sealed class Action {
        class DeleteRegistration(val registrations: List<String>) : Action()
    }

    fun handle(context: Context) {
        when (action) {
            is Action.DeleteRegistration -> deleteRegistration(context, action)
        }
    }

    private fun deleteRegistration(context: Context, action: Action.DeleteRegistration) {
        action.registrations.forEach {
            Distributor.deleteApp(context, it) {}
        }
    }
}

fun ViewModel.publishAction(action: AppAction) {
    viewModelScope.launch {
        EventBus.publish(action)
    }
}
