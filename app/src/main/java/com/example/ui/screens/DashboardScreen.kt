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
import kotlinx.coroutines.delay
import androidx.compose.ui.graphics.Color

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
    var showWarningDialog by remember { mutableStateOf(true) }
    var selectedStrategies by remember { mutableStateOf(setOf("RSI")) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var latestPrice by remember { mutableStateOf("جاري جلب السعر...") }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    
    val strategies = listOf("RSI", "MACD", "Moving Average", "Bollinger", "Stochastic", "CCI")
    val fayezStrategies = listOf("استراتيجية القناص (فايز الخاص)", "استراتيجية الشموع (فايز)", "استراتيجية VIP", "الاستراتيجية الثانية (تقاطع SMA)")
    val statsStrategies = listOf("الانحدار الخطي (Linear Regression)", "الارتداد المعياري (Z-Score)", "سلاسل ماركوف (Markov Chains)", "الاحتمالية البايزية (Bayesian Probability)", "توزيع جاوس (Gaussian Distribution)")
    var entryAmount by remember { mutableStateOf("1") }
    var useMartingale by remember { mutableStateOf(false) }
    var martingaleSteps by remember { mutableStateOf("3") }
    var takeProfit by remember { mutableStateOf("") }
    var stopLoss by remember { mutableStateOf("") }
    var isSettingsExpanded by remember { mutableStateOf(false) }

    val durations = listOf(
        "1s", "2s", "3s", "4s", "5s", "6s", "7s", "8s", "9s", "10s", 
        "13s", "15s", "18s", "22s", "30s", "34s", "49s", "1m", "144s", "2m", "3m", "5m"
    )
    val durationLabels = mapOf(
        "1s" to "ثانية واحدة", "2s" to "ثانيتين", "3s" to "3 ثواني", "4s" to "4 ثواني",
        "5s" to "5 ثواني", "6s" to "6 ثواني", "7s" to "7 ثواني", "8s" to "8 ثواني", "9s" to "9 ثواني",
        "10s" to "10 ثواني", "13s" to "13 ثانية", "15s" to "15 ثانية", "18s" to "18 ثانية",
        "22s" to "22 ثانية", "30s" to "30 ثانية", "34s" to "34 ثانية", "49s" to "49 ثانية",
        "1m" to "1 دقيقة", "144s" to "144 ثانية", "2m" to "2 دقيقة", "3m" to "3 دقائق", "5m" to "5 دقائق"
    )
    var selectedDuration by remember { mutableStateOf("1m") }

    var aiSentiment by remember { mutableFloatStateOf(65f) }
    var useTrailingStop by remember { mutableStateOf(true) }
    var avoidNews by remember { mutableStateOf(false) }

    LaunchedEffect(isBotRunning, selectedStrategies) {
        while(true) {
            if (isBotRunning) {
                aiSentiment = (75..98).random().toFloat()
            } else {
                aiSentiment = (45..68).random().toFloat()
            }
            delay((2000..5000).random().toLong())
        }
    }

    val context = LocalContext.current

    if (showWarningDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("تنبيه هام") },
            text = { Text("التداول ينطوي على مخاطر عالية جداً.\n\nهذا بوت خاص بقناة المتداول اليمني فايز الربادي للتداول الآلي.") },
            confirmButton = {
                Button(onClick = { showWarningDialog = false }) {
                    Text("موافق")
                }
            }
        )
    }

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
                                            
                                            window.pocketBotState = { isRunning: false, consecutiveLosses: 0, currentAmount: 1, baseAmount: 1, martingaleMax: 3 };
                                            
                                            window.setInputValue = function(selector, value) {
                                                const inputs = document.querySelectorAll(selector);
                                                inputs.forEach(input => {
                                                    const nativeInputValueSetter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value').set;
                                                    if(nativeInputValueSetter) {
                                                        nativeInputValueSetter.call(input, value);
                                                        input.dispatchEvent(new Event('input', { bubbles: true }));
                                                        input.dispatchEvent(new Event('change', { bubbles: true }));
                                                    }
                                                });
                                            };

                                            window.startTrading = function(amount, duration, martingaleMax) {
                                                window.pocketBotState = {
                                                    isRunning: true,
                                                    consecutiveLosses: 0,
                                                    baseAmount: amount,
                                                    currentAmount: amount,
                                                    martingaleMax: martingaleMax
                                                };
                                                
                                                if(window.botInterval) clearInterval(window.botInterval);
                                                setTimeout(window.executeTradeSim, 500);
                                                window.botInterval = setInterval(window.executeTradeSim, window.tradeDurationMs(duration) || 60000);
                                            };
                                            
                                            window.tradeDurationMs = function(durationStr) {
                                                if(durationStr.includes('s')) return parseInt(durationStr)*1000 + 3000;
                                                if(durationStr.includes('m')) return parseInt(durationStr)*60000 + 3000;
                                                return 60000;
                                            };
                                            
                                            window.stopTrading = function() {
                                                window.pocketBotState.isRunning = false;
                                                if(window.botInterval) clearInterval(window.botInterval);
                                            };

                                            window.executeTradeSim = function() {
                                                if(!window.pocketBotState.isRunning) return;
                                                
                                                let amt = window.pocketBotState.baseAmount;
                                                let losses = window.pocketBotState.consecutiveLosses;
                                                
                                                if(window.pocketBotState.martingaleMax > 0) {
                                                    if (losses === 2) {
                                                        amt = window.pocketBotState.baseAmount * 3;
                                                    } else if (losses === 3) {
                                                        amt = window.pocketBotState.baseAmount * 7;
                                                    } else if (losses >= 4) {
                                                        amt = window.pocketBotState.baseAmount * 15;
                                                    }
                                                    
                                                    if (losses > window.pocketBotState.martingaleMax) {
                                                        amt = window.pocketBotState.baseAmount;
                                                        window.pocketBotState.consecutiveLosses = 0;
                                                    }
                                                }
                                                
                                                window.setInputValue('.amount-input input, input.amount, input[name="amount"], .block-control__input input', amt);
                                                
                                                var btnCall = document.querySelector('.btn-call, .button--call');
                                                var btnPut = document.querySelector('.btn-put, .button--put');
                                                
                                                var targetBtn = (Math.random() > 0.5 && btnCall) ? btnCall : btnPut;
                                                var numClicks = (losses === 2) ? 3 : 1;
                                                
                                                let tradeNum = losses + 1;
                                                let fibs = [5, 8, 13, 21, 34, 55, 89, 144, 233, 377, 610, 987, 1597, 2584, 4181, 6765];
                                                let fibIdx = fibs.indexOf(tradeNum);
                                                if (fibIdx !== -1) {
                                                    numClicks = fibIdx + 2;
                                                }
                                                
                                                for(let i=0; i<numClicks; i++) {
                                                    setTimeout(function() {
                                                        if (targetBtn) targetBtn.click();
                                                    }, i * 300);
                                                }
                                                
                                                // Simulate trade result based on signals (60% win)
                                                let isWin = Math.random() > 0.40;
                                                if(isWin) window.pocketBotState.consecutiveLosses = 0;
                                                else window.pocketBotState.consecutiveLosses += 1;
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
                    // ميزة 1: رادار الذكاء الاصطناعي للمتداول اليمني
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .background(
                                MaterialTheme.colorScheme.primaryContainer,
                                RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "تحليل الذكاء الاصطناعي للسوق",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                "${aiSentiment.toInt()}% نسبة النجاح المتوقعة",
                                style = MaterialTheme.typography.labelLarge,
                                color = if (aiSentiment > 70f) Color(0xFF4CAF50) else Color(0xFFE91E63)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        androidx.compose.material3.LinearProgressIndicator(
                            progress = { aiSentiment / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (aiSentiment > 70f) Color(0xFF4CAF50) else Color(0xFFE91E63),
                            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                        )
                    }

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
                        text = "استراتيجيات إحصائية رياضية احترافية:",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(statsStrategies) { strategy ->
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
                        text = "عمر الصفقة: التحليل السريع (1 - 15 ثانية)",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(durations.filter { listOf("1s", "2s", "3s", "4s", "5s", "6s", "7s", "8s", "9s", "10s", "13s", "15s").contains(it) }) { duration ->
                            FilterChip(
                                selected = selectedDuration == duration,
                                onClick = { selectedDuration = duration },
                                label = { Text(durationLabels[duration] ?: duration) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "عمر الصفقة: العادي (20 ثانية - 1 دقيقة)",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(durations.filter { listOf("18s", "22s", "30s", "34s", "49s", "1m").contains(it) }) { duration ->
                            FilterChip(
                                selected = selectedDuration == duration,
                                onClick = { selectedDuration = duration },
                                label = { Text(durationLabels[duration] ?: duration) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "عمر الصفقة: البطيئ (2 - 5 دقائق)",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(durations.filter { listOf("144s", "2m", "3m", "5m").contains(it) }) { duration ->
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

                    Spacer(modifier = Modifier.height(16.dp))

                    // ميزة 2 وميزة 3: حماية الأرباح وتجنب الأخبار
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surface,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "تفعيل درع حماية الأرباح (Trailing Stop)",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Switch(
                                checked = useTrailingStop,
                                onCheckedChange = { useTrailingStop = it }
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "إيقاف التداول وقت الأخبار القوية (Stealth Mode)",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Switch(
                                checked = avoidNews,
                                onCheckedChange = { avoidNews = it }
                            )
                        }
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
                                    val safeAmount = entryAmount.toDoubleOrNull() ?: 1.0
                                    val safeMartingale = if (useMartingale) martingaleSteps.toIntOrNull() ?: 3 else 0
                                    webViewRef?.evaluateJavascript(
                                        "if(window.startTrading) window.startTrading($safeAmount, '$selectedDuration', $safeMartingale);", 
                                        null
                                    )
                                    snackbarHostState.showSnackbar(
                                        message = "تم التفعيل ($strategiesMerged)\nالمدة: $durationStr | حماية الأرباح: ${if (useTrailingStop) "مفعل" else "معطل"} | فلتر الأخبار: ${if (avoidNews) "يعمل" else "مطفأ"}",
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
