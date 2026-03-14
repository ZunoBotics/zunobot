#!/usr/bin/env python3
"""
Zunobot REST API Server
Runs on Raspberry Pi to receive commands from Android app
"""

from flask import Flask, request, jsonify
from flask_cors import CORS
import sys
import os
import logging
import subprocess
import threading
import time
from pathlib import Path

# Add lerobot to path dynamically
_script_dir = Path(__file__).parent
_lerobot_root = _script_dir.parent.parent
sys.path.insert(0, str(_lerobot_root / 'src'))
sys.path.insert(0, str(_lerobot_root / 'lerobot' / 'examples' / 'lekiwi'))

try:
    from robot_controller import RobotController
except ImportError:
    print("Warning: robot_controller not found, using mock mode")
    RobotController = None

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

# Create Flask app
app = Flask(__name__)
CORS(app)  # Enable CORS for cross-origin requests

# Global robot controller
robot = None
detection_process = None
detection_running = False

def init_robot():
    """Initialize robot controller"""
    global robot
    try:
        if RobotController:
            robot = RobotController()
            logger.info("Robot controller initialized")
        else:
            logger.warning("Robot controller not available - running in mock mode")
    except Exception as e:
        logger.error(f"Failed to initialize robot: {e}")
        robot = None

@app.route('/api/ping', methods=['GET'])
def ping():
    """Health check endpoint"""
    return jsonify({
        'status': 'ok',
        'message': 'Zunobot API server is running',
        'robot_available': robot is not None
    })

@app.route('/api/status', methods=['GET'])
def get_status():
    """Get robot status"""
    return jsonify({
        'connected': robot is not None,
        'detection_running': detection_running,
        'timestamp': time.time()
    })

@app.route('/api/move', methods=['POST'])
def move():
    """
    Move robot in a direction
    Body: {"direction": "forward|backward|left|right", "duration": 1.0}
    """
    try:
        data = request.get_json()
        direction = data.get('direction')
        duration = float(data.get('duration', 1.0))
        
        if not robot:
            return jsonify({'error': 'Robot not initialized'}), 503
        
        logger.info(f"Moving {direction} for {duration}s")
        robot.move(direction, duration)
        
        return jsonify({
            'success': True,
            'direction': direction,
            'duration': duration
        })
    except Exception as e:
        logger.error(f"Move error: {e}")
        return jsonify({'error': str(e)}), 500

@app.route('/api/turn', methods=['POST'])
def turn():
    """
    Turn/rotate robot
    Body: {"direction": "left|right", "angle": 90}
    """
    try:
        data = request.get_json()
        direction = data.get('direction')
        angle = int(data.get('angle', 90))
        
        if not robot:
            return jsonify({'error': 'Robot not initialized'}), 503
        
        logger.info(f"Turning {direction} {angle} degrees")
        robot.turn(direction, angle)
        
        return jsonify({
            'success': True,
            'direction': direction,
            'angle': angle
        })
    except Exception as e:
        logger.error(f"Turn error: {e}")
        return jsonify({'error': str(e)}), 500

@app.route('/api/head', methods=['POST'])
def move_head():
    """
    Move head to position
    Body: {"position": "left|right|center"}
    """
    try:
        data = request.get_json()
        position = data.get('position')
        
        if not robot:
            return jsonify({'error': 'Robot not initialized'}), 503
        
        logger.info(f"Moving head to {position}")
        robot.move_head(position)
        
        return jsonify({
            'success': True,
            'position': position
        })
    except Exception as e:
        logger.error(f"Head movement error: {e}")
        return jsonify({'error': str(e)}), 500

@app.route('/api/look-around', methods=['POST'])
def look_around():
    """
    Perform look around gesture
    """
    try:
        if not robot:
            return jsonify({'error': 'Robot not initialized'}), 503
        
        logger.info("Performing look around gesture")
        robot.look_around()
        
        return jsonify({'success': True})
    except Exception as e:
        logger.error(f"Look around error: {e}")
        return jsonify({'error': str(e)}), 500

@app.route('/api/greeting', methods=['POST'])
def greeting():
    """
    Play greeting
    Body: {"language": "auto|english|luganda|kiswahili"}
    """
    try:
        data = request.get_json()
        language = data.get('language', 'auto')
        
        if not robot:
            return jsonify({'error': 'Robot not initialized'}), 503
        
        logger.info(f"Playing {language} greeting")
        robot.play_greeting(language)
        
        return jsonify({
            'success': True,
            'language': language
        })
    except Exception as e:
        logger.error(f"Greeting error: {e}")
        return jsonify({'error': str(e)}), 500

