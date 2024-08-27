import 'dart:developer';

import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:gps_requester/gps_requester.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  bool _isGpsEnabled = false;
  final _gpsRequesterPlugin = GpsRequester();
  bool isRed = false;

  @override
  void initState() {
    super.initState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    bool isGpsEnabled;
    log('_isGpsEnabled: $_isGpsEnabled');
    // isGpsEnabled = await _gpsRequesterPlugin.requestLocationService();
    // log('isGpsEnabled: $isGpsEnabled');

    try {
      isGpsEnabled = await _gpsRequesterPlugin.checkLocationService();

      log('checkLocationService: isGpsEnabled: $isGpsEnabled');

      if (!isGpsEnabled) {
        try {
          isGpsEnabled = await _gpsRequesterPlugin.requestLocationService();

          log('requestLocationService: isGpsEnabled: $isGpsEnabled');
        } catch (errorRequest) {
          isGpsEnabled = false;
          log('requestLocationService: error: $errorRequest');
        }

        // If the widget was removed from the tree while the asynchronous platform
        // message was in flight, we want to discard the reply rather than calling
        // setState to update our non-existent appearance.
        if (!mounted) return;
      }
      setState(() => _isGpsEnabled = isGpsEnabled);
    } catch (e) {
      log('error: $e');
    }
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: GestureDetector(
            onTap: () {
              setState(() {
                isRed = !isRed;
              });
              initPlatformState();
            },
            child: ColoredBox(
              color: isRed ? Colors.red : Colors.green,
              child: const Padding(
                padding: EdgeInsets.all(8.0),
                child: Text('SzSS On GPS'),
              ),
            ),
          ),
        ),
      ),
    );
  }
}
