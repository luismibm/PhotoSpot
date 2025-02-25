package com.luismibm.photospot

import android.annotation.SuppressLint
import android.app.Application
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import java.util.ArrayList
import java.util.Locale
import java.util.concurrent.Executors

class SharedViewModel(application: Application): AndroidViewModel(application) {

    val app = application

    val currentAddress = MutableLiveData<String>()
    val checkPermission = MutableLiveData<String>()
    val buttonText = MutableLiveData<String>()
    // val loadingIcon = MutableLiveData<Boolean>()

    var mTrackingLocation = false

    lateinit var mFusedLocationClient: FusedLocationProviderClient
    fun setFusedLocationClient(fusedLocationProviderClient: FusedLocationProviderClient) {
        this.mFusedLocationClient = fusedLocationProviderClient
    }

    val mLocationCallback = object: LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            // fetchAddress(locationResult.lastLocation)
            locationResult.lastLocation?.let { fetchAddress(it) }
        }
    }

    fun getLocationRequest(): LocationRequest {
        return LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000).setMinUpdateIntervalMillis(5000).build()
    }

    fun switchTrackingLocation() {
        if (!mTrackingLocation) {
            startTrackingLocation(needsChecking = true)
        } else {
            stopTrackingLocation()
        }
    }

    @SuppressLint("MissingPermission")
    fun startTrackingLocation(needsChecking: Boolean) {
        if (needsChecking) {
            checkPermission.postValue("Check")
        } else {
            mFusedLocationClient.requestLocationUpdates(getLocationRequest(), mLocationCallback, null)
            currentAddress.postValue("Loading...")
            mTrackingLocation = true
            // loadingIcon.postValue(true)
            buttonText.value = "Stop Tracking"
        }
    }

    fun stopTrackingLocation() {
        if (mTrackingLocation) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback)
            mTrackingLocation = false
            // loadingIcon.postValue(false)
            buttonText.value = "Start Tracking"
        }
    }

    fun fetchAddress(location: Location) {

        val executor = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())
        val geocoder = Geocoder(app.applicationContext, Locale.getDefault())

        executor.execute {
            val addresses: List<Address>?
            var resultMessage = ""

            try {
                addresses = geocoder.getFromLocation(
                    location.latitude,
                    location.longitude,
                    1
                )
                if (addresses.isNullOrEmpty()) {
                    if (resultMessage.isEmpty()) {
                        resultMessage = "No Address Found"
                        Log.e("LUISMIBM", resultMessage)
                    }
                } else {
                    val address = addresses.get(0)
                    val addressParts = ArrayList<String>()
                    for (i in 0..address.maxAddressLineIndex) {
                        addressParts.add(address.getAddressLine(i))
                    }
                    resultMessage = TextUtils.join("\n", addressParts)
                    handler.post {
                        if (mTrackingLocation) {
                            currentAddress.postValue(resultMessage)
                        }
                    }
                }

            } catch (e: Exception) {
                resultMessage = "Failed: fetchAddress()"
                Log.e("LUISMIBM", resultMessage)
            }

        }

    }

}