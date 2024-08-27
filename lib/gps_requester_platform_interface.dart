import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'gps_requester_method_channel.dart';

abstract class GpsRequesterPlatform extends PlatformInterface {
  /// Constructs a GpsRequesterPlatform.
  GpsRequesterPlatform() : super(token: _token);

  static final Object _token = Object();

  static GpsRequesterPlatform _instance = MethodChannelGpsRequester();

  /// The default instance of [GpsRequesterPlatform] to use.
  ///
  /// Defaults to [MethodChannelGpsRequester].
  static GpsRequesterPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [GpsRequesterPlatform] when
  /// they register themselves.
  static set instance(GpsRequesterPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<bool> requestLocationService() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<bool> checkLocationService() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }
}
