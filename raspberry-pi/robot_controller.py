"""
Robot Controller Wrapper
Wraps LeRobot commands for API server
"""

import sys
import time
from pathlib import Path

# Add lerobot src to path dynamically
sys.path.insert(0, str(Path(__file__).parent.parent.parent / 'src'))
sys.path.insert(0, str(Path(__file__).parent.parent.parent / 'lerobot' / 'examples' / 'lekiwi'))

from lerobot.robots.lekiwi import LeKiwi, LeKiwiConfig

class RobotController:
    """Wrapper for robot control commands"""
    
    # Head positions
    HEAD_LEFT = -85.0
    HEAD_RIGHT = -20.0
    HEAD_CENTER = -53.651
    
    # Movement speeds (reduced for safety)
    LINEAR_SPEED = 0.08  # m/s
    ANGULAR_SPEED = 20.0  # degrees/s
    
    def __init__(self):
        """Initialize robot connection"""
        config = LeKiwiConfig(
            id="default",
            port="/dev/ttyACM1",
            use_degrees=False,
            cameras={}
        )
        self.robot = LeKiwi(config)
        self.robot.connect()

        # Neutral pose
        self.neutral_arm = {
            "arm_shoulder_pan.pos": 0.586,
            "arm_shoulder_lift.pos": 1.953,
            "arm_elbow_flex.pos": -79.102,
            "arm_wrist_flex.pos": -11.621,
            "arm_head_pan.pos": self.HEAD_CENTER,
        }

        # Set neutral position
        self.robot.send_action({
            **self.neutral_arm,
            "x.vel": 0.0,
            "y.vel": 0.0,
            "theta.vel": 0.0,
        })
        time.sleep(0.5)
    
    def move(self, direction: str, duration: float = 1.0):
        """
        Move robot in a direction
        
        Args:
            direction: "forward", "backward", "left", "right"
            duration: movement duration in seconds
        """
        x, y, theta = 0.0, 0.0, 0.0
        
        if direction == "forward":
            x = self.LINEAR_SPEED
        elif direction == "backward":
            x = -self.LINEAR_SPEED
        elif direction == "left":
            y = self.LINEAR_SPEED
        elif direction == "right":
            y = -self.LINEAR_SPEED
        else:
            raise ValueError(f"Unknown direction: {direction}")
        
        # Send movement command
        self.robot.send_action({
            **self.neutral_arm,
            "x.vel": x,
            "y.vel": y,
            "theta.vel": 0.0,
        })
        
        # Sleep for duration
        time.sleep(duration)
        
        # Stop
        self.stop()
    
    def turn(self, direction: str, angle: int = 90):
        """
        Turn/rotate robot
        
        Args:
            direction: "left" or "right"
            angle: rotation angle in degrees
        """
        theta = 0.0
        
        if direction == "left":
            theta = self.ANGULAR_SPEED
        elif direction == "right":
            theta = -self.ANGULAR_SPEED
        else:
            raise ValueError(f"Unknown direction: {direction}")
        
        # Calculate duration
        duration = abs(angle / self.ANGULAR_SPEED)
        
        # Send rotation command
        self.robot.send_action({
            **self.neutral_arm,
            "x.vel": 0.0,
            "y.vel": 0.0,
            "theta.vel": theta,
        })
        
        # Sleep for duration
        time.sleep(duration)
        self.stop()

    def rotate(self, rotations: int = 1):
        """
        Rotate robot in-place for a number of full rotations

        Args:
            rotations: number of 360-degree rotations
        """
        duration = (360.0 / self.ANGULAR_SPEED) * rotations

        self.robot.send_action({
            **self.neutral_arm,
            "x.vel": 0.0,
            "y.vel": 0.0,
            "theta.vel": self.ANGULAR_SPEED,
        })

        time.sleep(duration)
        self.stop()

    def move_head(self, position: str):
        """
        Move head to position
        
        Args:
            position: "left", "right", or "center"
        """
        if position == "left":
            head_pos = self.HEAD_LEFT
        elif position == "right":
            head_pos = self.HEAD_RIGHT
        elif position == "center":
            head_pos = self.HEAD_CENTER
        else:
            raise ValueError(f"Unknown position: {position}")
        
        # Send head command
        arm_with_head = self.neutral_arm.copy()
        arm_with_head["arm_head_pan.pos"] = head_pos
        
        self.robot.send_action({
            **arm_with_head,
            "x.vel": 0.0,
            "y.vel": 0.0,
            "theta.vel": 0.0,
        })
        
        time.sleep(1.0)
    
    def look_around(self):
        """Perform look around gesture (left -> right -> center)"""
        positions = ["left", "right", "center"]
        
        for pos in positions:
            self.move_head(pos)
            time.sleep(1.0)
    
    def play_greeting(self, language: str = "auto"):
        """
        Play greeting audio
        
        Args:
            language: "auto", "english", "luganda", "kiswahili"
        """
        from gtts import gTTS
        import pygame
        import tempfile
        from pathlib import Path
        
        # Greeting texts
        greetings = {
            "english": {"text": "Welcome, Thank you for coming", "lang": "en", "tld": "com"},
            "luganda": {"text": "Nkulamusiza, weebale kujja", "lang": "en", "tld": "co.ug"},
            "kiswahili": {"text": "Karibu, asante kuja", "lang": "sw", "tld": "co.ke"},
        }
        
        # Select greeting
        if language == "auto":
            import random
            language = random.choice(list(greetings.keys()))
        
        greeting = greetings.get(language, greetings["english"])
        
        # Generate audio
        tts = gTTS(greeting["text"], lang=greeting["lang"], tld=greeting["tld"])
        temp_file = Path(tempfile.gettempdir()) / f"greeting_{language}.mp3"
        tts.save(str(temp_file))
        
        # Play audio
        pygame.mixer.init()
        pygame.mixer.music.load(str(temp_file))
        pygame.mixer.music.play()
        
        while pygame.mixer.music.get_busy():
            time.sleep(0.1)
        
        pygame.mixer.quit()
        temp_file.unlink()
    
    def stop(self):
        """Emergency stop"""
        self.robot.send_action({
            **self.neutral_arm,
            "x.vel": 0.0,
            "y.vel": 0.0,
            "theta.vel": 0.0,
        })
    
    def disconnect(self):
        """Disconnect from robot"""
        self.stop()
        self.robot.disconnect()
