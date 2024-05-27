// import 'package:android_alarm_manager/android_alarm_manager.dart';
import 'package:android_alarm_manager_plus/android_alarm_manager_plus.dart';
import 'package:flutter/material.dart';
// import 'package:device_apps/device_apps.dart';
import 'package:shared_preferences/shared_preferences.dart';
// import 'package:flutter_local_notifications/flutter_local_notifications.dart';

import 'AppBlockerService.dart';
import 'InstalledApps.dart';



void main() {
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('App Blocker'),
        ),
        body: InstalledApps(),
        // Center(
        //   child: Column(
        //     mainAxisAlignment: MainAxisAlignment.center,
        //     children: <Widget>[
        //       ElevatedButton(
        //         onPressed: () {
        //           AppBlockerService.startService(3600000); // Block for 1 hour
        //         },
        //         child: Text('Start Blocking'),
        //       ),
        //       ElevatedButton(
        //         onPressed: () {
        //           AppBlockerService.stopService();
        //         },
        //         child: Text('Stop Blocking'),
        //       ),
        //     ],
        //   ),
        // ),
      ),
    );
  }
}

// void main() async {
//   WidgetsFlutterBinding.ensureInitialized();
//   await AndroidAlarmManager.initialize();
//   runApp(MyApp());
// }
//
// void callback() {
//   // This is the function that will be called when the alarm triggers
//   print('Alarm triggered!');
// }
//
// class MyApp extends StatelessWidget {
//   @override
//   Widget build(BuildContext context) {
//     return MaterialApp(
//       home: Scaffold(
//         appBar: AppBar(
//           title: Text('Installed Applications'),
//         ),
//         body: InstalledApps(),
//       ),
//     );
//   }
// }
//
// class InstalledApps extends StatefulWidget {
//   @override
//   _InstalledAppsState createState() => _InstalledAppsState();
// }
//
// class _InstalledAppsState extends State<InstalledApps> {
//   List<Application> selectedApps = [];
//
//   @override
//   Widget build(BuildContext context) {
//     return Column(
//       children: [
//         Expanded(
//           child: FutureBuilder(
//             future: DeviceApps.getInstalledApplications(
//               includeAppIcons: true,
//               includeSystemApps: true,
//               onlyAppsWithLaunchIntent: true,
//             ),
//             builder: (context, snapshot) {
//               if (snapshot.connectionState == ConnectionState.waiting) {
//                 return CircularProgressIndicator();
//               } else {
//                 List<Application> apps = snapshot.data as List<Application>;
//                 return ListView.builder(
//                   itemCount: apps.length,
//                   itemBuilder: (context, index) {
//                     Application app = apps[index];
//                     return CheckboxListTile(
//                       title: Text(app.appName),
//                       subtitle: Text(app.packageName),
//                       value: selectedApps.contains(app),
//                       onChanged: (bool? value) {
//                         setState(() {
//                           if (value != null) {
//                             if (value) {
//                               selectedApps.add(app);
//                             } else {
//                               selectedApps.remove(app);
//                             }
//                           }
//                         });
//                       },
//                     );
//                   },
//                 );
//               }
//             },
//           ),
//         ),
//         Visibility(
//           visible: selectedApps.isNotEmpty,
//           child: ElevatedButton(
//             onPressed: () async {
//               // Save the selected apps to shared preferences
//               SharedPreferences prefs = await SharedPreferences.getInstance();
//               await prefs.setStringList(
//                 'blockedApps',
//                 selectedApps.map((app) => app.packageName).toList(),
//               );
//               // Set the alarm
//               AndroidAlarmManager.oneShot(
//                 Duration(seconds: 5), // Change this to your desired duration
//                 0, // This is a unique ID for this alarm
//                 callback,
//                 exact: true,
//                 wakeup: true,
//               );
//             },
//             child: Text('Block ${selectedApps.length} apps'),
//           ),
//         ),
//       ],
//     );
//   }
// }