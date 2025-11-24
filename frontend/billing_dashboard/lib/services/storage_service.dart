import 'dart:convert';
import 'package:shared_preferences/shared_preferences.dart';
import '../models/user.dart';
import '../utils/constants.dart';

class StorageService {
  static StorageService? _instance;
  static SharedPreferences? _preferences;

  StorageService._internal();

  static Future<StorageService> getInstance() async {
    _instance ??= StorageService._internal();
    _preferences ??= await SharedPreferences.getInstance();
    return _instance!;
  }

  // Token management
  Future<void> saveToken(String token) async {
    await _preferences!.setString(AppConstants.tokenKey, token);
  }

  Future<String?> getToken() async {
    return _preferences!.getString(AppConstants.tokenKey);
  }

  Future<void> removeToken() async {
    await _preferences!.remove(AppConstants.tokenKey);
  }

  // User management
  Future<void> saveUser(User user) async {
    final userJson = jsonEncode(user.toJson());
    await _preferences!.setString(AppConstants.userKey, userJson);
  }

  Future<User?> getUser() async {
    final userJson = _preferences!.getString(AppConstants.userKey);
    if (userJson != null) {
      try {
        final userMap = jsonDecode(userJson) as Map<String, dynamic>;
        return User.fromJson(userMap);
      } catch (e) {
        // If parsing fails, remove corrupted data
        await removeUser();
        return null;
      }
    }
    return null;
  }

  Future<void> removeUser() async {
    await _preferences!.remove(AppConstants.userKey);
  }

  // Clear all data 
  Future<void> clearAll() async {
    await removeToken();
    await removeUser();
  }

  // Check if user is logged in
  Future<bool> isLoggedIn() async {
    final token = await getToken();
    final user = await getUser();
    return token != null && token.isNotEmpty && user != null;
  }
}