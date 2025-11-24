import 'package:flutter/material.dart';
import '../services/auth_service.dart';
import '../services/api_service.dart';
import '../models/user.dart';
import '../utils/constants.dart';
import '../widgets/common/index.dart';
import '../widgets/charts/index.dart';
import 'assign_vendor_screen.dart';

class AdminDashboard extends StatefulWidget {
  const AdminDashboard({Key? key}) : super(key: key);

  @override
  State<AdminDashboard> createState() => _AdminDashboardState();
}

class _AdminDashboardState extends State<AdminDashboard> {
  late AuthService _authService;
  late ApiService _apiService;
  User? _currentUser;
  
  // Dashboard data
  Map<String, dynamic>? _analytics;
  List<dynamic>? _clients;
  List<dynamic>? _vendors;
  bool _isLoading = true;
  String? _errorMessage;

  @override
  void initState() {
    super.initState();
    _initServices();
  }

  Future<void> _initServices() async {
    try {
      _authService = await AuthService.getInstance();
      _apiService = await ApiService.getInstance();
      _currentUser = await _authService.getCurrentUser();
      
      if (_currentUser != null) {
        await _loadDashboardData();
      }
    } catch (e) {
      setState(() {
        _errorMessage = 'Failed to initialize: $e';
        _isLoading = false;
      });
    }
  }

  Future<void> _loadDashboardData() async {
    setState(() {
      _isLoading = true;
      _errorMessage = null;
    });

    try {
      // Load analytics data
      final analyticsResponse = await _apiService.get('/admin/analytics');
      
      // Load clients and vendors lists
      final clientsResponse = await _apiService.get('/admin/clients');
      final vendorsResponse = await _apiService.get('/admin/vendors');

      setState(() {
        _analytics = analyticsResponse.data;
        _clients = clientsResponse.data;
        _vendors = vendorsResponse.data;
        _isLoading = false;
      });
    } catch (e) {
      setState(() {
        _errorMessage = 'Failed to load dashboard data: $e';
        _isLoading = false;
      });
    }
  }

  Future<void> _assignVendor(int clientId, int vendorId) async {
    try {
      await _apiService.post('/admin/assign-vendor', data: {
        'clientId': clientId,
        'vendorId': vendorId,
        'billingModel': 'PACKAGE', // Default billing model
        'packageRate': 100.0, // Default package rate
        'tripRate': 15.0, // Default trip rate
        'standardDistanceLimit': 50.0, // Default distance limit in km
        'standardTimeLimit': 120.0, // Default time limit in minutes
        'employeeExtraDistanceRate': 2.0, // Default extra distance rate for employee
        'employeeExtraTimeRate': 1.5, // Default extra time rate for employee
        'vendorExtraDistanceRate': 3.0, // Default extra distance rate for vendor
        'vendorExtraTimeRate': 2.5, // Default extra time rate for vendor
      });
      
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Vendor assigned successfully with default rates'),
          backgroundColor: Colors.green,
        ),
      );
      
