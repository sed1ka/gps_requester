import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:gps_requester/gps_requester_method_channel.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  MethodChannelGpsRequester platform = MethodChannelGpsRequester();
  const MethodChannel channel = MethodChannel('gps_requester');

  setUp(() {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger.setMockMethodCallHandler(
      channel,
      (MethodCall methodCall) async {
        return 'harusnaaa apa';
      },
    );
  });

  tearDown(() {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger.setMockMethodCallHandler(channel, null);
  });

  test('requestLocationService', () async {
    dynamic checkLoc =  await platform.checkLocationService();
    print('checkLoc: $checkLoc');


    expect(await platform.requestLocationService(), true);
  });
}
