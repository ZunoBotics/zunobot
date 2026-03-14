#!/bin/bash
# Quick start script for Zunobot Android app

set -e

echo "===================================="
echo "Zunobot Quick Start"
echo "===================================="
echo ""

# Check if we're in the right directory
if [ ! -f "settings.gradle.kts" ]; then
    echo "❌ Error: Run this script from zunobot-app directory"
    exit 1
fi

# Function to display menu
show_menu() {
    echo ""
    echo "What would you like to do?"
    echo "1) Build Android APK"
    echo "2) Install APK on Android device (via ADB)"
    echo "3) Deploy server files to Raspberry Pi"
    echo "4) Test Raspberry Pi connection"
    echo "5) View installation instructions"
    echo "6) Exit"
    echo ""
    read -p "Enter choice [1-6]: " choice
}

# Build Android APK
build_apk() {
    echo ""
    echo "📱 Building Android APK..."
    echo ""
    
    if [ ! -f "gradlew" ]; then
        echo "❌ Gradle wrapper not found. Please open project in Android Studio first."
        return 1
    fi
    
    # Make gradlew executable
    chmod +x gradlew
    
    # Build debug APK
    ./gradlew assembleDebug
    
    if [ -f "app/build/outputs/apk/debug/app-debug.apk" ]; then
        echo ""
        echo "✅ APK built successfully!"
        echo "📍 Location: app/build/outputs/apk/debug/app-debug.apk"
        ls -lh app/build/outputs/apk/debug/app-debug.apk
    else
        echo "❌ Build failed. Check errors above."
        return 1
    fi
}

# Install APK on Android device
install_apk() {
    echo ""
    echo "📲 Installing APK on Android device..."
    echo ""
    
    APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
    
    if [ ! -f "$APK_PATH" ]; then
        echo "❌ APK not found. Please build it first (option 1)."
        return 1
    fi
    
    # Check if adb is available
    if ! command -v adb &> /dev/null; then
        echo "❌ ADB not found. Please install Android SDK Platform Tools."
        echo "   Or manually copy APK to device: $APK_PATH"
        return 1
    fi
    
    # Check if device is connected
    if ! adb devices | grep -q "device$"; then
        echo "❌ No Android device connected via USB."
        echo "   Connect device and enable USB debugging."
        return 1
    fi
    
    # Install APK
    adb install -r "$APK_PATH"
    
    echo ""
    echo "✅ App installed successfully!"
    echo "   Open 'Zunobot' app on your Android device"
}

# Deploy to Raspberry Pi
deploy_to_pi() {
    echo ""
    echo "🥧 Deploy to Raspberry Pi"
    echo ""
    
    read -p "Enter Raspberry Pi IP address: " PI_IP
    read -p "Enter username [pi]: " PI_USER
    PI_USER=${PI_USER:-pi}
    
    echo ""
    echo "Copying files to $PI_USER@$PI_IP..."
    
    # Create directory on Pi
    ssh "$PI_USER@$PI_IP" "mkdir -p /home/$PI_USER/lerobot/zunobot-app/raspberry-pi"
    
    # Copy Python files
    scp raspberry-pi/*.py "$PI_USER@$PI_IP:/home/$PI_USER/lerobot/zunobot-app/raspberry-pi/"
    scp raspberry-pi/requirements.txt "$PI_USER@$PI_IP:/home/$PI_USER/lerobot/zunobot-app/raspberry-pi/"
    scp raspberry-pi/zunobot-api.service "$PI_USER@$PI_IP:/home/$PI_USER/lerobot/zunobot-app/raspberry-pi/"
    
    echo ""
    echo "✅ Files copied successfully!"
    echo ""
    echo "Next steps on Raspberry Pi:"
    echo "1. SSH into Pi: ssh $PI_USER@$PI_IP"
    echo "2. Install dependencies:"
    echo "   cd /home/$PI_USER/lerobot/zunobot-app/raspberry-pi"
    echo "   conda activate lerobot"
    echo "   pip install -r requirements.txt"
    echo "3. Test server:"
    echo "   python robot_api_server.py"
    echo "4. Install service (optional):"
    echo "   sudo cp zunobot-api.service /etc/systemd/system/"
    echo "   sudo systemctl daemon-reload"
    echo "   sudo systemctl enable zunobot-api.service"
    echo "   sudo systemctl start zunobot-api.service"
}

# Test Raspberry Pi connection
test_connection() {
    echo ""
    echo "🔌 Test Raspberry Pi Connection"
    echo ""
    
    read -p "Enter Raspberry Pi IP address: " PI_IP
    PORT=${1:-5000}
    
    echo ""
    echo "Testing connection to $PI_IP:$PORT..."
    
    # Ping test
    echo -n "Ping test... "
    if ping -c 1 -W 2 "$PI_IP" &> /dev/null; then
        echo "✅"
    else
        echo "❌ Cannot ping Raspberry Pi"
        return 1
    fi
    
    # API test
    echo -n "API test... "
    if command -v curl &> /dev/null; then
        RESPONSE=$(curl -s -m 5 "http://$PI_IP:$PORT/api/ping" 2>/dev/null || echo "")
        if echo "$RESPONSE" | grep -q "ok"; then
            echo "✅"
            echo ""
            echo "API Response:"
            echo "$RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$RESPONSE"
            echo ""
            echo "✅ Connection successful!"
            echo "   Enter this in Android app settings:"
            echo "   IP Address: $PI_IP"
            echo "   Port: $PORT"
        else
            echo "❌ API server not responding"
            echo "   Make sure API server is running on Raspberry Pi"
            return 1
        fi
    else
        echo "⚠️  curl not installed, skipping API test"
    fi
}

# View installation instructions
view_instructions() {
    if [ -f "INSTALLATION.md" ]; then
        if command -v less &> /dev/null; then
            less INSTALLATION.md
        elif command -v more &> /dev/null; then
            more INSTALLATION.md
        else
            cat INSTALLATION.md
        fi
    else
        echo "❌ INSTALLATION.md not found"
    fi
}

# Main loop
while true; do
    show_menu
    
    case $choice in
        1)
            build_apk
            ;;
        2)
            install_apk
            ;;
        3)
            deploy_to_pi
            ;;
        4)
            test_connection
            ;;
        5)
            view_instructions
            ;;
        6)
            echo ""
            echo "👋 Goodbye!"
            exit 0
            ;;
        *)
            echo "❌ Invalid option. Please choose 1-6."
            ;;
    esac
    
    echo ""
    read -p "Press Enter to continue..."
done
