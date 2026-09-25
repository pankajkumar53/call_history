enum CallType {
  incoming,
  outgoing,
  missed,
  voiceMail,
  rejected,
  blocked,
  answeredExternally,
  unknown
}

class CallRecord {
  final String? name;
  final String? number;
  final int? duration;
  final int? timestamp;
  final CallType type;
  
  final int? cachedNumberType;
  final String? cachedNumberLabel;
  final String? countryIso;
  final int? isRead;
  final String? geocodedLocation;
  final int? numberPresentation;
  final String? phoneAccountComponentName;
  final String? phoneAccountId;
  final int? features;
  final int? dataUsage;
  final String? transcription;
  final String? voicemailUri;
  final String? id;
  final String? formattedNumber;
  final String? cachedMatchedNumber;
  final String? simDisplayName;

  CallRecord({
    this.name,
    this.number,
    this.duration,
    this.timestamp,
    required this.type,
    this.cachedNumberType,
    this.cachedNumberLabel,
    this.countryIso,
    this.isRead,
    this.geocodedLocation,
    this.numberPresentation,
    this.phoneAccountComponentName,
    this.phoneAccountId,
    this.features,
    this.dataUsage,
    this.transcription,
    this.voicemailUri,
    this.id,
    this.formattedNumber,
    this.cachedMatchedNumber,
    this.simDisplayName,
  });

  factory CallRecord.fromMap(Map<Object?, Object?> map) {
    return CallRecord(
      name: map['name'] as String?,
      number: map['number'] as String?,
      duration: map['duration'] as int?,
      timestamp: map['timestamp'] as int?,
      type: _getCallType(map['callType'] as int?),
      cachedNumberType: map['cachedNumberType'] as int?,
      cachedNumberLabel: map['cachedNumberLabel'] as String?,
      countryIso: map['countryIso'] as String?,
      isRead: map['isRead'] as int?,
      geocodedLocation: map['geocodedLocation'] as String?,
      numberPresentation: map['numberPresentation'] as int?,
      phoneAccountComponentName: map['phoneAccountComponentName'] as String?,
      phoneAccountId: map['phoneAccountId'] as String?,
      features: map['features'] as int?,
      dataUsage: map['dataUsage'] as int?,
      transcription: map['transcription'] as String?,
      voicemailUri: map['voicemailUri'] as String?,
      id: map['id'] as String?,
      formattedNumber: map['cachedFormattedNumber'] as String?,
      cachedMatchedNumber: map['cachedMatchedNumber'] as String?,
      simDisplayName: map['simDisplayName'] as String?,
    );
  }

  static CallType _getCallType(int? typeInt) {
    switch (typeInt) {
      case 1:
        return CallType.incoming;
      case 2:
        return CallType.outgoing;
      case 3:
        return CallType.missed;
      case 4:
        return CallType.voiceMail;
      case 5:
        return CallType.rejected;
      case 6:
        return CallType.blocked;
      case 7:
        return CallType.answeredExternally;
      default:
        return CallType.unknown;
    }
  }

  @override
  String toString() {
    return 'CallRecord{name: $name, number: $number, duration: $duration, timestamp: $timestamp, type: $type}';
  }
}

class CallQueryOptions {
  final int limit;
  final int offset;
  final int? dateFrom;
  final int? dateTo;
  final CallType? type;
  final String? number;

  CallQueryOptions({
    this.limit = 100,
    this.offset = 0,
    this.dateFrom,
    this.dateTo,
    this.type,
    this.number,
  });
}

class CallQueryRequest {
  final int limit;
  final int offset;
  final int? dateFrom;
  final int? dateTo;
  final int? type;
  final String? number;

  CallQueryRequest({
    this.limit = 100,
    this.offset = 0,
    this.dateFrom,
    this.dateTo,
    this.type,
    this.number,
  });

  Map<String, dynamic> toMap() {
    return {
      'limit': limit,
      'offset': offset,
      'dateFrom': dateFrom,
      'dateTo': dateTo,
      'type': type,
      'number': number,
    };
  }
}
