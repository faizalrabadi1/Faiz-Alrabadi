package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.utils.AccessManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    var generatedCode by remember { mutableStateOf<String?>(null) }
    var codeExpiry by remember { mutableStateOf<String?>(null) }

    val durations = listOf(
        Pair("ساعة واحدة", 1f),
        Pair("يوم واحد", 24f),
        Pair("3 أيام", 72f),
        Pair("أسبوع واحد", 168f),
        Pair("شهر واحد (30 يوم)", 720f),
        Pair("سنة واحدة", 8760f)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("لوحة تحكم المدير") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "عودة")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "توليد أكواد الإحالة والدخول",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            if (generatedCode != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "الكود الجديد:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = generatedCode!!,
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "صالح حتى: ${codeExpiry}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("رمز الدخول", generatedCode)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "تم النسخ بنجاح", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = "نسخ")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("نسخ الكود")
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }

            Text(
                text = "اختر صلاحية الكود:",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.Start)
            )
            
            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(durations) { (label, hours) ->
                    OutlinedButton(
                        onClick = {
                            val code = AccessManager.generateCode(hours)
                            generatedCode = code
                            codeExpiry = AccessManager.getExpiryDateStr(code)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text(text = "كود لمدة $label", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}
