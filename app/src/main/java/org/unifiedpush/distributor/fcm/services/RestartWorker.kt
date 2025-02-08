@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.unifiedpush.distributor.fcm.services

import android.content.Context
import androidx.work.*
import org.unifiedpush.distributor.WorkerCompanion

class RestartWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {

    /**
     * Restart the service if we have never received an event, or haven't received an event
     * in the expected time
     */
    override fun doWork(): Result {
        FgService.startService(applicationContext)
        return Result.success()
    }

    companion object : WorkerCompanion(RestartWorker::class.java) {
        override fun canRun(context: Context): Boolean {
            // We don't have any credential requirement, if we don't have
            // a uaid yet, it will be created during the initial sync
            return true
        }

        override fun isServiceStarted(context: Context): Boolean {
            return FgService.isServiceStarted()
        }
    }
}
