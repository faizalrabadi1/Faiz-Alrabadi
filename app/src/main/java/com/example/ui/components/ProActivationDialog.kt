package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.util.ActiveLicenseInfo
import com.example.util.AppLanguage
import com.example.util.AppStrings
import com.example.util.ProLicenseManager

@Composable
fun ProActivationDialog(
    currentLanguage: AppLanguage,
    licenseManager: ProLicenseManager,
    onDismiss: () -> Unit,
    onActivatedSuccess: () -> Unit
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    var serialInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    val licenseInfo = remember { licenseManager.getActiveLicenseInfo() }

    val goldGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFFFFD700), Color(0xFFFFA500))
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Diamond,
                    contentDescription = null,
                    tint = Color(0xFFFFB300),
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = AppStrings.proActivationTitle(currentLanguage),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // If Pro is already active, show details
                if (licenseInfo.isActive) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF4CAF50))
                                Text(
                                    text = AppStrings.proActiveMessage(currentLanguage),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = AppStrings.subscriptionType(currentLanguage),
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = licenseInfo.durationName,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = AppStrings.remainingTime(currentLanguage),
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = licenseInfo.getRemainingTimeFormatted(currentLanguage),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (!licenseInfo.isLifetime) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = AppStrings.expiresOn(currentLanguage),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = licenseInfo.getFormattedExpiry(),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Text(
                        text = AppStrings.proActivationDesc(currentLanguage),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Input Field for Serial Number
                OutlinedTextField(
                    value = serialInput,
                    onValueChange = { 
                        serialInput = it
                        errorMessage = null
                    },
                    label = { Text(AppStrings.serialInputLabel(currentLanguage), style = MaterialTheme.typography.bodySmall) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    trailingIcon = {
                        IconButton(onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = clipboard.primaryClip
                            if (clip != null && clip.itemCount > 0) {
                                serialInput = clip.getItemAt(0).text.toString().trim()
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.ContentPaste,
                                contentDescription = AppStrings.pasteFromClipboard(currentLanguage)
                            )
                        }
                    }
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                if (successMessage != null) {
                    Text(
                        text = successMessage ?: "",
                        color = Color(0xFF4CAF50),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Contact Channel Button for buying/requesting serial
                TextButton(
                    onClick = { uriHandler.openUri("https://www.youtube.com/@Yemeni-trader") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFB300),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = AppStrings.contactToBuyPro(currentLanguage),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (serialInput.isBlank()) {
                        errorMessage = if (currentLanguage == AppLanguage.ARABIC) "يرجى كتابة السيريال أولاً" else "Please enter serial first"
                        return@Button
                    }
                    val result = licenseManager.activateSerial(serialInput)
                    if (result.isSuccess) {
                        val dur = result.getOrNull()
                        successMessage = if (currentLanguage == AppLanguage.ARABIC) "تم تفعيل الاشتراك بنجاح (${dur?.arabicName})" else "License activated successfully (${dur?.englishName})"
                        errorMessage = null
                        onActivatedSuccess()
                    } else {
                        errorMessage = result.exceptionOrNull()?.message ?: (if (currentLanguage == AppLanguage.ARABIC) "السيريال غير صالح" else "Invalid serial")
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE69500)
                )
            ) {
                Text(AppStrings.activateLicense(currentLanguage), color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(AppStrings.close(currentLanguage))
            }
        }
    )
}
