import 'package:flutter/material.dart';
import '../services/api_service.dart';
import '../models/user.dart';
import '../utils/constants.dart';

class AssignVendorScreen extends StatefulWidget {
  final Map<String, dynamic> client;
  
  const AssignVendorScreen({Key? key, required this.client}) : super(key: key);

  @override
  State<AssignVendorScreen> createState() => _AssignVendorScreenState();
}

class _AssignVendorScreenState extends State<AssignVendorScreen> {
  late ApiService _apiService;
  
  List<dynamic> _availableVendors = [];
  List<dynamic> _selectedVendors = [];
  bool _isLoading = true;
  bool _isAssigning = false;
  String? _errorMessage;
  
  String? _clientBillingModel;

  @override
  void initState() {
    super.initState();
    _initServices();
  }

  Future<void> _initServices() async {
    try {
      _apiService = await ApiService.getInstance();
      _clientBillingModel = widget.client['preferredBillingModel'];
      await _loadAvailableVendors();
    } catch (e) {
      setState(() {
        _errorMessage = 'Failed to initialize: $e';
        _isLoading = false;
      });
    }
  }

  Future<void> _loadAvailableVendors() async {
    try {
      // Get all vendors
      final response = await _apiService.get('/admin/vendors');
      final allVendors = response.data as List;
      
      // Filter vendors by exact billing model match and rate availability
      final matchingVendors = allVendors.where((vendor) {
        final vendorBillingModel = vendor['preferredBillingModel'];
        final defaultPackageRate = vendor['defaultPackageRate'];
        final defaultTripRate = vendor['defaultTripRate'];
        
        // For HYBRID clients, only show HYBRID vendors with both rates
        if (_clientBillingModel == 'HYBRID') {
          return vendorBillingModel == 'HYBRID' &&
                 defaultPackageRate != null && 
                 defaultTripRate != null;
        }
        
        // For PACKAGE clients, only show PACKAGE vendors
        if (_clientBillingModel == 'PACKAGE') {
          return vendorBillingModel == 'PACKAGE';
        }
        
        // For TRIP clients, only show TRIP vendors with trip rates
        if (_clientBillingModel == 'TRIP') {
          return vendorBillingModel == 'TRIP' &&
                 defaultTripRate != null;
        }
        
        // Fallback: exact billing model match
        return vendorBillingModel == _clientBillingModel;
      }).toList();

      setState(() {
        _availableVendors = matchingVendors;
        _isLoading = false;
      });
    } catch (e) {
      setState(() {
        _errorMessage = 'Failed to load vendors: $e';
        _isLoading = false;
      });
    }
  }

  String _getFilterMessage() {
    switch (_clientBillingModel) {
      case 'HYBRID':
        return 'Showing only HYBRID vendors (fixed monthly + per-trip charges)';
      case 'PACKAGE':
        return 'Showing only PACKAGE vendors (fixed monthly rates)';
      case 'TRIP':
        return 'Showing only TRIP vendors (per-trip charges only)';
      default:
        return 'Showing vendors matching $_clientBillingModel billing model';
    }
  }

  String _getNoVendorsMessage() {
    switch (_clientBillingModel) {
      case 'HYBRID':
        return 'No HYBRID vendors available.\nHYBRID vendors offer fixed monthly rates plus per-trip charges.';
      case 'PACKAGE':
        return 'No PACKAGE vendors available.\nPACKAGE vendors offer fixed monthly rates only.';
      case 'TRIP':
        return 'No TRIP vendors available.\nTRIP vendors charge per-trip only.';
      default:
        return 'No vendors available for $_clientBillingModel billing model';
    }
  }



  Future<void> _assignVendors() async {
    if (_selectedVendors.isEmpty) {
      setState(() {
        _errorMessage = 'Please select at least one vendor';
      });
      return;
    }

    setState(() {
      _isAssigning = true;
      _errorMessage = null;
    });

    try {
      // Assign each selected vendor to the client
      for (final vendor in _selectedVendors) {
        // Use vendor's actual default rates
        final packageRate = vendor['defaultPackageRate'] ?? 0.0;
        final tripRate = vendor['defaultTripRate'] ?? 0.0;
        
        await _apiService.post('/admin/assign-vendor', data: {
          'clientId': widget.client['id'],
          'vendorId': vendor['id'],
          'billingModel': _clientBillingModel ?? 'PACKAGE',
          'packageRate': packageRate,
          'tripRate': tripRate,
          'standardDistanceLimit': 50.0, // Default distance limit in km
          'standardTimeLimit': 120.0, // Default time limit in minutes
          'employeeExtraDistanceRate': 2.0, // Default extra distance rate for employee
          'employeeExtraTimeRate': 1.5, // Default extra time rate for employee
          'vendorExtraDistanceRate': 3.0, // Default extra distance rate for vendor
          'vendorExtraTimeRate': 2.5, // Default extra time rate for vendor
          'estimatedVehiclesNeeded': 1, // Default vehicles needed
        });
      }

      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Successfully assigned ${_selectedVendors.length} vendor(s) to ${widget.client['name']}'),
            backgroundColor: Colors.green,
          ),
        );
        
