import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class AppSelectorPage extends StatefulWidget {
  const AppSelectorPage({super.key});

  @override
  State<AppSelectorPage> createState() => _AppSelectorPageState();
}

class _AppSelectorPageState extends State<AppSelectorPage> {
  static const platform = MethodChannel('app_opener');

  bool instagram = false;
  bool youtube = false;
  bool chrome = false;

  List<String> selectedApps = [];

  void _updateSelectedApps(String appName, bool isSelected) {
    setState(() {
      if (isSelected) {
        selectedApps.add(appName);
      } else {
        selectedApps.remove(appName);
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    final height = MediaQuery.of(context).size.height;

    return Scaffold(
      appBar: AppBar(
        title: const Text(
          "Select apps you want to block",
          style: TextStyle(
            color: Colors.white,
            fontWeight: FontWeight.bold,
          ),
        ),
        centerTitle: true,
        backgroundColor: const Color(0xFF001E3D),
      ),
      backgroundColor: const Color(0xFF001E3D),
      body: Padding(
        padding: const EdgeInsets.only(left: 45, right: 45),
        child: Column(
          children: [
            SizedBox(height: height * 0.05),
            _buildAppTile(
              appName: 'Instagram',
              icon: Icons.people,
              isSelected: instagram,
              onChanged: (bool value) {
                setState(() {
                  instagram = value;
                  _updateSelectedApps('Instagram', value);
                });
              },
            ),
            const SizedBox(height: 30),
            _buildAppTile(
              appName: 'Youtube',
              icon: Icons.play_arrow,
              isSelected: youtube,
              onChanged: (bool value) {
                setState(() {
                  youtube = value;
                  _updateSelectedApps('Youtube', value);
                });
              },
            ),
            const SizedBox(height: 30),
            _buildAppTile(
              appName: 'Chrome',
              icon: Icons.laptop_chromebook,
              isSelected: chrome,
              onChanged: (bool value) {
                setState(() {
                  chrome = value;
                  _updateSelectedApps('Chrome', value);
                });
              },
            ),
            const SizedBox(height: 30),
            Text(
              'Selected Apps: ${selectedApps.join(', ')}',
              style: const TextStyle(color: Colors.white, fontSize: 16),
            ),
            const Spacer(),
            ElevatedButton(
              onPressed: () {
                // Send selectedApps list to Kotlin
                platform.invokeMethod('updateBlockedApps', selectedApps);
              },
              style: ElevatedButton.styleFrom(
                backgroundColor: const Color(0xFF2A3D6A),
                padding:
                    const EdgeInsets.symmetric(horizontal: 50, vertical: 15),
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(25),
                ),
              ),
              child: const Text(
                'Save',
                style: TextStyle(
                  color: Colors.white,
                  fontSize: 16,
                ),
              ),
            ),
            const SizedBox(height: 20),
          ],
        ),
      ),
    );
  }

  Widget _buildAppTile({
    required String appName,
    required IconData icon,
    required bool isSelected,
    required ValueChanged<bool> onChanged,
  }) {
    return Container(
      height: 60,
      padding: const EdgeInsets.symmetric(horizontal: 20),
      decoration: BoxDecoration(
        color: const Color(0xFF2A3D6A),
        borderRadius: BorderRadius.circular(25),
      ),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.start,
        children: [
          Center(
            child: Stack(
              alignment: Alignment.center,
              children: [
                Container(
                  width: 35,
                  height: 35,
                  decoration: BoxDecoration(
                    color: Colors.grey[300],
                    shape: BoxShape.circle,
                  ),
                ),
                Icon(icon, size: 20),
              ],
            ),
          ),
          const SizedBox(width: 10),
          Text(
            appName,
            style: const TextStyle(color: Colors.white),
          ),
          const SizedBox(width: 110),
          Switch(
            value: isSelected,
            activeColor: const Color.fromARGB(255, 0, 0, 0),
            onChanged: onChanged,
          ),
        ],
      ),
    );
  }
}
