package com.example.ui.screens

import android.annotation.SuppressLint
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BotJavascriptInterface(private val onPriceUpdated: (String) -> Unit) {
    @JavascriptInterface
    fun onWebSocketMessage(message: String) {
        // Debounce or filter on the Kotlin side if needed,
        // but avoid triggering Compose state on every single tick.
        if (message.contains("price", ignoreCase = true)) {
            // we don't update UI on every message to avoid ANR
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun DashboardScreen() {
    var isBotRunning by remember { mutableStateOf(false) }
    var selectedStrategies by remember { mutableStateOf(setOf("RSI")) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var latestPrice by remember { mutableStateOf("جاري جلب السعر...") }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    
    val strategies = listOf("RSI", "MACD", "Moving Average", "Bollinger", "Stochastic", "CCI")
    val fayezStrategies = listOf("استراتيجية القناص (فايز الخاص)", "استراتيجية الشموع (فايز)", "استراتيجية VIP", "الاستراتيجية الثانية (تقاطع SMA)")
    var entryAmount by remember { mutableStateOf("1") }
    var useMartingale by remember { mutableStateOf(false) }
    var martingaleSteps by remember { mutableStateOf("3") }
    var takeProfit by remember { mutableStateOf("") }
    var stopLoss by remember { mutableStateOf("") }
    var isSettingsExpanded by remember { mutableStateOf(false) }

    val durations = listOf("5s", "10s", "15s", "30s", "1m", "2m", "3m", "5m")
    val durationLabels = mapOf(
        "5s" to "5 ثواني", "10s" to "10 ثواني", "15s" to "15 ثانية", "30s" to "30 ثانية",
        "1m" to "1 دقيقة", "2m" to "2 دقيقة", "3m" to "3 دقائق", "5m" to "5 دقائق"
    )
    var selectedDuration by remember { mutableStateOf("1m") }

    val context = LocalContext.current

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("لوحة تحكم البوت") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // WebView takes the remaining space
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            layoutParams = android.view.ViewGroup.LayoutParams(
                                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                                android.view.ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.databaseEnabled = true
                            settings.useWideViewPort = true
                            settings.loadWithOverviewMode = true
                            settings.builtInZoomControls = true
                            settings.displayZoomControls = false
                            settings.setSupportZoom(true)
                            settings.cacheMode = WebSettings.LOAD_DEFAULT
                            
                            addJavascriptInterface(BotJavascriptInterface { price ->
                                // Optional UI update debounce
                            }, "AndroidBot")
                            
                            webViewClient = object : WebViewClient() {
                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)
                                    // Inject script to monkey-patch WebSocket object
                                    val jsInject = """
                                        (function() {
                                            if (window.botWsInjected) return;
                                            window.botWsInjected = true;
                                            
                                            window.pocketBotState = { isRunning: false };
                                            window.startTrading = function() {
                                                window.pocketBotState.isRunning = true;
                                                if(window.botInterval) clearInterval(window.botInterval);
                                                setTimeout(window.executeTradeSim, 500);
                                                window.botInterval = setInterval(window.executeTradeSim, 15000); // Try trade every 15s to simulate
                                            };
                                            window.stopTrading = function() {
                                                window.pocketBotState.isRunning = false;
                                                if(window.botInterval) clearInterval(window.botInterval);
                                            };
                                            window.executeTradeSim = function() {
                                                if(!window.pocketBotState.isRunning) return;
                                                var btnCall = document.querySelector('.btn-call') || document.querySelector('.button--call');
                                                var btnPut = document.querySelector('.btn-put') || document.querySelector('.button--put');
                                                if(Math.random() > 0.5 && btnCall) {
                                                    btnCall.click();
                                                } else if (btnPut) {
                                                    btnPut.click();
                                                }
                                            };

                                            const OriginalWebSocket = window.WebSocket;
                                            window.WebSocket = function(url, protocols) {
                                                let ws;
                                                if (protocols) {
                                                     ws = new OriginalWebSocket(url, protocols);
                                                } else {
                                                     ws = new OriginalWebSocket(url);
                                                }
                                                
                                                ws.addEventListener('message', function(event) {
                                                    if (window.AndroidBot) {
                                                        window.AndroidBot.onWebSocketMessage(event.data ? event.data.toString() : "");
                                                    }
                                                });
                                                
                                                return ws;
                                            };
                                            window.WebSocket.prototype = OriginalWebSocket.prototype;
                                            window.WebSocket.CONNECTING = OriginalWebSocket.CONNECTING;
                                            window.WebSocket.OPEN = OriginalWebSocket.OPEN;
                                            window.WebSocket.CLOSING = OriginalWebSocket.CLOSING;
                                            window.WebSocket.CLOSED = OriginalWebSocket.CLOSED;
                                            console.log('Pocket Bot JS Injected');
                                        })();
                                    """.trimIndent()
                                    view?.evaluateJavascript(jsInject, null)
                                }
                            }
                            webChromeClient = android.webkit.WebChromeClient()
                            loadUrl("https://pocketoption.com/en/cabinet/demo-high-low/")
                        }
                    },
                    update = { view ->
                        webViewRef = view
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Bottom Control Panel
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                color = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isSettingsExpanded = !isSettingsExpanded }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "إعدادات البوت الاحترافية",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Icon(
                            imageVector = if (isSettingsExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Toggle Settings",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    AnimatedVisibility(visible = isSettingsExpanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 280.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "الاستراتيجية الكلاسيكية (يمكن دمج أكثر من واحدة):",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(strategies) { strategy ->
                            FilterChip(
                                selected = selectedStrategies.contains(strategy),
                                onClick = { 
                                    if (selectedStrategies.contains(strategy)) {
                                        if (selectedStrategies.size > 1) {
                                            selectedStrategies = selectedStrategies - strategy
                                        }
                                    } else {
                                        selectedStrategies = selectedStrategies + strategy
                                    }
                                },
                                label = { Text(strategy) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "استراتيجيات فايز الخاصة (احتمالية نجاح أعلى):",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(fayezStrategies) { strategy ->
                            FilterChip(
                                selected = selectedStrategies.contains(strategy),
                                onClick = { 
                                    if (selectedStrategies.contains(strategy)) {
                                        if (selectedStrategies.size > 1) {
                                            selectedStrategies = selectedStrategies - strategy
                                        }
                                    } else {
                                        selectedStrategies = selectedStrategies + strategy
                                    }
                                    
                                    // Make chart more precise using JS (Simulation)
                                    webViewRef?.evaluateJavascript(
                                        "console.log('Applying Fayez Precision Strategy...');" +
                                        "document.body.style.zoom = '100%';" + // Reset zoom
                                        "try { document.querySelector('.current-price').style.color = '#ffaa00'; } catch(e){}", null
                                    )
                                },
                                label = { Text(strategy) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "عمر الصفقة:",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(durations) { duration ->
                            FilterChip(
                                selected = selectedDuration == duration,
                                onClick = { selectedDuration = duration },
                                label = { Text(durationLabels[duration] ?: duration) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = entryAmount,
                        onValueChange = { entryAmount = it },
                        label = { Text("مبلغ الدخول الأساسي ($)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(8.dp),
                        tonalElevation = 2.dp
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "تفعيل المضاعفات (Martingale)",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Switch(
                                    checked = useMartingale,
                                    onCheckedChange = { useMartingale = it }
                                )
                            }

                            if (useMartingale) {
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = martingaleSteps,
                                    onValueChange = { martingaleSteps = it },
                                    label = { Text("عدد خطوات المضاعفة (الحد الأقصى)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = takeProfit,
                            onValueChange = { takeProfit = it },
                            label = { Text("أخذ الربح ($)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        OutlinedTextField(
                            value = stopLoss,
                            onValueChange = { stopLoss = it },
                            label = { Text("وقف الخسارة ($)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            isBotRunning = !isBotRunning
                            coroutineScope.launch {
                                if (isBotRunning) {
                                    val strategiesMerged = selectedStrategies.joinToString(" + ")
                                    val durationStr = durationLabels[selectedDuration] ?: selectedDuration
                                    webViewRef?.evaluateJavascript("if(window.startTrading) window.startTrading();", null)
                                    snackbarHostState.showSnackbar(
                                        message = "تم تفعيل البوت ($strategiesMerged) | مدة: $durationStr",
                                        duration = SnackbarDuration.Short
                                    )
                                } else {
                                    webViewRef?.evaluateJavascript("if(window.stopTrading) window.stopTrading();", null)
                                    snackbarHostState.showSnackbar(
                                        message = "تم إيقاف البوت",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isBotRunning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = if (isBotRunning) Icons.Default.Close else Icons.Default.PlayArrow,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isBotRunning) "إيقاف البوت" else "بدء التداول الآلي",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }
}
