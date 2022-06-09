package com.spin.id.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionUtils {

  companion object {
    const val MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE : Int = 123
    const val MY_PERMISSIONS_REQUEST_LOCATION : Int = 124
    const val PERMISSION_CAMERA : String = Manifest.permission.CAMERA
    const val PERMISSION_READ_EXTERNAL_STORAGE : String = Manifest.permission
      .READ_EXTERNAL_STORAGE
    const val PERMISSION_WRITE_EXTERNAL_STORAGE : String = Manifest.permission
      .WRITE_EXTERNAL_STORAGE
    const val PERMISSION_FINE_LOCATION : String = Manifest.permission.ACCESS_FINE_LOCATION
    const val PERMISSION_COARSE_LOCATION : String = Manifest.permission.ACCESS_COARSE_LOCATION
    const val PERMISSION_RECORD_AUDIO : String = Manifest.permission.RECORD_AUDIO
    const val PERMISSION_READ_CONTACTS : String = Manifest.permission.READ_CONTACTS

    fun isPermissionGranted(@NonNull context: Context, @NonNull permissionRequestName:
    String) : Boolean{
      return (ContextCompat.checkSelfPermission(context, permissionRequestName) ==
        PackageManager.PERMISSION_GRANTED)
    }

    fun isRequestPermissionAccepted(@NonNull grantResults: IntArray) : Boolean{
      return grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
    }

    fun requestPermission(@NonNull activity: Activity, @NonNull permissionRequestName:
    Array<String>, requestCode: Int) {
      ActivityCompat.requestPermissions(activity, permissionRequestName, requestCode)
    }

    fun checkLocationPermission(context: Context): Boolean {
      val permissions = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION)

      var permissionsGranted  = false
      val currentApiVersion : Int = Build.VERSION.SDK_INT

      if(currentApiVersion >= Build.VERSION_CODES.M){
        for(permission in permissions){
          if(ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(context as Activity, permission)){
              ActivityCompat.requestPermissions(context as Activity,
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,
                  Manifest.permission.ACCESS_FINE_LOCATION),
                MY_PERMISSIONS_REQUEST_LOCATION)
            }else{
              ActivityCompat.requestPermissions(context as Activity,
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,
                  Manifest.permission.ACCESS_FINE_LOCATION),
                MY_PERMISSIONS_REQUEST_LOCATION)
            }
            permissionsGranted = false
            break
          }else{
            permissionsGranted = true
          }
        }
        return permissionsGranted
      }else{
        return true
      }
    }

    fun checkPermission(context: Context) : Boolean {
      val permissions = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA)

      var permissionsGranted  = false
      val currentApiVersion : Int = Build.VERSION.SDK_INT

      if(currentApiVersion >= Build.VERSION_CODES.M){
        for(permission in permissions){
          if(ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(context as Activity, permission)){
              ActivityCompat.requestPermissions(context as Activity,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,
                  Manifest.permission.WRITE_EXTERNAL_STORAGE,
                  Manifest.permission.CAMERA),
                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE)
            }else{
              ActivityCompat.requestPermissions(context as Activity,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,
                  Manifest.permission.WRITE_EXTERNAL_STORAGE,
                  Manifest.permission.CAMERA),
                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE)
            }
            permissionsGranted = false;
            break
          }else{
            permissionsGranted = true
          }
        }
        return permissionsGranted
      }else{
        return true
      }
    }
  }

}