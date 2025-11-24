import 'dart:typed_data';
import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:pdf/pdf.dart';
import 'package:pdf/widgets.dart' as pw;
import 'package:printing/printing.dart';
import '../models/user.dart';

class PdfService {
  static Future<void> generateClientReport({
    required User user,
    required Map<String, dynamic> dashboardSummary,
    required List<dynamic> recentTrips,
    required List<dynamic> employees,
    required List<dynamic> vendors,
  }) async {
    try {
      final pdf = pw.Document();

    pdf.addPage(
      pw.MultiPage(
        pageFormat: PdfPageFormat.a4,
        margin: const pw.EdgeInsets.all(32),
        build: (pw.Context context) {
          return [
            // Header
            pw.Header(
              level: 0,
              child: pw.Row(
                mainAxisAlignment: pw.MainAxisAlignment.spaceBetween,
                children: [
                  pw.Text(
                    'Client Dashboard Report',
                    style: pw.TextStyle(
                      fontSize: 24,
                      fontWeight: pw.FontWeight.bold,
                      color: PdfColors.blue800,
                    ),
                  ),
                  pw.Text(
                    'Generated: ${DateTime.now().toString().split(' ')[0]}',
                    style: const pw.TextStyle(fontSize: 12, color: PdfColors.grey600),
                  ),
                ],
              ),
            ),
            
            pw.SizedBox(height: 20),
            
            // Client Information
            pw.Container(
              padding: const pw.EdgeInsets.all(16),
              decoration: pw.BoxDecoration(
                border: pw.Border.all(color: PdfColors.grey300),
                borderRadius: pw.BorderRadius.circular(8),
              ),
              child: pw.Column(
                crossAxisAlignment: pw.CrossAxisAlignment.start,
                children: [
                  pw.Text(
                    'Client Information',
                    style: pw.TextStyle(fontSize: 18, fontWeight: pw.FontWeight.bold),
                  ),
                  pw.SizedBox(height: 10),
                  pw.Text('Name: ${user.name}'),
                  pw.Text('Email: ${user.email}'),
                  pw.Text('Role: ${user.role.toString().split('.').last}'),
                ],
              ),
            ),
            
            pw.SizedBox(height: 20),
            
            // Dashboard Statistics
            pw.Text(
              'Dashboard Statistics',
              style: pw.TextStyle(fontSize: 18, fontWeight: pw.FontWeight.bold),
            ),
            pw.SizedBox(height: 10),
            
            pw.Table(
              border: pw.TableBorder.all(color: PdfColors.grey300),
              children: [
                pw.TableRow(
                  decoration: const pw.BoxDecoration(color: PdfColors.grey100),
                  children: [
                    pw.Padding(
                      padding: const pw.EdgeInsets.all(8),
                      child: pw.Text('Metric', style: pw.TextStyle(fontWeight: pw.FontWeight.bold)),
                    ),
                    pw.Padding(
                      padding: const pw.EdgeInsets.all(8),
                      child: pw.Text('Value', style: pw.TextStyle(fontWeight: pw.FontWeight.bold)),
                    ),
                  ],
                ),
                pw.TableRow(
                  children: [
                    pw.Padding(
                      padding: const pw.EdgeInsets.all(8),
                      child: pw.Text('Total Employees'),
                    ),
                    pw.Padding(
                      padding: const pw.EdgeInsets.all(8),
                      child: pw.Text('${dashboardSummary['totalEmployees'] ?? 0}'),
                    ),
                  ],
                ),
                pw.TableRow(
                  children: [
                    pw.Padding(
                      padding: const pw.EdgeInsets.all(8),
                      child: pw.Text('Assigned Vendors'),
                    ),
                    pw.Padding(
                      padding: const pw.EdgeInsets.all(8),
                      child: pw.Text('${dashboardSummary['assignedVendors'] ?? 0}'),
                    ),
                  ],
                ),
                pw.TableRow(
                  children: [
                    pw.Padding(
                      padding: const pw.EdgeInsets.all(8),
                      child: pw.Text('Total Trips'),
                    ),
                    pw.Padding(
                      padding: const pw.EdgeInsets.all(8),
                      child: pw.Text('${dashboardSummary['totalTrips'] ?? 0}'),
                    ),
                  ],
                ),
                pw.TableRow(
                  children: [
                    pw.Padding(
                      padding: const pw.EdgeInsets.all(8),
                      child: pw.Text('Recent Trips (30d)'),
                    ),
                    pw.Padding(
                      padding: const pw.EdgeInsets.all(8),
                      child: pw.Text('${dashboardSummary['recentTrips'] ?? 0}'),
                    ),
                  ],
                ),
              ],
            ),
            
            pw.SizedBox(height: 20),
            
            // Assigned Vendors
            if (vendors.isNotEmpty) ...[
              pw.Text(
                'Assigned Vendors & Negotiated Rates',
                style: pw.TextStyle(fontSize: 18, fontWeight: pw.FontWeight.bold),
              ),
              pw.SizedBox(height: 5),
              pw.Text(
                'Note: Rates shown are specific agreements between your company and each vendor.',
                style: pw.TextStyle(fontSize: 10, color: PdfColors.grey600, fontStyle: pw.FontStyle.italic),
              ),
              pw.SizedBox(height: 10),
              
              pw.Table(
                border: pw.TableBorder.all(color: PdfColors.grey300),
                children: [
                  pw.TableRow(
                    decoration: const pw.BoxDecoration(color: PdfColors.grey100),
                    children: [
                      pw.Padding(
                        padding: const pw.EdgeInsets.all(8),
                        child: pw.Text('Vendor Name', style: pw.TextStyle(fontWeight: pw.FontWeight.bold)),
                      ),
                      pw.Padding(
                        padding: const pw.EdgeInsets.all(8),
                        child: pw.Text('Billing Model', style: pw.TextStyle(fontWeight: pw.FontWeight.bold)),
                      ),
                      pw.Padding(
                        padding: const pw.EdgeInsets.all(8),
                        child: pw.Text('Package Rate', style: pw.TextStyle(fontWeight: pw.FontWeight.bold)),
                      ),
                    ],
                  ),
                  ...vendors.take(10).map((vendor) => pw.TableRow(
                    children: [
                      pw.Padding(
                        padding: const pw.EdgeInsets.all(8),
                        child: pw.Text(vendor['vendor']?['name'] ?? 'Unknown'),
                      ),
                      pw.Padding(
                        padding: const pw.EdgeInsets.all(8),
                        child: pw.Text(vendor['billingModel'] ?? 'N/A'),
                      ),
                      pw.Padding(
                        padding: const pw.EdgeInsets.all(8),
                        child: pw.Text('\$${vendor['packageRate'] ?? 0}'),
                      ),
                    ],
                  )).toList(),
                ],
              ),
              
              pw.SizedBox(height: 20),
            ],
            
            // Recent Trips
            if (recentTrips.isNotEmpty) ...[
              pw.Text(
                'Recent Trips (Last 5)',
                style: pw.TextStyle(fontSize: 18, fontWeight: pw.FontWeight.bold),
              ),
              pw.SizedBox(height: 10),
              
              pw.Table(
                border: pw.TableBorder.all(color: PdfColors.grey300),
                children: [
                  pw.TableRow(
                    decoration: const pw.BoxDecoration(color: PdfColors.grey100),
                    children: [
                      pw.Padding(
                        padding: const pw.EdgeInsets.all(8),
                        child: pw.Text('Employee', style: pw.TextStyle(fontWeight: pw.FontWeight.bold)),
                      ),
                      pw.Padding(
                        padding: const pw.EdgeInsets.all(8),
                        child: pw.Text('Vendor', style: pw.TextStyle(fontWeight: pw.FontWeight.bold)),
                      ),
                      pw.Padding(
                        padding: const pw.EdgeInsets.all(8),
                        child: pw.Text('Distance', style: pw.TextStyle(fontWeight: pw.FontWeight.bold)),
                      ),
                      pw.Padding(
                        padding: const pw.EdgeInsets.all(8),
                        child: pw.Text('Cost', style: pw.TextStyle(fontWeight: pw.FontWeight.bold)),
                      ),
                      pw.Padding(
                        padding: const pw.EdgeInsets.all(8),
                        child: pw.Text('Date', style: pw.TextStyle(fontWeight: pw.FontWeight.bold)),
                      ),
                    ],
                  ),
                  ...recentTrips.take(5).map((trip) => pw.TableRow(
                    children: [
                      pw.Padding(
                        padding: const pw.EdgeInsets.all(8),
                        child: pw.Text(trip['employeeName'] ?? 'Unknown'),
                      ),
                      pw.Padding(
                        padding: const pw.EdgeInsets.all(8),
                        child: pw.Text(trip['vendorName'] ?? 'Unknown'),
                      ),
                      pw.Padding(
                        padding: const pw.EdgeInsets.all(8),
                        child: pw.Text('${trip['distance'] ?? 0} km'),
                      ),
                      pw.Padding(
                        padding: const pw.EdgeInsets.all(8),
                        child: pw.Text('\$${trip['totalCost'] ?? 0}'),
                      ),
                      pw.Padding(
                        padding: const pw.EdgeInsets.all(8),
                        child: pw.Text(_formatDate(trip['date'])),
                      ),
                    ],
                  )).toList(),
                ],
              ),
            ],
          ];
        },
      ),
    );

      await _savePdf(pdf, 'Client_Dashboard_Report_${DateTime.now().millisecondsSinceEpoch}.pdf');
    } catch (e) {
      print('Error generating client PDF: $e');
      rethrow;
    }
  }

