package com.example.data.model

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

data class TutorProfile(
    val tutorName: String = "Tutor",
    val tutorPhone: String = "",
    val qrCodePath: String = "",
    val autoSendSms: Boolean = false
)

class TutorSettingsManager(private val context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("tutor_settings_prefs", Context.MODE_PRIVATE)

    private val _profileFlow = MutableStateFlow(loadProfile())
    val profileFlow: StateFlow<TutorProfile> = _profileFlow.asStateFlow()

    fun getProfile(): TutorProfile {
        return loadProfile()
    }

    private fun loadProfile(): TutorProfile {
        val name = prefs.getString(KEY_TUTOR_NAME, "Tutor") ?: "Tutor"
        val phone = prefs.getString(KEY_TUTOR_PHONE, "") ?: ""
        val qrPath = prefs.getString(KEY_QR_PATH, "") ?: ""
        val autoSend = prefs.getBoolean(KEY_AUTO_SEND, false)

        // Verify if QR file actually exists, if not clear invalid path
        val validQrPath = if (qrPath.isNotBlank() && File(qrPath).exists()) qrPath else ""

        return TutorProfile(
            tutorName = name,
            tutorPhone = phone,
            qrCodePath = validQrPath,
            autoSendSms = autoSend
        )
    }

    fun updateProfile(tutorName: String, tutorPhone: String, autoSendSms: Boolean) {
        prefs.edit()
            .putString(KEY_TUTOR_NAME, tutorName.trim())
            .putString(KEY_TUTOR_PHONE, tutorPhone.trim())
            .putBoolean(KEY_AUTO_SEND, autoSendSms)
            .apply()
        _profileFlow.value = loadProfile()
    }

    fun setQrCodePath(path: String) {
        prefs.edit().putString(KEY_QR_PATH, path).apply()
        _profileFlow.value = loadProfile()
    }

    fun clearQrCode() {
        val currentPath = prefs.getString(KEY_QR_PATH, "") ?: ""
        if (currentPath.isNotBlank()) {
            try {
                File(currentPath).delete()
            } catch (_: Exception) {}
        }
        prefs.edit().remove(KEY_QR_PATH).apply()
        _profileFlow.value = loadProfile()
    }

    companion object {
        private const val KEY_TUTOR_NAME = "key_tutor_name"
        private const val KEY_TUTOR_PHONE = "key_tutor_phone"
        private const val KEY_QR_PATH = "key_qr_path"
        private const val KEY_AUTO_SEND = "key_auto_send"

        @Volatile
        private var INSTANCE: TutorSettingsManager? = null

        fun getInstance(context: Context): TutorSettingsManager {
            return INSTANCE ?: synchronized(this) {
                val instance = TutorSettingsManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
