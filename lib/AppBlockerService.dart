import 'package:flutter/services.dart';

import 'AppBlockerService.dart';

class AppBlockerService {
  static const MethodChannel _channel = MethodChannel(
      'com.yourcompany.yourapp/appblocker');

  static Future<void> startService(List<String> packageName, int duration) async {
    try{
      await _channel.invokeMethod(
          'startService', {'packageName': packageName, 'duration': duration});
    }catch(e){
      print("Failed to start service: '${e.toString()}'.");
    }
  }


  static Future<void> stopService() async {
    try {
      await _channel.invokeMethod('stopService');
    } on PlatformException catch (e) {
      print("Failed to stop service: '${e.message}'.");
    }
  }
}

// mixin message {
//   static const String startService = 'startService';
//   static const String stopService = 'stopService';
// }