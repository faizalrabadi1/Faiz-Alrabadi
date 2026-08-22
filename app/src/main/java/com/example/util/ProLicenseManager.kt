package com.example.util

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class SubscriptionDuration(val code: String, val arabicName: String, val englishName: String, val durationMs: Long) {
    ONE_DAY("1D", "يوم واحد (24 ساعة)", "1 Day (24 Hours)", 24L * 60 * 60 * 1000),
    THREE_DAYS("3D", "3 أيام", "3 Days", 3L * 24 * 60 * 60 * 1000),
    ONE_WEEK("7D", "أسبوع كامل (7 أيام)", "1 Week (7 Days)", 7L * 24 * 60 * 60 * 1000),
    ONE_MONTH("30D", "شهر كامل (30 يوماً)", "1 Month (30 Days)", 30L * 24 * 60 * 60 * 1000),
    THREE_MONTHS("90D", "3 أشهر (90 يوماً)", "3 Months (90 Days)", 90L * 24 * 60 * 60 * 1000),
    ONE_YEAR("365D", "سنة كاملة (365 يوماً)", "1 Year (365 Days)", 365L * 24 * 60 * 60 * 1000),
    LIFETIME("LIFE", "اشتراك دائم (مدى الحياة)", "Lifetime (Unlimited)", Long.MAX_VALUE);

    fun getDisplayName(lang: AppLanguage): String = if (lang == AppLanguage.ARABIC) arabicName else englishName
}

data class GeneratedSerial(
    val serial: String,
    val durationCode: String,
    val clientName: String,
    val createdAt: Long,
    val note: String = ""
)

data class ActiveLicenseInfo(
    val isActive: Boolean,
    val serial: String,
    val durationName: String,
    val activatedAt: Long,
    val expiresAt: Long,
    val isLifetime: Boolean
) {
    fun getFormattedExpiry(): String {
        if (isLifetime) return "دائم / Lifetime"
        if (expiresAt <= 0) return "-"
        return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(expiresAt))
    }

    fun getRemainingTimeFormatted(lang: AppLanguage): String {
        if (isLifetime) return if (lang == AppLanguage.ARABIC) "دائم (بدون انتهاء)" else "Lifetime (No Expiry)"
        val diff = expiresAt - System.currentTimeMillis()
        if (diff <= 0) return if (lang == AppLanguage.ARABIC) "منتهي الصلاحية" else "Expired"
        
        val days = diff / (24 * 60 * 60 * 1000)
        val hours = (diff % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000)
        val minutes = (diff % (60 * 60 * 1000)) / (60 * 1000)

        return if (lang == AppLanguage.ARABIC) {
            when {
                days > 0 -> "$days يوم و $hours ساعة"
                hours > 0 -> "$hours ساعة و $minutes دقيقة"
                else -> "$minutes دقيقة"
            }
        } else {
            when {
                days > 0 -> "$days days, $hours hrs"
                hours > 0 -> "$hours hrs, $minutes mins"
                else -> "$minutes mins"
            }
        }
    }
}

class ProLicenseManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("yemeni_trader_pro_prefs", Context.MODE_PRIVATE)
    
    companion object {
        private const val SECRET_SALT = "FAYEZ_RABADI_PRO_SALT_2026"
        val MASTER_ADMIN_KEYS = listOf(
            "ADMIN-PRO-BOT-2026",
            "FAYEZ-ADMIN-2026",
            "ADMIN-VIP-PRO-777",
            "YEMENI-MASTER-999",
            "ADMIN-2026",
            "ADMIN-PRO-2026",
            "ADMIN",
            "MASTER"
        )
    }

    // Check if the provided key is Master Admin Key
    fun isMasterAdminKey(key: String): Boolean {
        val trimmed = key.trim().uppercase()
        if (MASTER_ADMIN_KEYS.contains(trimmed)) return true
        if (trimmed.startsWith("ADMIN") || trimmed.startsWith("FAYEZ") || trimmed.contains("MASTER") || trimmed.contains("BOT-2026")) return true
        return false
    }

    // Generate SHA-256 Checksum Signature
    private fun generateChecksum(payload: String): String {
        val input = "$payload-$SECRET_SALT"
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02X".format(it) }.take(6)
    }

    // Generate a new serial for client
    fun generateSerial(duration: SubscriptionDuration, clientName: String = "", note: String = ""): GeneratedSerial {
        val randomBlock = UUID.randomUUID().toString().replace("-", "").take(6).uppercase()
        val payload = "PRO-${duration.code}-$randomBlock"
        val checksum = generateChecksum(payload)
        val serial = "$payload-$checksum"

        val generated = GeneratedSerial(
            serial = serial,
            durationCode = duration.code,
            clientName = clientName.ifBlank { "عميل VIP" },
            createdAt = System.currentTimeMillis(),
            note = note
        )

        saveGeneratedSerialToHistory(generated)
        return generated
    }

    // Validate and Activate a serial number
    fun activateSerial(serialInput: String): Result<SubscriptionDuration> {
        val cleaned = serialInput.trim().uppercase()
        
        // Master override key check
        if (isMasterAdminKey(cleaned)) {
            saveActiveLicense(cleaned, SubscriptionDuration.LIFETIME, "Admin Master Access")
            return Result.success(SubscriptionDuration.LIFETIME)
        }

        val parts = cleaned.split("-")
        if (parts.size != 4 || parts[0] != "PRO") {
            return Result.failure(IllegalArgumentException("تنسيق السيريال غير صحيح / Invalid serial format"))
        }

        val durationCode = parts[1]
        val randomBlock = parts[2]
        val providedChecksum = parts[3]

        val expectedDuration = SubscriptionDuration.values().firstOrNull { it.code == durationCode }
            ?: return Result.failure(IllegalArgumentException("نوع الاشتراك غير معروف / Unknown duration code"))

        val payload = "PRO-$durationCode-$randomBlock"
        val calculatedChecksum = generateChecksum(payload)

        if (calculatedChecksum != providedChecksum) {
            return Result.failure(IllegalArgumentException("السيريال غير صالح أو غير معتمد / Invalid or tampered serial"))
        }

        saveActiveLicense(cleaned, expectedDuration, "VIP Member")
        return Result.success(expectedDuration)
    }

    private fun saveActiveLicense(serial: String, duration: SubscriptionDuration, clientName: String) {
        val now = System.currentTimeMillis()
        val expiresAt = if (duration == SubscriptionDuration.LIFETIME) Long.MAX_VALUE else now + duration.durationMs

        prefs.edit()
            .putBoolean("is_pro_active", true)
            .putString("pro_serial", serial)
            .putString("pro_duration_code", duration.code)
            .putLong("pro_activated_at", now)
            .putLong("pro_expires_at", expiresAt)
            .putString("pro_client_name", clientName)
            .apply()
    }

    // Deactivate / Cancel Pro
    fun deactivatePro() {
        prefs.edit()
            .putBoolean("is_pro_active", false)
            .remove("pro_serial")
            .remove("pro_duration_code")
            .remove("pro_activated_at")
            .remove("pro_expires_at")
            .apply()
    }

    // Check if Pro is currently active and not expired
    fun isProActive(): Boolean {
        val isActive = prefs.getBoolean("is_pro_active", false)
        if (!isActive) return false

        val expiresAt = prefs.getLong("pro_expires_at", 0L)
        if (expiresAt == Long.MAX_VALUE) return true // Lifetime

        if (System.currentTimeMillis() > expiresAt) {
            // Expired
            deactivatePro()
            return false
        }
        return true
    }

    // Get Active License Information
    fun getActiveLicenseInfo(): ActiveLicenseInfo {
        val isActive = isProActive()
        val serial = prefs.getString("pro_serial", "") ?: ""
        val durationCode = prefs.getString("pro_duration_code", "") ?: ""
        val activatedAt = prefs.getLong("pro_activated_at", 0L)
        val expiresAt = prefs.getLong("pro_expires_at", 0L)
        val isLifetime = expiresAt == Long.MAX_VALUE

        val durationObj = SubscriptionDuration.values().firstOrNull { it.code == durationCode }
        val durationName = durationObj?.arabicName ?: if (isLifetime) "اشتراك دائم" else "برو"

        return ActiveLicenseInfo(
            isActive = isActive,
            serial = serial,
            durationName = durationName,
            activatedAt = activatedAt,
            expiresAt = expiresAt,
            isLifetime = isLifetime
        )
    }

    // Generated Serials History (for Admin Dashboard)
    fun getGeneratedSerialsHistory(): List<GeneratedSerial> {
        val rawJson = prefs.getString("generated_serials_history", "[]") ?: "[]"
        val list = mutableListOf<GeneratedSerial>()
        try {
            val jsonArray = JSONArray(rawJson)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    GeneratedSerial(
                        serial = obj.getString("serial"),
                        durationCode = obj.getString("durationCode"),
                        clientName = obj.optString("clientName", "عميل VIP"),
                        createdAt = obj.getLong("createdAt"),
                        note = obj.optString("note", "")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list.sortedByDescending { it.createdAt }
    }

    private fun saveGeneratedSerialToHistory(item: GeneratedSerial) {
        val current = getGeneratedSerialsHistory().toMutableList()
        current.add(0, item)
        val jsonArray = JSONArray()
        current.take(200).forEach { s ->
            val obj = JSONObject()
            obj.put("serial", s.serial)
            obj.put("durationCode", s.durationCode)
            obj.put("clientName", s.clientName)
            obj.put("createdAt", s.createdAt)
            obj.put("note", s.note)
            jsonArray.put(obj)
        }
        prefs.edit().putString("generated_serials_history", jsonArray.toString()).apply()
    }

    fun deleteGeneratedSerial(serial: String) {
        val current = getGeneratedSerialsHistory().filterNot { it.serial == serial }
        val jsonArray = JSONArray()
        current.forEach { s ->
            val obj = JSONObject()
            obj.put("serial", s.serial)
            obj.put("durationCode", s.durationCode)
            obj.put("clientName", s.clientName)
            obj.put("createdAt", s.createdAt)
            obj.put("note", s.note)
            jsonArray.put(obj)
        }
        prefs.edit().putString("generated_serials_history", jsonArray.toString()).apply()
    }
}
