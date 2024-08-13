import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return const MaterialApp(
      home: MyHomePage(),
    );
  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({super.key});

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  static const platform = MethodChannel('com.example.wifi_direct');
  List<String> _peers = [];

  Future<void> _discoverPeers() async {
    try {
      final List<dynamic> peers = await platform.invokeMethod('discoverPeers');
      print('@@@@@@@@@@@@@@@@@ peers :  $peers');
      setState(() {
        _peers = peers.cast<String>();
      });
    } on PlatformException catch (e) {
      print("@@@@@@@@@@@@@@@@@ Failed to discover peers: '${e.message}'.");
    }
  }

  Future<void> _connectToDevice(String deviceAddress) async {
    try {
      await platform.invokeMethod('connect', {'address': deviceAddress});
    } on PlatformException catch (e) {
      print("@@@@@@@@@@@@@@@@@ Failed to connect: '${e.message}'.");
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Wi-Fi Direct Example'),
      ),
      body: Column(
        children: [
          ElevatedButton(
            onPressed: _discoverPeers,
            child: const Text('Discover Peers'),
          ),
          Expanded(
            child: ListView.builder(
              itemCount: _peers.length,
              itemBuilder: (context, index) {
                return ListTile(
                  title: Text(_peers[index]),
                  onTap: () => _connectToDevice(_peers[index]),
                );
              },
            ),
          ),
        ],
      ),
    );
  }
}
