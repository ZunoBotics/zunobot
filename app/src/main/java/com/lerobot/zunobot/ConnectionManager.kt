package com.lerobot.zunobot

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ConnectionManager(
    private val context: Context,
    private val prefs: SharedPreferences
) {
    
    private var apiClient: RobotApiClient? = null
    private var isConnected = false
    
    /**
     * Get API client instance
     */
    fun getApiClient(): RobotApiClient {
        if (apiClient == null) {
            val baseUrl = getBaseUrl()
            apiClient = RobotApiClient(baseUrl)
        }
        return apiClient!!
    }
    
    /**
     * Connect to Raspberry Pi
     */
    suspend fun connect(): Boolean = withContext(Dispatchers.IO) {
        try {
            val baseUrl = getBaseUrl()
            apiClient = RobotApiClient(baseUrl)
            
            // Test connection
            val response = apiClient!!.ping()
            isConnected = response.isSuccessful
            response.close()
            
            isConnected
        } catch (e: Exception) {
            isConnected = false
            false
        }
    }
    
    /**
     * Disconnect from robot
     */
    fun disconnect() {
        isConnected = false
        // Note: OkHttpClient doesn't need explicit disconnect
    }
    
    /**
     * Check if connected
     */
    fun isConnected(): Boolean = isConnected
    
    /**
     * Get base URL from preferences
     */
    private fun getBaseUrl(): String {
        val ip = prefs.getString("raspberry_pi_ip", "192.168.1.100") ?: "192.168.1.100"
        val port = prefs.getString("raspberry_pi_port", "5000")?.toIntOrNull() ?: 5000
        return "http://$ip:$port"
    }
    
    /**
     * Update connection settings
     */
    fun updateSettings() {
        // Recreate API client with new settings
        val baseUrl = getBaseUrl()
        apiClient = RobotApiClient(baseUrl)
        isConnected = false
    }
}
