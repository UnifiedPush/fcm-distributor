package org.unifiedpush.distributor.fcm.activities

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import org.unifiedpush.distributor.fcm.R
import org.unifiedpush.distributor.fcm.services.MessagingDatabase.Companion.getDb
import org.unifiedpush.distributor.fcm.services.PushUtils.sendUnregistered

class MainActivity : AppCompatActivity() {

    private lateinit var listView : ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
        setListView()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { /*granted ->*/
            }.launch(
                Manifest.permission.POST_NOTIFICATIONS
            )
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if(hasFocus) {
            setListView()
        }
    }

    private fun setListView(){
        listView = findViewById(R.id.applications_list)
        val db = getDb(this)
        val tokenList = db.listTokens().toMutableList()
        val appList = db.listTokens().map {
            db.getApp(it)
        } as MutableList
        listView.adapter = ArrayAdapter(
                this,
                android.R.layout.simple_list_item_1,
                appList
        )
        listView.setOnItemLongClickListener(
                fun(_: AdapterView<*>, _: View, position: Int, _: Long): Boolean {
                    val alert: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(
                            this)
                    alert.setTitle("Unregistering")
                    alert.setMessage("Are you sure to unregister ${appList[position]} ?")
                    alert.setPositiveButton("YES") { dialog, _ ->
                        sendUnregistered(this, tokenList[position])
                        db.unregisterApp(tokenList[position])
                        tokenList.removeAt(position)
                        appList.removeAt(position)
                        dialog.dismiss()
                    }
                    alert.setNegativeButton("NO") { dialog, _ -> dialog.dismiss() }
                    alert.show()
                    return true
                }
        )
    }
}
