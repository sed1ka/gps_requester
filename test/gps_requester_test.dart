import 'package:flutter_test/flutter_test.dart';
import 'package:gps_requester/gps_requester.dart';
import 'package:gps_requester/gps_requester_platform_interface.dart';
import 'package:gps_requester/gps_requester_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockGpsRequesterPlatform
    with MockPlatformInterfaceMixin
    implements GpsRequesterPlatform {

  @override
  Future<bool> requestLocationService() => Future.value(true);

  @override
  Future<bool> checkLocationService() => Future.value(true);
}

void main() {
  final GpsRequesterPlatform initialPlatform = GpsRequesterPlatform.instance;

  test('$MethodChannelGpsRequester is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelGpsRequester>());
  });

  test('getPlatformVersion', () async {
    GpsRequester gpsRequesterPlugin = GpsRequester();
    MockGpsRequesterPlatform fakePlatform = MockGpsRequesterPlatform();
    GpsRequesterPlatform.instance = fakePlatform;
dynamic rseult =    await fakePlatform.requestLocationService();
print('result: $rseult');
    expect(await fakePlatform.requestLocationService(), true);
  });
}
