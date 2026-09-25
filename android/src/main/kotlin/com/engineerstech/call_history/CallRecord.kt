package com.engineerstech.call_history

data class CallRecord(
    val name: String?,
    val number: String?,
    val duration: Int?,
    val timestamp: Long?,
    val callType: Int?,
    val cachedNumberType: Int?,
    val cachedNumberLabel: String?,
    val countryIso: String?,
    val isRead: Int?,
    val geocodedLocation: String?,
    val numberPresentation: Int?,
    val phoneAccountComponentName: String?,
    val phoneAccountId: String?,
    val features: Int?,
    val dataUsage: Long?,
    val transcription: String?,
    val voicemailUri: String?,
    val id: String?,
    val cachedFormattedNumber: String?,
    val cachedMatchedNumber: String?,
    val simDisplayName: String?
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "name" to name,
            "number" to number,
            "duration" to duration,
            "timestamp" to timestamp,
            "callType" to callType,
            "cachedNumberType" to cachedNumberType,
            "cachedNumberLabel" to cachedNumberLabel,
            "countryIso" to countryIso,
            "isRead" to isRead,
            "geocodedLocation" to geocodedLocation,
            "numberPresentation" to numberPresentation,
            "phoneAccountComponentName" to phoneAccountComponentName,
            "phoneAccountId" to phoneAccountId,
            "features" to features,
            "dataUsage" to dataUsage,
            "transcription" to transcription,
            "voicemailUri" to voicemailUri,
            "id" to id,
            "cachedFormattedNumber" to cachedFormattedNumber,
            "cachedMatchedNumber" to cachedMatchedNumber,
            "simDisplayName" to simDisplayName
        )
    }
}
