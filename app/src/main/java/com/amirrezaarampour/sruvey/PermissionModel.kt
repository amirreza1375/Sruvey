package com.bequarks.operatortrackkotlin.permission

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import com.amirrezaarampour.sruvey.Functions.Companion.isAboveQ
import com.amirrezaarampour.sruvey.R

class PermissionModel(val activity: Activity) {

    lateinit var callBack: IPermissionRequestCallBack

    @JvmName("setCallBack1")
    fun setCallBack(callBack: IPermissionRequestCallBack) {
        this.callBack = callBack
    }

    fun getStoragePermission(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (!isAboveQ()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!activity.shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        val builder = AlertDialog.Builder(activity)
                        builder.setTitle(activity.getString(R.string.storagePermissionExpTitle))
                        builder.setMessage(activity.getString(R.string.storagePermissionExpTitle))
                        builder.setPositiveButton(activity.getString(R.string.ok),
                            object : DialogInterface.OnClickListener {
                                override fun onClick(p0: DialogInterface?, p1: Int) {
                                    ActivityCompat.requestPermissions(
                                        activity,
                                        arrayOf(
                                            Manifest.permission.READ_EXTERNAL_STORAGE,
                                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                                        ),
                                        100
                                    )
                                }

                            })
                        val alert = builder.create()
//                        alert.show()
                        ActivityCompat.requestPermissions(
                            activity,
                            arrayOf(
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                            ),
                            100
                        )
                    } else {
                        ActivityCompat.requestPermissions(
                            activity,
                            arrayOf(
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                            ),
                            100
                        )
                    }
                }
                return false
            } else {
                return true
            }
        } else {
            return true
        }
    }

    fun getCameraPermission(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!activity.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                    val builder = AlertDialog.Builder(activity)
                    builder.setTitle(activity.getString(R.string.cameraPermissionExpTitle))
                    builder.setMessage(activity.getString(R.string.cameraPermissionExp))
                    builder.setPositiveButton(activity.getString(R.string.ok),
                        object : DialogInterface.OnClickListener {
                            override fun onClick(p0: DialogInterface?, p1: Int) {
                                ActivityCompat.requestPermissions(
                                    activity,
                                    arrayOf(Manifest.permission.CAMERA),
                                    101
                                )
                            }

                        })
                    val alert = builder.create()
//                    alert.show()
                    ActivityCompat.requestPermissions(
                        activity,
                        arrayOf(Manifest.permission.CAMERA),
                       101
                    )
                } else {
                    ActivityCompat.requestPermissions(
                        activity,
                        arrayOf(Manifest.permission.CAMERA),
                        101
                    )
                }
            }
            return false
        } else {
            return true
        }
    }

    fun getLocationPermission(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!activity.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    val builder = AlertDialog.Builder(activity)
                    builder.setTitle(activity.getString(R.string.locationPermissionExpTitle))
                    builder.setMessage(activity.getString(R.string.locationPermissionExp))
                    builder.setPositiveButton(activity.getString(R.string.ok),
                        object : DialogInterface.OnClickListener {
                            override fun onClick(p0: DialogInterface?, p1: Int) {
                                ActivityCompat.requestPermissions(
                                    activity,
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    ),
                                    102
                                )
                            }

                        })
                    val alert = builder.create()
//                    alert.show()
                    ActivityCompat.requestPermissions(
                        activity,
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ),
                       102
                    )
                } else {
                    ActivityCompat.requestPermissions(
                        activity,
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ),
                        102
                    )
                }
            }
            return false
        } else {
            return true
        }
    }

     fun isGPSPermitted(): Boolean {

        return (ActivityCompat.checkSelfPermission(
            activity,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
                == PackageManager.PERMISSION_GRANTED
                &&
                ActivityCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED)

    }

}