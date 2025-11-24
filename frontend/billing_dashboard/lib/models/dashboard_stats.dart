class AdminStats {
  final int totalClients;
  final int totalVendors;
  final int totalEmployees;
  final int totalAssignments;

  AdminStats({
    required this.totalClients,
    required this.totalVendors,
    required this.totalEmployees,
    required this.totalAssignments,
  });

  factory AdminStats.fromJson(Map<String, dynamic> json) {
    return AdminStats(
      totalClients: json['totalClients'] ?? 0,
      totalVendors: json['totalVendors'] ?? 0,
      totalEmployees: json['totalEmployees'] ?? 0,
      totalAssignments: json['totalAssignments'] ?? 0,
    );
  }
}

class VendorStats {
  final int assignedClients;
  final int totalTrips;
  final double monthlyEarnings;
  final double pendingPayouts;

  VendorStats({
    required this.assignedClients,
    required this.totalTrips,
    required this.monthlyEarnings,
    required this.pendingPayouts,
  });

  factory VendorStats.fromJson(Map<String, dynamic> json) {
    return VendorStats(
      assignedClients: json['assignedClients'] ?? 0,
      totalTrips: json['totalTrips'] ?? 0,
      monthlyEarnings: (json['monthlyEarnings'] ?? 0).toDouble(),
      pendingPayouts: (json['pendingPayouts'] ?? 0).toDouble(),
    );
  }
}

class EmployeeStats {
  final int monthlyTrips;
  final double totalIncentives;

  EmployeeStats({
    required this.monthlyTrips,
    required this.totalIncentives,
  });

  factory EmployeeStats.fromJson(Map<String, dynamic> json) {
    return EmployeeStats(
      monthlyTrips: json['monthlyTrips'] ?? 0,
      totalIncentives: (json['totalIncentives'] ?? 0).toDouble(),
    );
  }
}