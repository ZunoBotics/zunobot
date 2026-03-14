# Zunobot Installation Guide

Complete step-by-step guide for installing the Zunobot remote control system.

## Overview

This guide covers:
1. Setting up the REST API server on Raspberry Pi
2. Building and installing the Android app
3. Connecting the app to your robot

---

## Part 1: Raspberry Pi Setup

### Prerequisites

- Raspberry Pi with LeRobot installed
- Python 3.8+ with conda environment
- Internet connection

### Step 1: Install Dependencies

```bash
# Activate lerobot environment
conda activate lerobot

# Install Flask and other dependencies
pip install flask==3.0.0 flask-cors==4.0.0

# Or install from requirements file
cd /home/pi/lerobot/zunobot-app/raspberry-pi
pip install -r requirements.txt
```

### Step 2: Get Raspberry Pi IP Address

```bash
# Find your IP address
hostname -I

# Example output: 192.168.1.100
# Write this down - you'll need it for the Android app
```

### Step 3: Test API Server Manually

```bash
cd /home/pi/lerobot/zunobot-app/raspberry-pi
python robot_api_server.py
```

You should see:
```
Starting Zunobot API server...
Robot controller initialized
Server starting on 192.168.1.100:5000
============================================================
Enter this in Android app settings:
  IP Address: 192.168.1.100
  Port: 5000
============================================================
```

**Keep this terminal open** while testing the Android app.

### Step 4: Test API with curl

Open another terminal and test:

```bash
# Health check
curl http://localhost:5000/api/ping

# Expected response:
# {"status":"ok","message":"Zunobot API server is running","robot_available":true}
```

### Step 5: Set Up Auto-Start (Optional)

To make the API server start automatically on boot:

```bash
# Install systemd service
sudo cp zunobot-api.service /etc/systemd/system/
sudo systemctl daemon-reload

# Enable and start
sudo systemctl enable zunobot-api.service
sudo systemctl start zunobot-api.service

# Check status
sudo systemctl status zunobot-api.service

# View logs
sudo journalctl -u zunobot-api.service -f
```

### Step 6: Configure Firewall

```bash
# Allow port 5000
sudo ufw allow 5000/tcp

# Check firewall status
sudo ufw status
```

---

## Part 2: Android App Setup

### Prerequisites

- Android Studio installed on development computer
- Android device or emulator (Android 8.0+)
- USB debugging enabled on Android device

### Step 1: Open Project in Android Studio

```bash
# Open Android Studio
# File → Open → Select zunobot-app folder
```

Wait for Gradle sync to complete.

### Step 2: Build the App

#### Option A: Build APK (recommended for installation)

```bash
# From command line
cd zunobot-app
./gradlew assembleDebug

# APK will be at:
# app/build/outputs/apk/debug/app-debug.apk
```

#### Option B: Build in Android Studio

1. Build → Build Bundle(s) / APK(s) → Build APK(s)
2. Wait for build to complete
3. Click "locate" to find APK file

### Step 3: Install on Android Device

#### Option A: ADB Install

```bash
# Connect Android device via USB
# Enable USB debugging on device

# Install APK
adb install app/build/outputs/apk/debug/app-debug.apk
```

#### Option B: Direct Install

1. Copy APK to Android device (USB, email, cloud storage)
2. Open APK file on device
3. Allow installation from unknown sources if prompted
4. Tap "Install"

#### Option C: Run from Android Studio

1. Connect device via USB
2. Select device in Android Studio toolbar
3. Click Run (green triangle)

---

## Part 3: Connecting App to Robot

### Step 1: Ensure Same WiFi Network

**Critical**: Android device and Raspberry Pi must be on the **same WiFi network**.

### Step 2: Configure App

1. Open Zunobot app on Android
2. Tap menu (⋮) → Settings
3. Enter configuration:
   - **Raspberry Pi IP**: `192.168.1.100` (from Part 1, Step 2)
   - **Port**: `5000`
   - **Auto-connect**: Enable (optional)
4. Tap "Test Connection"
5. You should see: "✅ Connection successful!"

### Step 3: Connect and Control

1. Return to main screen
2. Tap menu (⋮) → Connect
3. Status should change to "Connected" (green)
4. Tap buttons to control robot!

---

## Part 4: Usage

### Basic Controls

| Button | Action |
|--------|--------|
| ⬆️ Forward | Move forward 1 second |
| ⬇️ Backward | Move backward 1 second |
| ⬅️ Left | Strafe left 1 second |
| ➡️ Right | Strafe right 1 second |
| ↶ Turn Left | Rotate 90° counterclockwise |
| ↷ Turn Right | Rotate 90° clockwise |
| 👀 Look Around | Head gesture (left→right→center) |
| 🗣️ Play Greeting | Multilingual greeting |
| ▶️ Start Detection | Start autonomous person detection |
| ⏹️ Stop Detection | Stop autonomous detection |
| 🛑 EMERGENCY STOP | Immediate stop all movement |

### Settings

- **Movement Duration**: Change how long movement commands last (default: 1.0s)
- **Greeting Language**: Choose language for greetings (auto rotates through all)

