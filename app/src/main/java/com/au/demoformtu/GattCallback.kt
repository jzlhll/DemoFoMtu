package com.au.demoformtu

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothProfile
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.util.UUID
import android.util.Log
import com.au.demoformtu.MainActivity.Companion.updateLog

class GattCallback(val activity: AppCompatActivity) : BluetoothGattCallback() {
    val UUID_WRITE_CHARACTERISTIC = UUID.fromString("0000ff01-0000-1000-8000-00805f9b34fb")
    val UUID_NOTIFICATION_CHARACTERISTIC = UUID.fromString("0000ff02-0000-1000-8000-00805f9b34fb")
    val UUID_SERVICE = UUID.fromString("0000ffff-0000-1000-8000-00805f9b34fb")

    @SuppressLint("MissingPermission")
    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
        super.onConnectionStateChange(gatt, status, newState)
        val isSuc = status == BluetoothGatt.GATT_SUCCESS
        updateLog("onConnectionStateChange() status $status, GATT_SUCCESS $isSuc, newState $newState")
        if (isSuc) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                gatt!!.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH)
                gatt.discoverServices()
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
        super.onServicesDiscovered(gatt, status)
        var service: BluetoothGattService? = null
        var writeChar: BluetoothGattCharacteristic? = null
        var notifyChar: BluetoothGattCharacteristic? = null
        val isSuc = status == BluetoothGatt.GATT_SUCCESS
        updateLog("onServicesDiscovered() status $status success:$isSuc")
        if (isSuc) {
            service = gatt!!.getService(UUID_SERVICE)
            if (service != null) {
                writeChar = service.getCharacteristic(UUID_WRITE_CHARACTERISTIC)
                notifyChar = service.getCharacteristic(UUID_NOTIFICATION_CHARACTERISTIC)
                if (notifyChar != null) {
                    gatt.setCharacteristicNotification(notifyChar, true)
                }
            }

            activity.lifecycleScope.launch {
                val mtu: Int = 251
                updateLog("requestMtu() $mtu")
                Log.w(MainActivity.TAG, "try to requestMtu >>> try to requestMtu")
                val requestMtu = gatt!!.requestMtu(mtu)
                updateLog("requestMtu() isSuccess $requestMtu")
                Log.w(MainActivity.TAG, "requestMtu() isSuccess $requestMtu")
            }
        }
    }

    override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
        super.onMtuChanged(gatt, mtu, status)
        Log.w(MainActivity.TAG, "onMtuChanged() status=$status, mtu= $mtu")
        updateLog("onMtuChanged() status=$status, mtu= $mtu")
    }
}