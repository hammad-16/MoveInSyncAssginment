import 'package:flutter/material.dart';
import '../services/auth_service.dart';
import '../services/api_service.dart';
import '../services/pdf_service.dart';
import '../models/user.dart';
import '../utils/constants.dart';
import '../widgets/common/index.dart';
import 'book_trip_screen.dart';
import 'client_trips_screen.dart';

class ClientDashboard extends StatefulWidget {
  const ClientDashboard({Key? key}) : super(key: key);

  @override
  State<ClientDashboard> createState() => _ClientDashboardState();
}

class _ClientDashboardState extends State<ClientDashboard> {
  late AuthService _authService;
  late ApiService _apiService;
  User? _currentUser;
  
  // Dashboard data
  Map<String, dynamic>? _dashboardSummary;
  List<dynamic>? _recentTrips;
  List<dynamic>? _employees;
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
      // Load dashboard summary
      final summaryResponse = await _apiService.get('/client/dashboard', queryParameters: {
        'clientId': _currentUser!.id,
      });
      
      // Load employees
      final employeesResponse = await _apiService.get('/client/employees', queryParameters: {
        'clientId': _currentUser!.id,
      });
      
      // Load vendors
      final vendorsResponse = await _apiService.get('/client/vendors', queryParameters: {
        'clientId': _currentUser!.id,
      });
      
      // Load recent trips
      final tripsResponse = await _apiService.get('/client/trips', queryParameters: {
        'clientId': _currentUser!.id,
      });

