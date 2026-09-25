import 'package:flutter/material.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:call_history/call_history.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: const CallHistoryScreen(),
    );
  }
}

class CallHistoryScreen extends StatefulWidget {
  const CallHistoryScreen({super.key});

  @override
  State<CallHistoryScreen> createState() => _CallHistoryScreenState();
}

class _CallHistoryScreenState extends State<CallHistoryScreen> {
  final CallHistory _callHistoryPlugin = CallHistory();
  final List<CallRecord> _callLogs = [];
  bool _isLoading = false;
  bool _hasMore = true;
  int _offset = 0;
  final int _limit = 10000;
  final ScrollController _scrollController = ScrollController();
  CallType? _selectedFilterType;

  @override
  void initState() {
    super.initState();
    _requestPermissionAndFetch();
    _scrollController.addListener(() {
      if (_scrollController.position.pixels >= _scrollController.position.maxScrollExtent - 200 &&
          !_isLoading &&
          _hasMore) {
        _fetchMoreCallLogs();
      }
    });
  }

  Future<void> _requestPermissionAndFetch() async {
    final status = await Permission.phone.request();
    if (!mounted) return;
    if (status.isGranted) {
      _fetchMoreCallLogs();
    } else {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Permission denied to read call logs')),
      );
    }
  }

  Future<void> _fetchMoreCallLogs() async {
    if (_isLoading || !_hasMore) return;

    setState(() {
      _isLoading = true;
    });

    try {
      final logs = await _callHistoryPlugin.getCallLogs(CallQueryOptions(
        limit: _limit, 
        offset: _offset,
        type: _selectedFilterType,
        number: "+916205730578"
      ));

      debugPrint("Fetched ${logs.length} logs");
      setState(() {
        if (logs.length < _limit) {
          _hasMore = false;
        }
        _callLogs.addAll(logs);
        _offset += _limit;
        _isLoading = false;
      });
    } catch (e) {
      if (!mounted) return;
      setState(() {
        _isLoading = false;
      });
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Error fetching logs: $e')),
      );
    }
  }

  Icon _getIconForType(CallType type) {
    switch (type) {
      case CallType.incoming:
        return const Icon(Icons.call_received, color: Colors.blue);
      case CallType.outgoing:
        return const Icon(Icons.call_made, color: Colors.green);
      case CallType.missed:
        return const Icon(Icons.call_missed, color: Colors.red);
      default:
        return const Icon(Icons.call, color: Colors.grey);
    }
  }

  String _formatDuration(int? durationInSeconds) {
    if (durationInSeconds == null) return "0s";
    final minutes = durationInSeconds ~/ 60;
    final seconds = durationInSeconds % 60;
    if (minutes > 0) {
      return "${minutes}m ${seconds}s";
    }
    return "${seconds}s";
  }
  
  String _formatDate(int? timestamp) {
      if (timestamp == null) return "Unknown";
      final date = DateTime.fromMillisecondsSinceEpoch(timestamp);
      return "${date.day}/${date.month}/${date.year} ${date.hour}:${date.minute.toString().padLeft(2, '0')}";
  }

  void _onFilterChanged(CallType? type) {
    if (_selectedFilterType == type) return;
    setState(() {
      _selectedFilterType = type;
      _callLogs.clear();
      _offset = 0;
      _hasMore = true;
    });
    _fetchMoreCallLogs();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Call History'),
        actions: [
          DropdownButton<CallType?>(
            value: _selectedFilterType,
            icon: const Icon(Icons.filter_list, color: Colors.white),
            dropdownColor: Colors.blue,
            style: const TextStyle(color: Colors.white),
            underline: Container(),
            onChanged: _onFilterChanged,
            items: const [
              DropdownMenuItem(value: null, child: Text('All Calls')),
              DropdownMenuItem(value: CallType.incoming, child: Text('Incoming')),
              DropdownMenuItem(value: CallType.outgoing, child: Text('Outgoing')),
              DropdownMenuItem(value: CallType.missed, child: Text('Missed')),
              DropdownMenuItem(value: CallType.rejected, child: Text('Rejected')),
            ],
          ),
          const SizedBox(width: 16),
        ],
      ),
      body: _callLogs.isEmpty && _isLoading
          ? const Center(child: CircularProgressIndicator())
          : _callLogs.isEmpty && !_isLoading
              ? const Center(child: Text("No calls found."))
              : ListView.builder(
              controller: _scrollController,
              itemCount: _callLogs.length + (_hasMore ? 1 : 0),
              itemBuilder: (context, index) {
                if (index == _callLogs.length) {
                  return const Center(
                    child: Padding(
                      padding: EdgeInsets.all(8.0),
                      child: CircularProgressIndicator(),
                    ),
                  );
                }

                final log = _callLogs[index];
                return ListTile(
                  leading: _getIconForType(log.type),
                  title: Text(log.name ?? log.number ?? "Unknown"),
                  subtitle: Text("${_formatDate(log.timestamp)}\nDuration: ${_formatDuration(log.duration)}"),
                  isThreeLine: true,
                );
              },
            ),
    );
  }

  @override
  void dispose() {
    _scrollController.dispose();
    super.dispose();
  }
}
