import 'package:device_apps/device_apps.dart';
import 'package:flutter/cupertino.dart';

import 'package:flutter/material.dart';
import 'AppBlockerService.dart';

class InstalledApps extends StatefulWidget {
  @override
  _InstalledAppsState createState() => _InstalledAppsState();
}

class _InstalledAppsState extends State<InstalledApps> {
  List<Application> selectedApps = [];
  List<String> selectedAppsList = [];
  final TextEditingController _controller = TextEditingController();
  final TextEditingController _searchController = TextEditingController();
  Future<List<Application>>? _appsFuture;
  String _searchText = "";

  @override
  void initState() {
    super.initState();
    _appsFuture = DeviceApps.getInstalledApplications(
      includeAppIcons: true,
      includeSystemApps: true,
      onlyAppsWithLaunchIntent: true,
    );
    _searchController.addListener(() {
      setState(() {
        _searchText = _searchController.text;
      });
    });
  }

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        TextField(
          controller: _searchController,
          decoration: InputDecoration(
            labelText: 'Search App',
          ),
        ),
        Expanded(
          child: FutureBuilder(
            future: _appsFuture,
            builder: (context, snapshot) {
              if (snapshot.connectionState == ConnectionState.waiting) {
                return const Center(child: CircularProgressIndicator());
              } else {
                List<Application> apps = snapshot.data as List<Application>;
                apps.sort((a, b) => a.appName.compareTo(b.appName));
                if (_searchText.isNotEmpty) {
                  apps = apps
                      .where((app) => app.appName
                          .toLowerCase()
                          .contains(_searchText.toLowerCase()))
                      .toList();
                }
                return ListView.builder(
                  itemCount: apps.length,
                  itemBuilder: (context, index) {
                    Application app = apps[index];
                    return CheckboxListTile(
                      title: Text(app.appName),
                      subtitle: Text('${app.packageName} - ${app.versionName}'),
                      value: selectedApps.contains(app),
                      secondary: app is ApplicationWithIcon
                          ? CircleAvatar(
                              backgroundImage: MemoryImage(app.icon),
                            )
                          : null,
                      onChanged: (bool? value) {
                        setState(() {
                          if (value != null) {
                            if (value) {
                              selectedApps.add(app);
                            } else {
                              selectedApps.remove(app);
                            }
                          }
                        });
                      },
                    );
                  },
                );
              }
            },
          ),
        ),
        TextField(
          controller: _controller,
          keyboardType: TextInputType.number,
          decoration: InputDecoration(
            labelText: 'Block duration in milliseconds',
          ),
        ),
        ElevatedButton(
          onPressed: () {
            if (_controller.text.isNotEmpty) {
              int duration = int.parse(_controller.text);
              // selectedApps.forEach((app) {
              //   AppBlockerService.startService(app.packageName, duration);
              // });
              selectedAppsList
                  .addAll(selectedApps.map((app) => app.packageName));
              print("selectedAppsList $selectedAppsList");
              AppBlockerService.startService(selectedAppsList, duration);
            }
          },
          child: Text('Block ${selectedApps.length} apps'),
        ),
      ],
    );
  }
}
