import 'package:dio/dio.dart';
import '../models/user.dart';
import '../models/dashboard_stats.dart';
import '../models/trip.dart';
import '../utils/constants.dart';
import 'storage_service.dart';

class ApiService {
  static ApiService? _instance;
  late Dio _dio;
  late StorageService _storage;

  ApiService._internal() {
    _dio = Dio(BaseOptions(
      baseUrl: AppConstants.apiBaseUrl,
      connectTimeout: AppConstants.apiTimeout,
      receiveTimeout: AppConstants.apiTimeout,
      headers: {
        'Content-Type': 'application/json',
      },
    ));
    _setupInterceptors();
  }

  static Future<ApiService> getInstance() async {
    _instance ??= ApiService._internal();
    _instance!._storage = await StorageService.getInstance();
    return _instance!;
  }

  void _setupInterceptors() {
    _dio.interceptors.add(InterceptorsWrapper(
      onRequest: (options, handler) async {
        // Add auth token
        final token = await _storage.getToken();
        if (token != null && token.isNotEmpty) {
          options.headers['Authorization'] = 'Bearer $token';
        }
        handler.next(options);
      },
      onError: (error, handler) {
        // Handle common errors
        if (error.response?.statusCode == 401) {
          // Token expired or invalid
          _storage.clearAll();
        }
        handler.next(error);
      },
    ));
  }

  // Authentication endpoints
  Future<LoginResponse> login(String email, String password) async {
    try {
      final response = await _dio.post(ApiEndpoints.login, data: {
        'email': email,
        'password': password,
      });
      return LoginResponse.fromJson(response.data);
    } catch (e) {
      throw _handleError(e);
    }
  }

  Future<LoginResponse> register(String name, String email, String password, String role, [String? billingModel]) async {
    try {
      final data = {
        'name': name,
        'email': email,
        'password': password,
        'role': role,
      };
      
      if (billingModel != null && billingModel.isNotEmpty) {
        data['preferredBillingModel'] = billingModel;
      }
      
      final response = await _dio.post(ApiEndpoints.register, data: data);
      return LoginResponse.fromJson(response.data);
    } catch (e) {
      throw _handleError(e);
    }
  }

  // Admin endpoints
  Future<AdminStats> getAdminStats() async {
    try {
      final response = await _dio.get(ApiEndpoints.adminAnalytics);
      return AdminStats.fromJson(response.data);
    } catch (e) {
      throw _handleError(e);
    }
  }

  Future<List<User>> getAllClients() async {
    try {
      final response = await _dio.get(ApiEndpoints.adminClients);
      return (response.data as List)
          .map((json) => User.fromJson(json))
          .toList();
    } catch (e) {
      throw _handleError(e);
    }
  }

  Future<List<User>> getAllVendors() async {
    try {
      final response = await _dio.get(ApiEndpoints.adminVendors);
      return (response.data as List)
          .map((json) => User.fromJson(json))
          .toList();
    } catch (e) {
      throw _handleError(e);
    }
  }

  // Vendor endpoints
  Future<VendorStats> getVendorStats(int vendorId) async {
    try {
      final response = await _dio.get(ApiEndpoints.vendorProfile, queryParameters: {
        'vendorId': vendorId,
      });
      return VendorStats.fromJson(response.data);
    } catch (e) {
      throw _handleError(e);
    }
  }

  Future<List<Trip>> getVendorTrips(int vendorId) async {
    try {
      final response = await _dio.get(ApiEndpoints.vendorTrips, queryParameters: {
        'vendorId': vendorId,
      });
      return (response.data as List)
          .map((json) => Trip.fromJson(json))
          .toList();
    } catch (e) {
      throw _handleError(e);
    }
  }

  // Employee endpoints
  Future<EmployeeStats> getEmployeeStats(int employeeId) async {
    try {
      final response = await _dio.get(ApiEndpoints.employeeProfile, queryParameters: {
        'employeeId': employeeId,
      });
      return EmployeeStats.fromJson(response.data);
    } catch (e) {
      throw _handleError(e);
    }
  }

  Future<List<Trip>> getEmployeeTrips(int employeeId) async {
    try {
      final response = await _dio.get(ApiEndpoints.employeeTrips, queryParameters: {
        'employeeId': employeeId,
      });
      return (response.data as List)
          .map((json) => Trip.fromJson(json))
          .toList();
    } catch (e) {
      throw _handleError(e);
    }
  }

  // Generic HTTP methods
  Future<Response> get(String path, {Map<String, dynamic>? queryParameters}) async {
    try {
      return await _dio.get(path, queryParameters: queryParameters);
    } catch (e) {
      throw _handleError(e);
    }
  }

  Future<Response> post(String path, {dynamic data}) async {
    try {
      return await _dio.post(path, data: data);
    } catch (e) {
      throw _handleError(e);
    }
  }

  Future<Response> put(String path, {dynamic data}) async {
    try {
      return await _dio.put(path, data: data);
    } catch (e) {
      throw _handleError(e);
    }
  }

  Future<Response> delete(String path) async {
    try {
      return await _dio.delete(path);
    } catch (e) {
      throw _handleError(e);
    }
  }

  String _handleError(dynamic error) {
    if (error is DioException) {
      switch (error.type) {
        case DioExceptionType.connectionTimeout:
        case DioExceptionType.receiveTimeout:
          return ErrorMessages.networkError;
        case DioExceptionType.badResponse:
          final statusCode = error.response?.statusCode;
          final message = error.response?.data['message'] ?? 'Unknown error';
          return 'Server error ($statusCode): $message';
        default:
          return ErrorMessages.networkError;
      }
    }
    return ErrorMessages.unknownError;
  }
}