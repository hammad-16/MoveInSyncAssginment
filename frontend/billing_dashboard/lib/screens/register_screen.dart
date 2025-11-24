import 'package:flutter/material.dart';
import '../services/auth_service.dart';
import '../services/api_service.dart';
import '../models/user.dart';
import '../utils/constants.dart';

class RegisterScreen extends StatefulWidget {
  const RegisterScreen({Key? key}) : super(key: key);

  @override
  State<RegisterScreen> createState() => _RegisterScreenState();
}

class _RegisterScreenState extends State<RegisterScreen> {
  final _formKey = GlobalKey<FormState>();
  final _nameController = TextEditingController();
  final _emailController = TextEditingController();
  final _passwordController = TextEditingController();
  final _confirmPasswordController = TextEditingController();
  
  bool _isLoading = false;
  bool _obscurePassword = true;
  bool _obscureConfirmPassword = true;
  String? _errorMessage;
  UserRole _selectedRole = UserRole.EMPLOYEE;
  
  // Client selection for employees
  List<Map<String, dynamic>> _availableClients = [];
  int? _selectedClientId;
  bool _loadingClients = false;
  
  // Billing model selection for clients and vendors
  String? _selectedBillingModel;

  late AuthService _authService;

  @override
  void initState() {
    super.initState();
    _initAuthService();
  }

  Future<void> _initAuthService() async {
    _authService = await AuthService.getInstance();
    await _loadClients();
  }
  
  Future<void> _loadClients() async {
    setState(() {
      _loadingClients = true;
    });
    
    try {
      final apiService = await ApiService.getInstance();
      final response = await apiService.get('/public/clients');
      
      // The response already contains only CLIENT role users
      final clients = (response.data as List)
          .map((user) => {
                'id': user['id'],
                'name': user['name'],
                'email': user['email'],
              })
          .toList();
      
      setState(() {
        _availableClients = clients;
        _loadingClients = false;
      });
    } catch (e) {
      setState(() {
        _loadingClients = false;
      });
      print('Error loading clients: $e');
    }
  }

  @override
  void dispose() {
    _nameController.dispose();
    _emailController.dispose();
    _passwordController.dispose();
    _confirmPasswordController.dispose();
    super.dispose();
  }

