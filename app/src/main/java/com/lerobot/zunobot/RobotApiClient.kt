package com.lerobot.zunobot

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class RobotApiClient(private val baseUrl: String) {
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()
    
    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    
    /**
     * Health check endpoint
     */
    suspend fun ping(): Response = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$baseUrl/api/ping")
            .get()
            .build()
        
        client.newCall(request).execute()
    }
    
    /**
     * Get robot status
     */
    suspend fun getStatus(): Response = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$baseUrl/api/status")
            .get()
            .build()
        
        client.newCall(request).execute()
    }
    
    /**
     * Move robot in a direction
     * @param direction: "forward", "backward", "left", "right"
     * @param duration: movement duration in seconds
     */
    suspend fun move(direction: String, duration: Float): Response = withContext(Dispatchers.IO) {
        val json = JSONObject().apply {
            put("direction", direction)
            put("duration", duration)
        }
        
        val body = json.toString().toRequestBody(JSON_MEDIA_TYPE)
        val request = Request.Builder()
            .url("$baseUrl/api/move")
            .post(body)
            .build()
        
        client.newCall(request).execute()
    }
    
    /**
     * Turn/rotate robot
     * @param direction: "left" or "right"
     * @param angle: rotation angle in degrees
     */
    suspend fun turn(direction: String, angle: Int): Response = withContext(Dispatchers.IO) {
        val json = JSONObject().apply {
            put("direction", direction)
            put("angle", angle)
        }
        
        val body = json.toString().toRequestBody(JSON_MEDIA_TYPE)
        val request = Request.Builder()
            .url("$baseUrl/api/turn")
            .post(body)
            .build()
        
        client.newCall(request).execute()
    }
    
    /**
     * Move head to position
     * @param position: "left", "right", or "center"
     */
    suspend fun moveHead(position: String): Response = withContext(Dispatchers.IO) {
        val json = JSONObject().apply {
            put("position", position)
        }
        
        val body = json.toString().toRequestBody(JSON_MEDIA_TYPE)
        val request = Request.Builder()
            .url("$baseUrl/api/head")
            .post(body)
            .build()
        
        client.newCall(request).execute()
    }
    
    /**
     * Perform look around gesture
     */
    suspend fun lookAround(): Response = withContext(Dispatchers.IO) {
        val json = JSONObject()
        val body = json.toString().toRequestBody(JSON_MEDIA_TYPE)
        
        val request = Request.Builder()
            .url("$baseUrl/api/look-around")
            .post(body)
            .build()
        
        client.newCall(request).execute()
    }
    
    /**
     * Play greeting
     * @param language: "auto", "english", "luganda", "kiswahili"
     */
    suspend fun greeting(language: String = "auto"): Response = withContext(Dispatchers.IO) {
        val json = JSONObject().apply {
            put("language", language)
        }
        
        val body = json.toString().toRequestBody(JSON_MEDIA_TYPE)
        val request = Request.Builder()
            .url("$baseUrl/api/greeting")
            .post(body)
            .build()
        
        client.newCall(request).execute()
    }
    
    /**
     * Emergency stop
     */
    suspend fun stop(): Response = withContext(Dispatchers.IO) {
        val json = JSONObject()
        val body = json.toString().toRequestBody(JSON_MEDIA_TYPE)
        
        val request = Request.Builder()
            .url("$baseUrl/api/stop")
            .post(body)
            .build()
        
        client.newCall(request).execute()
    }
    
    /**
     * Start person detection
     */
    suspend fun startDetection(): Response = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$baseUrl/api/detection/start")
            .post("{}".toRequestBody(JSON_MEDIA_TYPE))
            .build()
        
        client.newCall(request).execute()
    }
    
    /**
     * Stop person detection
     */
    suspend fun stopDetection(): Response = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$baseUrl/api/detection/stop")
            .post("{}".toRequestBody(JSON_MEDIA_TYPE))
            .build()
        
        client.newCall(request).execute()
    }
    
    /**
     * Get detection status
     */
    suspend fun getDetectionStatus(): Response = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$baseUrl/api/detection/status")
            .get()
            .build()
        
        client.newCall(request).execute()
    }
}
