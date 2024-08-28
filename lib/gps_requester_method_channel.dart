import 'dart:developer';

import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'gps_requester_platform_interface.dart';

/// An implementation of [GpsRequesterPlatform] that uses method channels.
class MethodChannelGpsRequester extends GpsRequesterPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('gps_requester');

  @override
  Future<bool> requestLocationService() async {
    final result = await methodChannel.invokeMethod('requestService');
    return result == 1;
  }

  @override
  Future<bool> checkLocationService() async {
    final result = await methodChannel.invokeMethod('checkService');
    return result == 1;
  }
}

