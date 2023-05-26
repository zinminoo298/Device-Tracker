package com.xpand.devicetracker

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Address
import android.location.Geocoder
import android.location.LocationRequest
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.xpand.devicetracker.DBManager.DatabaseHandler
import com.xpand.devicetracker.Model.LocationModel
import java.io.IOException
import java.util.*

class ForeGroundService : Service(){

    companion object{
        var isServiceRunning = false
        private var iconNotification: Bitmap? = null
        private var notification: Notification? = null
        private var mNotificationManager: NotificationManager? = null
        private val mNotificationId = 123
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        isServiceRunning = true
        //Create the notification just to let use know
        createNotification()
        //Get device location
        getLocation()
        return START_STICKY
    }

    private fun createNotification(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intentMainLanding = Intent(this, MainActivity::class.java)
            val pendingIntent =
                PendingIntent.getActivity(this, 0, intentMainLanding, 0)
            iconNotification = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)
            if (mNotificationManager == null) {
                mNotificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                assert(mNotificationManager != null)
                mNotificationManager?.createNotificationChannelGroup(
                    NotificationChannelGroup("chats_group", "Chats")
                )
                val notificationChannel =
                    NotificationChannel("service_channel", "Service Notifications",
                        NotificationManager.IMPORTANCE_MIN)
                notificationChannel.enableLights(false)
                notificationChannel.lockscreenVisibility = Notification.VISIBILITY_SECRET
                mNotificationManager?.createNotificationChannel(notificationChannel)
            }
            val builder = NotificationCompat.Builder(this, "service_channel")

            builder.setContentTitle(StringBuilder(resources.getString(R.string.app_name)).append(" service is running").toString())
                .setTicker(StringBuilder(resources.getString(R.string.app_name)).append("service is running").toString())
                .setContentText("Touch to open") //                    , swipe down for more options.
                .setSmallIcon(R.drawable.ic_track)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setWhen(0)
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
            if (iconNotification != null) {
                builder.setLargeIcon(Bitmap.createScaledBitmap(iconNotification!!, 128, 128, false))
            }
            builder.color = resources.getColor(R.color.purple_200)
            notification = builder.build()
            startForeground(mNotificationId, notification)
        }
    }

    private fun getLocation(){
        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // set the Location Request Properties
            val request = com.google.android.gms.location.LocationRequest()
            request.interval = 1000 * 60 * 3 // FOR 3 minutes
            request.fastestInterval = 1000 * 60 * 3
            request.priority = LocationRequest.QUALITY_HIGH_ACCURACY

            // Get the latest Location
            fusedLocationProviderClient.requestLocationUpdates(request, object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    // Check location is null?
                    if(locationResult.lastLocation != null){
                        var addresses: List<Address>? = null
                        val geocoder = Geocoder(this@ForeGroundService, Locale.getDefault())
                        try {
                            //convert Lat and Long to Location
                            addresses = geocoder.getFromLocation(locationResult.lastLocation.latitude, locationResult.lastLocation.longitude, 1)
                            val lat = addresses!![0].latitude.toString()
                            val long = addresses[0].longitude.toString()
                            val location = addresses[0].getAddressLine(0).toString()
                            val time = Calendar.getInstance().time.toString()

                            //Save the last lat, long, location , time to DB
                            DatabaseHandler(this@ForeGroundService).saveLocationToDatabase(lat, long, location, time)

                            // if the app is running , update the recycler list
                            if(MainActivity.isRunning!!){
                                // add the new location to recycler list
                                DatabaseHandler.locationList.add(LocationModel(lat, long, location, time))
                                // refresh the recyclerview
                                MainActivity().refreshRecyclerView()
                            }
                        } catch (e: IOException) {
                            Toast.makeText(this@ForeGroundService,"No Internet Connection", Toast.LENGTH_LONG).show()
                            e.printStackTrace()
                        }
                    }
                }
            }, Looper.myLooper()!!)
        } else {
            stopSelf()
        }
    }

    override fun onDestroy() {
        isServiceRunning = false
        super.onDestroy()
    }
}