      // Reload data
      await _loadDashboardData();
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text('Failed to assign vendor: $e'),
          backgroundColor: Colors.red,
        ),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    if (_currentUser == null) {
      return const Scaffold(
        body: Center(child: CircularProgressIndicator()),
      );
    }

    return Scaffold(
      appBar: CustomAppBar(
        title: 'Admin Dashboard',
        user: _currentUser!,
      ),
      drawer: AppDrawer(user: _currentUser!),
      body: _isLoading
          ? const Center(child: CircularProgressIndicator())
          : _errorMessage != null
              ? _buildErrorView()
              : _buildDashboardContent(),
    );
  }

  Widget _buildErrorView() {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          const Icon(Icons.error_outline, size: 64, color: Colors.red),
          const SizedBox(height: 16),
          Text(
            _errorMessage!,
            textAlign: TextAlign.center,
            style: const TextStyle(color: Colors.red),
          ),
          const SizedBox(height: 16),
          ElevatedButton(
            onPressed: _loadDashboardData,
            child: const Text('Retry'),
          ),
        ],
      ),
    );
  }

  Widget _buildDashboardContent() {
    return RefreshIndicator(
      onRefresh: _loadDashboardData,
      child: SingleChildScrollView(
        padding: const EdgeInsets.all(AppConstants.defaultPadding),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // System Overview
            Text(
              'System Overview',
              style: Theme.of(context).textTheme.headlineSmall?.copyWith(
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 16),
            
            // Metrics Grid
            ResponsiveGrid(
              mobileColumns: 2,
              tabletColumns: 3,
              desktopColumns: 3,
              spacing: 12,
              children: _buildMetricCards(),
            ),
            
            const SizedBox(height: 32),
            
            // Billing Model Distribution Chart
            SimplePieChart(
              title: 'Billing Model Distribution',
              data: _getBillingModelData(),
            ),
            
            const SizedBox(height: 32),
            
            // Vendor Assignment Section
            Text(
              'Vendor Assignment',
              style: Theme.of(context).textTheme.headlineSmall?.copyWith(
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 16),
            
            _buildVendorAssignmentSection(),
          ],
        ),
      ),
    );
  }

  List<Widget> _buildMetricCards() {
    if (_analytics == null) return [];

    return [
      StatCard(
        title: 'Total Clients',
        value: '${_analytics!['totalClients'] ?? 0}',
        icon: Icons.people,
        color: Colors.blue,
      ),
      StatCard(
        title: 'Total Vendors',
        value: '${_analytics!['totalVendors'] ?? 0}',
        icon: Icons.business,
        color: Colors.green,
      ),
      StatCard(
        title: 'Total Employees',
        value: '${_analytics!['totalEmployees'] ?? 0}',
        icon: Icons.badge,
        color: Colors.orange,
      ),
    ];
  }

  Widget _buildVendorAssignmentSection() {
    if (_clients == null || _vendors == null) {
      return const Center(child: Text('No data available'));
    }

    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text(
              'List Of Clients',
              style: TextStyle(
                fontSize: 16,
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 16),
            
            // Clients list
            if (_clients!.isEmpty)
              const Text('No clients found')
            else
              ListView.separated(
                shrinkWrap: true,
                physics: const NeverScrollableScrollPhysics(),
                itemCount: _clients!.length,
                separatorBuilder: (context, index) => const Divider(),
                itemBuilder: (context, index) {
                  final client = _clients![index];
                  
                  // Check for vendor assignment from backend
                  final assignedVendor = client['assignedVendor'];
                  final isAssigned = assignedVendor != null && assignedVendor is Map && assignedVendor.isNotEmpty;
                  
                  // Get vendor name from the backend response
                  String? vendorName;
                  if (isAssigned) {
                    vendorName = assignedVendor['name']?.toString();
                  } else {
                  }
                  
                  return ListTile(
                    leading: CircleAvatar(
                      child: Text(client['name'][0].toUpperCase()),
                    ),
                    title: Text(client['name']),
                    subtitle: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(client['email']),
                        if (client['preferredBillingModel'] != null)
                          Text(
                            'Prefers: ${client['preferredBillingModel']}',
                            style: TextStyle(
                              fontSize: 12,
                              color: Colors.blue[600],
                              fontWeight: FontWeight.w500,
                            ),
                          ),
                      ],
                    ),
                    trailing: Row(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        if (isAssigned)
                          Chip(
                            label: Text(vendorName ?? 'Assigned Vendor'),
                            backgroundColor: Colors.green[100],
                          )
                        else ...[
                          Chip(
                            label: const Text('Unassigned'),
                            backgroundColor: Colors.orange[100],
                          ),
                          const SizedBox(width: 8),
                          ElevatedButton(
                            onPressed: () => _navigateToAssignVendor(client),
                            style: ElevatedButton.styleFrom(
                              backgroundColor: Color(AppConstants.primaryColorValue),
                              foregroundColor: Colors.white,
                              padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                            ),
                            child: const Text('Assign'),
                          ),
                        ],
                      ],
                    ),
                  );
                },
              ),
          ],
        ),
      ),
    );
  }

  Future<void> _navigateToAssignVendor(Map<String, dynamic> client) async {
    final result = await Navigator.of(context).push(
      MaterialPageRoute(
        builder: (context) => AssignVendorScreen(client: client),
      ),
    );
    
    // If assignment was successful, refresh the dashboard
    if (result == true) {
      _loadDashboardData();
    }
  }

  void _showAssignVendorDialog() {
    if (_clients == null || _vendors == null) return;

    final unassignedClients = _clients!.where((client) {
      final assignedVendor = client['assignedVendor'] ?? client['vendor'];
      return assignedVendor == null || 
             (assignedVendor is Map && assignedVendor.isEmpty) ||
             (assignedVendor is String && assignedVendor.isEmpty);
    }).toList();
    
    if (unassignedClients.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('All clients are already assigned to vendors'),
          backgroundColor: Colors.blue,
        ),
      );
      return;
    }

    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Assign Vendors to Clients'),
        content: SizedBox(
          width: double.maxFinite,
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Text('${unassignedClients.length} unassigned client(s) found'),
              const SizedBox(height: 16),
              const Text('Click "Assign" next to each client to select compatible vendors based on their preferred billing model.'),
            ],
          ),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Close'),
          ),
        ],
      ),
    );
  }



  Map<String, double> _getBillingModelData() {
    if (_analytics != null && _analytics!['billingModelDistribution'] != null) {
      final distribution = _analytics!['billingModelDistribution'] as Map<String, dynamic>;
      return Map<String, double>.from(
        distribution.map((key, value) => MapEntry(key, (value as num).toDouble())),
      );
    }
    
    // Return sample data if no real data is available
    return {
      'PACKAGE': 3.0,
      'TRIP': 2.0,
      'HYBRID': 1.0,
    };
  }
}