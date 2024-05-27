import 'package:flutter/material.dart';

import 'package:flutter/material.dart';

import 'package:flutter/material.dart';

class TimeSelectionBottomSheet extends StatefulWidget {
  final TextEditingController controller;

  const TimeSelectionBottomSheet({Key? key, required this.controller}) : super(key: key);

  @override
  _TimeSelectionBottomSheetState createState() => _TimeSelectionBottomSheetState();
}

class _TimeSelectionBottomSheetState extends State<TimeSelectionBottomSheet> {
  bool showMoreOptions = false;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: EdgeInsets.all(8.0),
      decoration: const BoxDecoration(
        image: DecorationImage(
          image: AssetImage('assets/images/your_image.jpg'), // replace with your image path
          fit: BoxFit.cover,
        ),
      borderRadius: BorderRadius.only(
          topLeft: Radius.circular(16.0),
          topRight: Radius.circular(16.0),
        ),
      ),
      child: GridView.count(
        crossAxisCount: 2,
        children: showMoreOptions ? _buildOptions(true) : _buildOptions(false),
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
      return TextButton(
        onPressed: () {
          if (time['text'] == 'More') {
            setState(() {
              showMoreOptions = true;
            });
          } else if (time['text'] == 'Others') {
            showTimePicker(
              context: context,
              initialTime: TimeOfDay.now(),
            ).then((TimeOfDay? pickedTime) {
              if (pickedTime != null) {
                widget.controller.text = ((pickedTime.hour * 60 + pickedTime.minute) * 60000).toString();
                Navigator.pop(context);
              }
            });
          } else {
            widget.controller.text = time['duration'];
            Navigator.pop(context);
          }
        },
        child: Center(child: Text(time['text'])),
      );
    }).toList();
  }
}
