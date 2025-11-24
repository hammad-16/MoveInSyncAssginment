import 'package:flutter/material.dart';
import '../services/auth_service.dart';
import '../services/api_service.dart';
import '../models/user.dart';
import '../utils/constants.dart';

class BookTripScreen extends StatefulWidget {
  const BookTripScreen({Key? key}) : super(key: key);

  @override
  State<BookTripScreen> createState() => _BookTripScreenState();
}

class _BookTripScreenState extends State<BookTripScreen> {
  final _formKey = GlobalKey<FormState>();
  final _distanceController = TextEditingController();
  final _durationController = TextEditingController();
  
  late AuthService _authService;
  late ApiService _apiService;
  User? _currentUser;
  
  List<dynamic> _employees = [];
  List<dynamic> _vendors = [];
  int? _selectedEmployeeId;
  int? _selectedVendorId;
  DateTime _selectedDate = DateTime.now();
  
  bool _isLoading = false;
  bool _loadingData = true;
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
        await _loadData();
      }
    } catch (e) {
      setState(() {
        _errorMessage = 'Failed to initialize: $e';
        _loadingData = false;
      });
    }
  }

  Future<void> _loadData() async {
    try {
      // Load employees
      final employeesResponse = await _apiService.get('/client/employees', queryParameters: {
        'clientId': _currentUser!.id,
      });
      
      // Load vendors
      final vendorsResponse = await _apiService.get('/client/vendors', queryParameters: {
        'clientId': _currentUser!.id,
      });

      setState(() {
        _employees = employeesResponse.data ?? [];
        _vendors = vendorsResponse.data ?? [];
        _loadingData = false;
      });
    } catch (e) {
      setState(() {
        _errorMessage = 'Failed to load data: $e';
        _loadingData = false;
      });
    }
  }

  Future<void> _bookTrip() async {
    if (!_formKey.currentState!.validate()) return;
    
    if (_selectedEmployeeId == null) {
      setState(() {
        _errorMessage = 'Please select an employee';
      });
      return;
    }
    
    if (_selectedVendorId == null) {
      setState(() {
        _errorMessage = 'Please select a vendor';
      });
      return;
    }

    setState(() {
      _isLoading = true;
      _errorMessage = null;
    });

    try {
      final response = await _apiService.post('/client/trips/book', data: {
        'clientId': _currentUser!.id,
        'employeeId': _selectedEmployeeId,
        'vendorId': _selectedVendorId,
        'distance': double.parse(_distanceController.text),
        'duration': double.parse(_durationController.text),
        'tripDate': _selectedDate.toIso8601String().split('T')[0],
      });

      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('Trip booked successfully!'),
            backgroundColor: Colors.green,
          ),
        );
        
        Navigator.of(context).pop(true); // Return true to indicate success
      }
    } catch (e) {
      setState(() {
        _errorMessage = 'Failed to book trip: $e';
      });
    } finally {
      setState(() {
        _isLoading = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Book Trip'),
        backgroundColor: Color(AppConstants.primaryColorValue),
        foregroundColor: Colors.white,
      ),
      body: _loadingData
          ? const Center(child: CircularProgressIndicator())
          : _buildBookingForm(),
    );
  }

  Widget _buildBookingForm() {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(AppConstants.defaultPadding),
      child: Form(
        key: _formKey,
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Card(
              child: Padding(
                padding: const EdgeInsets.all(16),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'Trip Details',
                      style: Theme.of(context).textTheme.titleLarge?.copyWith(
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                    const SizedBox(height: 16),
                    
                    // Employee Selection
                    DropdownButtonFormField<int>(
                      value: _selectedEmployeeId,
                      decoration: InputDecoration(
                        labelText: 'Select Employee',
                        prefixIcon: const Icon(Icons.person),
                        border: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(AppConstants.borderRadius),
                        ),
                      ),
                      items: _employees.map<DropdownMenuItem<int>>((employee) {
                        return DropdownMenuItem<int>(
                          value: employee['employee']['id'],
                          child: Text(employee['employee']['name']),
                        );
                      }).toList(),
                      onChanged: (int? value) {
                        setState(() {
                          _selectedEmployeeId = value;
                        });
                      },
                      validator: (value) {
                        if (value == null) return 'Please select an employee';
                        return null;
                      },
                    ),
                    const SizedBox(height: 16),
                    
                    // Vendor Selection
                    DropdownButtonFormField<int>(
                      value: _selectedVendorId,
                      decoration: InputDecoration(
                        labelText: 'Select Vendor',
                        prefixIcon: const Icon(Icons.business),
                        border: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(AppConstants.borderRadius),
                        ),
                      ),
                      items: _vendors.map<DropdownMenuItem<int>>((vendor) {
                        return DropdownMenuItem<int>(
                          value: vendor['vendor']['id'],
                          child: Text(vendor['vendor']['name']),
                        );
                      }).toList(),
                      onChanged: (int? value) {
                        setState(() {
                          _selectedVendorId = value;
                        });
                      },
                      validator: (value) {
                        if (value == null) return 'Please select a vendor';
                        return null;
                      },
                    ),
                    const SizedBox(height: 16),
                    
                    // Distance
                    TextFormField(
                      controller: _distanceController,
                      keyboardType: TextInputType.number,
                      decoration: InputDecoration(
                        labelText: 'Distance (km)',
                        prefixIcon: const Icon(Icons.straighten),
                        border: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(AppConstants.borderRadius),
                        ),
                      ),
                      validator: (value) {
                        if (value == null || value.isEmpty) {
                          return 'Distance is required';
                        }
                        if (double.tryParse(value) == null || double.parse(value) <= 0) {
                          return 'Enter a valid distance';
                        }
                        return null;
                      },
                    ),
                    const SizedBox(height: 16),
                    
                    // Duration
                    TextFormField(
                      controller: _durationController,
                      keyboardType: TextInputType.number,
                      decoration: InputDecoration(
                        labelText: 'Duration (minutes)',
                        prefixIcon: const Icon(Icons.timer),
                        border: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(AppConstants.borderRadius),
                        ),
                      ),
                      validator: (value) {
                        if (value == null || value.isEmpty) {
                          return 'Duration is required';
                        }
                        if (double.tryParse(value) == null || double.parse(value) <= 0) {
                          return 'Enter a valid duration';
                        }
                        return null;
                      },
                    ),
                    const SizedBox(height: 16),
                    
                    // Date Selection
                    ListTile(
                      leading: const Icon(Icons.calendar_today),
                      title: const Text('Trip Date'),
                      subtitle: Text('${_selectedDate.day}/${_selectedDate.month}/${_selectedDate.year}'),
                      trailing: const Icon(Icons.arrow_forward_ios),
                      onTap: () async {
                        final date = await showDatePicker(
                          context: context,
                          initialDate: _selectedDate,
                          firstDate: DateTime.now().subtract(const Duration(days: 7)),
                          lastDate: DateTime.now().add(const Duration(days: 30)),
                        );
                        if (date != null) {
                          setState(() {
                            _selectedDate = date;
                          });
                        }
                      },
                    ),
                  ],
                ),
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
            
            // Book Trip Button
            ElevatedButton(
              onPressed: _isLoading ? null : _bookTrip,
              style: ElevatedButton.styleFrom(
                backgroundColor: Color(AppConstants.primaryColorValue),
                foregroundColor: Colors.white,
                padding: const EdgeInsets.symmetric(vertical: 16),
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(AppConstants.borderRadius),
                ),
              ),
              child: _isLoading
                  ? const SizedBox(
                      height: 20,
                      width: 20,
                      child: CircularProgressIndicator(
                        strokeWidth: 2,
                        valueColor: AlwaysStoppedAnimation<Color>(Colors.white),
                      ),
                    )
                  : const Text(
                      'Book Trip',
                      style: TextStyle(
                        fontSize: 16,
                        fontWeight: FontWeight.w600,
                      ),
                    ),
            ),
          ],
        ),
      ),
    );
  }

  @override
  void dispose() {
    _distanceController.dispose();
    _durationController.dispose();
    super.dispose();
  }
}