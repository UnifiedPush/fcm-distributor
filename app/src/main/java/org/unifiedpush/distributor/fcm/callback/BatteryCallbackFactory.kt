package org.unifiedpush.distributor.fcm.callback

import android.content.Context
import java.util.concurrent.atomic.AtomicBoolean
import org.unifiedpush.distributor.callback.BatteryCallback
import org.unifiedpush.distributor.callback.CallbackFactory

object BatteryCallbackFactory : CallbackFactory<BatteryCallbackFactory.MainBatteryCallback>() {

    class MainBatteryCallback : BatteryCallback() {
        override val lowBattery = AtomicBoolean(false)
        override fun onBatteryLow(context: Context) {}
        override fun onBatteryOk(context: Context) {}
    }

    override fun new(context: Context): MainBatteryCallback {
        return MainBatteryCallback()
    }
}
