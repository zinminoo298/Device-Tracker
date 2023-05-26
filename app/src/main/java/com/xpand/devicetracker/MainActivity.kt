package com.xpand.devicetracker

import android.Manifest
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract.Data
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.*
import com.google.android.gms.tasks.CancellationTokenSource
import com.xpand.devicetracker.Adapter.RecyclerAdapter
import com.xpand.devicetracker.DBManager.DatabaseHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.*


open class MainActivity : AppCompatActivity() {

    companion object{
        var isRunning:Boolean? = null
        private lateinit var recyclerView: RecyclerView
        private lateinit var viewAdapter: RecyclerView.Adapter<*>
        private lateinit var viewManager: RecyclerView.LayoutManager
        private var REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerview)

        //Open Or Create SQLite DB to save location
        DatabaseHandler(this).openDatabase()

        //Get Location List from DB
        asyncGetLocationFromDB()
    }

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Check if foreground service is running
            if(!ForeGroundService.isServiceRunning){
                // Main Activity running condition for check in Service
                isRunning = true
                //Start ForeGround Service
                startService(Intent(this, ForeGroundService::class.java))
            }
        } else {
            // if no permission , ask persmission
            askPermission()
        }
    }

    private  fun askPermission() {
        ActivityCompat.requestPermissions(
            this@MainActivity,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkPermission()
            } else {
                Toast.makeText(this, "Required Permission", Toast.LENGTH_SHORT).show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun loadRecyclerView(){
        viewManager = LinearLayoutManager(this)
        viewAdapter = RecyclerAdapter(DatabaseHandler.locationList, this)
        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }
    }

    //Scroll Recycler view to last location when restart the app
    private fun scrollRecyclerView(){
        if(DatabaseHandler.locationList.isNotEmpty()){
            recyclerView.smoothScrollToPosition(DatabaseHandler.locationList.size-1)
        }
    }

    //refresh recyclerview from Service
    fun refreshRecyclerView(){
        viewAdapter.notifyDataSetChanged()
    }

    private fun asyncGetLocationFromDB() {
        val deferred = lifecycleScope.async(Dispatchers.IO) {
            // Main Activity running condition for check in Service
            isRunning = true
            //Get Location List from DB
            DatabaseHandler(this@MainActivity).getLocationFromDatabase()
        }
        lifecycleScope.launch(Dispatchers.Main) {
            if (deferred.isActive) {
                val progressDialogBuilder = createProgressDialog(this@MainActivity)
                try {
                    progressDialogBuilder.show()
                    deferred.await()

                } finally {
                    //after async finish do these tasks
                    loadRecyclerView()
                    scrollRecyclerView()
                    checkPermission()
                    progressDialogBuilder.cancel()
                }
            } else {
                //if async is not active do these tasks
                deferred.await()
                loadRecyclerView()
                scrollRecyclerView()
                checkPermission()
            }
        }
    }

    private fun createProgressDialog(context: Context): ProgressDialog {
        val progressDialog = ProgressDialog(context)
        progressDialog.setTitle("Please Wait")
        progressDialog.setMessage("Loading data ....")
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        return progressDialog
    }

    override fun onDestroy() {
        isRunning = false
        super.onDestroy()
    }

    override fun onPause() {
        isRunning = false
        super.onPause()
    }

    override fun onRestart() {
        asyncGetLocationFromDB()
        isRunning = true
        println("RESTART")
        super.onRestart()
    }
}

