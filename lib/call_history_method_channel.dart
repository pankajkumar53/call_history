import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'call_history_platform_interface.dart';
import 'src/models.dart';

/// An implementation of [CallHistoryPlatform] that uses method channels.
class MethodChannelCallHistory extends CallHistoryPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('call_history');

  @override
  Future<List<CallRecord>> getCallLogs(CallQueryRequest request) async {
    final logs = await methodChannel.invokeListMethod<Map<Object?, Object?>>(
      'getCallLogs',
      request.toMap(),
    );
    if (logs == null) return [];
    
    return logs.map((logMap) => CallRecord.fromMap(logMap)).toList();
  }
}
