import 'package:flutter/material.dart';

import 'package:flutter/material.dart';

import 'package:flutter/material.dart';

class TimeSelectionBottomSheet extends StatefulWidget {
  final TextEditingController controller;

  const TimeSelectionBottomSheet({Key? key, required this.controller})
      : super(key: key);

  @override
  _TimeSelectionBottomSheetState createState() =>
      _TimeSelectionBottomSheetState();
}

class _TimeSelectionBottomSheetState extends State<TimeSelectionBottomSheet> {
  bool showMoreOptions = false;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: EdgeInsets.all(8.0),
      decoration: const BoxDecoration(
        image: DecorationImage(
          image: AssetImage(
              'assets/images/your_image.jpg'), // replace with your image path
          fit: BoxFit.cover,
        ),
        borderRadius: BorderRadius.only(
          topLeft: Radius.circular(16.0),
          topRight: Radius.circular(16.0),
        ),
      ),
      child: Column(
        children: [
          const Center(
            child:Text(
              'Put me in',
              style: TextStyle(
                color: Colors.white,
                fontSize: 20.0,
                fontWeight: FontWeight.bold,
              ),
            ),
          ),
          Expanded(
            child: GridView.count(
              crossAxisCount: 2,
              children: showMoreOptions ? _buildOptions(true) : _buildOptions(false),
            ),
          ),
        ],
      ),
    );
  }

  List<Widget> _buildOptions(bool moreOptions) {
    List<Map<String, dynamic>> timeData = [
      {'text': '5 \nMins', 'duration': '300000'},
      {'text': '10 \nMins', 'duration': '600000'},
      {'text': '15 \nMins', 'duration': '900000'},
      {'text': '20 \nMins', 'duration': '1200000'},
      {'text': '30 \nMins', 'duration': '1800000'},
    ];

    if (moreOptions) {
      timeData.addAll([
        {'text': '45 \nMins', 'duration': '2700000'},
        {'text': '1 \nHour', 'duration': '3600000'},
        {'text': 'Others', 'duration': null},
      ]);
    } else {
      timeData.add({'text': 'More', 'duration': null});
    }

    return timeData.map((time) {
      return Padding(
        padding: const EdgeInsets.all(50.0),
        child: Container(
          decoration: BoxDecoration(
            color: Colors.transparent,
            shape: BoxShape.circle,
            border: Border.all(
              color: Colors.white, // Set border color
              width: 3, // Set border width
            ),
          ),
          child: TextButton(
            onPressed: () {
              if (time['text'] == 'More') {
                setState(() {
                  showMoreOptions = true;
                });
              } else if (time['text'] == 'Others') {
                showTimePicker(
                  context: context,
                  initialTime: TimeOfDay.now(),
                  builder: (BuildContext context, Widget? child) {
                    return MediaQuery(
                      data: MediaQuery.of(context).copyWith(alwaysUse24HourFormat: true),
                      child: child!,
                    );
                  },
                ).then((TimeOfDay? pickedTime) {
                  if (pickedTime != null) {

                    int hour = pickedTime.hour;
                    int minute = pickedTime.minute;
                    int currentHour = DateTime.now().hour;
                    int currentMinute = DateTime.now().minute;

                    String selectedTime = '$hour:$minute';
                    print("TimeCheck selectedTime $selectedTime");
                    String currentTime = '$currentHour:$currentMinute';
                    print("TimeCheck currentTime $currentTime");

                    if (hour < currentHour || (hour == currentHour && minute < currentMinute)) {
                      ScaffoldMessenger.of(context).showSnackBar(
                        const SnackBar(
                          content: Text('Please select a time in the future'),
                        ),
                      );
                      return;
                    }else{
                      //get difference in time
                      int difference = (hour - currentHour) * 60 + (minute - currentMinute);
                      print("TimeCheck differenceTime $difference");

                      widget.controller.text = (difference * 60000).toString();
                      Navigator.pop(context);


                    }
                  }
                });
              } else {
                widget.controller.text = time['duration'];
                Navigator.pop(context);
              }
            },
            child: Center(child: Text(time['text'],
              style: const TextStyle(
                color: Colors.white,
                fontSize: 20.0,
                fontWeight: FontWeight.bold,
              ),
            )),
          ),
        ),
      );
    }).toList();
  }
}
