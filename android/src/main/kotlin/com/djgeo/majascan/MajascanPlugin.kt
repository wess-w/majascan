package com.djgeo.majascan

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.util.Log
import androidx.annotation.NonNull
import com.djgeo.majascan.g_scanner.QrCodeScannerActivity
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry

class MajascanPlugin() : MethodCallHandler, PluginRegistry.ActivityResultListener,FlutterPlugin, ActivityAware {

    private lateinit var channel : MethodChannel
    lateinit var activity: Activity
    val SCANRESULT = "scan"
    val Request_Scan = 1
/*    companion object {
        @JvmStatic
        fun registerWith(registrar: Registrar) {
            registrar.activity()?.let {
                // If a background flutter view tries to register the plugin,
                // there will be no activity from the registrar,
                // we stop the registering process immediately because the plugin requires an activity.

                val majascanPlugin = MajascanPlugin(it)
                val channel = MethodChannel(registrar.messenger(), "majascan")
                channel.setMethodCallHandler(majascanPlugin)

                // 注册ActivityResult回调
                registrar.addActivityResultListener(majascanPlugin)
            }
        }

        const val SCANRESULT = "scan"
        const val Request_Scan = 1
    }*/

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding){
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "majascan")
        channel.setMethodCallHandler(this)
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    private var mResult: Result? = null
    private var mResultPeriod = 0L

    override fun onMethodCall(@NonNull call: MethodCall,@NonNull result: MethodChannel.Result) {

        when (call.method) {
            SCANRESULT -> {
                val args: Map<String, String>? = call.arguments()
                activity.let {
                    val intent = Intent(it, QrCodeScannerActivity::class.java)
                    args?.keys?.map { key -> intent.putExtra(key, args[key]) }
                    it.startActivityForResult(intent, Request_Scan)
                    mResult = result
                }
            }
            else -> result.notImplemented()
        }
    }

    //issue tracking https://github.com/flutter/flutter/issues/29092
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        val currentTime = System.currentTimeMillis()
        if (requestCode == Request_Scan && resultCode == RESULT_OK && data != null) {
            if (currentTime - mResultPeriod >= 1000) {
                mResultPeriod = currentTime
                val resultString = data.getStringExtra(QrCodeScannerActivity.BUNDLE_SCAN_CALLBACK)
                resultString?.let {
                    mResult?.success(it)
                }
                return true
            }
        }
        return false
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
        binding.addActivityResultListener(this)
    }

    override fun onDetachedFromActivityForConfigChanges() {
        TODO("Not yet implemented")
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        TODO("Not yet implemented")
    }

    override fun onDetachedFromActivity() {
        TODO("Not yet implemented")
    }
}
