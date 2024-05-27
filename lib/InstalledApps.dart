import 'dart:convert';

import 'package:device_apps/device_apps.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/foundation.dart';

import 'package:flutter/material.dart';
import 'AppBlockerService.dart';
import 'database_helper.dart';

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
  Future<List<Map<String, dynamic>>>? _dbFuture; // Define a Future variable
  String _searchText = "";
  final dbHelper = DatabaseHelper.instance;

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
    _dbFuture = dbHelper.queryAllRows(); // Initialize the Future variable
  }

  Map<String, dynamic>? findApp(
      List<Map<String, dynamic>> apps, String packageName) {
    for (var app in apps) {
      if (app[DatabaseHelper.columnPackageName] == packageName) {
        return app;
      }
    }
    return null;
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
            future: _dbFuture,
            builder: (context, snapshot) {
              if (snapshot.connectionState == ConnectionState.waiting) {
                return const Center(child: CircularProgressIndicator());
              } else {
                if (snapshot.data != null) {
                  List<Map<String, dynamic>> appsFromDb =
                      snapshot.data as List<Map<String, dynamic>>;
                  return ListView.builder(
                    itemCount: appsFromDb.length,
                    itemBuilder: (context, index) {
                      Map<String, dynamic> app = appsFromDb[index];
                      DateTime startTime = DateTime.fromMillisecondsSinceEpoch(
                          app[DatabaseHelper.columnBlockStartTime]);
                      DateTime expectedEndTime = startTime.add(Duration(
                          milliseconds:
                              app[DatabaseHelper.columnBlockDuration]));
                      String duration =
                          ((app[DatabaseHelper.columnBlockDuration] / 60000)
                                  .round())
                              .toString();
                      return ListTile(
                        title: Text(app[DatabaseHelper.columnAppName]),
                        subtitle: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(app[DatabaseHelper.columnPackageName]),
                            Text('Blocked for: $duration minutes'),
                            Text('Start time: ${startTime.toString()}'),
                            Text(
                              'Expected end time: ${expectedEndTime.toString()}',
                              style: TextStyle(
                                color: DateTime.now().isAfter(expectedEndTime)
                                    ? Colors.red
                                    : Colors.green,
                              ),
                            ),
                          ],
                        ),
                        leading: app[DatabaseHelper.columnAppIconImage] != null
                            ? CircleAvatar(
                                backgroundImage: MemoryImage(base64Decode(
                                    app[DatabaseHelper.columnAppIconImage])),
                              )
                            : null,
                        trailing: IconButton(
                          icon: Icon(Icons.delete),
                          onPressed: () async {
                            DateTime expectedEndTime = startTime.add(Duration(
                                milliseconds: app[DatabaseHelper.columnBlockDuration]));
                            if (DateTime.now().isBefore(expectedEndTime)) {
                              showDialog(
                                context: context,
                                builder: (BuildContext context) {
                                  return AlertDialog(
                                    title: Text('End App Blocking'),
                                    content: Text('Are you sure you want to end app blocking?'),
                                    actions: <Widget>[
                                      TextButton(
                                        child: Text('Cancel'),
                                        onPressed: () {
                                          Navigator.of(context).pop();
                                        },
                                      ),
                                      TextButton(
                                        child: Text('Confirm'),
                                        onPressed: () async {
                                          await AppBlockerService.stopService();
                                          await dbHelper.delete(app[DatabaseHelper.columnId]);
                                          setState(() {
                                            _dbFuture = dbHelper.queryAllRows();
                                          });
                                          Navigator.of(context).pop();
                                        },
                                      ),
                                    ],
                                  );
                                },
                              );
                            } else {
                              await dbHelper.delete(app[DatabaseHelper.columnId]);
                              setState(() {
                                _dbFuture = dbHelper.queryAllRows();
                              });
                            }
                          },
                        ),
                      );
                    },
                  );
                } else {
                  // Handle the case where snapshot.data is null
                  return const Center(child: Text('No apps blocked yet.'));
                }
              }
            },
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
          onPressed: () async {
            if (_controller.text.isNotEmpty) {
              int duration = int.parse(_controller.text);

              selectedAppsList
                  .addAll(selectedApps.map((app) => app.packageName));
              print("selectedAppsList $selectedAppsList");
              AppBlockerService.startService(selectedAppsList, duration)
                  .then((value) => {});

              // Insert or update each selected app in the database
              for (var app in selectedApps) {
                // Create a row to insert or update
                String base64Icon = base64Encode(
                    app is ApplicationWithIcon ? app.icon : Uint8List(0));
                Map<String, dynamic> row = {
                  DatabaseHelper.columnPackageName: app.packageName,
                  DatabaseHelper.columnAppName: app.appName,
                  DatabaseHelper.columnAppIconImage: base64Icon,
                  DatabaseHelper.columnBlockStartTime:
                      DateTime.now().millisecondsSinceEpoch,
                  DatabaseHelper.columnBlockDuration: duration,
                  // Add other columns if needed
                };

                // Query the database for the app
                List<Map<String, dynamic>> existingRows =
                    await dbHelper.queryAllRows();
                Map<String, dynamic>? existingApp =
                    findApp(existingRows, app.packageName);

                if (existingApp != null) {
                  // If the app exists, update the row
                  row[DatabaseHelper.columnId] =
                      existingApp[DatabaseHelper.columnId];
                  await dbHelper.update(row).then((value) => {
                        if (kDebugMode) {print('updated row id: $value')}
                      });
                } else {
                  // If the app does not exist, insert a new row
                  await dbHelper.insert(row).then((value) => {
                        if (kDebugMode) {print('inserted row id: $value')}
                      });
                }
              }

              // Refresh the FutureBuilder by calling setState
              setState(() {
                _dbFuture = dbHelper.queryAllRows();
                _controller.clear();
                _searchController.clear();
                selectedApps.clear();
                selectedAppsList.clear();
              });
            }
          },
          child: Text('Block ${selectedApps.length} apps'),
        ),
      ],
    );
  }
}