---

## Troubleshooting

### "Connection failed" Error

1. **Check IP address**:
   ```bash
   # On Raspberry Pi
   hostname -I
   ```

2. **Verify API server running**:
   ```bash
   # On Raspberry Pi
   ps aux | grep robot_api_server
   # Or if using systemd:
   sudo systemctl status zunobot-api.service
   ```

3. **Check firewall**:
   ```bash
   sudo ufw status
   sudo ufw allow 5000/tcp
   ```

4. **Test from Raspberry Pi**:
   ```bash
   curl http://localhost:5000/api/ping
   ```

5. **Ping Raspberry Pi from Android**:
   - Install network utility app
   - Ping Raspberry Pi IP
   - If ping fails, check WiFi connection

### "Robot not initialized" Error

1. **Check LeRobot installation**:
   ```bash
   conda activate lerobot
   python -c "from lerobot.robots.lekiwi import LeKiwi; print('OK')"
   ```

2. **Check robot connection**:
   ```bash
   ls /dev/ttyUSB* /dev/ttyACM*
   ```

3. **Restart API server**:
   ```bash
   sudo systemctl restart zunobot-api.service
   ```

### App Crashes

1. **Check Android version**: Requires Android 8.0+
2. **Clear app data**: Settings → Apps → Zunobot → Clear Data
3. **Reinstall app**:
   ```bash
   adb uninstall com.lerobot.zunobot
   adb install app-debug.apk
   ```

### Commands Not Working

1. **Check API server logs**:
   ```bash
   sudo journalctl -u zunobot-api.service -n 100
   ```

2. **Test endpoint manually**:
   ```bash
   curl -X POST http://192.168.1.100:5000/api/move \
     -H "Content-Type: application/json" \
     -d '{"direction":"forward","duration":1.0}'
   ```

3. **Check robot motors powered**: Ensure robot is powered on

### Different WiFi Networks

**Problem**: Android and Raspberry Pi on different networks

**Solution**:
1. Connect both to same WiFi network
2. Or set up Raspberry Pi as WiFi hotspot
3. Or use VPN for remote access

---

## Advanced Configuration

### Static IP for Raspberry Pi

Configure static IP in router settings:

1. Access router admin panel
2. Find DHCP settings
3. Reserve IP for Raspberry Pi MAC address
4. Recommended: `192.168.1.100`

### Remote Access (Outside Local Network)

**Option 1: VPN**
- Set up VPN server on local network
- Connect Android device to VPN
- Access Raspberry Pi via local IP

**Option 2: Port Forwarding** (Not recommended - security risk)
- Forward port 5000 on router
- Add authentication to API (not implemented)

### Multiple Robots

To control multiple robots:

1. Run API server on different port for each robot:
   ```python
   app.run(host='0.0.0.0', port=5001)  # Robot 2
   ```

2. Save different profiles in Android app settings

---

## Performance Optimization

### API Server

- Already configured for production mode
- Uses threading for concurrent requests
- Connection timeout: 5 seconds

### Android App

- Uses connection pooling (OkHttp)
- Background thread for network calls
- Efficient coroutines for async operations

---

## Security Notes

⚠️ **Current implementation has NO authentication**

**Safe for**:
- Local network use only
- Trusted WiFi networks
- Home/lab environments

**NOT safe for**:
- Public networks
- Internet exposure
- Production deployment

**To add authentication** (future enhancement):
- Implement API key in server
- Add authorization header in app
- Use HTTPS instead of HTTP

---

## Maintenance

### Update API Server

```bash
cd /home/pi/lerobot/zunobot-app/raspberry-pi
git pull  # If using git
sudo systemctl restart zunobot-api.service
```

### Update Android App

1. Build new APK
2. Install over existing app (data preserved)

### Backup Configuration

Android app settings stored in shared preferences (auto-backed up by Android).

---

## Development

### Modifying API Endpoints

Edit `raspberry-pi/robot_api_server.py`:

```python
@app.route('/api/custom', methods=['POST'])
def custom_command():
    # Your code here
    return jsonify({'success': True})
```

### Adding App Features

1. Edit `MainActivity.kt` for new buttons
2. Add API client method in `RobotApiClient.kt`
3. Update layout in `activity_main.xml`

### Testing

```bash
# Android app
./gradlew test

# API server (manual testing)
curl -X POST http://localhost:5000/api/test
```

---

## Quick Reference

### Raspberry Pi Commands

```bash
# Start server manually
python robot_api_server.py

# Start as service
sudo systemctl start zunobot-api.service

# View logs
sudo journalctl -u zunobot-api.service -f

# Get IP address
hostname -I
```

### Android Commands

```bash
# Build APK
./gradlew assembleDebug

# Install
adb install app/build/outputs/apk/debug/app-debug.apk

# View logs
adb logcat | grep Zunobot
```

---

## Support

For issues:

1. Check troubleshooting section above
2. View API server logs: `sudo journalctl -u zunobot-api.service`
3. View Android logs: `adb logcat`
4. See main project documentation

---

**Installation complete!** 🎉

You can now control your robot wirelessly from your Android device.