  Future<void> _handleRegister() async {
    if (!_formKey.currentState!.validate()) return;
    
    // Validate client selection for employees
    if (_selectedRole == UserRole.EMPLOYEE && _selectedClientId == null) {
      setState(() {
        _errorMessage = 'Please select a client company for the employee';
      });
      return;
    }

    setState(() {
      _isLoading = true;
      _errorMessage = null;
    });

    try {
      final registerResponse = await _authService.register(
        _nameController.text.trim(),
        _emailController.text.trim(),
        _passwordController.text,
        _selectedRole.toString().split('.').last,
        _selectedBillingModel,
      );

      // If employee, create client-employee relationship
      if (_selectedRole == UserRole.EMPLOYEE && _selectedClientId != null) {
        try {
          final apiService = await ApiService.getInstance();
          await apiService.post('/client/employees', data: {
            'clientId': _selectedClientId,
            'employeeId': registerResponse.user.id,
          });
        } catch (e) {
          print('Warning: Failed to create client-employee relationship: $e');
          // Continue anyway, user is registered
        }
      }

      if (mounted) {
        // Show success message
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text(_selectedRole == UserRole.EMPLOYEE 
                ? 'Registration successful! You are now associated with your company.'
                : 'Registration successful! Welcome!'),
            backgroundColor: Colors.green,
          ),
        );

        // Navigate based on user role
        _navigateToRoleDashboard(registerResponse.user.role);
      }
    } catch (e) {
      if (mounted) {
        setState(() {
          _errorMessage = e.toString();
        });
      }
    } finally {
      if (mounted) {
        setState(() {
          _isLoading = false;
        });
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
      backgroundColor: Colors.grey[50],
      body: SafeArea(
        child: Center(
          child: SingleChildScrollView(
            padding: const EdgeInsets.all(AppConstants.defaultPadding),
            child: Card(
              elevation: AppConstants.cardElevation,
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(AppConstants.borderRadius),
              ),
              child: Container(
                width: double.infinity,
                constraints: const BoxConstraints(maxWidth: 400),
                padding: const EdgeInsets.all(30),
                child: Form(
                  key: _formKey,
                  child: Column(
                    mainAxisSize: MainAxisSize.min,
                    crossAxisAlignment: CrossAxisAlignment.stretch,
                    children: [
                      // App Title
                      
                      Text(
                        'Create Account',
                        textAlign: TextAlign.center,
                        style: Theme.of(context).textTheme.headlineSmall?.copyWith(
                          fontWeight: FontWeight.bold,
                          color: Color(AppConstants.primaryColorValue),
                        ),
                      ),
                      const SizedBox(height: 8),
                      
                      Text(
                        'Join the billing platform',
                        textAlign: TextAlign.center,
                        style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                          color: Colors.grey[600],
                        ),
                      ),
                      const SizedBox(height: 32),

                      // Name Field
                      TextFormField(
                        controller: _nameController,
                        textInputAction: TextInputAction.next,
                        decoration: InputDecoration(
                          labelText: 'Full Name',
                          hintText: 'Enter your full name',
                          prefixIcon: const Icon(Icons.person_outlined),
                          border: OutlineInputBorder(
                            borderRadius: BorderRadius.circular(AppConstants.borderRadius),
                          ),
                        ),
                        validator: (value) {
                          if (value == null || value.isEmpty) {
                            return 'Name is required';
                          }
                          if (value.length < 2) {
                            return 'Name must be at least 2 characters';
                          }
                          return null;
                        },
                      ),
                      const SizedBox(height: 16),

                      // Email Field
                      TextFormField(
                        controller: _emailController,
                        keyboardType: TextInputType.emailAddress,
                        textInputAction: TextInputAction.next,
                        decoration: InputDecoration(
                          labelText: 'Email',
                          hintText: 'Enter your email',
                          prefixIcon: const Icon(Icons.email_outlined),
                          border: OutlineInputBorder(
                            borderRadius: BorderRadius.circular(AppConstants.borderRadius),
                          ),
                        ),
                        validator: (value) {
                          if (value == null || value.isEmpty) {
                            return 'Email is required';
                          }
                          if (!RegExp(r'^[\w-\.]+@([\w-]+\.)+[\w-]{2,4}$').hasMatch(value)) {
                            return 'Enter a valid email';
                          }
                          return null;
                        },
                      ),
                      const SizedBox(height: 16),

                      // Role Selection (excluding ADMIN)
                      DropdownButtonFormField<UserRole>(
                        value: _selectedRole,
                        isExpanded: true,
                        decoration: InputDecoration(
                          labelText: 'Role',
                          prefixIcon: const Icon(Icons.work_outlined),
                          border: OutlineInputBorder(
                            borderRadius: BorderRadius.circular(AppConstants.borderRadius),
                          ),
                        ),
                        items: UserRole.values
                            .where((role) => role != UserRole.ADMIN) // Exclude ADMIN role
                            .map((role) {
                          return DropdownMenuItem(
                            value: role,
                            child: Text(role.toString().split('.').last),
                          );
                        }).toList(),
                        onChanged: (UserRole? newRole) {
                          if (newRole != null) {
                            setState(() {
                              _selectedRole = newRole;
                              _selectedClientId = null; // Reset client selection
                              _selectedBillingModel = null; // Reset billing model selection
                            });
                          }
                        },
                      ),
                      const SizedBox(height: 16),

                      // Client Selection (only for employees)
                      if (_selectedRole == UserRole.EMPLOYEE) ...[
                        _loadingClients
                            ? Container(
                                padding: const EdgeInsets.all(16),
                                decoration: BoxDecoration(
                                  border: Border.all(color: Colors.grey[300]!),
                                  borderRadius: BorderRadius.circular(AppConstants.borderRadius),
                                ),
                                child: const Row(
                                  children: [
                                    SizedBox(
                                      width: 20,
                                      height: 20,
                                      child: CircularProgressIndicator(strokeWidth: 2),
                                    ),
                                    SizedBox(width: 12),
                                    Text('Loading companies...'),
                                  ],
                                ),
                              )
                            : DropdownButtonFormField<int>(
                                value: _selectedClientId,
                                isExpanded: true,
                                decoration: InputDecoration(
                                  labelText: 'Select Company',
                                  hintText: 'Choose your company',
                                  prefixIcon: const Icon(Icons.business_outlined),
                                  border: OutlineInputBorder(
                                    borderRadius: BorderRadius.circular(AppConstants.borderRadius),
                                  ),
                                ),
                                items: _availableClients.map((client) {
                                  return DropdownMenuItem<int>(
                                    value: client['id'],
                                    child: Container(
                                      width: double.infinity,
                                      child: Text(
                                        '${client['name']} (${client['email']})',
                                        overflow: TextOverflow.ellipsis,
                                        maxLines: 1,
                                      ),
                                    ),
                                  );
                                }).toList(),
                                onChanged: (int? clientId) {
                                  setState(() {
                                    _selectedClientId = clientId;
                                  });
                                },
                                validator: (value) {
                                  if (_selectedRole == UserRole.EMPLOYEE && value == null) {
                                    return 'Please select your company';
                                  }
                                  return null;
                                },
                              ),
                        const SizedBox(height: 16),
                      ],

                      // Billing Model Selection (for clients and vendors)
                      if (_selectedRole == UserRole.CLIENT || _selectedRole == UserRole.VENDOR) ...[
                        DropdownButtonFormField<String>(
                          value: _selectedBillingModel,
                          isExpanded: true,
                          decoration: InputDecoration(
                            labelText: 'Preferred Billing Model',
                            hintText: 'Choose your billing preference',
                            prefixIcon: const Icon(Icons.payment_outlined),
                            border: OutlineInputBorder(
                              borderRadius: BorderRadius.circular(AppConstants.borderRadius),
                            ),
                          ),
                          items: const [
                            DropdownMenuItem(
                              value: 'PACKAGE',
                              child: Text('Package Model - Fixed monthly rate'),
                            ),
                            DropdownMenuItem(
                              value: 'TRIP',
                              child: Text('Trip Model - Pay per trip'),
                            ),
                            DropdownMenuItem(
                              value: 'HYBRID',
                              child: Text('Hybrid Model - Package + per trip'),
                            ),
                          ],
                          onChanged: (String? value) {
                            setState(() {
                              _selectedBillingModel = value;
                            });
                          },
                          validator: (value) {
                            if ((_selectedRole == UserRole.CLIENT || _selectedRole == UserRole.VENDOR) && value == null) {
                              return 'Please select a billing model';
                            }
                            return null;
                          },
                        ),
                        const SizedBox(height: 16),
                      ],

                      // Password Field
                      TextFormField(
                        controller: _passwordController,
                        obscureText: _obscurePassword,
                        textInputAction: TextInputAction.next,
                        decoration: InputDecoration(
                          labelText: 'Password',
                          hintText: 'Enter your password',
                          prefixIcon: const Icon(Icons.lock_outlined),
                          suffixIcon: IconButton(
                            icon: Icon(
                              _obscurePassword ? Icons.visibility : Icons.visibility_off,
                            ),
                            onPressed: () {
                              setState(() {
                                _obscurePassword = !_obscurePassword;
                              });
                            },
                          ),
                          border: OutlineInputBorder(
                            borderRadius: BorderRadius.circular(AppConstants.borderRadius),
                          ),
                        ),
                        validator: (value) {
                          if (value == null || value.isEmpty) {
                            return 'Password is required';
                          }
                          if (value.length < 6) {
                            return 'Password must be at least 6 characters';
                          }
                          return null;
                        },
                      ),
                      const SizedBox(height: 16),

                      // Confirm Password Field
                      TextFormField(
                        controller: _confirmPasswordController,
                        obscureText: _obscureConfirmPassword,
                        textInputAction: TextInputAction.done,
                        onFieldSubmitted: (_) => _handleRegister(),
                        decoration: InputDecoration(
                          labelText: 'Confirm Password',
                          hintText: 'Confirm your password',
                          prefixIcon: const Icon(Icons.lock_outlined),
                          suffixIcon: IconButton(
                            icon: Icon(
                              _obscureConfirmPassword ? Icons.visibility : Icons.visibility_off,
                            ),
                            onPressed: () {
                              setState(() {
                                _obscureConfirmPassword = !_obscureConfirmPassword;
                              });
                            },
                          ),
                          border: OutlineInputBorder(
                            borderRadius: BorderRadius.circular(AppConstants.borderRadius),
                          ),
                        ),
                        validator: (value) {
                          if (value == null || value.isEmpty) {
                            return 'Please confirm your password';
                          }
                          if (value != _passwordController.text) {
                            return 'Passwords do not match';
                          }
                          return null;
                        },
                      ),
                      const SizedBox(height: 24),

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

                      // Register Button
                      ElevatedButton(
                        onPressed: _isLoading ? null : _handleRegister,
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
                                'Create Account',
                                style: TextStyle(
                                  fontSize: 16,
                                  fontWeight: FontWeight.w600,
                                ),
                              ),
                      ),
                      const SizedBox(height: 16),

                      // Login Link
                      Row(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          Text(
                            'Already have an account? ',
                            style: TextStyle(color: Colors.grey[600]),
                          ),
                          TextButton(
                            onPressed: () {
                              Navigator.of(context).pushReplacementNamed(AppConstants.loginRoute);
                            },
                            child: Text(
                              'Sign In',
                              style: TextStyle(
                                color: Color(AppConstants.primaryColorValue),
                                fontWeight: FontWeight.w600,
                              ),
                            ),
                          ),
                        ],
                      ),
                    ],
                  ),
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }
}