# App Blocker

App Blocker is a Flutter application that allows users to block selected applications on their device for a specified duration. This is particularly useful for managing screen time and productivity.

## Features

- List all installed applications on the device.
- Search for applications in the list.
- Select multiple applications to block.
- Set the duration for which the applications should be blocked.
- Block and unblock applications with a single tap.

## Tech Stack

- Flutter
- Dart
- Kotlin
- Java
- Gradle

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes.

### Prerequisites

- Flutter SDK
- Android Studio or VS Code
- An Android or iOS device or emulator

### Installation

1. Clone the repository:
    ```
    git clone https://github.com/yourusername/app-blocker.git
    ```
2. Navigate into the project directory:
    ```
    cd app-blocker
    ```
3. Get Flutter packages:
    ```
    flutter pub get
    ```
4. Run the app:
    ```
    flutter run
    ```

## Project Structure

The Dart code resides in the `lib` directory. The main file is `lib/main.dart`, which initializes the Flutter application. The `lib/InstalledApps.dart` file contains the UI and logic for listing installed applications and handling user interactions. The `lib/AppBlockerService.dart` file is responsible for communicating with the native Android service to start and stop the blocking of applications.

The native Android code is in the `android` directory. The `MainActivity.kt` file initializes the Flutter engine and sets up a MethodChannel for communication between Dart and Kotlin. The `AppBlockerService.kt` file is a foreground service that handles the blocking of applications.

## Contributing

We welcome contributions from the community. If you wish to contribute, please take a moment to review our **CONTRIBUTING.md** guidelines.

## License

This project is licensed under the MIT License - see the **LICENSE.md** file for details.

## Acknowledgments

- Flutter's CheckboxListTile widget for providing an easy way to create a selectable list of installed applications.
- The DeviceApps package for providing a simple API to query installed applications.
- The Android Alarm Manager Plus package for scheduling the blocking of applications.

## Contact

If you have any questions, feel free to open an issue or submit a pull request.

## Getting Started

This project is a starting point for a Flutter application.

A few resources to get you started if this is your first Flutter project:

- [Lab: Write your first Flutter app](https://docs.flutter.dev/get-started/codelab)
- [Cookbook: Useful Flutter samples](https://docs.flutter.dev/cookbook)

For help getting started with Flutter development, view the
[online documentation](https://docs.flutter.dev/), which offers tutorials,
samples, guidance on mobile development, and a full API reference.
