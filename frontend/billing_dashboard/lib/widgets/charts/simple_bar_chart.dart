import 'package:flutter/material.dart';
import 'package:fl_chart/fl_chart.dart';

class SimpleBarChart extends StatelessWidget {
  final Map<String, double> data;
  final String title;

  const SimpleBarChart({
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
              child: BarChart(
                BarChartData(
                  alignment: BarChartAlignment.spaceAround,
                  maxY: _getMaxY(),
                  titlesData: FlTitlesData(
                    rightTitles: const AxisTitles(sideTitles: SideTitles(showTitles: false)),
                    topTitles: const AxisTitles(sideTitles: SideTitles(showTitles: false)),
                    bottomTitles: AxisTitles(
                      sideTitles: SideTitles(
                        showTitles: true,
                        getTitlesWidget: _getBottomTitles,
                        reservedSize: 40,
                      ),
                    ),
                    leftTitles: AxisTitles(
                      sideTitles: SideTitles(
                        showTitles: true,
                        reservedSize: 40,
                        getTitlesWidget: _getLeftTitles,
                      ),
                    ),
                  ),
                  borderData: FlBorderData(show: false),
                  barGroups: _generateBarGroups(),
                  gridData: const FlGridData(show: false),
                  barTouchData: BarTouchData(
                    enabled: true,
                    touchTooltipData: BarTouchTooltipData(
                      tooltipBgColor: Colors.blueGrey.withOpacity(0.8),
                      getTooltipItem: _getTooltipItem,
                    ),
                  ),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  List<BarChartGroupData> _generateBarGroups() {
    final entries = data.entries.toList();
    
    return entries.asMap().entries.map((entry) {
      final index = entry.key;
      final dataEntry = entry.value;
      
      return BarChartGroupData(
        x: index,
        barRods: [
          BarChartRodData(
            toY: dataEntry.value,
            color: Colors.blue,
            width: 20,
            borderRadius: const BorderRadius.only(
              topLeft: Radius.circular(4),
              topRight: Radius.circular(4),
            ),
          ),
        ],
      );
    }).toList();
  }

  Widget _getBottomTitles(double value, TitleMeta meta) {
    final keys = data.keys.toList();
    if (value.toInt() >= 0 && value.toInt() < keys.length) {
      String text = keys[value.toInt()];
      // Handle longer labels better
      if (text.length > 12) {
        // Split on parentheses or spaces for better wrapping
        if (text.contains('(')) {
          final parts = text.split('(');
          text = '${parts[0].trim()}\n(${parts[1]}';
        } else if (text.length > 8) {
          text = '${text.substring(0, 6)}..';
        }
      }
      return SideTitleWidget(
        axisSide: meta.axisSide,
        child: Text(
          text, 
          style: const TextStyle(fontSize: 10),
          textAlign: TextAlign.center,
        ),
      );
    }
    return const SizedBox();
  }

  Widget _getLeftTitles(double value, TitleMeta meta) {
    return SideTitleWidget(
      axisSide: meta.axisSide,
      child: Text(
        value.toInt().toString(),
        style: const TextStyle(fontSize: 10),
      ),
    );
  }

  double _getMaxY() {
    if (data.isEmpty) return 10;
    final maxValue = data.values.reduce((a, b) => a > b ? a : b);
    return (maxValue * 1.2).ceilToDouble();
  }

  BarTooltipItem? _getTooltipItem(BarChartGroupData group, int groupIndex, BarChartRodData rod, int rodIndex) {
    final keys = data.keys.toList();
    final values = data.values.toList();
    
    if (groupIndex >= 0 && groupIndex < keys.length) {
      final key = keys[groupIndex];
      final actualValue = values[groupIndex];
      
      // Show actual value in tooltip, especially for scaled earnings
      String tooltipText = key;
      if (key.contains('×')) {
        // Extract multiplier and show actual value
        final multiplierMatch = RegExp(r'×(\d+)').firstMatch(key);
        if (multiplierMatch != null) {
          final multiplier = int.parse(multiplierMatch.group(1)!);
          final actualEarnings = actualValue * multiplier;
          tooltipText = 'Earnings: \$${actualEarnings.toStringAsFixed(2)}';
        }
      } else {
        tooltipText = '$key: ${actualValue.toStringAsFixed(actualValue == actualValue.toInt() ? 0 : 2)}';
      }
      
      return BarTooltipItem(
        tooltipText,
        const TextStyle(
          color: Colors.white,
          fontWeight: FontWeight.bold,
          fontSize: 12,
        ),
      );
    }
    return null;
  }
}