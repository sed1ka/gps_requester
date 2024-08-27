package com.example.gps_requester

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.location.LocationManager
import android.os.Build
import android.util.Log
import androidx.annotation.NonNull
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.location.Priority
import com.google.android.gms.location.SettingsClient
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.ActivityResultListener

class GpsRequesterPlugin : FlutterPlugin, MethodCallHandler, ActivityAware, ActivityResultListener {
    private lateinit var channel: MethodChannel
    private lateinit var context: Context

    private var activity: Activity? = null
    private var settingsClient: SettingsClient? = null
    private var requestServiceResult: Result? = null
    private var locationRequest: LocationRequest? = null
    private var locationSettingsRequest: LocationSettingsRequest? = null
    private var locationManager: LocationManager? = null

    private var mActivityBinding: ActivityPluginBinding? = null

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "gps_requester")
        channel.setMethodCallHandler(this)
        context = flutterPluginBinding.applicationContext
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        when (call.method) {
            "requestService" -> onRequestService(result)
            "checkService" -> onCheckService(result)
            else -> result.notImplemented()
        }
    }

    private fun checkServiceEnabled(): Boolean {
        val locManager = locationManager
            ?: activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager = locManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return locManager.isLocationEnabled
        }
        val gpsEnabled: Boolean = locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val networkEnabled: Boolean =
            locManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        Log.d("gps_requester", "gpsEnabled: $gpsEnabled")
        Log.d("gps_requester", "networkEnabled: $networkEnabled")
        return gpsEnabled || networkEnabled
    }

    private fun onCheckService(result: Result) {
        try {
            result.success(if (this.checkServiceEnabled()) 1 else 0)
        } catch (e: java.lang.Exception) {
            result.error(
                "SERVICE_STATUS_ERROR",
                "Location service status couldn't be determined",
                null
            )
        }
    }

    private fun onRequestService(result: Result) {
        val currentActivity = activity ?: run {
            result.error("NO_ACTIVITY", "Activity is null", null)
            return
        }
        if (settingsClient == null) {
            settingsClient = LocationServices.getSettingsClient(currentActivity)
        }

        if (locationRequest == null) {
            locationRequest = LocationRequest.create().apply {
                interval = 10000
                priority = Priority.PRIORITY_LOW_POWER
            }
        }

        if (locationSettingsRequest == null) {
            val builder = LocationSettingsRequest.Builder()
            builder.addLocationRequest(locationRequest!!)
            locationSettingsRequest = builder.build()
        }

        try {
            if (this.checkServiceEnabled()) {
                result.success(1)
                return
            }
        } catch (e: Exception) {
            result.error(
                "SERVICE_STATUS_ERROR",
                "Location service status couldn't be determined",
                null
            )
            return
        }

        this.requestServiceResult = result
        settingsClient?.checkLocationSettings(locationSettingsRequest!!)?.addOnFailureListener(
            currentActivity
        ) { e ->
            if (e is ResolvableApiException) {
                val rae: ResolvableApiException = e
                when (rae.statusCode) {
                    // Show the dialog by calling startResolutionForResult(), and check the
                    // result in onActivityResult().
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                        rae.startResolutionForResult(currentActivity, GPS_ENABLE_REQUEST)
                    } catch (sie: IntentSender.SendIntentException) {
                        result.error(
                            "SERVICE_STATUS_ERROR",
                            "Could not resolve location request",
                            null
                        )
                    }

                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> result.error(
                        "SERVICE_STATUS_DISABLED",
                        "Failed to get location. Location services disabled",
                        null
                    )
                }
            } else {
                result.error("SERVICE_STATUS_ERROR", "Unexpected error type received", null)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        Log.d("gps_requester", "requestCode: $requestCode")
        Log.d("gps_requester", "resultCode: $resultCode")
        Log.d("gps_requester", "Intent: $data")

        when (requestCode) {
            GPS_ENABLE_REQUEST -> {
                if (this.requestServiceResult == null) {
                    return false
                }
                if (resultCode == Activity.RESULT_OK) {
                    this.requestServiceResult?.success(1)
                } else {
                    this.requestServiceResult?.success(0)
                }
                this.requestServiceResult = null
                return true
            }

            else -> return false
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
        binding.addActivityResultListener(this)
    }


    override fun onDetachedFromActivityForConfigChanges() {
        activity = null
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        activity = binding.activity
    }

    override fun onDetachedFromActivity() {
        activity = null
    }

    companion object {
        private const val GPS_ENABLE_REQUEST = 0x1001
    }
}
