import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  @override
  void initState() {
    super.initState();
    _checkIntent();
  }

  Future<void> _checkIntent() async {
    const MethodChannel platform =
        MethodChannel('com.example.t1_leairn/intent');
    try {
      final bool openPracticePage =
          await platform.invokeMethod('getIntentData');
      if (openPracticePage) {
        Navigator.pushReplacement(
          context,
          MaterialPageRoute(builder: (context) => const PracticePage()),
        );
      }
    } on PlatformException catch (e) {
      print("Failed to get intent data: '${e.message}'.");
    }
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: const HomePage(),
    );
  }
}

class HomePage extends StatelessWidget {
  const HomePage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Home Page'),
      ),
      body: const Center(
        child: Text('Welcome to the Home Page!'),
      ),
    );
  }
}

class PracticePage extends StatelessWidget {
  const PracticePage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Practice Page'),
      ),
      body: const Center(
        child: Text('This is the practice page!'),
      ),
    );
  }
}
