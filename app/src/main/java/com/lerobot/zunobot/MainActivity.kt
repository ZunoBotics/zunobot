package com.lerobot.zunobot

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    
    private lateinit var prefs: SharedPreferences
    private lateinit var apiClient: RobotApiClient
    private lateinit var connectionManager: ConnectionManager
    
    // UI Elements
    private lateinit var statusText: TextView
    private lateinit var forwardBtn: Button
    private lateinit var backwardBtn: Button
    private lateinit var leftBtn: Button
    private lateinit var rightBtn: Button
    private lateinit var turnLeftBtn: Button
    private lateinit var turnRightBtn: Button
    private lateinit var lookAroundBtn: Button
    private lateinit var greetingBtn: Button
    private lateinit var stopBtn: Button
    private lateinit var detectionStartBtn: Button
    private lateinit var detectionStopBtn: Button
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Set action bar logo
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setLogo(R.drawable.logo_actionbar)
        supportActionBar?.setDisplayUseLogoEnabled(true)
        
        // Initialize preferences
        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        
        // Initialize connection
        connectionManager = ConnectionManager(this, prefs)
        apiClient = connectionManager.getApiClient()
        
        // Setup UI
        setupViews()
        setupButtonListeners()
        
        // Auto-connect if enabled
        if (prefs.getBoolean("auto_connect", false)) {
            connectToRobot()
        }
    }
    
    private fun setupViews() {
        statusText = findViewById(R.id.statusText)
        forwardBtn = findViewById(R.id.forwardBtn)
        backwardBtn = findViewById(R.id.backwardBtn)
        leftBtn = findViewById(R.id.leftBtn)
        rightBtn = findViewById(R.id.rightBtn)
        turnLeftBtn = findViewById(R.id.turnLeftBtn)
        turnRightBtn = findViewById(R.id.turnRightBtn)
        lookAroundBtn = findViewById(R.id.lookAroundBtn)
        greetingBtn = findViewById(R.id.greetingBtn)
        stopBtn = findViewById(R.id.stopBtn)
        detectionStartBtn = findViewById(R.id.detectionStartBtn)
        detectionStopBtn = findViewById(R.id.detectionStopBtn)
        
        updateConnectionStatus(false)
    }
    
    private fun setupButtonListeners() {
        // Movement buttons
        forwardBtn.setOnClickListener { sendMoveCommand("forward") }
        backwardBtn.setOnClickListener { sendMoveCommand("backward") }
        leftBtn.setOnClickListener { sendMoveCommand("left") }
        rightBtn.setOnClickListener { sendMoveCommand("right") }
        
        // Turn buttons
        turnLeftBtn.setOnClickListener { sendTurnCommand("left", 90) }
        turnRightBtn.setOnClickListener { sendTurnCommand("right", 90) }
        
        // Gesture buttons
        lookAroundBtn.setOnClickListener { sendLookAroundCommand() }
        greetingBtn.setOnClickListener { sendGreetingCommand() }
        
        // Stop button
        stopBtn.setOnClickListener { sendStopCommand() }
        
        // Detection buttons
        detectionStartBtn.setOnClickListener { startDetection() }
        detectionStopBtn.setOnClickListener { stopDetection() }
    }
    
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            R.id.action_connect -> {
                connectToRobot()
                true
            }
            R.id.action_disconnect -> {
                disconnectFromRobot()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun connectToRobot() {
        lifecycleScope.launch {
            statusText.text = "Connecting..."
            setButtonsEnabled(false)
            
            val success = connectionManager.connect()
            
            if (success) {
                updateConnectionStatus(true)
                Toast.makeText(this@MainActivity, "Connected to robot", Toast.LENGTH_SHORT).show()
            } else {
                updateConnectionStatus(false)
                Toast.makeText(this@MainActivity, "Connection failed", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun disconnectFromRobot() {
        connectionManager.disconnect()
        updateConnectionStatus(false)
        Toast.makeText(this, "Disconnected", Toast.LENGTH_SHORT).show()
    }
    
    private fun updateConnectionStatus(connected: Boolean) {
        if (connected) {
            statusText.text = "Connected"
            statusText.setTextColor(getColor(android.R.color.holo_green_dark))
            setButtonsEnabled(true)
        } else {
            statusText.text = "Not Connected"
            statusText.setTextColor(getColor(android.R.color.holo_red_dark))
            setButtonsEnabled(false)
        }
    }
    
    private fun setButtonsEnabled(enabled: Boolean) {
        forwardBtn.isEnabled = enabled
        backwardBtn.isEnabled = enabled
        leftBtn.isEnabled = enabled
        rightBtn.isEnabled = enabled
        turnLeftBtn.isEnabled = enabled
        turnRightBtn.isEnabled = enabled
        lookAroundBtn.isEnabled = enabled
        greetingBtn.isEnabled = enabled
        stopBtn.isEnabled = enabled
        detectionStartBtn.isEnabled = enabled
        detectionStopBtn.isEnabled = enabled
    }
    
    private fun sendMoveCommand(direction: String) {
        lifecycleScope.launch {
            try {
                val duration = prefs.getString("move_duration", "1.0")?.toFloat() ?: 1.0f
                val response = apiClient.move(direction, duration)
                
                if (response.isSuccessful) {
                    Toast.makeText(this@MainActivity, "Moving $direction", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MainActivity, "Command failed", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }
    
    private fun sendTurnCommand(direction: String, angle: Int) {
        lifecycleScope.launch {
            try {
                val response = apiClient.turn(direction, angle)
                
                if (response.isSuccessful) {
                    Toast.makeText(this@MainActivity, "Turning $direction", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MainActivity, "Command failed", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }
    
    private fun sendLookAroundCommand() {
        lifecycleScope.launch {
            try {
                val response = apiClient.lookAround()
                
                if (response.isSuccessful) {
                    Toast.makeText(this@MainActivity, "Looking around", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MainActivity, "Command failed", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }
    
    private fun sendGreetingCommand() {
        lifecycleScope.launch {
            try {
                val language = prefs.getString("greeting_language", "auto") ?: "auto"
                val response = apiClient.greeting(language)
                
                if (response.isSuccessful) {
                    Toast.makeText(this@MainActivity, "Playing greeting", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MainActivity, "Command failed", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }
    
    private fun sendStopCommand() {
        lifecycleScope.launch {
            try {
                val response = apiClient.stop()
                
                if (response.isSuccessful) {
                    Toast.makeText(this@MainActivity, "Robot stopped", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MainActivity, "Stop command failed", Toast.LENGTH_SHORT).show()
                }
            } catch (e:Exception) {
                handleError(e)
            }
        }
    }
    
    private fun startDetection() {
        lifecycleScope.launch {
            try {
                val response = apiClient.startDetection()
                response.use {
                    if (it.isSuccessful) {
                        Toast.makeText(this@MainActivity, "Detection started", Toast.LENGTH_SHORT).show()
                    } else {
                        val errorBody = it.body?.string() ?: "Unknown error"
                        Toast.makeText(this@MainActivity, "Failed: Code ${it.code} - $errorBody", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }
    
    private fun stopDetection() {
        lifecycleScope.launch {
            try {
                val response = apiClient.stopDetection()
                response.use {
                    if (it.isSuccessful) {
                        Toast.makeText(this@MainActivity, "Detection stopped", Toast.LENGTH_SHORT).show()
                    } else {
                        val errorBody = it.body?.string() ?: "Unknown error"
                        Toast.makeText(this@MainActivity, "Failed: Code ${it.code} - $errorBody", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }
    
    private fun handleError(e: Exception) {
        runOnUiThread {
            val errorMsg = when {
                e.message?.contains("Failed to connect") == true -> "Cannot connect to robot. Check IP/port and ensure API server is running."
                e.message?.contains("timeout") == true -> "Connection timeout. Robot may be unreachable."
                e.message.isNullOrBlank() -> "Network error. Please check connection and try again."
                else -> "Error: ${e.message}"
            }
            Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show()
            updateConnectionStatus(false)
        }
    }
}
