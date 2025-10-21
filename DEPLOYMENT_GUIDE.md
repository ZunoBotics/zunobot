# ZunoBot Deployment Guide

## Repository Information
- **GitHub Repository**: https://github.com/ZunoBotics/zunobot.git
- **Branch**: main
- **Last Updated**: October 21, 2025

## Successfully Pushed to GitHub ✓

Your updated LeRobot code with custom LeKiwi configuration has been successfully pushed to the ZunoBotics repository!

## What Was Uploaded

1. **Latest LeRobot Code**: Based on upstream HuggingFace LeRobot (commit b954337a)
2. **Custom LeKiwi Configuration**: Your IP, camera settings, and motor configurations
3. **All Examples**: Including LeKiwi teleoperation, recording, replay, and evaluation scripts
4. **Complete Source Code**: 481 files with 106,529+ lines of code

## Deploying to Raspberry Pi

### Step 1: SSH into Your Raspberry Pi

```bash
ssh pi@192.168.1.19
# Or whatever your Pi's username and IP is
```

### Step 2: Clone the Repository

```bash
cd ~
git clone https://github.com/ZunoBotics/zunobot.git lerobot
cd lerobot
```

### Step 3: Install Dependencies

```bash
# Create conda environment
conda create -y -n lerobot python=3.10
conda activate lerobot

# Install LeRobot with Feetech motor support
pip install --no-binary=av -e ".[feetech]"
```

### Step 4: Verify Installation

```bash
# Check if lerobot is installed
python -c "import lerobot; print(lerobot.__version__)"

# Find your motor bus port
python -m lerobot.scripts.lerobot_find_port
```

### Step 5: Run LeKiwi Host Server

On the Raspberry Pi:

```bash
cd ~/lerobot
python -m lerobot.robots.lekiwi.lekiwi_host
```

This will:
- Start the LeKiwi robot server
- Listen for commands on port 5555
- Stream observations on port 5556
- Use your configured cameras (/dev/video0, /dev/video2)

### Step 6: Teleoperate from Your Laptop

On your laptop/computer:

```bash
cd ~/lerobot/lerobot  # Navigate to your local lerobot directory
python examples/lekiwi/teleoperate.py
```

**Note**: You'll need to update the IP address in `examples/lekiwi/teleoperate.py`:
```python
robot_config = LeKiwiClientConfig(remote_ip="192.168.1.19", id="my_lekiwi")
```

## Custom Configurations Already Set

### Network Configuration
- **Raspberry Pi IP**: 192.168.1.19
- **Command Port**: 5555
- **Video Port**: 5556

### Camera Configuration
Located in: `src/lerobot/robots/lekiwi/config_lekiwi.py`

```python
"front": OpenCVCameraConfig(
    index_or_path="/dev/video0",
    fps=30,
    width=640,
    height=480,
    rotation=Cv2Rotation.ROTATE_180
)
"wrist": OpenCVCameraConfig(
    index_or_path="/dev/video2",
    fps=30,
    width=480,
    height=640,
    rotation=Cv2Rotation.ROTATE_90
)
```

### Motor Configuration
- Port: `/dev/ttyACM0` (default, update if different)
- Motor IDs: 1-6 for arm, 7-9 for wheels
- Calibration data: Will be loaded from `.cache/calibration/lekiwi/`

## Available Scripts

### On Raspberry Pi (Host)
```bash
# Run the LeKiwi host server
python -m lerobot.robots.lekiwi.lekiwi_host

# Find USB port for motors
python -m lerobot.scripts.lerobot_find_port

# Calibrate motors
python -m lerobot.scripts.lerobot_calibrate --robot.type=lekiwi
```

### On Your Laptop (Client)
```bash
# Teleoperate the robot
python examples/lekiwi/teleoperate.py

# Record demonstrations
python examples/lekiwi/record.py

# Replay recorded episodes
python examples/lekiwi/replay.py

# Evaluate a trained policy
python examples/lekiwi/evaluate.py
```

## Troubleshooting

### If cameras are not found:
```bash
# List available cameras
ls /dev/video*

# Test camera
python -m lerobot.scripts.lerobot_find_cameras
```

### If motors are not connecting:
```bash
# Find the USB port
python -m lerobot.scripts.lerobot_find_port

# Update the port in config_lekiwi.py if needed
```

### If connection fails:
1. Check Raspberry Pi IP: `hostname -I` on the Pi
2. Verify ports 5555 and 5556 are open
3. Ensure both devices are on the same network
4. Check firewall settings

## File Structure

```
lerobot/
├── src/lerobot/
│   ├── robots/lekiwi/          # LeKiwi robot implementation
│   │   ├── config_lekiwi.py    # Configuration (your IP is here)
│   │   ├── lekiwi.py           # Robot class
│   │   ├── lekiwi_client.py    # Client for laptop
│   │   └── lekiwi_host.py      # Server for Raspberry Pi
│   ├── motors/feetech/         # Feetech motor drivers
│   ├── cameras/opencv/         # Camera interface
│   └── scripts/                # Utility scripts
├── examples/lekiwi/            # Usage examples
│   ├── teleoperate.py
│   ├── record.py
│   ├── replay.py
│   └── evaluate.py
└── docs/source/lekiwi.mdx      # Documentation
```

## Next Steps

1. ✅ Code is now on GitHub
2. Clone on Raspberry Pi (see Step 2 above)
3. Install dependencies
4. Run lekiwi_host on Pi
5. Run teleoperate on laptop
6. Start collecting data!

## Need Help?

- **Documentation**: `docs/source/lekiwi.mdx`
- **Discord**: #mobile-so-100-arm channel
- **GitHub Issues**: https://github.com/huggingface/lerobot/issues
- **Your Backup**: `~/lerobot_full_backup_20251021_093410/`

---

**Repository**: https://github.com/ZunoBotics/zunobot.git
**Ready to clone on Raspberry Pi!** 🚀
