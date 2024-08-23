import 'package:flutter/services.dart';

class StackExchangePlugin {
  static const MethodChannel _channel = MethodChannel('stack_exchange_plugin');

  static Future<List<Map<String, dynamic>>> getQuestions(
      String tags, int pageSize) async {
    final List<dynamic> questions =
        await _channel.invokeMethod('getQuestions', {
      'tags': tags,
      'pageSize': pageSize,
    });
    return List<Map<String, dynamic>>.from(questions);
  }
}
