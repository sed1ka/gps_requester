package com.sed1ka.gps_requester

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.location.LocationManager
import android.os.Build

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

    private var activity: Activity? = null
    private var activityBinding: ActivityPluginBinding? = null
    private var settingsClient: SettingsClient? = null
    private var requestServiceResult: Result? = null
    private var locationRequest: LocationRequest? = null
    private var locationSettingsRequest: LocationSettingsRequest? = null
    private var locationManager: LocationManager? = null


    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "gps_requester")
        channel.setMethodCallHandler(this)
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
            locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_LOW_POWER,
                10000
            ).build()
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

        // Add ActivityResultListener
        activityBinding?.addActivityResultListener(this)
        requestServiceResult = result
        settingsClient?.checkLocationSettings(locationSettingsRequest!!)?.addOnFailureListener(
            currentActivity
        ) { e ->
            if (e is ResolvableApiException) {
                val rae: ResolvableApiException = e
                when (rae.statusCode) {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
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
        when (requestCode) {
            GPS_ENABLE_REQUEST -> {
                if (requestServiceResult == null) {
                    return false
                }
                if (resultCode == Activity.RESULT_OK) {
                    requestServiceResult?.success(1)
                } else {
                    requestServiceResult?.success(0)
                }
                requestServiceResult = null
                activityBinding?.removeActivityResultListener(this)
                return true
            }

            else -> return false
        }
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
        activityBinding?.removeActivityResultListener(this)
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
        activityBinding = binding
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        activity = binding.activity
        activityBinding = binding
    }

    override fun onDetachedFromActivityForConfigChanges() {
        activity = null
        activityBinding?.removeActivityResultListener(this)
    }

    override fun onDetachedFromActivity() {
        activity = null
        activityBinding?.removeActivityResultListener(this)
    }

    companion object {
        private const val GPS_ENABLE_REQUEST = 0x1001
    }
}
