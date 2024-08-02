import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:t1_leairn/app_select_page.dart';
import 'package:t1_leairn/practise_page.dart';

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
      home: isHomePage ? const AppSelectorPage() : const PractisePage(),
    );
  }
}
