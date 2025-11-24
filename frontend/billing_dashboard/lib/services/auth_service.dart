import '../models/user.dart';
import 'api_service.dart';
import 'storage_service.dart';

class AuthService {
  static AuthService? _instance;
  late ApiService _apiService;
  late StorageService _storageService;

  AuthService._internal();

  static Future<AuthService> getInstance() async {
    _instance ??= AuthService._internal();
    _instance!._apiService = await ApiService.getInstance();
    _instance!._storageService = await StorageService.getInstance();
    return _instance!;
  }

  // Login user
  Future<LoginResponse> login(String email, String password) async {
    try {
      final loginResponse = await _apiService.login(email, password);
      
      // Save token and user data
      await _storageService.saveToken(loginResponse.token);
      await _storageService.saveUser(loginResponse.user);
      
      return loginResponse;
    } catch (e) {
      throw e;
    }
  }

  // Register user
  Future<LoginResponse> register(String name, String email, String password, String role, [String? billingModel]) async {
    try {
      final registerResponse = await _apiService.register(name, email, password, role, billingModel);
      
      // Save token and user data (auto-login after registration)
      await _storageService.saveToken(registerResponse.token);
      await _storageService.saveUser(registerResponse.user);
      
      return registerResponse;
    } catch (e) {
      throw e;
    }
  }

  // Logout user
  Future<void> logout() async {
    await _storageService.clearAll();
  }

  // Get current user
  Future<User?> getCurrentUser() async {
    return await _storageService.getUser();
  }

  // Get current token
  Future<String?> getToken() async {
    return await _storageService.getToken();
  }

  // Check if user is authenticated
  Future<bool> isAuthenticated() async {
    return await _storageService.isLoggedIn();
  }

  // Get user role
  Future<UserRole?> getUserRole() async {
    final user = await getCurrentUser();
    return user?.role;
  }

  // Check if user has specific role
  Future<bool> hasRole(UserRole role) async {
    final userRole = await getUserRole();
    return userRole == role;
  }

  // Check if user is admin
  Future<bool> isAdmin() async {
    return await hasRole(UserRole.ADMIN);
  }

  // Check if user is vendor
  Future<bool> isVendor() async {
    return await hasRole(UserRole.VENDOR);
  }

  // Check if user is employee
  Future<bool> isEmployee() async {
    return await hasRole(UserRole.EMPLOYEE);
  }
}