import 'call_history_platform_interface.dart';
import 'src/models.dart';

export 'src/models.dart' show CallRecord, CallType, CallQueryOptions;

class CallHistory {
  /// Fetches a paginated list of call logs.
  /// Note: The application must have the READ_CALL_LOG permission granted before calling this method.
  Future<List<CallRecord>> getCallLogs(CallQueryOptions options) async {
    final request = CallQueryRequest(
      limit: options.limit,
      offset: options.offset,
      dateFrom: options.dateFrom,
      dateTo: options.dateTo,
      type: _typeToInt(options.type),
      number: options.number,
    );
    
    final logs = await CallHistoryPlatform.instance.getCallLogs(request);
    return logs;
  }

  int? _typeToInt(CallType? type) {
    if (type == null) return null;
    switch (type) {
      case CallType.incoming: return 1;
      case CallType.outgoing: return 2;
      case CallType.missed: return 3;
      case CallType.voiceMail: return 4;
      case CallType.rejected: return 5;
      case CallType.blocked: return 6;
      case CallType.answeredExternally: return 7;
      case CallType.unknown: return null;
    }
  }
}
