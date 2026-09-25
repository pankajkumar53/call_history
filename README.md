<p align="center">
  <img
    src="https://raw.githubusercontent.com/pankajkumar53/call_history/main/logo.svg"
    width="180"
    alt="Call History Plugin Logo"
  />
</p>

<h1 align="center">call_history</h1>

A highly optimized Flutter plugin to natively fetch Android call logs. Built with performance in
mind, this plugin handles massive datasets smoothly without freezing the Flutter UI.

## ✨ Features

- **🚀 Extreme Performance:** Uses Kotlin Coroutines (`Dispatchers.IO`) to fetch data on a background
  thread, guaranteeing zero UI jank.
- **📄 Native Pagination:** Supports `limit` and `offset` for smooth infinite scrolling using direct
  SQLite pagination mechanisms.
- **🔍 Native Database Filtering:** Filter by Date, Call Type, or Phone Number directly at the native
  database level for blazingly fast `O(log N)` lookup speeds.
- **💎 Rich Data Models:** Returns over 15+ data points per call log, including advanced details like
  `simDisplayName` (using highly optimized O(1) memory caching).
- **🛡️ Strongly Typed API:** Built by native developers for Flutter developers. Fully type-safe Dart
  DTOs and Enums (No messy `Map<String, dynamic>`).

## ⚙️ Installation

Add `call_history` to your `pubspec.yaml`:

```yaml
dependencies:
  call_history: ^1.0.0
```

## 🔒 Permissions

To access call logs, you must declare the `READ_CALL_LOG` permission in your Android
`AndroidManifest.xml` file located at `android/app/src/main/AndroidManifest.xml`:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <!-- Add this line -->
    <uses-permission android:name="android.permission.READ_CALL_LOG" />
    <uses-permission android:name="android.permission.READ_PHONE_STATE" /> <!-- Optional: To fetch SIM Display Name -->

    <application ...>
        ...
    </application>
</manifest>
```

> **Note:** You must request this permission at runtime before calling the plugin methods. We
> recommend using the [`permission_handler`](https://pub.dev/packages/permission_handler) package.

## 💻 Usage

Import the package:

```dart
import 'package:call_history/call_history.dart';
```

Fetch the call logs using the `CallQueryOptions` DTO:

```dart
final CallHistory _callHistoryPlugin = CallHistory();

Future<void> fetchLogs() async {
  try {
    final List<CallRecord> logs = await _callHistoryPlugin.getCallLogs(
      CallQueryOptions(
        limit: 50,          // Number of records to return
        offset: 0,          // Starting offset for pagination
        type: CallType.missed, // (Optional) Filter by Call Type
        // number: "+919999999999", // (Optional) Filter by exact phone number
      ),
    );

    for (var log in logs) {
      print('Name: ${log.name}, Number: ${log.number}, Duration: ${log.duration}s');
    }
  } catch (e) {
    print('Error fetching call logs: $e');
  }
}
```

## 🎛️ CallQueryOptions (Filters)

| Property   | Type        | Description                                                         |
|------------|-------------|---------------------------------------------------------------------|
| `limit`    | `int`       | Maximum number of records to fetch. Default is `100`.               |
| `offset`   | `int`       | Number of records to skip for pagination. Default is `0`.           |
| `dateFrom` | `int?`      | Filter logs created *after* this Unix timestamp (in milliseconds).  |
| `dateTo`   | `int?`      | Filter logs created *before* this Unix timestamp (in milliseconds). |
| `type`     | `CallType?` | Filter by `CallType` (e.g., `incoming`, `outgoing`, `missed`).      |
| `number`   | `String?`   | Filter logs matching this exact phone number.                       |

## 📦 CallRecord Data Class

The `CallRecord` object provides extensive information about each call:

| Property           | Type       | Description                                                |
|--------------------|------------|------------------------------------------------------------|
| `id`               | `String?`  | Unique SQLite Database ID.                                 |
| `name`             | `String?`  | Cached caller name (from contacts).                        |
| `number`           | `String?`  | The phone number.                                          |
| `formattedNumber`  | `String?`  | Number formatted with country codes.                       |
| `duration`         | `int?`     | Duration of the call in seconds.                           |
| `timestamp`        | `int?`     | Unix timestamp of the call in milliseconds.                |
| `type`             | `CallType` | Enum representing incoming, outgoing, missed, etc.         |
| `simDisplayName`   | `String?`  | Name of the SIM card used (e.g., "Jio", "Airtel").         |
| `countryIso`       | `String?`  | ISO 3166-1 two-letter country code.                        |
| `geocodedLocation` | `String?`  | The location associated with the number.                   |
| `isRead`           | `int?`     | `1` if a missed call has been acknowledged, `0` otherwise. |
| `dataUsage`        | `int?`     | Data usage in bytes (for video calls).                     |
| `features`         | `int?`     | Bit-mask for call features (e.g., VoLTE, Video).           |
| `voicemailUri`     | `String?`  | URI for voicemail (if applicable).                         |
| `transcription`    | `String?`  | Text transcription of a voicemail.                         |
| `phoneAccountId`   | `String?`  | The unique ID of the PhoneAccount used for the call.       |

## 🤝 Support

If you find this package helpful, please leave a Like 👍 on pub.dev and a Star ⭐ on GitHub!
Feel free to open issues or submit pull requests for any improvements.
