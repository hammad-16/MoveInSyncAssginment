import 'package:flutter/material.dart';
import '../services/auth_service.dart';
import '../services/api_service.dart';
import '../models/user.dart';
import '../utils/constants.dart';
import '../widgets/common/index.dart';

class EmployeeDashboard extends StatefulWidget {
  const EmployeeDashboard({Key? key}) : super(key: key);

  @override
  State<EmployeeDashboard> createState() => _EmployeeDashboardState();
}

class _EmployeeDashboardState extends State<EmployeeDashboard> {
  late AuthService _authService;
  late ApiService _apiService;
  User? _currentUser;
  
  // Dashboard data
  Map<String, dynamic>? _employeeStats;
  List<dynamic>? _tripHistory;
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
      // Load employee profile data
      final profileResponse = await _apiService.get('/employee/profile', queryParameters: {
        'employeeId': _currentUser!.id,
      });
      
      // Load trip history
      final tripsResponse = await _apiService.get('/employee/trips', queryParameters: {
        'employeeId': _currentUser!.id,
      });

      setState(() {
        _employeeStats = profileResponse.data;
        _tripHistory = tripsResponse.data;
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
        title: 'Employee Dashboard',
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
            // Personal Metrics
            Text(
              'Personal Overview',
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
            
            // Trip History Section
            Text(
              'Trip History',
              style: Theme.of(context).textTheme.headlineSmall?.copyWith(
                fontWeight: FontWeight.bold,
              ),
            ),
            
            const SizedBox(height: 16),
            
            _buildTripHistorySection(),
          ],
        ),
      ),
    );
  }

  List<Widget> _buildMetricCards() {
    final monthlyTrips = _getMonthlyTripsCount();
    final totalTrips = _tripHistory?.length ?? 0;
    final avgDistance = _getAverageDistance();
    final avgDuration = _getAverageDuration();
    
    return [
      StatCard(
        title: 'Monthly Trips',
        value: '$monthlyTrips',
        icon: Icons.trip_origin,
        color: Colors.blue,
      ),
      StatCard(
        title: 'Total Trips',
        value: '$totalTrips',
        icon: Icons.history,
        color: Colors.green,
      ),
      StatCard(
        title: 'Avg Distance',
        value: '${avgDistance.toStringAsFixed(1)} km',
        icon: Icons.straighten,
        color: Colors.orange,
      ),
      StatCard(
        title: 'Avg Duration',
        value: '${avgDuration.toStringAsFixed(0)} min',
        icon: Icons.timer,
        color: Colors.purple,
      ),
    ];
  }

  int _getMonthlyTripsCount() {
    if (_tripHistory == null) return 0;
    
    final now = DateTime.now();
    final currentMonth = now.month;
    final currentYear = now.year;
    
    return _tripHistory!.where((trip) {
      try {
        final tripDate = DateTime.parse(trip['createdAt'] ?? trip['date'] ?? '');
        return tripDate.month == currentMonth && tripDate.year == currentYear;
      } catch (e) {
        return false;
      }
    }).length;
  }

  double _getTotalIncentive() {
    if (_tripHistory == null) return 0.0;
    
    return _tripHistory!.fold(0.0, (sum, trip) {
      final distance = (trip['distance'] ?? 0).toDouble();
      final duration = (trip['duration'] ?? 0).toDouble();
      return sum + (distance * 2.0) + (duration * 0.5);
    });
  }

  double _getMonthlyEarning() {
    if (_tripHistory == null) return 0.0;
    
    final now = DateTime.now();
    final currentMonth = now.month;
    final currentYear = now.year;
    
    return _tripHistory!.where((trip) {
      try {
        final tripDate = DateTime.parse(trip['createdAt'] ?? trip['date'] ?? '');
        return tripDate.month == currentMonth && tripDate.year == currentYear;
      } catch (e) {
        return false;
      }
    }).fold(0.0, (sum, trip) {
      final distance = (trip['distance'] ?? 0).toDouble();
      final duration = (trip['duration'] ?? 0).toDouble();
      return sum + (distance * 2.0) + (duration * 0.5);
    });
  }

  double _getAverageDistance() {
    if (_tripHistory == null || _tripHistory!.isEmpty) return 0.0;
    
    final totalDistance = _tripHistory!.fold(0.0, (sum, trip) {
      return sum + (trip['distance'] ?? 0).toDouble();
    });
    
    return totalDistance / _tripHistory!.length;
  }

  double _getAverageDuration() {
    if (_tripHistory == null || _tripHistory!.isEmpty) return 0.0;
    
    final totalDuration = _tripHistory!.fold(0.0, (sum, trip) {
      return sum + (trip['duration'] ?? 0).toDouble();
    });
    
    return totalDuration / _tripHistory!.length;
  }

  Widget _buildTripHistorySection() {
    if (_tripHistory == null || _tripHistory!.isEmpty) {
      return Card(
        child: Padding(
          padding: const EdgeInsets.all(32),
          child: Column(
            children: [
              Icon(
                Icons.history,
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
                'Your trip history will appear here',
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
                    'Destination',
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
                    'Distance',
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
            itemCount: _tripHistory!.length,
            separatorBuilder: (context, index) => const Divider(height: 1),
            itemBuilder: (context, index) {
              final trip = _tripHistory![index];
              
              return ListTile(
                leading: CircleAvatar(
                  backgroundColor: Colors.blue[100],
                  child: Icon(
                    Icons.location_on,
                    color: Colors.blue[700],
                    size: 20,
                  ),
                ),
                title: Text(
                  trip['pickupLocation'] ?? trip['destination'] ?? 'Trip #${index + 1}',
                  style: const TextStyle(fontWeight: FontWeight.w500),
                ),
                subtitle: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'Client: ${trip['clientName'] ?? _currentUser?.name ?? 'Your Company'}',
                      style: TextStyle(color: Colors.grey[600]),
                    ),
                    Text(
                      'Duration: ${trip['duration'] ?? 0} min',
                      style: TextStyle(color: Colors.grey[500], fontSize: 12),
                    ),
                  ],
                ),
                trailing: Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  crossAxisAlignment: CrossAxisAlignment.end,
                  children: [
                    Text(
                      '${trip['distance'] ?? 0} km',
                      style: const TextStyle(
                        fontWeight: FontWeight.bold,
                        color: Colors.blue,
                      ),
                    ),
                    Text(
                      _formatDate(trip['createdAt'] ?? trip['date']),
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
        ],
      ),
    );
  }

  String _formatDate(String? dateStr) {
    if (dateStr == null || dateStr.isEmpty) {
      // Use current date as fallback
      final now = DateTime.now();
      return '${now.day}/${now.month}/${now.year}';
    }
    try {
      final date = DateTime.parse(dateStr);
      return '${date.day}/${date.month}/${date.year}';
    } catch (e) {
      // If parsing fails, use current date
      final now = DateTime.now();
      return '${now.day}/${now.month}/${now.year}';
    }
  }

  String _calculateIncentive(Map<String, dynamic> trip) {
    // Calculate incentive based on distance and duration
    final distance = (trip['distance'] ?? 0).toDouble();
    final duration = (trip['duration'] ?? 0).toDouble();
    
    // Simple incentive calculation: $2 per km + $0.5 per minute
    final incentive = (distance * 2.0) + (duration * 0.5);
    
    return incentive.toStringAsFixed(2);
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
            _buildDetailRow('Pickup', trip['pickupLocation'] ?? 'Not specified'),
            _buildDetailRow('Destination', trip['destination'] ?? 'Not specified'),
            _buildDetailRow('Client', trip['clientName'] ?? _currentUser?.name ?? 'Your Company'),
            _buildDetailRow('Date', _formatDate(trip['createdAt'] ?? trip['date'])),
            _buildDetailRow('Distance', '${trip['distance'] ?? 0} km'),
            _buildDetailRow('Duration', '${trip['duration'] ?? 0} min'),
            _buildDetailRow('Status', trip['status'] ?? 'Completed'),
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

  Widget _buildDetailRow(String label, String value) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          SizedBox(
            width: 100,
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
}