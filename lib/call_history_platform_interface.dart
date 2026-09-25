import 'package:plugin_platform_interface/plugin_platform_interface.dart';
import 'src/models.dart';
import 'call_history_method_channel.dart';

abstract class CallHistoryPlatform extends PlatformInterface {
  /// Constructs a CallHistoryPlatform.
  CallHistoryPlatform() : super(token: _token);

  static final Object _token = Object();

  static CallHistoryPlatform _instance = MethodChannelCallHistory();

  /// The default instance of [CallHistoryPlatform] to use.
  ///
  /// Defaults to [MethodChannelCallHistory].
  static CallHistoryPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [CallHistoryPlatform] when
  /// they register themselves.
  static set instance(CallHistoryPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<List<CallRecord>> getCallLogs(CallQueryRequest request) {
    throw UnimplementedError('getCallLogs() has not been implemented.');
  }
}
