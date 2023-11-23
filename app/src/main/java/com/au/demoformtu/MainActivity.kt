package com.au.demoformtu

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.au.demoformtu.BlePermissionHelp.blePermissions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : AppCompatActivity() {
    companion object {
        const val TAG = "DemoMtu"
        var logChangeCallback:IShowTextCallback? = null

        private val logs = mutableListOf<String>()

        fun updateLog(log:String, clearLog:Boolean = false) {
            val sb = StringBuilder()
            synchronized(logs) {
                if (clearLog) {
                    logs.clear()
                }
                logs.add(log)
                logs.forEach {
                    sb.append(it).append("\n\n")
                }
            }
            logChangeCallback?.onText(sb.toString())
        }

        var inputAddress:String? = null
    }

    lateinit var bluetoothManager: BluetoothManager
    private val blePermissionHelper = multiplePermissionsForResult()
    val activityHelper = activityForResult()

    private val scanDevice = ScanBluetoothDevice(this)
    private lateinit var showText:TextView
    private lateinit var editText:EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        setContentView(R.layout.activity_main)
        showText = findViewById(R.id.showText)
        editText = findViewById(R.id.edit)

        findViewById<Button>(R.id.sureBtn).setOnClickListener {
            val address = editText.text.toString().uppercase(Locale.ROOT)
            inputAddress = address
            saveAddressToSp(address)
            checkPermission(true)
        }

        logChangeCallback = object : IShowTextCallback {
            override fun onText(str: String) {
                lifecycleScope.launch {
                    showText.text = str
                }
            }
        }
    }

    private var isResumeCount = 0

    override fun onResume() {
        super.onResume()
        if (isResumeCount == 0) {
            val address = readAddressFromSp()?.uppercase(Locale.ROOT)
            if (!address.isNullOrEmpty()) {
                editText.setText(address)
                inputAddress = address
            }

            checkPermission(true)
        }
        isResumeCount++
    }

    private fun checkPermission(jumpToApp:Boolean) {
        if (BlePermissionHelp.isPermissionGrant(this)) {
            showText.text = "Has Permission, start ble Scan..."
            startBleScan()
        } else {
            val canShow = BlePermissionHelp.canShowRequestDialogUi(this)
            Log.w(TAG, "request permission!!! canShowDialog $canShow")
            showText.text = "request permission!!! canShowDialog $canShow"
            if (!canShow) {
                showText.text = "no permission!!!It will jump to appDetail in 5s..."
                Toast.makeText(this, "$TAG Please give the bluetooth permission!", Toast.LENGTH_LONG).show()
                if (jumpToApp) {
                    lifecycleScope.launch {
                        delay(5000)
                        activityHelper.jumpToAppDetail(this@MainActivity) {
                            checkPermission(false)
                        }
                    }
                }
            } else {
                BlePermissionHelp.requestPermission2(blePermissionHelper, blePermissions) {
                    Log.w(TAG, "request permission... $it")
                    if (it) {
                        startBleScan()
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startBleScan() {
        Log.w(TAG, "start ble Scan! $inputAddress")
        lifecycleScope.launch(Dispatchers.IO) {
            delay(3000)
            val isSuc = scanDevice.scan(bluetoothManager)
            if (isSuc) {
                updateLog("start ble Scan! $inputAddress", true)
            } else {
                updateLog("start ble Scan! no inputAddress.", true)
            }
        }
    }

    private fun saveAddressToSp(address:String) {
        val sp = this.getSharedPreferences("save_address", Context.MODE_PRIVATE)
        sp.edit().putString("address", address).apply()
    }

    private fun readAddressFromSp(): String? {
        val sp = this.getSharedPreferences("save_address", Context.MODE_PRIVATE)
        return sp.getString("address", "")
    }
}