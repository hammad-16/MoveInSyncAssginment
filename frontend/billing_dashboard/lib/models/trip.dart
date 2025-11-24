class Trip {
  final int id;
  final String date;
  final String vendorName;
  final String clientName;
  final String employeeName;
  final double distance;
  final int duration; // in minutes
  final double incentiveAmount;
  final String status;

  Trip({
    required this.id,
    required this.date,
    required this.vendorName,
    required this.clientName,
    required this.employeeName,
    required this.distance,
    required this.duration,
    required this.incentiveAmount,
    required this.status,
  });

  factory Trip.fromJson(Map<String, dynamic> json) {
    return Trip(
      id: json['id'] ?? 0,
      date: json['date'] ?? '',
      vendorName: json['vendorName'] ?? '',
      clientName: json['clientName'] ?? '',
      employeeName: json['employeeName'] ?? '',
      distance: (json['distance'] ?? 0).toDouble(),
      duration: json['duration'] ?? 0,
      incentiveAmount: (json['incentiveAmount'] ?? 0).toDouble(),
      status: json['status'] ?? 'COMPLETED',
    );
  }

  String get formattedDate {
    try {
      final dateTime = DateTime.parse(date);
      return '${dateTime.day}/${dateTime.month}/${dateTime.year}';
    } catch (e) {
      return date;
    }
  }

  String get formattedDistance {
    return '${distance.toStringAsFixed(1)} km';
  }

  String get formattedDuration {
    final hours = duration ~/ 60;
    final minutes = duration % 60;
    if (hours > 0) {
      return '${hours}h ${minutes}m';
    }
    return '${minutes}m';
  }

  String get formattedIncentive {
    return '₹${incentiveAmount.toStringAsFixed(2)}';
  }
}