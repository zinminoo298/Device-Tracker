package com.xpand.devicetracker.DBManager

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.xpand.devicetracker.Model.LocationModel
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class DatabaseHandler(val context: Context) {
    companion object{
        const val DATABASE = "database.db"
        var locationList = ArrayList<LocationModel>()
    }

    // Open or Create DB for the first time
    fun openDatabase(): SQLiteDatabase {
        val dbFile=context.getDatabasePath(DATABASE)
        if (!dbFile.exists()) {
            try {
                val checkDB=context.openOrCreateDatabase(DATABASE, Context.MODE_PRIVATE, null)

                checkDB?.close()
                copyDatabase(dbFile)
            } catch (e: IOException) {
                throw RuntimeException("Error creating source database", e)
            }
        }
        return SQLiteDatabase.openDatabase(dbFile.path, null, SQLiteDatabase.OPEN_READWRITE)
    }

    @SuppressLint("WrongConstant")
    // Copy the pre made SQLite DB from assets to application level
    private fun copyDatabase(dbFile: File) {
        val `is`=context.assets.open(DATABASE)
        val os= FileOutputStream(dbFile)

        val buffer=ByteArray(1024)
        while(`is`.read(buffer)>0) {
            os.write(buffer)
        }

        os.flush()
        os.close()
        `is`.close()
    }

    // Get location list from DB
    fun getLocationFromDatabase(){
        val db = context.openOrCreateDatabase(DATABASE,Context.MODE_PRIVATE,null)
        val query = "SELECT * FROM tbl_location"
        val cursor = db.rawQuery(query,null)
        if(cursor.moveToFirst()){
            locationList.clear()
            do{
                val lat = cursor.getString(0)
                val lon = cursor.getString(1)
                val location = cursor.getString(2)
                val time = cursor.getString(3)
                locationList.add(LocationModel(lat, lon, location, time))
            }while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
    }

    // Save location list to DB
    fun saveLocationToDatabase(lat:String, lon:String, location:String, time:String){
        val db = context.openOrCreateDatabase(DATABASE, Context.MODE_PRIVATE, null)
        val values = ContentValues()
        values.put("latitude", lat)
        values.put("longitude", lon)
        values.put("location", location)
        values.put("time", time)
        db.insertWithOnConflict("tbl_location", null, values, SQLiteDatabase.CONFLICT_IGNORE)
        db.close()
    }
}