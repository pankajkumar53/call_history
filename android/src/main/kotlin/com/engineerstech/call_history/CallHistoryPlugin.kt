package com.engineerstech.call_history

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Build
import android.provider.CallLog
import android.os.Bundle
import android.content.ContentResolver
import androidx.core.content.ContextCompat
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** CallHistoryPlugin */
class CallHistoryPlugin: FlutterPlugin, MethodCallHandler {
  private lateinit var channel : MethodChannel
  private lateinit var context: Context

  override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "call_history")
    channel.setMethodCallHandler(this)
    context = flutterPluginBinding.applicationContext
  }

  override fun onMethodCall(call: MethodCall, result: Result) {
    if (call.method == "getCallLogs") {
      val limit = call.argument<Int>("limit") ?: 100
      val offset = call.argument<Int>("offset") ?: 0
      val dateFrom = call.argument<Long>("dateFrom")
      val dateTo = call.argument<Long>("dateTo")
      val type = call.argument<Int>("type")
      val number = call.argument<String>("number")

      // Check permission
      if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
        result.error("PERMISSION_DENIED", "READ_CALL_LOG permission not granted", null)
        return
      }

      CoroutineScope(Dispatchers.IO).launch {
        try {
          val callLogs = fetchCallLogs(limit, offset, dateFrom, dateTo, type, number)
          withContext(Dispatchers.Main) {
            result.success(callLogs.map { it.toMap() })
          }
        } catch (e: Exception) {
          withContext(Dispatchers.Main) {
            result.error("QUERY_ERROR", e.message, null)
          }
        }
      }
    } else {
      result.notImplemented()
    }
  }

  private fun fetchCallLogs(
    limit: Int, 
    offset: Int,
    dateFrom: Long?,
    dateTo: Long?,
    type: Int?,
    number: String?
  ): List<CallRecord> {
    val callLogs = mutableListOf<CallRecord>()
    
    val projection = arrayOf(
      CallLog.Calls.CACHED_NAME,
      CallLog.Calls.NUMBER,
      CallLog.Calls.DURATION,
      CallLog.Calls.DATE,
      CallLog.Calls.TYPE,
      CallLog.Calls.CACHED_NUMBER_TYPE,
      CallLog.Calls.CACHED_NUMBER_LABEL,
      CallLog.Calls.COUNTRY_ISO,
      CallLog.Calls.IS_READ,
      CallLog.Calls.GEOCODED_LOCATION,
      CallLog.Calls.NUMBER_PRESENTATION,
      CallLog.Calls.PHONE_ACCOUNT_COMPONENT_NAME,
      CallLog.Calls.PHONE_ACCOUNT_ID,
      CallLog.Calls.FEATURES,
      CallLog.Calls.DATA_USAGE,
      CallLog.Calls.TRANSCRIPTION,
      CallLog.Calls.VOICEMAIL_URI,
      CallLog.Calls._ID,
      CallLog.Calls.CACHED_FORMATTED_NUMBER,
      CallLog.Calls.CACHED_MATCHED_NUMBER
    )
    
    val selectionList = mutableListOf<String>()
    val selectionArgsList = mutableListOf<String>()

    if (dateFrom != null) {
        selectionList.add("${CallLog.Calls.DATE} >= ?")
        selectionArgsList.add(dateFrom.toString())
    }
    if (dateTo != null) {
        selectionList.add("${CallLog.Calls.DATE} <= ?")
        selectionArgsList.add(dateTo.toString())
    }
    if (type != null) {
        selectionList.add("${CallLog.Calls.TYPE} = ?")
        selectionArgsList.add(type.toString())
    }
    if (number != null) {
        selectionList.add("${CallLog.Calls.NUMBER} = ?")
        selectionArgsList.add(number)
    }

    val selectionString = if (selectionList.isNotEmpty()) selectionList.joinToString(" AND ") else null
    val selectionArgsArray = if (selectionArgsList.isNotEmpty()) selectionArgsList.toTypedArray() else null

    val cursor: Cursor? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
       // Android 11+ supports modern bundle-based limits/offsets
       val queryArgs = Bundle().apply {
           putInt(ContentResolver.QUERY_ARG_LIMIT, limit)
           putInt(ContentResolver.QUERY_ARG_OFFSET, offset)
           putStringArray(ContentResolver.QUERY_ARG_SORT_COLUMNS, arrayOf(CallLog.Calls.DATE))
           putInt(ContentResolver.QUERY_ARG_SORT_DIRECTION, ContentResolver.QUERY_SORT_DIRECTION_DESCENDING)
           if (selectionString != null) {
               putString(ContentResolver.QUERY_ARG_SQL_SELECTION, selectionString)
               putStringArray(ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS, selectionArgsArray)
           }
       }
       context.contentResolver.query(
           CallLog.Calls.CONTENT_URI,
           projection,
           queryArgs,
           null
       )
    } else {
        // Fallback for older Android versions
        context.contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            projection,
            selectionString,
            selectionArgsArray,
            "${CallLog.Calls.DATE} DESC" // Note: the limit/offset logic will be handled inside cursor iteration
        )
    }

    // High performance O(1) SIM Name lookup map (Prevents IPC bottleneck inside cursor loop)
    val simNameMap = mutableMapOf<String, String>()
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
        try {
            val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as? android.telecom.TelecomManager
            telecomManager?.callCapablePhoneAccounts?.forEach { handle ->
                val account = telecomManager.getPhoneAccount(handle)
                if (account != null) {
                    simNameMap[handle.id] = account.label.toString()
                }
            }
        } catch (e: SecurityException) {
            // Ignore if security exception happens
        }
    }

    cursor?.use {
        val nameIndex = it.getColumnIndex(CallLog.Calls.CACHED_NAME)
        val numberIndex = it.getColumnIndex(CallLog.Calls.NUMBER)
        val durationIndex = it.getColumnIndex(CallLog.Calls.DURATION)
        val dateIndex = it.getColumnIndex(CallLog.Calls.DATE)
        val typeIndex = it.getColumnIndex(CallLog.Calls.TYPE)
        val cachedNumberTypeIndex = it.getColumnIndex(CallLog.Calls.CACHED_NUMBER_TYPE)
        val cachedNumberLabelIndex = it.getColumnIndex(CallLog.Calls.CACHED_NUMBER_LABEL)
        val countryIsoIndex = it.getColumnIndex(CallLog.Calls.COUNTRY_ISO)
        val isReadIndex = it.getColumnIndex(CallLog.Calls.IS_READ)
        val geocodedLocationIndex = it.getColumnIndex(CallLog.Calls.GEOCODED_LOCATION)
        val numberPresentationIndex = it.getColumnIndex(CallLog.Calls.NUMBER_PRESENTATION)
        val phoneAccountComponentNameIndex = it.getColumnIndex(CallLog.Calls.PHONE_ACCOUNT_COMPONENT_NAME)
        val phoneAccountIdIndex = it.getColumnIndex(CallLog.Calls.PHONE_ACCOUNT_ID)
        val featuresIndex = it.getColumnIndex(CallLog.Calls.FEATURES)
        val dataUsageIndex = it.getColumnIndex(CallLog.Calls.DATA_USAGE)
        val transcriptionIndex = it.getColumnIndex(CallLog.Calls.TRANSCRIPTION)
        val voicemailUriIndex = it.getColumnIndex(CallLog.Calls.VOICEMAIL_URI)
        val idIndex = it.getColumnIndex(CallLog.Calls._ID)
        val cachedFormattedNumberIndex = it.getColumnIndex(CallLog.Calls.CACHED_FORMATTED_NUMBER)
        val cachedMatchedNumberIndex = it.getColumnIndex(CallLog.Calls.CACHED_MATCHED_NUMBER)

        // For older versions, skip rows until offset
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            it.moveToPosition(offset - 1)
        }

        var count = 0
        while (it.moveToNext() && count < limit) {
            val name = if (nameIndex != -1) it.getString(nameIndex) else null
            val number = if (numberIndex != -1) it.getString(numberIndex) else null
            val duration = if (durationIndex != -1) it.getInt(durationIndex) else null
            val date = if (dateIndex != -1) it.getLong(dateIndex) else null
            val type = if (typeIndex != -1) it.getInt(typeIndex) else null
            
            val cachedNumberType = if (cachedNumberTypeIndex != -1) it.getInt(cachedNumberTypeIndex) else null
            val cachedNumberLabel = if (cachedNumberLabelIndex != -1) it.getString(cachedNumberLabelIndex) else null
            val countryIso = if (countryIsoIndex != -1) it.getString(countryIsoIndex) else null
            val isRead = if (isReadIndex != -1) it.getInt(isReadIndex) else null
            val geocodedLocation = if (geocodedLocationIndex != -1) it.getString(geocodedLocationIndex) else null
            val numberPresentation = if (numberPresentationIndex != -1) it.getInt(numberPresentationIndex) else null
            val phoneAccountComponentName = if (phoneAccountComponentNameIndex != -1) it.getString(phoneAccountComponentNameIndex) else null
            val phoneAccountId = if (phoneAccountIdIndex != -1) it.getString(phoneAccountIdIndex) else null
            val features = if (featuresIndex != -1) it.getInt(featuresIndex) else null
            val dataUsage = if (dataUsageIndex != -1 && !it.isNull(dataUsageIndex)) it.getLong(dataUsageIndex) else null
            val transcription = if (transcriptionIndex != -1) it.getString(transcriptionIndex) else null
            val voicemailUri = if (voicemailUriIndex != -1) it.getString(voicemailUriIndex) else null
            val id = if (idIndex != -1) it.getString(idIndex) else null
            
            val cachedFormattedNumber = if (cachedFormattedNumberIndex != -1) it.getString(cachedFormattedNumberIndex) else null
            val cachedMatchedNumber = if (cachedMatchedNumberIndex != -1) it.getString(cachedMatchedNumberIndex) else null
            val simDisplayName = if (phoneAccountId != null) simNameMap[phoneAccountId] else null

            callLogs.add(
                CallRecord(
                    name = name,
                    number = number,
                    duration = duration,
                    timestamp = date,
                    callType = type,
                    cachedNumberType = cachedNumberType,
                    cachedNumberLabel = cachedNumberLabel,
                    countryIso = countryIso,
                    isRead = isRead,
                    geocodedLocation = geocodedLocation,
                    numberPresentation = numberPresentation,
                    phoneAccountComponentName = phoneAccountComponentName,
                    phoneAccountId = phoneAccountId,
                    features = features,
                    dataUsage = dataUsage,
                    transcription = transcription,
                    voicemailUri = voicemailUri,
                    id = id,
                    cachedFormattedNumber = cachedFormattedNumber,
                    cachedMatchedNumber = cachedMatchedNumber,
                    simDisplayName = simDisplayName
                )
            )
            count++
        }
    }
    
    return callLogs
  }

  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }
}
