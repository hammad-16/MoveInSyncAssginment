import 'package:flutter/material.dart';
import 'screens/login_screen.dart';
import 'screens/register_screen.dart';
import 'screens/admin_dashboard.dart';
import 'screens/vendor_dashboard.dart';
import 'screens/client_dashboard.dart';
import 'screens/employee_dashboard.dart';
import 'screens/splash_screen.dart';
import 'utils/constants.dart';

void main() {
  runApp(const BillingDashboardApp());
}

class BillingDashboardApp extends StatelessWidget {
  const BillingDashboardApp({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: AppConstants.appName,
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        primarySwatch: MaterialColor(
          AppConstants.primaryColorValue,
          <int, Color>{
            50: Color(0xFFE3F2FD),
            100: Color(0xFFBBDEFB),
            200: Color(0xFF90CAF9),
            300: Color(0xFF64B5F6),
            400: Color(0xFF42A5F5),
            500: Color(AppConstants.primaryColorValue),
            600: Color(0xFF1E88E5),
            700: Color(0xFF1976D2),
            800: Color(0xFF1565C0),
            900: Color(0xFF0D47A1),
          },
        ),
        useMaterial3: true,
        appBarTheme: AppBarTheme(
          backgroundColor: Color(AppConstants.primaryColorValue),
          foregroundColor: Colors.white,
          elevation: 0,
        ),
        cardTheme: CardTheme(
          elevation: AppConstants.cardElevation,
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(AppConstants.borderRadius),
          ),
        ),
        elevatedButtonTheme: ElevatedButtonThemeData(
          style: ElevatedButton.styleFrom(
            backgroundColor: Color(AppConstants.primaryColorValue),
            foregroundColor: Colors.white,
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(AppConstants.borderRadius),
            ),
          ),
        ),
      ),
      initialRoute: '/',
      routes: {
        '/': (context) => const SplashScreen(),
        AppConstants.loginRoute: (context) => const LoginScreen(),
        '/register': (context) => const RegisterScreen(),
        AppConstants.adminDashboardRoute: (context) => const AdminDashboard(),
        AppConstants.vendorDashboardRoute: (context) => const VendorDashboard(),
        AppConstants.clientDashboardRoute: (context) => const ClientDashboard(),
        AppConstants.employeeDashboardRoute: (context) => const EmployeeDashboard(),
      },
    );
  }
}