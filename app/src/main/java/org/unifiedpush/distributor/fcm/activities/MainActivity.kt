package org.unifiedpush.distributor.fcm.activities

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import org.unifiedpush.distributor.fcm.R
import org.unifiedpush.distributor.fcm.services.MessagingDatabase
import org.unifiedpush.distributor.fcm.services.sendUnregistered


class MainActivity : AppCompatActivity() {

    private lateinit var listView : ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
        setListView()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if(hasFocus) {
            setListView()
        }
    }

    private fun setListView(){
        listView = findViewById<ListView>(R.id.applications_list)
        val db = MessagingDatabase(this)
        val tokenList = db.listTokens().toMutableList()
        val appList = emptyArray<String>().toMutableList()
        tokenList.forEach {
            appList.add(db.getApp(it))
        }
        db.close()
        listView.adapter = ArrayAdapter(
                this,
                android.R.layout.simple_list_item_1,
                appList
        )
        listView.setOnItemLongClickListener(
                fun(parent: AdapterView<*>, v: View, position: Int, id: Long): Boolean {
                    val alert: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(
                            this@MainActivity)
                    alert.setTitle("Unregistering")
                    alert.setMessage("Are you sure to unregister ${appList[position]} ?")
                    alert.setPositiveButton("YES") { dialog, _ ->
                        sendUnregistered(this, tokenList[position])
                        val db = MessagingDatabase(this)
                        db.unregisterApp(tokenList[position])
                        db.close()
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
