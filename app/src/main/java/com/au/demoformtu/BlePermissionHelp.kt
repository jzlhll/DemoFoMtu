package com.au.demoformtu

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity

object BlePermissionHelp {
    val blePermissions =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,)
        else
            arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION,)

    fun canShowRequestDialogUi(activity: FragmentActivity) : Boolean{
        for (permission in blePermissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                return true
            }
        }

        return false
    }


    inline fun safeRun(
        helper: ActivityResultHelper<Array<String>, Map<String, @JvmSuppressWildcards Boolean>>,
        crossinline block: () -> Unit
    ) {
        requestPermission(helper, permissionList = blePermissions) {
            block.invoke()
        }
    }

    fun isPermissionGrant(context: Context): Boolean {
        return blePermissions.hasPermission(context)
    }

    fun requestPermission(
        permission: ActivityResultHelper<Array<String>, Map<String, @JvmSuppressWildcards Boolean>>,
        permissionList: Array<String>,
        block: () -> Unit
    ) {
        permission.launch(permissionList) {
            var hasPermission = false
            for (entry in it) {
                if (!entry.value) {
                    hasPermission = false
                    break
                } else {
                    hasPermission = true
                }
            }
            if (hasPermission) block.invoke()
        }
    }

    fun requestPermission2(
        permission: ActivityResultHelper<Array<String>, Map<String, @JvmSuppressWildcards Boolean>>,
        permissionList: Array<String>,
        block: (Boolean) -> Unit
    ) {
        permission.launch(permissionList) {
            var hasPermission = false
            for (entry in it) {
                if (!entry.value) {
                    hasPermission = false
                    break
                } else {
                    hasPermission = true
                }
            }
            block.invoke(hasPermission)
        }
    }
}