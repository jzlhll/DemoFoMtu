package com.au.demoformtu

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.au.demoformtu.MainActivity.Companion.updateLog
import kotlinx.coroutines.delay
import java.util.Locale

class ScanBluetoothDevice(private val activity: AppCompatActivity) {
    @SuppressLint("MissingPermission")
    suspend fun scan(bluetoothManager: BluetoothManager) : Boolean{
        var address = MainActivity.inputAddress ?: return false
        address = address.uppercase(Locale.ROOT)
        val bluetoothDevice = getBluetoothDevice(bluetoothManager, address, true)
        Log.d(MainActivity.TAG, "bluetoothDevice $address device:${bluetoothDevice?.address}")
        updateLog("bluetoothDevice $address device:${bluetoothDevice?.address}")

        val gatt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            bluetoothDevice?.connectGatt(activity, false, GattCallback(activity), BluetoothDevice.TRANSPORT_LE)
        } else {
            bluetoothDevice?.connectGatt(activity, false, GattCallback(activity))
        }
        return true
    }

    @SuppressLint("MissingPermission")
    private suspend fun getBluetoothDevice(bluetoothManager: BluetoothManager, address: String, isNeedDiscover: Boolean = true): BluetoothDevice? {
        val adapter = bluetoothManager.adapter
        if (!adapter.isEnabled) return null
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return null
        if (isNeedDiscover) {
            try {
                if (!adapter.isDiscovering) {
                    adapter.startDiscovery()
                    delay(2000L)
                    //adapter.cancelDiscovery()
                    adapter.getRemoteDevice(address)
                }else{
                    adapter.getRemoteDevice(address)
                }
            } catch (e:Exception) {
                e.printStackTrace()
            }
        }
        return adapter.getRemoteDevice(address)
    }
}