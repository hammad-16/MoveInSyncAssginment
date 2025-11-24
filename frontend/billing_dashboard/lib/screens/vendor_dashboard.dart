import 'package:flutter/material.dart';
import '../services/auth_service.dart';
import '../services/api_service.dart';
import '../services/pdf_service.dart';
import '../models/user.dart';
import '../utils/constants.dart';
import '../widgets/common/index.dart';
import '../widgets/charts/index.dart';
import 'vendor_trips_screen.dart';

class VendorDashboard extends StatefulWidget {
  const VendorDashboard({Key? key}) : super(key: key);

  @override
  State<VendorDashboard> createState() => _VendorDashboardState();
}

class _VendorDashboardState extends State<VendorDashboard> {
  late AuthService _authService;
  late ApiService _apiService;
  User? _currentUser;
  
  // Dashboard data
  Map<String, dynamic>? _vendorStats;
  List<dynamic>? _recentTrips;
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
      
      // Check if user is actually a vendor
      if (_currentUser!.role != UserRole.VENDOR) {
        setState(() {
          _errorMessage = 'Access denied: You are not registered as a vendor. Current role: ${_currentUser!.role}';
          _isLoading = false;
        });
        return;
      }

      // Initialize with default data
      Map<String, dynamic> combinedStats = {
        'id': _currentUser!.id,
        'name': _currentUser!.name,
        'email': _currentUser!.email,
        'role': _currentUser!.role,
        'maxClientCapacity': 0,
        'currentLoad': 0,
        'availableCapacity': 0,
        'totalClients': 0,
        'totalTrips': 0,
        'monthlyEarnings': 0,
        'pendingPayouts': 0,
        'billingModel': 'PACKAGE',
        'packageRate': 100,
        'tripRate': 15,
        'extraDistanceRate': 3,
        'extraTimeRate': 2.5,
      };
      
      List<dynamic> recentTrips = [];
      
      // Try to load vendor profile data
      try {
        final profileResponse = await _apiService.get('/vendor/profile', queryParameters: {
          'vendorId': _currentUser!.id,
        });
        
        if (profileResponse.data != null) {
          combinedStats.addAll(Map<String, dynamic>.from(profileResponse.data));
        }
      } catch (profileError) {
        // Check if it's a 400 error (vendor not found or not a vendor)
        if (profileError.toString().contains('400')) {
          setState(() {
            _errorMessage = 'Vendor profile not found. You may need to complete your vendor registration or contact support.';
            _isLoading = false;
          });
          return;
        }
        // For other errors, continue with defaults

      }
      
      // Try to load vendor rates
      try {

        final ratesResponse = await _apiService.get('/vendor/rates', queryParameters: {
          'vendorId': _currentUser!.id,
        });

        if (ratesResponse.data != null) {
          combinedStats.addAll(Map<String, dynamic>.from(ratesResponse.data));
        }
      } catch (ratesError) {

        // Keep default rates
      }
      
      // Try to load recent trips
      try {

        final tripsResponse = await _apiService.get('/vendor/trips', queryParameters: {
          'vendorId': _currentUser!.id,
        });

        if (tripsResponse.data is List) {
          recentTrips = (tripsResponse.data as List).take(10).toList();
        }
      } catch (tripsError) {

        // Continue with empty trips list
      }

      // Try to load performance stats
      try {

        final performanceResponse = await _apiService.get('/vendor/reports/performance', queryParameters: {
          'vendorId': _currentUser!.id,
        });

        if (performanceResponse.data != null) {
          final perfData = Map<String, dynamic>.from(performanceResponse.data);
          // Override the default values with actual performance data
          combinedStats['totalTrips'] = perfData['totalTrips'] ?? 0;
          combinedStats['monthlyEarnings'] = perfData['monthlyEarnings'] ?? 0;
          combinedStats['totalEarnings'] = perfData['totalEarnings'] ?? 0;
          combinedStats['averageEarningsPerTrip'] = perfData['averageEarningsPerTrip'] ?? 0;
          combinedStats['recentTrips'] = perfData['recentTrips'] ?? 0;
        }
      } catch (performanceError) {

        // Continue with default values
      }

      // Calculate derived stats
      combinedStats['totalClients'] = combinedStats['currentLoad'] ?? 0;

