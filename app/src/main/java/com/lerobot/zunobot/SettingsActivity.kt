package com.lerobot.zunobot

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings_container, SettingsFragment())
                .commit()
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences, rootKey)
            
            // Raspberry Pi Connection Settings
            val ipPreference = findPreference<EditTextPreference>("raspberry_pi_ip")
            ipPreference?.summaryProvider = EditTextPreference.SimpleSummaryProvider.getInstance()
            
            val portPreference = findPreference<EditTextPreference>("raspberry_pi_port")
            portPreference?.summaryProvider = EditTextPreference.SimpleSummaryProvider.getInstance()
            
            // Test Connection Button
            val testConnectionPreference = findPreference<Preference>("test_connection")
            testConnectionPreference?.setOnPreferenceClickListener {
                testConnection()
                true
            }
            
            // Auto-connect switch
            val autoConnectPreference = findPreference<SwitchPreferenceCompat>("auto_connect")
            autoConnectPreference?.setOnPreferenceChangeListener { _, newValue ->
                val enabled = newValue as Boolean
                android.widget.Toast.makeText(
                    requireContext(),
                    if (enabled) "Auto-connect enabled" else "Auto-connect disabled",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                true
            }
            
            // Movement duration
            val moveDurationPreference = findPreference<EditTextPreference>("move_duration")
            moveDurationPreference?.summaryProvider = EditTextPreference.SimpleSummaryProvider.getInstance()
            
            // Greeting language
            val greetingLanguagePreference = findPreference<androidx.preference.ListPreference>("greeting_language")
            greetingLanguagePreference?.summaryProvider = androidx.preference.ListPreference.SimpleSummaryProvider.getInstance()
        }
        
        private fun testConnection() {
            val prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(requireContext())
            val ip = prefs.getString("raspberry_pi_ip", "") ?: ""
            val port = prefs.getString("raspberry_pi_port", "5000")?.toIntOrNull() ?: 5000
            
            if (ip.isEmpty()) {
                android.widget.Toast.makeText(
                    requireContext(),
                    "Please enter Raspberry Pi IP address",
                    android.widget.Toast.LENGTH_LONG
                ).show()
                return
            }
            
            // Test connection in background
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val apiClient = RobotApiClient("http://$ip:$port")
                    val response = apiClient.ping()
                    
                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            android.widget.Toast.makeText(
                                requireContext(),
                                "✅ Connection successful!",
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                        } else {
                            android.widget.Toast.makeText(
                                requireContext(),
                                "❌ Connection failed: ${response.code}",
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        val errorMsg = when {
                            e.message?.contains("SSH-2.0") == true -> 
                                "❌ Wrong port! Port $port is SSH (22?). Use port 5000 for the API server."
                            e.message?.contains("Failed to connect") == true -> 
                                "❌ Cannot reach robot. Check IP address and ensure API server is running."
                            else -> "❌ Error: ${e.message}"
                        }
                        android.widget.Toast.makeText(
                            requireContext(),
                            errorMsg,
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }
}
