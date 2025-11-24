import 'package:flutter/material.dart';
import '../../models/user.dart';
import '../../services/auth_service.dart';
import '../../utils/constants.dart';

class AppDrawer extends StatelessWidget {
  final User user;

  const AppDrawer({Key? key, required this.user}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Drawer(
      child: Column(
        children: [
          // User Header
          UserAccountsDrawerHeader(
            decoration: BoxDecoration(
              color: Color(AppConstants.primaryColorValue),
            ),
            accountName: Text(user.name),
            accountEmail: Text(user.email),
            currentAccountPicture: CircleAvatar(
              backgroundColor: Colors.white,
              child: Text(
                user.name.isNotEmpty ? user.name[0].toUpperCase() : 'U',
                style: TextStyle(
                  color: Color(AppConstants.primaryColorValue),
                  fontWeight: FontWeight.bold,
                  fontSize: 20,
                ),
              ),
            ),
          ),
          
          // Navigation Items
          Expanded(
            child: ListView(
              padding: EdgeInsets.zero,
              children: _buildNavigationItems(context),
            ),
          ),
          
          // Logout
          const Divider(),
          ListTile(
            leading: const Icon(Icons.logout, color: Colors.red),
            title: const Text('Logout'),
            onTap: () => _handleLogout(context),
          ),
          const SizedBox(height: 16),
        ],
      ),
    );
  }

  List<Widget> _buildNavigationItems(BuildContext context) {
    final items = <Widget>[];
    
    // Dashboard (always available)
    items.add(
      ListTile(
        leading: const Icon(Icons.dashboard),
        title: const Text('Dashboard'),
        onTap: () {
          Navigator.pop(context);
          _navigateToRoleDashboard(context);
        },
      ),
    );

    // Role-specific navigation
    switch (user.role) {
      case UserRole.ADMIN:
        // Admin navigation items removed - all functionality is available on dashboard
        break;
        
      case UserRole.VENDOR:
        // Vendor-specific items removed since trips and earnings are on dashboard
        break;
        
      case UserRole.EMPLOYEE:
        // Employee navigation items removed - all information is available on dashboard
        break;
        
      case UserRole.CLIENT:
        // Client-specific items removed since billing is not implemented
        break;
    }

    return items;
  }

  void _navigateToRoleDashboard(BuildContext context) {
    String route;
    switch (user.role) {
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
    Navigator.pushReplacementNamed(context, route);
  }

  Future<void> _handleLogout(BuildContext context) async {
    try {
      final authService = await AuthService.getInstance();
      await authService.logout();
      
      if (context.mounted) {
        Navigator.pushReplacementNamed(context, AppConstants.loginRoute);
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('Logged out successfully'),
            backgroundColor: Colors.green,
          ),
        );
      }
    } catch (e) {
      if (context.mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Logout failed: $e'),
            backgroundColor: Colors.red,
          ),
        );
      }
    }
  }
}