package com.au.demoformtu

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner

fun LifecycleOwner.multiplePermissionsForResult() =
    activityResultHelper(lifecycle, ActivityResultContracts.RequestMultiplePermissions())

fun LifecycleOwner.permissionForResult() =
    activityResultHelper(lifecycle, ActivityResultContracts.RequestPermission())


fun LifecycleOwner.activityForResult() =
    activityResultHelper(lifecycle, ActivityResultContracts.StartActivityForResult())

fun <I, O> activityResultHelper(
    lifecycle: Lifecycle,
    resultContract: ActivityResultContract<I, O>
) =
    ActivityResultHelper(resultContract).also {
        lifecycle.addObserver(it)
    }

open class ActivityResultHelper<I, O>(val resultContract: ActivityResultContract<I, O>) :
    DefaultLifecycleObserver, ActivityResultCallback<O> {

    private var launcher: ActivityResultLauncher<I>? = null

    val resultLauncher
        get() = launcher

    private var onResult: ((O) -> Unit)? = null
    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        launcher = when (owner) {
            is AppCompatActivity -> {
                owner.registerForActivityResult(resultContract, this)
            }
            is Fragment -> {
                owner.registerForActivityResult(resultContract, this)
            }
            else -> {
                null
            }
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        onResult = null
        launcher?.unregister()
        launcher = null
    }

    override fun onActivityResult(result: O) {
        onResult?.invoke(result)
    }

    fun launch(
        intent: I,
        option: ActivityOptionsCompat? = null,
        block: (O) -> Unit
    ) {
        this.onResult = block
        launcher?.launch(intent, option)
    }
}


fun String.hasPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context.applicationContext,
        this
    ) == PackageManager.PERMISSION_GRANTED
}

fun List<String>.checkPermission(context: Context): MutableList<String> {
    val noPermission = mutableListOf<String>()
    forEach {
        if (!it.hasPermission(context)) {
            noPermission.add(it)
        }
    }
    return noPermission
}

fun List<String>.hasPermission(context: Context): Boolean {
    return checkPermission(context).isEmpty()
}

fun Array<String>.checkPermission(context: Context): MutableList<String> {
    val noPermission = mutableListOf<String>()
    forEach {
        if (!it.hasPermission(context)) {
            noPermission.add(it)
        }
    }
    return noPermission
}

fun Array<String>.hasPermission(context: Context): Boolean {
    return checkPermission(context).isEmpty()
}

fun ActivityResultHelper<Intent, ActivityResult>.jumpToAppDetail(appContext: Context, afterBackAppBlock:((ActivityResult)->Unit)? = null) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    intent.data = Uri.fromParts("package", appContext.packageName, null)
    launch(intent) {
        afterBackAppBlock?.invoke(it)
    }
}

