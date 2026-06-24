package com.example.utils

import java.security.MessageDigest

object AccessManager {
    private const val SECRET = "YEMENI_TRADER_SECRET_2026_V1"
    const val ADMIN_PASS = "admin2026"

    fun isAdmin(code: String): Boolean {
        return code == ADMIN_PASS
    }

    fun generateCode(hours: Float): String {
        val expiryMs = System.currentTimeMillis() + (hours * 60 * 60 * 1000).toLong()
        val expiryHex = expiryMs.toString(16).uppercase()
        val signature = hash(expiryHex + SECRET).take(6).uppercase()
        return "$expiryHex-$signature"
    }

    fun isValid(code: String): Boolean {
        try {
            val parts = code.uppercase().split("-")
            if (parts.size != 2) return false
            val expiryHex = parts[0]
            val signature = parts[1]
            
            val expectedSig = hash(expiryHex + SECRET).take(6).uppercase()
            if (signature != expectedSig) return false
            
            val expiryMs = expiryHex.toLong(16)
            if (System.currentTimeMillis() > expiryMs) return false
            
            return true
        } catch (e: Exception) {
            return false
        }
    }
    
    fun getExpiryDateStr(code: String): String {
        try {
            val parts = code.uppercase().split("-")
            val expiryMs = parts[0].toLong(16)
            val date = java.util.Date(expiryMs)
            val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
            return format.format(date)
        } catch (e: Exception) {
            return "غير محدد"
        }
    }

    private fun hash(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