      setState(() {
        _vendorStats = combinedStats;
        _recentTrips = recentTrips;
        _isLoading = false;
      });
      

    } catch (e) {

      setState(() {
        _errorMessage = 'Failed to load dashboard: ${e.toString()}. Please check your internet connection and try again.';
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
        title: 'Vendor Dashboard',
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
            // Business Metrics
            Text(
              'Business Overview',
              style: Theme.of(context).textTheme.headlineSmall?.copyWith(
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 16),
            
            // Metrics Grid
            ResponsiveGrid(
              mobileColumns: 2,
              tabletColumns: 4,
              desktopColumns: 4,
              spacing: 12,
              children: _buildMetricCards(),
            ),
            
            const SizedBox(height: 32),
            
            // Performance Chart with proper scaling
            if (_vendorStats != null)
              SimpleBarChart(
                title: 'Monthly Performance Overview',
                data: _getChartData(),
              ),
            
            const SizedBox(height: 32),
            
            // Quick Actions Section
            Row(
              children: [
                Expanded(
                  child: Card(
                    child: InkWell(
                      onTap: _showRateManagementDialog,
                      borderRadius: BorderRadius.circular(8),
                      child: Padding(
                        padding: const EdgeInsets.all(16),
                        child: Column(
                          children: [
                            Icon(Icons.edit, size: 32, color: Colors.blue[700]),
                            const SizedBox(height: 8),
                            const Text(
                              'Update Rates',
                              textAlign: TextAlign.center,
                              style: TextStyle(fontWeight: FontWeight.w500),
                            ),
                          ],
                        ),
                      ),
                    ),
                  ),
                ),
                const SizedBox(width: 16),
                Expanded(
                  child: Card(
                    child: InkWell(
                      onTap: _generatePdfReport,
                      borderRadius: BorderRadius.circular(8),
                      child: Padding(
                        padding: const EdgeInsets.all(16),
                        child: Column(
                          children: [
                            Icon(Icons.analytics_outlined, size: 32, color: Colors.green[700]),
                            const SizedBox(height: 8),
                            const Text(
                              'View Reports',
                              textAlign: TextAlign.center,
                              style: TextStyle(fontWeight: FontWeight.w500),
                            ),
                          ],
                        ),
                      ),
                    ),
                  ),
                ),
              ],
            ),
            
            const SizedBox(height: 32),
            
            // Rate Management Section
            Text(
              'Rate Management',
              style: Theme.of(context).textTheme.headlineSmall?.copyWith(
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 16),
            
            _buildRateManagementSection(),
            
            const SizedBox(height: 32),
            
            // Recent Trips Section
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

  List<Widget> _buildMetricCards() {
    if (_vendorStats == null) return [];

    return [
      StatCard(
        title: 'Active Clients',
        value: '${_vendorStats!['totalClients'] ?? 0}',
        icon: Icons.people,
        color: Colors.blue,
      ),
      StatCard(
        title: 'Total Trips',
        value: '${_vendorStats!['totalTrips'] ?? 0}',
        icon: Icons.trip_origin,
        color: Colors.green,
      ),
      StatCard(
        title: 'Monthly Earnings',
        value: '\$${_vendorStats!['monthlyEarnings'] ?? 0}',
        icon: Icons.attach_money,
        color: Colors.orange,
      ),
      StatCard(
        title: 'Total Earnings',
        value: '\$${_vendorStats!['totalEarnings'] ?? 0}',
        icon: Icons.payment,
        color: Colors.purple,
      ),
    ];
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
                'No trips found',
                style: TextStyle(
                  fontSize: 16,
                  color: Colors.grey[600],
                ),
              ),
              const SizedBox(height: 8),
              Text(
                'Your completed trips will appear here',
                style: TextStyle(
                  color: Colors.grey[500],
                ),
              ),
            ],
          ),
        ),
      );
    }

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
                    'Client',
                    style: Theme.of(context).textTheme.titleSmall?.copyWith(
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ),
                Expanded(
                  child: Text(
                    'Date',
                    style: Theme.of(context).textTheme.titleSmall?.copyWith(
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ),
                Expanded(
                  child: Text(
                    'Amount',
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
            itemCount: _recentTrips!.length,
            separatorBuilder: (context, index) => const Divider(height: 1),
            itemBuilder: (context, index) {
              final trip = _recentTrips![index];
              return ListTile(
                leading: CircleAvatar(
                  backgroundColor: Colors.green[100],
                  child: Icon(
                    Icons.trip_origin,
                    color: Colors.green[700],
                    size: 20,
                  ),
                ),
                title: Text(
                  trip['clientName'] ?? 'Unknown Client',
                  style: const TextStyle(fontWeight: FontWeight.w500),
                ),
                subtitle: Text(
                  trip['destination'] ?? 'No destination',
                  style: TextStyle(color: Colors.grey[600]),
                ),
                trailing: Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  crossAxisAlignment: CrossAxisAlignment.end,
                  children: [
                    Text(
                      '\$${trip['amount'] ?? 0}',
                      style: const TextStyle(
                        fontWeight: FontWeight.bold,
                        color: Colors.green,
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
                onTap: () {
                  _showTripDetails(trip);
                },
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

  void _showTripDetails(Map<String, dynamic> trip) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Trip Details'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            _buildDetailRow('Client', trip['clientName'] ?? 'Unknown'),
            _buildDetailRow('Destination', trip['destination'] ?? 'N/A'),
            _buildDetailRow('Date', _formatDate(trip['date'])),
            _buildDetailRow('Amount', '\$${trip['amount'] ?? 0}'),
            _buildDetailRow('Status', trip['status'] ?? 'Unknown'),
          ],
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

  Widget _buildRateManagementSection() {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text(
              'Default Billing Model & Rates',
              style: TextStyle(
                fontSize: 16,
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 16),
            
            // Current rates display
            if (_vendorStats != null) ...[
              _buildRateRow('Billing Model', _getBillingModelDescription(_vendorStats!['billingModel'] ?? 'Not Set')),
              // Package Model: Only monthly rate
              if (_vendorStats!['billingModel'] == 'PACKAGE')
                _buildRateRow('Monthly Package Rate', '\$${_vendorStats!['packageRate'] ?? 0}/month (all-inclusive)'),
              
              // Trip Model: Only per-trip rate
              if (_vendorStats!['billingModel'] == 'TRIP')
                _buildRateRow('Per Trip Rate', '\$${_vendorStats!['tripRate'] ?? 0}/trip'),
              
              // Hybrid Model: Both rates
              if (_vendorStats!['billingModel'] == 'HYBRID') ...[ 
                _buildRateRow('Monthly Base Rate', '\$${_vendorStats!['packageRate'] ?? 0}/month'),
                _buildRateRow('Additional Per Trip', '\$${_vendorStats!['tripRate'] ?? 0}/trip'),
              ],
              
              // Separator for overage rates
              const SizedBox(height: 8),
              _buildRateRow('Extra Distance Rate', '\$${_vendorStats!['extraDistanceRate'] ?? 0}/km'),
              _buildRateRow('Extra Time Rate', '\$${_vendorStats!['extraTimeRate'] ?? 0}/min'),
            ] else
              const Text('Rate information not available'),
            

          ],
        ),
      ),
    );
  }

  Widget _buildRateRow(String label, String value) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(
            label,
            style: const TextStyle(fontWeight: FontWeight.w500),
          ),
          Text(
            value,
            style: const TextStyle(color: Colors.green, fontWeight: FontWeight.bold),
          ),
        ],
      ),
    );
  }

  void _showRateManagementDialog() {
    final packageRateController = TextEditingController(
      text: _vendorStats?['packageRate']?.toString() ?? '100',
    );
    final tripRateController = TextEditingController(
      text: _vendorStats?['tripRate']?.toString() ?? '15',
    );
    final extraDistanceController = TextEditingController(
      text: _vendorStats?['extraDistanceRate']?.toString() ?? '3',
    );
    final extraTimeController = TextEditingController(
      text: _vendorStats?['extraTimeRate']?.toString() ?? '2.5',
    );
    
    final currentBillingModel = _vendorStats?['billingModel'] ?? 'PACKAGE';

    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Update Your Rates'),
        content: SizedBox(
          width: double.maxFinite,
          child: SingleChildScrollView(
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                // Current Billing Model Display (read-only)
                Container(
                  padding: const EdgeInsets.all(12),
                  decoration: BoxDecoration(
                    color: Colors.grey[50],
                    borderRadius: BorderRadius.circular(8),
                    border: Border.all(color: Colors.grey[300]!),
                  ),
                  child: Row(
                    children: [
                      Icon(Icons.info_outline, size: 16, color: Colors.grey[600]),
                      const SizedBox(width: 8),
                      Text(
                        'Current Billing Model: ',
                        style: TextStyle(fontWeight: FontWeight.w500, color: Colors.grey[700]),
                      ),
                      Text(
                        _getBillingModelDescription(currentBillingModel),
                        style: TextStyle(fontWeight: FontWeight.bold, color: Colors.blue[700]),
                      ),
                    ],
                  ),
                ),
                const SizedBox(height: 16),
                

                
                // Package Rate (only for PACKAGE and HYBRID models)
                if (currentBillingModel == 'PACKAGE' || currentBillingModel == 'HYBRID') ...[
                  TextFormField(
                    controller: packageRateController,
                    decoration: InputDecoration(
                      labelText: currentBillingModel == 'PACKAGE' 
                        ? 'Monthly Package Rate (\$/month)' 
                        : 'Base Monthly Rate (\$/month)',
                      border: const OutlineInputBorder(),
                      prefixText: '\$ ',
                      helperText: currentBillingModel == 'PACKAGE'
                        ? 'Fixed monthly rate covering all trips'
                        : 'Base monthly rate + per-trip charges',
                    ),
                    keyboardType: TextInputType.number,
                  ),
                  const SizedBox(height: 16),
                ],
                
                // Trip Rate (only for TRIP and HYBRID models)
                if (currentBillingModel == 'TRIP' || currentBillingModel == 'HYBRID') ...[
                  TextFormField(
                    controller: tripRateController,
                    decoration: InputDecoration(
                      labelText: currentBillingModel == 'TRIP'
                        ? 'Per Trip Rate (\$/trip)'
                        : 'Additional Per Trip Rate (\$/trip)',
                      border: const OutlineInputBorder(),
                      prefixText: '\$ ',
                      helperText: currentBillingModel == 'TRIP'
                        ? 'Charge per individual trip'
                        : 'Additional charge per trip on top of monthly base',
                    ),
                    keyboardType: TextInputType.number,
                  ),
                  const SizedBox(height: 16),
                ],
                
                // Extra Distance Rate
                TextFormField(
                  controller: extraDistanceController,
                  decoration: const InputDecoration(
                    labelText: 'Extra Distance Rate (\$/km)',
                    border: OutlineInputBorder(),
                    prefixText: '\$ ',
                  ),
                  keyboardType: TextInputType.number,
                ),
                const SizedBox(height: 16),
                
                // Extra Time Rate
                TextFormField(
                  controller: extraTimeController,
                  decoration: const InputDecoration(
                    labelText: 'Extra Time Rate (\$/min)',
                    border: OutlineInputBorder(),
                    prefixText: '\$ ',
                  ),
                  keyboardType: TextInputType.number,
                ),
                

              ],
            ),
          ),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Cancel'),
          ),
          ElevatedButton(
            onPressed: () async {
              try {

                await _apiService.put('/vendor/rates', data: {
                  'vendorId': _currentUser!.id,
                  'billingModel': currentBillingModel,
                  'packageRate': double.parse(packageRateController.text),
                  'tripRate': double.parse(tripRateController.text),
                  'extraDistanceRate': double.parse(extraDistanceController.text),
                  'extraTimeRate': double.parse(extraTimeController.text),
                });
                
                Navigator.pop(context);
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(
                    content: Text('Rates updated successfully'),
                    backgroundColor: Colors.green,
                  ),
                );
                
                // Reload dashboard data
                await _loadDashboardData();
              } catch (e) {

                String errorMessage = 'Failed to update rates';
                
                if (e.toString().contains('400')) {
                  errorMessage = 'Invalid vendor data. Please contact support.';
                } else if (e.toString().contains('401')) {
                  errorMessage = 'Authentication required. Please login again.';
                } else if (e.toString().contains('403')) {
                  errorMessage = 'Access denied. You may not have vendor permissions.';
                } else if (e.toString().contains('500')) {
                  errorMessage = 'Server error. Please try again later.';
                } else {
                  errorMessage = 'Network error. Please check your connection.';
                }
                
                ScaffoldMessenger.of(context).showSnackBar(
                  SnackBar(
                    content: Text(errorMessage),
                    backgroundColor: Colors.red,
                    duration: const Duration(seconds: 4),
                  ),
                );
              }
            },
            child: const Text('Update Rates'),
          ),
        ],
      ),
    );
  }

  Widget _buildDetailRow(String label, String value) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          SizedBox(
            width: 80,
            child: Text(
              '$label:',
              style: const TextStyle(fontWeight: FontWeight.bold),
            ),
          ),
          Expanded(child: Text(value)),
        ],
      ),
    );
  }

  Widget _buildPerformanceMetric(String title, String value, String subtitle, Color color, IconData icon) {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: color.withOpacity(0.1),
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: color.withOpacity(0.3)),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Icon(icon, color: color, size: 20),
              const SizedBox(width: 8),
              Expanded(
                child: Text(
                  title,
                  style: TextStyle(
                    fontSize: 12,
                    color: color.withOpacity(0.8),
                    fontWeight: FontWeight.w500,
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(height: 8),
          Text(
            value,
            style: TextStyle(
              fontSize: 24,
              fontWeight: FontWeight.bold,
              color: color,
            ),
          ),
          const SizedBox(height: 4),
          Text(
            subtitle,
            style: TextStyle(
              fontSize: 10,
              color: Colors.grey[600],
            ),
          ),
        ],
      ),
    );
  }

  String _calculateAverageEarningsPerTrip() {
    if (_vendorStats == null) return '0.00';
    
    final totalTrips = _vendorStats!['totalTrips'] ?? 0;
    final monthlyEarnings = _vendorStats!['monthlyEarnings'] ?? 0;
    
    if (totalTrips == 0) return '0.00';
    
    final average = monthlyEarnings / totalTrips;
    return average.toStringAsFixed(2);
  }

  Map<String, double> _getChartData() {
    if (_vendorStats == null) return {};
    
    final clients = (_vendorStats!['currentLoad'] ?? 0).toDouble();
    final trips = (_vendorStats!['totalTrips'] ?? 0).toDouble();
    final earnings = (_vendorStats!['monthlyEarnings'] ?? 0).toDouble();
    
    // Find the maximum value to determine scaling
    final maxValue = [clients, trips, earnings].reduce((a, b) => a > b ? a : b);
    
    // If earnings are much larger than other values, scale them down for better visualization
    double scaledEarnings = earnings;
    String earningsLabel = 'Earnings (\$)';
    
    if (earnings > 0 && (earnings > trips * 5 || earnings > clients * 10)) {
      // Scale earnings to be more proportional
      if (earnings >= 100) {
        scaledEarnings = earnings / 10;
        earningsLabel = 'Earnings (×10\$)';
      } else if (earnings >= 50) {
        scaledEarnings = earnings / 5;
        earningsLabel = 'Earnings (×5\$)';
      } else {
        scaledEarnings = earnings / 2;
        earningsLabel = 'Earnings (×2\$)';
      }
    }
    
    return {
      'Clients': clients,
      'Trips': trips,
      earningsLabel: scaledEarnings,
    };
  }

  Future<void> _generatePdfReport() async {
    if (_vendorStats == null || _currentUser == null) {
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

      // Get client list for the vendor
      List<dynamic> clients = [];
      try {
        final clientsResponse = await _apiService.get('/vendor/clients', queryParameters: {
          'vendorId': _currentUser!.id,
        });
        clients = clientsResponse.data ?? [];
      } catch (e) {

      }

      await PdfService.generateVendorReport(
        user: _currentUser!,
        vendorStats: _vendorStats!,
        recentTrips: _recentTrips ?? [],
        clients: clients,
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

  void _navigateToAllTrips() {
    Navigator.of(context).push(
      MaterialPageRoute(builder: (context) => const VendorTripsScreen()),
    );
  }

  String _getBillingModelDescription(String billingModel) {
    switch (billingModel.toUpperCase()) {
      case 'PACKAGE':
        return 'Package Model (Fixed monthly rate)';
      case 'TRIP':
        return 'Trip Model (Per trip charging)';
      case 'HYBRID':
        return 'Hybrid Model (Monthly + per trip)';
      default:
        return billingModel;
    }
  }

  String _getBillingModelExplanation(String billingModel) {
    switch (billingModel.toUpperCase()) {
      case 'PACKAGE':
        return '• Package Model: Clients pay a fixed monthly rate that covers ALL trips\n'
               '• No per-trip charges whatsoever (only overages for extra distance/time)\n'
               '• Predictable monthly billing - clients know exact cost upfront';
      case 'TRIP':
        return '• Trip Model: Clients pay ONLY for individual trips taken\n'
               '• No monthly base rate or package fee - pure pay-per-use\n'
               '• Cost varies entirely based on actual trip usage';
      case 'HYBRID':
        return '• Hybrid Model: Clients pay monthly base rate PLUS additional per-trip charges\n'
               '• Monthly base fee + extra cost for each trip taken\n'
               '• Combines fixed monthly cost with variable trip-based billing';
      default:
        return 'Select a billing model to see explanation';
    }
  }
}