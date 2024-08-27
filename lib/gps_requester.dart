import 'gps_requester_platform_interface.dart';

class GpsRequester {
  Future<bool> requestLocationService() {
    return GpsRequesterPlatform.instance.requestLocationService();
  }

  Future<bool> checkLocationService() {
    return GpsRequesterPlatform.instance.checkLocationService();
  }
}
