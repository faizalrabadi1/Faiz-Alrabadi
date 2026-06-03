package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.utils.AccessManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(onNavigateToDashboard: () -> Unit, onNavigateToAdmin: () -> Unit) {
    val context = LocalContext.current
    val sharedPrefs = context.getSharedPreferences("PocketBotPrefs", Context.MODE_PRIVATE)
    
    // Check existing valid code
    LaunchedEffect(Unit) {
        val savedCode = sharedPrefs.getString("ACCESS_CODE", null)
        if (savedCode != null && AccessManager.isValid(savedCode)) {
            onNavigateToDashboard()
        }
    }

    var accountId by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "بوكت بوت للتحليل الآلي",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "يرجى إدخال كود الإحالة الصالح للمتابعة والدخول إلى نظام التداول.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = accountId,
            onValueChange = { 
                accountId = it
                errorMessage = null 
            },
            label = { Text("كود الإحالة (Access Code)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = errorMessage != null,
            supportingText = errorMessage?.let { { Text(it) } }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val inputCode = accountId.trim()
                if (inputCode.isEmpty()) {
                    errorMessage = "الرجاء إدخال الكود"
                    return@Button
                }
                
                if (AccessManager.isAdmin(inputCode)) {
                    onNavigateToAdmin()
                    return@Button
                }

                coroutineScope.launch {
                    isLoading = true
                    delay(1000) // Simulate verification delay
                    isLoading = false
                    
                    if (AccessManager.isValid(inputCode)) {
                        sharedPrefs.edit().putString("ACCESS_CODE", inputCode).apply()
                        onNavigateToDashboard()
                    } else {
                        errorMessage = "الكود غير صالح أو منتهي الصلاحية"
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text("تحقق ودخول")
            }
        }
    }
}
