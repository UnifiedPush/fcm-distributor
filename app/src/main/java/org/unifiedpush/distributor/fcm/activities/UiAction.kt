package org.unifiedpush.distributor.fcm.activities

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.unifiedpush.distributor.fcm.EventBus

class UiAction(val action: Action) {
    enum class Action {
        RefreshRegistrations
    }

    fun handle(action: (Action) -> Unit) {
        action(this.action)
    }

    companion object {
        fun publish(type: Action) {
            CoroutineScope(Dispatchers.IO).launch {
                EventBus.publish(UiAction(type))
            }
        }
    }
}
