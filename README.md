**OpenNannyApp** is an Android application designed to connect with and control a [OpenNannyApi](https://github.com/knowledgeescalation/OpenNannyApi). The app provides a user-friendly interface to monitor environmental conditions, stream video feed, and play music through the connected monitoring device. For full tutorial visit: https://knowledgeescalation.com/posts/open-nanny.

## Features

**Environmental Monitoring**: View real-time sensor data including:
- Temperature
- Humidity
- CO2 levels
- Battery charge status

**Video Streaming**: Live video feed from the monitoring device using WebRTC
  
**Music Player**: Browse and play music files through the monitoring device:
- Directory navigation
- Playback controls (play, pause, stop)
- Volume control
- Playback progress tracking & rewind
- Night Light Control: Toggle between day and night mode for the monitoring device's LED lighting system

## Technical Architecture

**Frontend**: Android app built with Jetpack Compose

**Backend**: RESTful API communication with the monitoring device

**Video Streaming**: WebRTC for real-time video transmission

**Authentication**: Token-based authentication for secure communication

## App Structure

**Main Screen**: Central hub with navigation to the three main features:
- Sensors
- Video
- Music

**Sensors Screen**: Displays environmental data with auto-refresh capability
  
**Video Screen**: Shows live video feed with night light toggle control

**Music Screen**: Hierarchical browser for music directories and files with full playback controls

## Implementation Details

Network Communication
- RESTful API with token-based authentication
- WebSocket connection for WebRTC signaling
- Automatic token refresh mechanism

UI Framework
- Built with Jetpack Compose
- Material Design 3 components
- Responsive layout design

Architecture Pattern
- MVVM (Model-View-ViewModel) architecture
- ViewModel and StateFlow for state management
- Coroutines for asynchronous operations

## Requirements

- Android 7.0 or higher
- Network connection for connecting to the monitoring device
- Monitoring device configured with the appropriate server software

## Setup

1. Install the app on your Android device.
2. Configure the connection settings to point to your monitoring device and enter authentication credentials to establish a secure connection (`app/src/main/res/values/strings.xml`).
```xml
<resources>
	<string name="app_name">OpenNannyApp</string>
	<string name="api_ip">NANNY_IP</string>
	<string name="api_user">USER_NAME</string>
	<string name="api_pass">USER_PASS</string>
</resources>
```
4. Add Root CA certificate (`app/src/main/res/raw/ca.crt`).
3. Enter authentication credentials to establish a secure connection.
4. Navigate through the app to access different monitoring and control features.

