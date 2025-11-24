import 'package:flutter/material.dart';
import '../services/auth_service.dart';
import '../models/user.dart';
import '../utils/constants.dart';

class SplashScreen extends StatefulWidget {
  const SplashScreen({Key? key}) : super(key: key);

  @override
  State<SplashScreen> createState() => _SplashScreenState();
}

class _SplashScreenState extends State<SplashScreen> {
  @override
  void initState() {
    super.initState();
    _checkAuthStatus();
  }

  Future<void> _checkAuthStatus() async {
    
    await Future.delayed(const Duration(seconds: 2));

    try {
      final authService = await AuthService.getInstance();
      final isAuthenticated = await authService.isAuthenticated();

      if (mounted) {
        if (isAuthenticated) {
          
          final user = await authService.getCurrentUser();
          if (user != null) {
            _navigateToRoleDashboard(user.role);
          } else {
            // User data corrupted, go to login
            Navigator.of(context).pushReplacementNamed(AppConstants.loginRoute);
          }
        } else {
          // User not logged in, go to login screen
          Navigator.of(context).pushReplacementNamed(AppConstants.loginRoute);
        }
      }
    } catch (e) {
      // Error checking auth status, go to login
      if (mounted) {
        Navigator.of(context).pushReplacementNamed(AppConstants.loginRoute);
      }
    }
  }

  void _navigateToRoleDashboard(UserRole role) {
    String route;
    switch (role) {
      case UserRole.ADMIN:
        route = AppConstants.adminDashboardRoute;
        break;
      case UserRole.VENDOR:
        route = AppConstants.vendorDashboardRoute;
        break;
      case UserRole.EMPLOYEE:
        route = AppConstants.employeeDashboardRoute;
        break;
      case UserRole.CLIENT:
        route = AppConstants.clientDashboardRoute;
        break;
    }

    Navigator.of(context).pushReplacementNamed(route);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Color(AppConstants.primaryColorValue),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            // App Icon
            Container(
              padding: const EdgeInsets.all(24),
              decoration: BoxDecoration(
                color: Colors.white,
                borderRadius: BorderRadius.circular(20),
              ),
              child: Icon(
                Icons.account_balance,
                size: 64,
                color: Color(AppConstants.primaryColorValue),
              ),
            ),
            const SizedBox(height: 24),
            
            // App Name
            Text(
              AppConstants.appName,
              style: const TextStyle(
                color: Colors.white,
                fontSize: 24,
                fontWeight: FontWeight.bold,
              ),
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 8),
            
            // Version
            Text(
              'Version ${AppConstants.appVersion}',
              style: TextStyle(
                color: Colors.white.withOpacity(0.8),
                fontSize: 14,
              ),
            ),
            const SizedBox(height: 48),
            
            // Loading Indicator
            const CircularProgressIndicator(
              valueColor: AlwaysStoppedAnimation<Color>(Colors.white),
            ),
            const SizedBox(height: 16),
            
            Text(
              'Loading...',
              style: TextStyle(
                color: Colors.white.withOpacity(0.8),
                fontSize: 16,
              ),
            ),
          ],
        ),
      ),
    );
  }
}