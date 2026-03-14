# Zunobot Remote Control App

Android app for remotely controlling the LeKiwi robot running on Raspberry Pi.

## Features

- 🎮 **Remote Control**: Control robot with simple button taps
- 🔧 **Easy Configuration**: WiFi and Raspberry Pi pairing setup
- 🤖 **Robot Commands**:
  - Movement: Forward, Backward, Left, Right, Rotate
  - Head: Look around gesture
  - Detection: Start/Stop person detection
  - Audio: Play greeting
- 📡 **Real-time Status**: Connection status and robot feedback
- ⚙️ **Settings**: Configure Raspberry Pi IP, port, and credentials

## Architecture

```
Android App (Kotlin) ←→ HTTP/REST API ←→ Raspberry Pi (Python Flask)
```

## Quick Start

### 1. Install on Raspberry Pi

```bash
cd /home/pi/lerobot
python lerobot/examples/lekiwi/robot_api_server.py
```

The server runs on port 5000.

### 2. Install Android App

```bash
# Build APK
cd zunobot-app
./gradlew assembleDebug

# Install on device
adb install app/build/outputs/apk/debug/app-debug.apk
```

### 3. Configure Connection

1. Open Zunobot app
2. Tap Settings (⚙️)
3. Enter Raspberry Pi IP address (e.g., `192.168.1.100`)
4. Port: `5000`
5. Tap "Test Connection"
6. Return to main screen

### 4. Control Robot

Tap buttons to control the robot remotely!

## Project Structure

```
zunobot-app/
├── app/                          # Android app source
│   ├── src/
│   │   └── main/
│   │       ├── java/com/lerobot/zunobot/
│   │       │   ├── MainActivity.kt        # Main control screen
│   │       │   ├── SettingsActivity.kt    # Configuration screen
│   │       │   ├── RobotApiClient.kt      # Network communication
│   │       │   └── ConnectionManager.kt   # Connection handling
│   │       ├── res/
│   │       │   ├── layout/
│   │       │   │   ├── activity_main.xml       # Main UI
│   │       │   │   ├── activity_settings.xml   # Settings UI
│   │       │   │   └── dialog_pairing.xml      # Pairing dialog
│   │       │   ├── values/
│   │       │   │   ├── strings.xml
│   │       │   │   ├── colors.xml
│   │       │   │   └── themes.xml
│   │       │   └── drawable/               # Icons and graphics
│   │       └── AndroidManifest.xml
│   └── build.gradle.kts
├── raspberry-pi/                 # Raspberry Pi server
│   ├── robot_api_server.py      # Flask REST API server
│   ├── robot_controller.py      # Robot command wrapper
│   └── requirements.txt         # Python dependencies
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
└── README.md                     # This file
```

## API Endpoints

### Raspberry Pi REST API

Base URL: `http://<RASPBERRY_PI_IP>:5000`

#### Robot Control

- **POST** `/api/move` - Move robot
  ```json
  {"direction": "forward", "duration": 1.0}
  ```

- **POST** `/api/turn` - Rotate robot
  ```json
  {"direction": "left", "angle": 90}
  ```

- **POST** `/api/head` - Move head
  ```json
  {"position": "left"}  // left, right, center
  ```

- **POST** `/api/look-around` - Perform look around gesture
  ```json
  {}
  ```

- **POST** `/api/greeting` - Play greeting
  ```json
  {"language": "auto"}  // auto, english, luganda, kiswahili
  ```

- **POST** `/api/stop` - Emergency stop
  ```json
  {}
  ```

#### Detection Control

- **POST** `/api/detection/start` - Start person detection
- **POST** `/api/detection/stop` - Stop person detection
- **GET** `/api/detection/status` - Get detection status

#### System

- **GET** `/api/status` - Get robot status
- **GET** `/api/ping` - Health check

## Configuration

### Raspberry Pi Setup

1. **Install dependencies**:
```bash
pip install flask flask-cors
```

2. **Start API server**:
```bash
python raspberry-pi/robot_api_server.py
# Or as systemd service (see raspberry-pi/zunobot-api.service)
```

3. **Get IP address**:
```bash
hostname -I
```

### Android App Settings

- **Raspberry Pi IP**: Local network IP (e.g., `192.168.1.100`)
- **Port**: `5000` (default)
- **Auto-connect**: Enable to connect on app start
- **Connection timeout**: 5 seconds (default)

## Network Requirements

### Same WiFi Network
Both Android device and Raspberry Pi must be on the same WiFi network.

### Firewall Rules
Ensure port 5000 is accessible on Raspberry Pi:
```bash
sudo ufw allow 5000/tcp
```

### Static IP (Recommended)
Configure static IP for Raspberry Pi in router settings for consistent connection.

## Development

### Building the App

```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease
```

### Running on Emulator

1. Start Android emulator
2. Run: `./gradlew installDebug`

### Testing API Server

```bash
# Test with curl
curl http://192.168.1.100:5000/api/ping
curl -X POST http://192.168.1.100:5000/api/greeting -H "Content-Type: application/json" -d '{"language":"english"}'
```

## Troubleshooting

### Cannot Connect to Raspberry Pi

1. **Check IP address**:
   ```bash
   hostname -I  # On Raspberry Pi
   ```

2. **Ping test**:
   ```bash
   ping 192.168.1.100  # From Android device terminal
   ```

3. **Check server running**:
   ```bash
   sudo systemctl status zunobot-api  # If using systemd
   # Or check process
   ps aux | grep robot_api_server
   ```

4. **Check firewall**:
   ```bash
   sudo ufw status
   sudo ufw allow 5000/tcp
   ```

### App Crashes

1. Check Android Studio Logcat
2. Verify API server is running
3. Test endpoints with curl
4. Check network permissions in AndroidManifest.xml

### Slow Response

1. Reduce network timeout in Settings
2. Ensure strong WiFi signal
3. Check Raspberry Pi CPU usage
4. Use static IP for faster DNS resolution

## Security Notes

- API has no authentication (local network only)
- Do not expose to public internet without adding auth
- Use VPN for remote access outside local network
- Consider adding API key authentication for production

## Future Enhancements

- [ ] WebSocket for real-time status updates
- [ ] Camera video streaming
- [ ] Voice command integration
- [ ] Multiple robot support
- [ ] Gesture recording and playback
- [ ] Autonomous mode toggle
- [ ] Battery level monitoring
- [ ] API authentication

## Requirements

### Android App
- Android 8.0 (API 26) or higher
- Internet permission (local network)
- ~10 MB storage

### Raspberry Pi
- Python 3.8+
- Flask, flask-cors
- LeRobot installed
- Port 5000 available

## License

Part of LeRobot project - see main LICENSE file

## Support

For issues, see main project TROUBLESHOOTING.md

---

**Version**: 1.0  
**Last Updated**: March 14, 2026  
**Compatible with**: Raspberry Pi 4/5, Android 8.0+