        Navigator.of(context).pop(true); // Return success
      }
    } catch (e) {
      setState(() {
        _errorMessage = 'Failed to assign vendors: $e';
      });
    } finally {
      setState(() {
        _isAssigning = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Assign Vendors to ${widget.client['name']}'),
        backgroundColor: Color(AppConstants.primaryColorValue),
        foregroundColor: Colors.white,
      ),
      body: _isLoading
          ? const Center(child: CircularProgressIndicator())
          : _buildAssignmentForm(),
      bottomNavigationBar: _buildBottomBar(),
    );
  }

  Widget _buildAssignmentForm() {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(AppConstants.defaultPadding),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Client Info Card
          Card(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    'Client Information',
                    style: Theme.of(context).textTheme.titleLarge?.copyWith(
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                  const SizedBox(height: 8),
                  Text('Name: ${widget.client['name']}'),
                  Text('Email: ${widget.client['email']}'),
                  Text('Preferred Model: ${_clientBillingModel ?? 'Not specified'}'),
                ],
              ),
            ),
          ),
          
          const SizedBox(height: 16),
          
          // Available Vendors Section
          Text(
            'Available Vendors (${_availableVendors.length})',
            style: Theme.of(context).textTheme.titleLarge?.copyWith(
              fontWeight: FontWeight.bold,
            ),
          ),
          

          
          if (_clientBillingModel != null)
            Container(
              margin: const EdgeInsets.symmetric(vertical: 8),
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(
                color: Colors.blue[50],
                borderRadius: BorderRadius.circular(8),
                border: Border.all(color: Colors.blue[200]!),
              ),
              child: Row(
                children: [
                  Icon(Icons.info_outline, color: Colors.blue[700], size: 20),
                  const SizedBox(width: 8),
                  Expanded(
                    child: Text(
                      _getFilterMessage(),
                      style: TextStyle(color: Colors.blue[700]),
                    ),
                  ),
                ],
              ),
            ),
          
          const SizedBox(height: 16),
          
          // Error Message
          if (_errorMessage != null)
            Container(
              padding: const EdgeInsets.all(12),
              margin: const EdgeInsets.only(bottom: 16),
              decoration: BoxDecoration(
                color: Colors.red[50],
                borderRadius: BorderRadius.circular(AppConstants.borderRadius),
                border: Border.all(color: Colors.red[200]!),
              ),
              child: Row(
                children: [
                  Icon(Icons.error_outline, color: Colors.red[700], size: 20),
                  const SizedBox(width: 8),
                  Expanded(
                    child: Text(
                      _errorMessage!,
                      style: TextStyle(color: Colors.red[700]),
                    ),
                  ),
                ],
              ),
            ),
          
          // Vendors List
          if (_availableVendors.isEmpty)
            Card(
              child: Padding(
                padding: const EdgeInsets.all(32),
                child: Column(
                  children: [
                    Icon(
                      Icons.business_outlined,
                      size: 48,
                      color: Colors.grey[400],
                    ),
                    const SizedBox(height: 16),
                    Text(
                      'No compatible vendors found',
                      style: TextStyle(
                        fontSize: 16,
                        color: Colors.grey[600],
                      ),
                    ),
                    const SizedBox(height: 8),
                    Text(
                      _getNoVendorsMessage(),
                      style: TextStyle(
                        color: Colors.grey[500],
                      ),
                      textAlign: TextAlign.center,
                    ),
                  ],
                ),
              ),
            )
          else
            Card(
              child: Column(
                children: _availableVendors.map((vendor) {
                  final isSelected = _selectedVendors.any((v) => v['id'] == vendor['id']);
                  
                  return CheckboxListTile(
                    value: isSelected,
                    onChanged: (bool? value) {
                      setState(() {
                        if (value == true) {
                          _selectedVendors.add(vendor);
                        } else {
                          _selectedVendors.removeWhere((v) => v['id'] == vendor['id']);
                        }
                      });
                    },
                    title: Text(
                      vendor['name'],
                      style: const TextStyle(fontWeight: FontWeight.w500),
                    ),
                    subtitle: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text('Email: ${vendor['email']}'),
                        Text('Billing Model: ${vendor['preferredBillingModel'] ?? 'Not specified'}'),
                        if (vendor['defaultPackageRate'] != null)
                          Text('Package Rate: \$${vendor['defaultPackageRate']}/month'),
                        if (vendor['defaultTripRate'] != null)
                          Text('Trip Rate: \$${vendor['defaultTripRate']}/trip'),
                      ],
                    ),
                    secondary: CircleAvatar(
                      backgroundColor: Colors.blue[100],
                      child: Icon(
                        Icons.business,
                        color: Colors.blue[700],
                      ),
                    ),
                  );
                }).toList(),
              ),
            ),
        ],
      ),
    );
  }

  Widget _buildBottomBar() {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.white,
        boxShadow: [
          BoxShadow(
            color: Colors.grey.withOpacity(0.3),
            spreadRadius: 1,
            blurRadius: 5,
            offset: const Offset(0, -3),
          ),
        ],
      ),
      child: Row(
        children: [
          Expanded(
            child: Text(
              '${_selectedVendors.length} vendor(s) selected',
              style: const TextStyle(fontWeight: FontWeight.w500),
            ),
          ),
          ElevatedButton(
            onPressed: _isAssigning || _selectedVendors.isEmpty ? null : _assignVendors,
            style: ElevatedButton.styleFrom(
              backgroundColor: Color(AppConstants.primaryColorValue),
              foregroundColor: Colors.white,
              padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 12),
            ),
            child: _isAssigning
                ? const SizedBox(
                    width: 20,
                    height: 20,
                    child: CircularProgressIndicator(
                      strokeWidth: 2,
                      valueColor: AlwaysStoppedAnimation<Color>(Colors.white),
                    ),
                  )
                : const Text('Assign Vendors'),
          ),
        ],
      ),
    );
  }
}