      setState(() {
        _dashboardSummary = summaryResponse.data;
        _employees = employeesResponse.data;
        _vendors = vendorsResponse.data;
        _recentTrips = tripsResponse.data;
        _isLoading = false;
      });
    } catch (e) {
      setState(() {
        _errorMessage = 'Failed to load dashboard data: $e';
        _isLoading = false;
      });
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
        title: 'Client Dashboard',
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
            // Welcome Section
            Text(
              'Welcome, ${_currentUser!.name}',
              style: Theme.of(context).textTheme.headlineSmall?.copyWith(
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 8),
            Text(
              'Client Dashboard Overview',
              style: Theme.of(context).textTheme.bodyLarge?.copyWith(
                color: Colors.grey[600],
              ),
            ),
            const SizedBox(height: 24),
            
            // Summary Metrics
            ResponsiveGrid(
              mobileColumns: 2,
              tabletColumns: 4,
              desktopColumns: 4,
              spacing: 12,
              children: _buildSummaryCards(),
            ),
            
            const SizedBox(height: 32),
            
            // Set of Actions
            Text(
              'What would you like to do?',
              style: Theme.of(context).textTheme.headlineSmall?.copyWith(
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 16),
            
            _buildQuickActions(),
            
            const SizedBox(height: 32),
            
            // Recent Activity
            Text(
              'Recent Trips',
              style: Theme.of(context).textTheme.headlineSmall?.copyWith(
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 16),
            
            _buildRecentTripsSection(),
          ],
        ),
      ),
    );
  }

  List<Widget> _buildSummaryCards() {
    if (_dashboardSummary == null) return [];

    return [
      StatCard(
        title: 'Total Employees',
        value: '${_dashboardSummary!['totalEmployees'] ?? 0}',
        icon: Icons.people,
        color: Colors.blue,
      ),
      StatCard(
        title: 'Assigned Vendors',
        value: '${_dashboardSummary!['assignedVendors'] ?? 0}',
        icon: Icons.business,
        color: Colors.green,
      ),
      StatCard(
        title: 'Total Trips',
        value: '${_dashboardSummary!['totalTrips'] ?? 0}',
        icon: Icons.trip_origin,
        color: Colors.orange,
      ),
      StatCard(
        title: 'Recent Trips (30d)',
        value: '${_dashboardSummary!['recentTrips'] ?? 0}',
        icon: Icons.history,
        color: Colors.purple,
      ),
    ];
  }

  Widget _buildQuickActions() {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Row(
          children: [
            Expanded(
              child: _buildActionButton(
                'Book Trip',
                Icons.add_location_alt,
                _navigateToBookTrip,
              ),
            ),
            const SizedBox(width: 16),
            Expanded(
              child: _buildActionButton(
                'View Reports',
                Icons.analytics_outlined,
                _generatePdfReport,
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildActionButton(String title, IconData icon, VoidCallback onTap) {
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(8),
      child: Container(
        padding: const EdgeInsets.all(16),
        decoration: BoxDecoration(
          border: Border.all(color: Colors.grey[300]!),
          borderRadius: BorderRadius.circular(8),
        ),
        child: Column(
          children: [
            Icon(icon, size: 32, color: Colors.blue[700]),
            const SizedBox(height: 8),
            Text(
              title,
              textAlign: TextAlign.center,
              style: const TextStyle(fontWeight: FontWeight.w500),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildRecentTripsSection() {
    if (_recentTrips == null || _recentTrips!.isEmpty) {
      return Card(
        child: Padding(
          padding: const EdgeInsets.all(32),
          child: Column(
            children: [
              Icon(
                Icons.trip_origin,
                size: 48,
                color: Colors.grey[400],
              ),
              const SizedBox(height: 16),
              Text(
                'No recent trips',
                style: TextStyle(
                  fontSize: 16,
                  color: Colors.grey[600],
                ),
              ),
              const SizedBox(height: 8),
              Text(
                'Employee trips will appear here',
                style: TextStyle(
                  color: Colors.grey[500],
                ),
              ),
            ],
          ),
        ),
      );
    }

    // Show only the first 5 trips
    final displayTrips = _recentTrips!.take(5).toList();

    return Card(
      child: Column(
        children: [
          // Header
          Container(
            padding: const EdgeInsets.all(16),
            decoration: BoxDecoration(
              color: Colors.grey[50],
              borderRadius: const BorderRadius.only(
                topLeft: Radius.circular(8),
                topRight: Radius.circular(8),
              ),
            ),
            child: Row(
              children: [
                Expanded(
                  flex: 2,
                  child: Text(
                    'Employee',
                    style: Theme.of(context).textTheme.titleSmall?.copyWith(
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ),
                Expanded(
                  child: Text(
                    'Vendor',
                    style: Theme.of(context).textTheme.titleSmall?.copyWith(
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ),
                Expanded(
                  child: Text(
                    'Date',
                    textAlign: TextAlign.right,
                    style: Theme.of(context).textTheme.titleSmall?.copyWith(
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ),
              ],
            ),
          ),
          
          // Trips List
          ListView.separated(
            shrinkWrap: true,
            physics: const NeverScrollableScrollPhysics(),
            itemCount: displayTrips.length,
            separatorBuilder: (context, index) => const Divider(height: 1),
            itemBuilder: (context, index) {
              final trip = displayTrips[index];
              return ListTile(
                leading: CircleAvatar(
                  backgroundColor: Colors.blue[100],
                  child: Icon(
                    Icons.person,
                    color: Colors.blue[700],
                    size: 20,
                  ),
                ),
                title: Text(
                  trip['employeeName'] ?? 'Unknown Employee',
                  style: const TextStyle(fontWeight: FontWeight.w500),
                ),
                subtitle: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'Distance: ${trip['distance'] ?? 0} km • Duration: ${trip['duration'] ?? 0} min',
                      style: TextStyle(color: Colors.grey[600]),
                    ),
                    Text(
                      'Cost: \$${trip['totalCost'] ?? 0} • Vendor: ${trip['vendorName'] ?? 'Unknown'}',
                      style: TextStyle(color: Colors.green[700], fontSize: 12, fontWeight: FontWeight.w500),
                    ),
                  ],
                ),
                trailing: Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  crossAxisAlignment: CrossAxisAlignment.end,
                  children: [
                    Text(
                      trip['billingModel'] ?? 'PACKAGE',
                      style: TextStyle(
                        fontWeight: FontWeight.w500,
                        color: Colors.blue[700],
                        fontSize: 12,
                      ),
                    ),
                    Text(
                      _formatDate(trip['date']),
                      style: TextStyle(
                        fontSize: 12,
                        color: Colors.grey[600],
                      ),
                    ),
                  ],
                ),
              );
            },
          ),
          
          // View All Button
          if (_recentTrips!.length > 5)
            Container(
              width: double.infinity,
              padding: const EdgeInsets.all(16),
              child: TextButton(
                onPressed: _navigateToAllTrips,
                child: Text('View All ${_recentTrips!.length} Trips'),
              ),
            ),
        ],
      ),
    );
  }

  String _formatDate(String? dateStr) {
    if (dateStr == null) return 'Unknown';
    try {
      final date = DateTime.parse(dateStr);
      return '${date.day}/${date.month}/${date.year}';
    } catch (e) {
      return 'Invalid date';
    }
  }

  void _navigateToBookTrip() async {
    final result = await Navigator.of(context).push(
      MaterialPageRoute(builder: (context) => const BookTripScreen()),
    );
    
    // If trip was booked successfully, refresh the dashboard
    if (result == true) {
      _loadDashboardData();
    }
  }

  void _navigateToAllTrips() {
    Navigator.of(context).push(
      MaterialPageRoute(builder: (context) => const ClientTripsScreen()),
    );
  }

  Future<void> _generatePdfReport() async {
    if (_dashboardSummary == null || _currentUser == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Dashboard data not available'),
          backgroundColor: Colors.red,
        ),
      );
      return;
    }

    try {
      // Show loading indicator
      showDialog(
        context: context,
        barrierDismissible: false,
        builder: (context) => const AlertDialog(
          content: Row(
            children: [
              CircularProgressIndicator(),
              SizedBox(width: 16),
              Text('Generating PDF report...'),
            ],
          ),
        ),
      );

      await PdfService.generateClientReport(
        user: _currentUser!,
        dashboardSummary: _dashboardSummary!,
        recentTrips: _recentTrips ?? [],
        employees: _employees ?? [],
        vendors: _vendors ?? [],
      );

      // Close loading dialog
      if (mounted) Navigator.pop(context);

      // Show success message
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('PDF report generated successfully!'),
            backgroundColor: Colors.green,
          ),
        );
      }
    } catch (e) {
      // Close loading dialog
      if (mounted) Navigator.pop(context);
      
      // Show error message
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Failed to generate PDF: $e'),
            backgroundColor: Colors.red,
          ),
        );
      }
    }
  }

  void _showComingSoon(String feature) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text('$feature coming soon!'),
        backgroundColor: Colors.blue,
      ),
    );
  }
}