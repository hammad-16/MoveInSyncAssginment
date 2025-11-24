import 'package:flutter/material.dart';
import 'package:fl_chart/fl_chart.dart';
import '../../utils/constants.dart';

class SimplePieChart extends StatelessWidget {
  final Map<String, double> data;
  final String title;

  const SimplePieChart({
    Key? key,
    required this.data,
    required this.title,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              title,
              style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 16),
            SizedBox(
              height: 200,
              child: Row(
                children: [
                  Expanded(
                    flex: 2,
                    child: PieChart(
                      PieChartData(
                        sections: _generateSections(),
                        centerSpaceRadius: 40,
                        sectionsSpace: 2,
                      ),
                    ),
                  ),
                  Expanded(child: _buildLegend()),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  List<PieChartSectionData> _generateSections() {
    final total = data.values.fold(0.0, (sum, value) => sum + value);
    final entries = data.entries.toList();
    
    return entries.asMap().entries.map((entry) {
      final index = entry.key;
      final dataEntry = entry.value;
      final percentage = total > 0 ? (dataEntry.value / total * 100) : 0.0;
      
      return PieChartSectionData(
        color: _getColor(index),
        value: dataEntry.value,
        title: '${percentage.toStringAsFixed(0)}%',
        radius: 50,
        titleStyle: const TextStyle(
          fontSize: 14,
          fontWeight: FontWeight.bold,
          color: Colors.white,
        ),
      );
    }).toList();
  }

  Widget _buildLegend() {
    final entries = data.entries.toList();
    
    return Column(
      mainAxisAlignment: MainAxisAlignment.center,
      children: entries.asMap().entries.map((entry) {
        final index = entry.key;
        final dataEntry = entry.value;
        
        return Padding(
          padding: const EdgeInsets.symmetric(vertical: 4),
          child: Row(
            children: [
              Container(
                width: 12,
                height: 12,
                decoration: BoxDecoration(
                  color: _getColor(index),
                  shape: BoxShape.circle,
                ),
              ),
              const SizedBox(width: 8),
              Expanded(
                child: Text(
                  '${dataEntry.key}: ${dataEntry.value.toInt()}',
                  style: const TextStyle(fontSize: 12),
                ),
              ),
            ],
          ),
        );
      }).toList(),
    );
  }

  Color _getColor(int index) {
    const colors = [Colors.blue, Colors.green, Colors.orange, Colors.purple];
    return colors[index % colors.length];
  }
}