  static Future<void> generateVendorReport({
    required User user,
    required Map<String, dynamic> vendorStats,
    required List<dynamic> recentTrips,
    required List<dynamic> clients,
  }) async {
    try {
      final pdf = pw.Document();

    pdf.addPage(
      pw.MultiPage(
        pageFormat: PdfPageFormat.a4,
        margin: const pw.EdgeInsets.all(32),
        build: (pw.Context context) {
          return [

            pw.Header(
              level: 0,
              child: pw.Row(
                mainAxisAlignment: pw.MainAxisAlignment.spaceBetween,
                children: [
                  pw.Text(
                    'Vendor Dashboard Report',
                    style: pw.TextStyle(
                      fontSize: 24,
                      fontWeight: pw.FontWeight.bold,
                      color: PdfColors.green800,
                    ),
                  ),
                  pw.Text(
                    'Generated: ${DateTime.now().toString().split(' ')[0]}',
                    style: const pw.TextStyle(fontSize: 12, color: PdfColors.grey600),
                  ),
                ],
              ),
            ),
            
            pw.SizedBox(height: 20),
            
            // Vendor Information
            pw.Container(
              padding: const pw.EdgeInsets.all(16),
              decoration: pw.BoxDecoration(
                border: pw.Border.all(color: PdfColors.grey300),
                borderRadius: pw.BorderRadius.circular(8),
              ),
              child: pw.Column(
                crossAxisAlignment: pw.CrossAxisAlignment.start,
                children: [
                  pw.Text(
                    'Vendor Information',
                    style: pw.TextStyle(fontSize: 18, fontWeight: pw.FontWeight.bold),
                  ),
                  pw.SizedBox(height: 10),
                  pw.Text('Name: ${user.name}'),
                  pw.Text('Email: ${user.email}'),
                  pw.Text('Role: ${user.role.toString().split('.').last}'),
                ],
              ),
            ),
            
            pw.SizedBox(height: 20),
            
            // Performance Statistics
            pw.Text(
              'Performance Statistics',
              style: pw.TextStyle(fontSize: 18, fontWeight: pw.FontWeight.bold),
            ),
            pw.SizedBox(height: 10),
            
            pw.Table(
              border: pw.TableBorder.all(color: PdfColors.grey300),
              children: [
                pw.TableRow(
                  decoration: const pw.BoxDecoration(color: PdfColors.grey100),
                  children: [
                    pw.Padding(
                      padding: const pw.EdgeInsets.all(8),
                      child: pw.Text('Metric', style: pw.TextStyle(fontWeight: pw.FontWeight.bold)),
                    ),
                    pw.Padding(
                      padding: const pw.EdgeInsets.all(8),
                      child: pw.Text('Value', style: pw.TextStyle(fontWeight: pw.FontWeight.bold)),
                    ),
                  ],
                ),
                pw.TableRow(
                  children: [
                    pw.Padding(
                      padding: const pw.EdgeInsets.all(8),
                      child: pw.Text('Active Clients'),
                    ),
                    pw.Padding(
                      padding: const pw.EdgeInsets.all(8),
                      child: pw.Text('${vendorStats['totalClients'] ?? 0}'),
                    ),
                  ],
                ),
                pw.TableRow(
                  children: [
                    pw.Padding(
                      padding: const pw.EdgeInsets.all(8),
                      child: pw.Text('Total Trips'),
                    ),
                    pw.Padding(
                      padding: const pw.EdgeInsets.all(8),
                      child: pw.Text('${vendorStats['totalTrips'] ?? 0}'),
                    ),
                  ],
                ),
                pw.TableRow(
                  children: [
                    pw.Padding(
                      padding: const pw.EdgeInsets.all(8),
                      child: pw.Text('Monthly Earnings'),
                    ),
                    pw.Padding(
                      padding: const pw.EdgeInsets.all(8),
                      child: pw.Text('\$${vendorStats['monthlyEarnings'] ?? 0}'),
                    ),
                  ],
                ),
                pw.TableRow(
                  children: [
                    pw.Padding(
                      padding: const pw.EdgeInsets.all(8),
                      child: pw.Text('Total Earnings'),
                    ),
                    pw.Padding(
                      padding: const pw.EdgeInsets.all(8),
                      child: pw.Text('\$${vendorStats['totalEarnings'] ?? 0}'),
                    ),
                  ],
                ),
              ],
            ),
            
            pw.SizedBox(height: 20),
            
            // Rate Information
            pw.Text(
              'Default Vendor Rates',
              style: pw.TextStyle(fontSize: 18, fontWeight: pw.FontWeight.bold),
            ),
            pw.SizedBox(height: 5),
            pw.Text(
              'Note: These are your default rates. Actual rates may vary per client based on negotiated agreements.',
              style: pw.TextStyle(fontSize: 10, color: PdfColors.grey600, fontStyle: pw.FontStyle.italic),
            ),
            pw.SizedBox(height: 10),
            
            pw.Table(
              border: pw.TableBorder.all(color: PdfColors.grey300),
              children: [
                pw.TableRow(
                  decoration: const pw.BoxDecoration(color: PdfColors.grey100),
                  children: [
                    pw.Padding(
                      padding: const pw.EdgeInsets.all(8),
                      child: pw.Text('Rate Type', style: pw.TextStyle(fontWeight: pw.FontWeight.bold)),
                    ),
                    pw.Padding(
                      padding: const pw.EdgeInsets.all(8),
                      child: pw.Text('Amount', style: pw.TextStyle(fontWeight: pw.FontWeight.bold)),
                    ),
                  ],
                ),
                pw.TableRow(
                  children: [
                    pw.Padding(
                      padding: const pw.EdgeInsets.all(8),
                      child: pw.Text('Billing Model'),
                    ),
                    pw.Padding(
                      padding: const pw.EdgeInsets.all(8),
                      child: pw.Text('${vendorStats['billingModel'] ?? 'Not Set'}'),
                    ),
                  ],
                ),
                pw.TableRow(
                  children: [
                    pw.Padding(
                      padding: const pw.EdgeInsets.all(8),
                      child: pw.Text(
                        vendorStats['billingModel'] == 'PACKAGE' 
                          ? 'Monthly Package Rate'
                          : vendorStats['billingModel'] == 'HYBRID'
                            ? 'Monthly Base Rate'
                            : 'Package Rate (N/A for Trip Model)'
                      ),
                    ),
                    pw.Padding(
                      padding: const pw.EdgeInsets.all(8),
                      child: pw.Text(
                        vendorStats['billingModel'] == 'PACKAGE' 
                          ? '\$${vendorStats['packageRate'] ?? 0}/month (all-inclusive)'
                          : vendorStats['billingModel'] == 'HYBRID'
                            ? '\$${vendorStats['packageRate'] ?? 0}/month (base rate)'
                            : 'N/A'
                      ),
                    ),
                  ],
                ),
                pw.TableRow(
                  children: [
                    pw.Padding(
                      padding: const pw.EdgeInsets.all(8),
                      child: pw.Text(
                        vendorStats['billingModel'] == 'TRIP' 
                          ? 'Per Trip Rate'
                          : vendorStats['billingModel'] == 'HYBRID'
                            ? 'Additional Per Trip Rate'
                            : 'Trip Rate (N/A for Package Model)'
                      ),
                    ),
                    pw.Padding(
                      padding: const pw.EdgeInsets.all(8),
                      child: pw.Text(
                        vendorStats['billingModel'] == 'TRIP' 
                          ? '\$${vendorStats['tripRate'] ?? 0}/trip'
                          : vendorStats['billingModel'] == 'HYBRID'
                            ? '\$${vendorStats['tripRate'] ?? 0}/trip (additional)'
                            : 'N/A'
                      ),
                    ),
                  ],
                ),
                pw.TableRow(
                  children: [
                    pw.Padding(
                      padding: const pw.EdgeInsets.all(8),
                      child: pw.Text('Extra Distance Rate'),
                    ),
                    pw.Padding(
                      padding: const pw.EdgeInsets.all(8),
                      child: pw.Text('\$${vendorStats['extraDistanceRate'] ?? 0}/km'),
                    ),
                  ],
                ),
                pw.TableRow(
                  children: [
                    pw.Padding(
                      padding: const pw.EdgeInsets.all(8),
                      child: pw.Text('Extra Time Rate'),
                    ),
                    pw.Padding(
                      padding: const pw.EdgeInsets.all(8),
                      child: pw.Text('\$${vendorStats['extraTimeRate'] ?? 0}/min'),
                    ),
                  ],
                ),
              ],
            ),
            
            pw.SizedBox(height: 20),
            
            // Recent Trips (Last 5)
            if (recentTrips.isNotEmpty) ...[
              pw.Text(
                'Recent Trips (Last 5)',
                style: pw.TextStyle(fontSize: 18, fontWeight: pw.FontWeight.bold),
              ),
              pw.SizedBox(height: 10),
              
              pw.Table(
                border: pw.TableBorder.all(color: PdfColors.grey300),
                children: [
                  pw.TableRow(
                    decoration: const pw.BoxDecoration(color: PdfColors.grey100),
                    children: [
                      pw.Padding(
                        padding: const pw.EdgeInsets.all(8),
                        child: pw.Text('Client', style: pw.TextStyle(fontWeight: pw.FontWeight.bold)),
                      ),
                      pw.Padding(
                        padding: const pw.EdgeInsets.all(8),
                        child: pw.Text('Destination', style: pw.TextStyle(fontWeight: pw.FontWeight.bold)),
                      ),
                      pw.Padding(
                        padding: const pw.EdgeInsets.all(8),
                        child: pw.Text('Amount', style: pw.TextStyle(fontWeight: pw.FontWeight.bold)),
                      ),
                      pw.Padding(
                        padding: const pw.EdgeInsets.all(8),
                        child: pw.Text('Date', style: pw.TextStyle(fontWeight: pw.FontWeight.bold)),
                      ),
                    ],
                  ),
                  ...recentTrips.take(5).map((trip) => pw.TableRow(
                    children: [
                      pw.Padding(
                        padding: const pw.EdgeInsets.all(8),
                        child: pw.Text(trip['clientName'] ?? 'Unknown'),
                      ),
                      pw.Padding(
                        padding: const pw.EdgeInsets.all(8),
                        child: pw.Text(trip['destination'] ?? 'N/A'),
                      ),
                      pw.Padding(
                        padding: const pw.EdgeInsets.all(8),
                        child: pw.Text('\$${trip['amount'] ?? 0}'),
                      ),
                      pw.Padding(
                        padding: const pw.EdgeInsets.all(8),
                        child: pw.Text(_formatDate(trip['date'])),
                      ),
                    ],
                  )).toList(),
                ],
              ),
            ],
          ];
        },
      ),
    );

      await _savePdf(pdf, 'Vendor_Dashboard_Report_${DateTime.now().millisecondsSinceEpoch}.pdf');
    } catch (e) {
      print('Error generating vendor PDF: $e');
      rethrow;
    }
  }

