package com.example.stack_exchange_plugin

import android.content.Context
import android.os.AsyncTask
import android.widget.Toast
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class StackExchangePlugin: FlutterPlugin, MethodChannel.MethodCallHandler {
    private lateinit var channel: MethodChannel
    private lateinit var context: Context

    override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(binding.binaryMessenger, "stack_exchange_plugin")
        context = binding.applicationContext
        channel.setMethodCallHandler(this)
    }
override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
    if (call.method == "fetchData") {
        fetchData(result)
    } else {
        result.notImplemented()
    }
}

private fun fetchData(result: MethodChannel.Result) {
    FetchDataTask(context, result).execute()
}

private class FetchDataTask(
    private val context: Context,
    private val result: MethodChannel.Result
) : AsyncTask<Void, Void, String>() {

    override fun doInBackground(vararg params: Void?): String {
        return try {
            val url = URL("https://api.stackexchange.com/2.3/questions?order=desc&sort=activity&site=stackoverflow")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            val inStream = BufferedReader(InputStreamReader(connection.inputStream))
            val content = StringBuilder()
            var inputLine: String?
            while (inStream.readLine().also { inputLine = it } != null) {
                content.append(inputLine)
            }
            inStream.close()
            connection.disconnect()
            content.toString()  // Return the fetched data
        } catch (e: Exception) {
            "Failed to fetch data!"
        }
    }

    override fun onPostExecute(resultString: String) {
        // Show a toast with the result
        Toast.makeText(context, resultString, Toast.LENGTH_LONG).show()
        
        // Send the result back to Flutter
        result.success(resultString)
    }
}

   



}