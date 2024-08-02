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
  static const platform = MethodChannel('app_opener');
  bool isHomePage = true;

  @override
  void initState() {
    super.initState();
    platform.setMethodCallHandler((call) async {
      if (call.method == 'switch_scene') {
        setState(() {
          isHomePage = !isHomePage;
        });
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: Text(isHomePage ? 'Home Page' : 'Practice Page'),
        ),
        body: Center(
          child: Text(
            isHomePage ? 'This is the Home Page' : 'This is the Practice Page',
            style: const TextStyle(fontSize: 24),
          ),
        ),
      ),
    );
  }
}