  static Future<void> _savePdf(pw.Document pdf, String fileName) async {
    try {
      final bytes = await pdf.save();
      
      // Use the printing package for all platforms 
      await Printing.layoutPdf(
        onLayout: (PdfPageFormat format) async => bytes,
        name: fileName,
      );
    } catch (e) {
      print('Error saving PDF: $e');
      rethrow;
    }
  }

  static String _formatDate(String? dateStr) {
    if (dateStr == null) return 'Unknown';
    try {
      final date = DateTime.parse(dateStr);
      return '${date.day}/${date.month}/${date.year}';
    } catch (e) {
      return 'Invalid date';
    }
  }

  static String _formatRateByBillingModel(Map<String, dynamic> vendor) {
    final billingModel = vendor['billingModel'] ?? 'PACKAGE';
    final packageRate = vendor['packageRate'] ?? 0;
    final tripRate = vendor['tripRate'] ?? 0;
    
    switch (billingModel.toString().toUpperCase()) {
      case 'PACKAGE':
        return '\$${packageRate}/month (Package Model)';
      case 'TRIP':
        return '\$${tripRate}/trip (Trip Model)';
      case 'HYBRID':
        return '\$${packageRate}/month + \$${tripRate}/trip (Hybrid Model)';
      default:
        return '\$${packageRate}';
    }
  }
}