@app.route('/api/stop', methods=['POST'])
def stop():
    """Emergency stop"""
    try:
        if not robot:
            return jsonify({'error': 'Robot not initialized'}), 503
        
        logger.info("Emergency stop")
        robot.stop()
        
        return jsonify({'success': True})
    except Exception as e:
        logger.error(f"Stop error: {e}")
        return jsonify({'error': str(e)}), 500

@app.route('/api/detection/start', methods=['POST'])
def start_detection():
    """Start person detection"""
    global detection_process, detection_running, robot

    try:
        if detection_running:
            return jsonify({'error': 'Detection already running'}), 400

        logger.info("Starting person detection")

        # Disconnect API robot so detection script can connect
        if robot:
            try:
                robot.disconnect()
            except Exception:
                pass
            robot = None

        follower_script = str(_lerobot_root / 'lerobot' / 'examples' / 'lekiwi' / 'autonomous_person_follower.py')
        detection_process = subprocess.Popen([sys.executable, follower_script])

        detection_running = True

        return jsonify({'success': True})
    except Exception as e:
        logger.error(f"Detection start error: {e}")
        return jsonify({'error': str(e)}), 500

@app.route('/api/detection/stop', methods=['POST'])
def stop_detection():
    """Stop person detection"""
    global detection_process, detection_running

    try:
        if not detection_running:
            return jsonify({'error': 'Detection not running'}), 400

        logger.info("Stopping person detection")

        if detection_process:
            detection_process.terminate()
            try:
                detection_process.wait(timeout=5)
            except subprocess.TimeoutExpired:
                detection_process.kill()
            detection_process = None

        detection_running = False

        # Reconnect API robot
        init_robot()

        return jsonify({'success': True})
    except Exception as e:
        logger.error(f"Detection stop error: {e}")
        return jsonify({'error': str(e)}), 500

@app.route('/api/detection/status', methods=['GET'])
def detection_status():
    """Get detection status"""
    return jsonify({
        'running': detection_running,
        'pid': detection_process.pid if detection_process else None
    })

@app.route('/api/rotate', methods=['POST'])
def rotate():
    """
    Rotate robot in place for N full rotations
    Body: {"rotations": 1}
    """
    try:
        data = request.get_json() or {}
        rotations = int(data.get('rotations', 1))

        if not robot:
            return jsonify({'error': 'Robot not initialized'}), 503

        logger.info(f"Rotating {rotations} time(s)")
        robot.rotate(rotations)

        return jsonify({'success': True, 'rotations': rotations})
    except Exception as e:
        logger.error(f"Rotate error: {e}")
        return jsonify({'error': str(e)}), 500

@app.route('/api/voice-command', methods=['POST'])
def voice_command():
    """
    Execute a natural-language voice command
    Body: {"command": "forward"}
    Supported: forward, backward, left, right, turn left, turn right,
               look around, rotate, stop, greet / greeting + synonyms
    """
    try:
        data = request.get_json() or {}
        command = data.get('command', '').lower().strip()

        if not robot:
            return jsonify({'error': 'Robot not initialized'}), 503

        logger.info(f"Voice command: {command}")

        if command in ('forward', 'go forward', 'move forward'):
            robot.move('forward')
        elif command in ('backward', 'back', 'go back', 'move backward'):
            robot.move('backward')
        elif command in ('left', 'move left', 'go left', 'strafe left'):
            robot.move('left')
        elif command in ('right', 'move right', 'go right', 'strafe right'):
            robot.move('right')
        elif command in ('turn left', 'rotate left'):
            robot.turn('left')
        elif command in ('turn right', 'rotate right'):
            robot.turn('right')
        elif command in ('look around', 'look'):
            robot.look_around()
        elif command in ('rotate', 'spin', 'spin around', 'full rotation'):
            robot.rotate(1)
        elif command in ('stop', 'halt', 'freeze'):
            robot.stop()
        elif command in ('greet', 'greeting', 'hello', 'wave'):
            robot.play_greeting('auto')
        else:
            return jsonify({'error': f'Unknown command: {command}'}), 400

        return jsonify({'success': True, 'command': command})
    except Exception as e:
        logger.error(f"Voice command error: {e}")
        return jsonify({'error': str(e)}), 500

if __name__ == '__main__':
    logger.info("Starting Zunobot API server...")
    
    # Initialize robot
    init_robot()
    
    # Get host IP
    import socket
    hostname = socket.gethostname()
    local_ip = socket.gethostbyname(hostname)
    
    logger.info(f"Server starting on {local_ip}:5000")
    logger.info("="*60)
    logger.info("Enter this in Android app settings:")
    logger.info(f"  IP Address: {local_ip}")
    logger.info(f"  Port: 5000")
    logger.info("="*60)
    
    # Run server
    app.run(
        host='0.0.0.0',  # Listen on all interfaces
        port=5000,
        debug=False,
        threaded=True
    )
