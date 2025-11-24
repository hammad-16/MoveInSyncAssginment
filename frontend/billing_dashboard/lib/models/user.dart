class User {
  final int id;
  final String name;
  final String email;
  final UserRole role;

  User({
    required this.id,
    required this.name,
    required this.email,
    required this.role,
  });

  factory User.fromJson(Map<String, dynamic> json) {
    return User(
      id: json['userId'] ?? json['id'] ?? 0,
      name: json['name'] ?? '',
      email: json['email'] ?? '',
      role: UserRole.fromString(json['role'] ?? 'EMPLOYEE'),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'name': name,
      'email': email,
      'role': role.toString(),
    };
  }
}

enum UserRole {
  ADMIN,
  CLIENT,
  VENDOR,
  EMPLOYEE;

  static UserRole fromString(String role) {
    switch (role.toUpperCase()) {
      case 'ADMIN':
        return UserRole.ADMIN;
      case 'CLIENT':
        return UserRole.CLIENT;
      case 'VENDOR':
        return UserRole.VENDOR;
      case 'EMPLOYEE':
        return UserRole.EMPLOYEE;
      default:
        return UserRole.EMPLOYEE;
    }
  }

  @override
  String toString() {
    return name;
  }
}

class LoginRequest {
  final String email;
  final String password;

  LoginRequest({
    required this.email,
    required this.password,
  });

  Map<String, dynamic> toJson() {
    return {
      'email': email,
      'password': password,
    };
  }
}

class LoginResponse {
  final String token;
  final User user;
  final String message;

  LoginResponse({
    required this.token,
    required this.user,
    required this.message,
  });

  factory LoginResponse.fromJson(Map<String, dynamic> json) {
    return LoginResponse(
      token: json['token'] ?? '',
      message: json['message'] ?? 'Login successful',
      user: User(
        id: json['userId'] ?? 0,
        name: json['name'] ?? '',
        email: json['email'] ?? '',
        role: UserRole.fromString(json['role'] ?? 'EMPLOYEE'),
      ),
    );
  }
}