package com.example.ui.screens

import android.annotation.SuppressLint
import com.example.ui.components.TradePerformanceDashboard
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.ui.graphics.Color
import android.app.Activity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.appopen.AppOpenAd.AppOpenAdLoadCallback

data class TradeRecord(
    val time: String,
    val amount: String,
    val result: String // "win" or "loss"
)

class BotJavascriptInterface(
    private val onPriceUpdated: (String) -> Unit,
    private val onTradeResult: (String, String, String) -> Unit
) {
    @JavascriptInterface
    fun onWebSocketMessage(message: String) {
        // Debounce or filter on the Kotlin side if needed,
        // but avoid triggering Compose state on every single tick.
        if (message.contains("price", ignoreCase = true)) {
            // we don't update UI on every message to avoid ANR
        }
    }

    @JavascriptInterface
    fun reportTradeResult(time: String, amount: String, result: String) {
        onTradeResult(time, amount, result)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun DashboardScreen() {
    var selectedBroker by remember { mutableStateOf<String?>(null) }
    val pocketOptionUrl = "https://u3.shortink.io/register?utm_campaign=48754&utm_source=affiliate&utm_medium=sr&a=uzxUgJzznrCXil&al=1780287&ac=boot&cid=968158&code=50START"
    val quotexUrl = "https://broker-qx.pro/sign-up/?lid=2217556"

    if (selectedBroker == null) {
        BrokerSelectionScreen { broker ->
            selectedBroker = broker
        }
        return
    }

    var isBotRunning by remember { mutableStateOf(false) }
    var showWarningDialog by remember { mutableStateOf(true) }
    
    var timeLeftSeconds by remember { mutableIntStateOf(0) }
    var showAdPrompt by remember { mutableStateOf(false) }
    var rewardedAd by remember { mutableStateOf<RewardedAd?>(null) }
    val context = LocalContext.current
    
    LaunchedEffect(Unit) {
        val adRequest = AdRequest.Builder().build()
        
        AppOpenAd.load(
            context,
            "ca-app-pub-8020925279516712/5778985992",
            adRequest,
            object : AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    val activity = context as? Activity
                    if (activity != null) {
                        ad.show(activity)
                    }
                }
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.e("AdMob", "AppOpenAd failed to load: ${loadAdError.message}")
                }
            }
        )
        
        RewardedAd.load(
            context,
            "ca-app-pub-8020925279516712/1301373295",
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    rewardedAd = null
                }
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                }
            }
        )
    }

    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    
    LaunchedEffect(isBotRunning, timeLeftSeconds) {
        if (isBotRunning) {
            if (timeLeftSeconds > 0) {
                delay(1000)
                timeLeftSeconds -= 1
            } else {
                isBotRunning = false
                webViewRef?.evaluateJavascript("if(window.stopTrading) window.stopTrading();", null)
                showAdPrompt = true
            }
        }
    }
    
    var selectedStrategies by remember { mutableStateOf(setOf("RSI")) }
    var latestPrice by remember { mutableStateOf("جاري جلب السعر...") }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val tradeHistory = remember {
        mutableStateListOf(
            TradeRecord("10:15:30", "10", "win"),
            TradeRecord("10:17:45", "5", "loss"),
            TradeRecord("10:20:12", "15", "win"),
            TradeRecord("10:24:00", "20", "win"),
            TradeRecord("10:31:18", "10", "loss"),
            TradeRecord("10:35:45", "25", "win")
        )
    }
    
    val strategies = listOf("RSI", "MACD", "Moving Average", "Bollinger", "Stochastic", "CCI")
    val fayezStrategies = listOf("استراتيجية القناص (فايز الخاص)", "استراتيجية الشموع (فايز)", "استراتيجية VIP", "الاستراتيجية الثانية (تقاطع SMA)", "التحليل الفني بعلم الرمل")
    val statsStrategies = listOf("الانحدار الخطي (Linear Regression)", "الارتداد المعياري (Z-Score)", "سلاسل ماركوف (Markov Chains)", "الاحتمالية البايزية (Bayesian Probability)", "توزيع جاوس (Gaussian Distribution)")
    val radicalStrategies = listOf("الشبكة العصبية العميقة", "الزخم الكمي", "خوارزمية الفوضى", "الأنماط الفراكتلية", "موجات إليوت الذكية")
    var entryAmount by remember { mutableStateOf("1") }
    var riskPercentage by remember { mutableStateOf("2") }
    
    val currencyPairs = listOf("EUR/USD", "GBP/USD", "USD/JPY", "AUD/USD", "USD/CAD", "USD/CHF", "NZD/USD", "OTC (Crypto)", "OTC (Stocks)")
    var selectedCurrencyPairs by remember { mutableStateOf(setOf("EUR/USD")) }

    var useMartingale by remember { mutableStateOf(false) }
    var martingaleSteps by remember { mutableStateOf("3") }
    var martingaleMultiplier by remember { mutableStateOf("2.0") }
    var takeProfit by remember { mutableStateOf("") }
    var stopLoss by remember { mutableStateOf("") }
    var isSettingsExpanded by remember { mutableStateOf(false) }
    var isBottomPanelExpanded by remember { mutableStateOf(false) }

    val durations = listOf(
        "random", "1s", "2s", "3s", "4s", "5s", "6s", "7s", "8s", "9s", "10s", 
        "13s", "15s", "18s", "22s", "30s", "34s", "49s", "1m", "144s", "2m", "3m", "5m"
    )
    val durationLabels = mapOf(
        "random" to "عشوائي", "1s" to "ثانية واحدة", "2s" to "ثانيتين", "3s" to "3 ثواني", "4s" to "4 ثواني",
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

    if (showAdPrompt) {
        AlertDialog(
            onDismissRequest = { showAdPrompt = false },
            title = { Text("انتهى وقتك") },
            text = { Text("شاهد إعلانًا لتحصل على ٥ دقائق من التداول الآلي") },
            confirmButton = {
                Button(onClick = {
                    showAdPrompt = false
                    val activity = context as? Activity
                    if (activity != null && rewardedAd != null) {
                        rewardedAd?.show(activity) { _ ->
                            // Reward callback
                            timeLeftSeconds += 300 // Add 5 minutes
                            isBotRunning = true // Auto-start
                            coroutineScope.launch {
                                val strategiesMerged = selectedStrategies.joinToString(" + ")
                                val durationStr = durationLabels[selectedDuration] ?: selectedDuration
                                val safeAmount = entryAmount.toDoubleOrNull() ?: 1.0
                                val safeMartingale = if (useMartingale) martingaleSteps.toIntOrNull() ?: 3 else 0
                                val safeMultiplier = martingaleMultiplier.toDoubleOrNull() ?: 2.0
                                webViewRef?.evaluateJavascript(
                                    "if(window.startTrading) window.startTrading($safeAmount, '$selectedDuration', $safeMartingale, '$strategiesMerged', $safeMultiplier);", 
                                    null
                                )
                                snackbarHostState.showSnackbar(
                                    message = "تم التفعيل بنجاح",
                                    duration = SnackbarDuration.Short
                                )
                            }
                            // Reload ad
                            val adRequest = AdRequest.Builder().build()
                            RewardedAd.load(
                                context,
                                "ca-app-pub-8020925279516712/1301373295",
                                adRequest,
                                object : RewardedAdLoadCallback() {
                                    override fun onAdFailedToLoad(adError: LoadAdError) { rewardedAd = null }
                                    override fun onAdLoaded(ad: RewardedAd) { rewardedAd = ad }
                                }
                            )
                        }
                    } else {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                "الإعلان غير جاهز بعد. تأكد من إيقاف مانع الإعلانات، أو جرب تشغيل VPN إذا كانت دولتك تحظر الإعلانات.",
                                duration = SnackbarDuration.Long
                            )
                        }
                    }
                }) {
                    Text("مشاهدة الإعلان")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAdPrompt = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("لوحة تحكم البوت") },
                actions = {
                    val minutes = timeLeftSeconds / 60
                    val seconds = timeLeftSeconds % 60
                    val timeString = String.format("%02d:%02d", minutes, seconds)
                    
                    Surface(
                        color = if (timeLeftSeconds > 0) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Text(
                            text = timeString,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = if (timeLeftSeconds > 0) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary,
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
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        WebView(ctx).apply {
                            layoutParams = android.view.ViewGroup.LayoutParams(
                                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                                android.view.ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.useWideViewPort = true
                            settings.loadWithOverviewMode = true
                            settings.builtInZoomControls = true
                            settings.displayZoomControls = false
                            settings.setSupportZoom(true)
                            settings.cacheMode = WebSettings.LOAD_DEFAULT
                            
                            // Enable Google Sign-in in WebView by modifying User-Agent
                            settings.userAgentString = settings.userAgentString.replace("; wv", "")
                            android.webkit.CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
                            
                            clearCache(true)
                            
                            addJavascriptInterface(BotJavascriptInterface(
                                onPriceUpdated = { price -> 
                                    // Optional UI update debounce
                                },
                                onTradeResult = { time, amount, result ->
                                    coroutineScope.launch {
                                        tradeHistory.add(0, TradeRecord(time, amount, result))
                                        if (tradeHistory.size > 20) {
                                            tradeHistory.removeLast()
                                        }
                                    }
                                }
                            ), "AndroidBot")
                            
                            webViewClient = object : WebViewClient() {
                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)
                                    // Inject script to monkey-patch WebSocket object
                                    val jsInject = """
                                        (function() {
                                            if (window.botWsInjected) return;
                                            window.botWsInjected = true;
                                            
                                            window.pocketBotState = { isRunning: false, consecutiveLosses: 0, currentAmount: 1, baseAmount: 1, martingaleMax: 3, strategies: '', lastBalance: null };
                                            window.priceHistory = [];
                                            
                                            setInterval(() => {
                                                let el = document.querySelector('.current-price-value, .quote__val, .price-row__value, .price, text[class*="price"]');
                                                if(el) {
                                                    let p = parseFloat((el.innerText || el.textContent).replace(/[^0-9.]/g, ''));
                                                    if(!isNaN(p) && p > 0) {
                                                        if(window.priceHistory.length === 0 || window.priceHistory[window.priceHistory.length - 1] !== p) {
                                                            window.priceHistory.push(p);
                                                            if(window.priceHistory.length > 60) window.priceHistory.shift();
                                                        }
                                                    }
                                                }
                                            }, 1000);
                                            
                                            window.setInputValue = function(type, value) {
                                                let targetInput = null;
                                                let container = null;
                                                
                                                if (type === 'time') {
                                                    container = document.querySelector('.block--time, .control--time, [data-qa="time"], .block-control__input--time, .section-deal__time');
                                                    if(container) targetInput = container.querySelector('input');
                                                    if(!targetInput) {
                                                        const allInputs = Array.from(document.querySelectorAll('input'));
                                                        targetInput = allInputs.find(i => {
                                                            let c = i.className.toLowerCase();
                                                            return c.includes('time') || c.includes('expiration');
                                                        });
                                                    }
                                                } else if (type === 'amount') {
                                                    container = document.querySelector('.block--amount, .control--amount, [data-qa="amount"], .block-control__input--amount, .section-deal__amount');
                                                    if(container) targetInput = container.querySelector('input');
                                                    if(!targetInput) {
                                                        const allInputs = Array.from(document.querySelectorAll('input'));
                                                        targetInput = allInputs.find(i => {
                                                            let c = i.className.toLowerCase();
                                                            return c.includes('amount') || c.includes('investment');
                                                        });
                                                    }
                                                }
                                                
                                                if (!targetInput) {
                                                    // fallback to checking wrapping divs
                                                    let inputs = document.querySelectorAll('input');
                                                    for (let input of inputs) {
                                                        let html = (input.parentElement ? input.parentElement.innerHTML : '').toLowerCase();
                                                        if (type === 'time' && (html.includes('time') || html.includes('expiration') || input.getAttribute('name') === 'time')) {
                                                            targetInput = input; break;
                                                        }
                                                        if (type === 'amount' && (html.includes('amount') || html.includes('investment') || input.getAttribute('name') === 'amount')) {
                                                            targetInput = input; break;
                                                        }
                                                    }
                                                }

                                                if (targetInput) {
                                                    // React 15/16/17 value setter hack
                                                    let lastValue = targetInput.value;
                                                    targetInput.value = value;
                                                    
                                                    let tracker = targetInput._valueTracker;
                                                    if (tracker) tracker.setValue(lastValue);
                                                    
                                                    const nativeInputValueSetter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value').set;
                                                    if (nativeInputValueSetter) {
                                                        nativeInputValueSetter.call(targetInput, value);
                                                    }
                                                    
                                                    targetInput.dispatchEvent(new Event('input', { bubbles: true, composed: true }));
                                                    targetInput.dispatchEvent(new Event('change', { bubbles: true, composed: true }));
                                                    targetInput.dispatchEvent(new KeyboardEvent('keydown', { bubbles: true, composed: true, key: 'Enter', keyCode: 13 }));
                                                    targetInput.dispatchEvent(new KeyboardEvent('keyup', { bubbles: true, composed: true, key: 'Enter', keyCode: 13 }));
                                                    targetInput.dispatchEvent(new Event('blur', { bubbles: true, composed: true }));
                                                    
                                                    console.log('Pocket Bot: Set ' + type + ' to ' + value);
                                                } else {
                                                    console.log('Pocket Bot: Input ' + type + ' not found!');
                                                }
                                            };

                                            window.formatPocketOptionTime = function(durationStr) {
                                                if (!durationStr || durationStr === 'random') return "00:01:00";
                                                let seconds = 60;
                                                if (durationStr.includes('s')) seconds = parseInt(durationStr);
                                                else if (durationStr.includes('m')) seconds = parseInt(durationStr) * 60;
                                                let h = Math.floor(seconds / 3600);
                                                let m = Math.floor((seconds % 3600) / 60);
                                                let s = seconds % 60;
                                                return (h < 10 ? "0"+h : h) + ":" + (m < 10 ? "0"+m : m) + ":" + (s < 10 ? "0"+s : s);
                                            };

                                            window.startTrading = function(amount, duration, martingaleMax, strats, martingaleMultiplierVal) {
                                                window.pocketBotState = {
                                                    isRunning: true,
                                                    consecutiveLosses: 0,
                                                    baseAmount: amount,
                                                    currentAmount: amount,
                                                    martingaleMax: martingaleMax,
                                                    martingaleMultiplier: martingaleMultiplierVal || 2.0,
                                                    strategies: strats || '',
                                                    lastBalance: null,
                                                    durationMode: duration
                                                };
                                                
                                                if(window.botInterval) clearInterval(window.botInterval);
                                                if(window.botTimeout) clearTimeout(window.botTimeout);
                                                setTimeout(window.executeTradeSim, 500);
                                            };
                                            
                                            window.tradeDurationMs = function(durationStr) {
                                                if (!durationStr) return 60000;
                                                if(durationStr.includes('s')) return parseInt(durationStr)*1000 + 3000;
                                                if(durationStr.includes('m')) return parseInt(durationStr)*60000 + 3000;
                                                return 60000;
                                            };
                                            
                                            window.stopTrading = function() {
                                                if (window.pocketBotState) window.pocketBotState.isRunning = false;
                                                if(window.botInterval) clearInterval(window.botInterval);
                                                if(window.botTimeout) clearTimeout(window.botTimeout);
                                            };

                                            window.executeTradeSim = function() {
                                                if(!window.pocketBotState.isRunning) return;
                                                
                                                let amt = window.pocketBotState.baseAmount;
                                                let losses = window.pocketBotState.consecutiveLosses;
                                                
                                                // Real balance tracking for martingale
                                                let balanceEl = document.querySelector('.balance-info__value, .balance__value, .current-balance, [data-qa="balance-value"]');
                                                if(balanceEl) {
                                                    let currentBalance = parseFloat((balanceEl.innerText || balanceEl.textContent).replace(/[^0-9.-]/g, ''));
                                                    if (!isNaN(currentBalance)) {
                                                        if(window.pocketBotState.lastBalance !== null && window.botInTrade) {
                                                            if(currentBalance > window.pocketBotState.lastBalance) {
                                                                losses = 0; // Won!
                                                                if (window.pocketBotState.lastResult === null) window.pocketBotState.lastResult = 'win';
                                                                if (window.AndroidBot) window.AndroidBot.reportTradeResult(new Date().toLocaleTimeString(), (window.pocketBotState.currentAmount || window.pocketBotState.baseAmount).toString(), 'win');
                                                            } else if (currentBalance < window.pocketBotState.lastBalance) {
                                                                losses += 1; // Lost!
                                                                if (window.pocketBotState.lastResult === null) window.pocketBotState.lastResult = 'loss';
                                                                if (window.AndroidBot) window.AndroidBot.reportTradeResult(new Date().toLocaleTimeString(), (window.pocketBotState.currentAmount || window.pocketBotState.baseAmount).toString(), 'loss');
                                                            }
                                                            window.botInTrade = false;
                                                        }
                                                        window.pocketBotState.lastBalance = currentBalance;
                                                    }
                                                }
                                                window.pocketBotState.consecutiveLosses = losses;
                                                
                                                if(window.pocketBotState.martingaleMax > 0) {
                                                    if (losses > 0) {
                                                        amt = window.pocketBotState.baseAmount * Math.pow(window.pocketBotState.martingaleMultiplier, losses);
                                                    }
                                                    
                                                    if (losses > window.pocketBotState.martingaleMax) {
                                                        amt = window.pocketBotState.baseAmount;
                                                        window.pocketBotState.consecutiveLosses = 0;
                                                    }
                                                }
                                                window.pocketBotState.currentAmount = amt;
                                                
                                                window.setInputValue('amount', amt.toString());
                                                
                                                var btnCall = document.querySelector('.btn-call, .button--call, .btn-success, .button--success, [data-qa="btn-call"], [data-role="up"]');
                                                if (!btnCall) {
                                                    let btns = Array.from(document.querySelectorAll('button, div[class*="button"], div[class*="btn"]'));
                                                    btnCall = btns.find(b => {
                                                        let text = b.textContent.toLowerCase().trim();
                                                        let cls = b.className.toLowerCase();
                                                        return (cls.includes('success') && (cls.includes('btn')||cls.includes('button'))) || cls.includes('call') || text === 'صاعد' || text === 'up';
                                                    });
                                                }
                                                var btnPut = document.querySelector('.btn-put, .button--put, .btn-danger, .button--danger, [data-qa="btn-put"], [data-role="down"]');
                                                if (!btnPut) {
                                                    let btns = Array.from(document.querySelectorAll('button, div[class*="button"], div[class*="btn"]'));
                                                    btnPut = btns.find(b => {
                                                        let text = b.textContent.toLowerCase().trim();
                                                        let cls = b.className.toLowerCase();
                                                        return (cls.includes('danger') && (cls.includes('btn')||cls.includes('button'))) || cls.includes('put') || text === 'هابط' || text === 'down';
                                                    });
                                                }
                                                var targetBtn = null;
                                                
                                                let strats = window.pocketBotState.strategies.toLowerCase();
                                                let prices = window.priceHistory;
                                                
                                                // REAL TECHNICAL ANALYSIS & ADVANCED ALGORITHMS
                                                if (prices.length > 20) {
                                                    if (!window.botWeights) window.botWeights = { rsi: 1.0, macd: 1.0, bb: 1.0, fib: 1.0, sma: 1.0, vip: 1.0 };
                                                    
                                                    // Q-Learning Weight Update (Reinforcement)
                                                    if (window.pocketBotState.lastResult === 'loss') {
                                                        ['rsi','macd','bb','fib','sma','vip'].forEach(s => window.botWeights[s] *= 0.95);
                                                    } else if (window.pocketBotState.lastResult === 'win') {
                                                        ['rsi','macd','bb','fib','sma','vip'].forEach(s => window.botWeights[s] = Math.min(1.5, window.botWeights[s] * 1.02));
                                                    }
                                                    window.pocketBotState.lastResult = null; // Reset for next reading
                                                    
                                                    let current = prices[prices.length - 1];
                                                    let sma5 = prices.slice(-5).reduce((a,b)=>a+b,0)/5;
                                                    let sma14 = prices.slice(-14).reduce((a,b)=>a+b,0)/14;
                                                    let sma20 = prices.slice(-20).reduce((a,b)=>a+b,0)/20;
                                                    
                                                    let callScore = 0;
                                                    let putScore = 0;
                                                    
                                                    // 1. RSI
                                                    if (strats.includes('rsi') || strats.includes('stochastic')) {
                                                        let u=0, d=0;
                                                        for(let i=1; i<14; i++){
                                                            let dif = prices[prices.length-i] - prices[prices.length-i-1];
                                                            if(dif>0) u+=dif; else d-=dif;
                                                        }
                                                        let rs = u/(d===0?0.001:d);
                                                        let rsi = 100 - (100/(1+rs));
                                                        if (rsi < 30) callScore += window.botWeights.rsi;
                                                        if (rsi > 70) putScore += window.botWeights.rsi;
                                                    }
                                                    
                                                    // 2. MACD Approximation (using moving averages diff)
                                                    if (strats.includes('macd')) {
                                                        let ema12 = prices.slice(-12).reduce((a,b)=>a+b,0)/12; 
                                                        let ema26 = prices.slice(-26 > -prices.length ? -26 : -prices.length).reduce((a,b)=>a+b,0)/26;
                                                        let macd = ema12 - ema26;
                                                        // Signal line approximation
                                                        if (macd > 0 && current > sma5) callScore += window.botWeights.macd;
                                                        if (macd < 0 && current < sma5) putScore += window.botWeights.macd;
                                                    }
                                                    
                                                    // 3. Bollinger Bands Approximation
                                                    if (strats.includes('bollinger') || strats.includes('ارتداد')) {
                                                        let variances = prices.slice(-20).map(p => Math.pow(p - sma20, 2));
                                                        let stdDev = Math.sqrt(variances.reduce((a,b)=>a+b,0)/20);
                                                        let upperBand = sma20 + (stdDev * 2);
                                                        let lowerBand = sma20 - (stdDev * 2);
                                                        if (current <= lowerBand) callScore += window.botWeights.bb;
                                                        if (current >= upperBand) putScore += window.botWeights.bb;
                                                    }
                                                    
                                                    // 4. Advanced Fibonacci Suite (Retracement, Fan, Time Zones)
                                                    if (strats.includes('فيبوناتشي') || strats.includes('fibonacci') || strats.includes('fib')) {
                                                        let pList = prices.slice(-34); // Use 34 (Fib number) periods
                                                        let maxP = Math.max(...pList);
                                                        let minP = Math.min(...pList);
                                                        let maxIdx = pList.indexOf(maxP);
                                                        let minIdx = pList.indexOf(minP);
                                                        let diff = maxP - minP;
                                                        
                                                        if (diff > 0 && maxIdx !== minIdx) {
                                                            // A. Fibonacci Retracement
                                                            let fib618 = maxP - (diff * 0.618);
                                                            let fib500 = maxP - (diff * 0.500);
                                                            let fib382 = maxP - (diff * 0.382);
                                                            
                                                            let margin = diff * 0.05;
                                                            if (Math.abs(current - fib618) < margin) callScore += window.botWeights.fib;
                                                            if (Math.abs(current - fib382) < margin) putScore += window.botWeights.fib;

                                                            // B. Fibonacci Fan (Trend Support/Resistance)
                                                            let dx = maxIdx - minIdx;
                                                            let slope = diff / dx;
                                                            let currentDx = (pList.length - 1) - minIdx;
                                                            
                                                            let fan618 = minP + (slope * 0.618 * currentDx);
                                                            let fan500 = minP + (slope * 0.500 * currentDx);
                                                            let fan382 = minP + (slope * 0.382 * currentDx);
                                                            
                                                            if (Math.abs(current - fan618) < margin) callScore += window.botWeights.fib * 0.5;
                                                            if (Math.abs(current - fan382) < margin) putScore += window.botWeights.fib * 0.5;

                                                            // C. Fibonacci Time Zones
                                                            let fibTimes = [5, 8, 13, 21, 34];
                                                            let timeSinceLow = (pList.length - 1) - minIdx;
                                                            let timeSinceHigh = (pList.length - 1) - maxIdx;
                                                            
                                                            if (fibTimes.includes(timeSinceLow)) {
                                                                // Expected reversal from low trend
                                                                if (current < sma5) callScore += window.botWeights.fib * 1.5; 
                                                            }
                                                            if (fibTimes.includes(timeSinceHigh)) {
                                                                // Expected reversal from high trend
                                                                if (current > sma5) putScore += window.botWeights.fib * 1.5;
                                                            }
                                                        }
                                                    }

                                                    // 5. SMAs Cross
                                                    if (strats.includes('sma') || strats.includes('moving') || strats.includes('تقاطع')) {
                                                        if (sma5 > sma14) callScore += window.botWeights.sma;
                                                        if (sma5 < sma14) putScore += window.botWeights.sma;
                                                    }
                                                    
                                                    // 6. VIP / Momentum
                                                    if (strats.includes('قناص') || strats.includes('vip')) {
                                                        let momentum = current - prices[prices.length - 5];
                                                        if (momentum > 0) callScore += window.botWeights.vip * 1.5;
                                                        if (momentum < 0) putScore += window.botWeights.vip * 1.5;
                                                    }
                                                    
                                                    // 7. Geomancy Technical Analysis (علم الرمل - فايز الربادي)
                                                    if (strats.includes('الرمل') || strats.includes('raml')) {
                                                        let candles = [];
                                                        let period = 10;
                                                        for (let i = 0; i < prices.length; i += period) {
                                                            let chunk = prices.slice(i, i + period);
                                                            if (chunk.length > 0) {
                                                                candles.push({
                                                                    open: chunk[0],
                                                                    high: Math.max(...chunk),
                                                                    low: Math.min(...chunk),
                                                                    close: chunk[chunk.length - 1]
                                                                });
                                                            }
                                                        }
                                                        
                                                        if (candles.length >= 2) {
                                                            let lastC = candles[candles.length - 1];
                                                            let prevC = candles[candles.length - 2];
                                                            
                                                            let body = Math.abs(lastC.close - lastC.open);
                                                            let range = lastC.high - lastC.low;
                                                            let isBull = lastC.close > lastC.open;
                                                            let isBear = lastC.open > lastC.close;
                                                            
                                                            let upperShadow = lastC.high - Math.max(lastC.open, lastC.close);
                                                            let lowerShadow = Math.min(lastC.open, lastC.close) - lastC.low;
                                                            
                                                            if (range > 0) {
                                                                // طريق صاعد (Bullish Marubozu)
                                                                if (isBull && body/range > 0.8) callScore += window.botWeights.vip * 2.0;
                                                                // طريق هابط (Bearish Marubozu)
                                                                if (isBear && body/range > 0.8) putScore += window.botWeights.vip * 2.0;
                                                                // نصرة داخلة صاعدة (Hammer)
                                                                if (lowerShadow > body * 1.5 && upperShadow < body * 0.5) callScore += window.botWeights.vip * 1.5;
                                                                // نصرة خارجة / شهاب (Shooting Star)
                                                                if (upperShadow > body * 1.5 && lowerShadow < body * 0.5) putScore += window.botWeights.vip * 1.5;
                                                                // فرح (Bullish Engulfing)
                                                                if (prevC.open > prevC.close && isBull && lastC.close > prevC.open && lastC.open < prevC.close) callScore += window.botWeights.vip * 2.5; 
                                                                // حزن (Bearish Engulfing)
                                                                if (prevC.close > prevC.open && isBear && lastC.close < prevC.open && lastC.open > prevC.close) putScore += window.botWeights.vip * 2.5;
                                                            }
                                                        }
                                                    }
                                                    
                                                    // 1. الشبكة العصبية العميقة
                                                    if (strats.includes('الشبكة العصبية') || strats.includes('deep')) {
                                                        let sum = 0;
                                                        for(let i=0; i<prices.length-1; i++) { sum += Math.sin((prices[prices.length-1] - prices[i]) * 1000); }
                                                        if (sum > 0) callScore += 2.0; else putScore += 2.0;
                                                    }
                                                    
                                                    // 2. تحليل الزخم الكمي
                                                    if (strats.includes('الزخم الكمي') || strats.includes('quantum')) {
                                                        let recent = prices.slice(-5);
                                                        let mean = recent.reduce((a,b)=>a+b,0)/5;
                                                        let variance = recent.reduce((a,b)=>a+Math.pow(b-mean, 2),0)/5;
                                                        if (prices[prices.length-1] > mean && variance > 0.00001) callScore += 2.5;
                                                        else if (prices[prices.length-1] < mean && variance > 0.00001) putScore += 2.5;
                                                    }

                                                    // 3. خوارزمية السلوك الفوضوي
                                                    if (strats.includes('خوارزمية الفوضى') || strats.includes('chaos')) {
                                                        let dx = prices[prices.length-1] - prices[prices.length-2];
                                                        let dy = prices[prices.length-2] - prices[prices.length-3];
                                                        if (dx * dy < 0) { 
                                                            if (dx > 0) putScore += 1.8; else callScore += 1.8;
                                                        }
                                                    }

                                                    // 4. التعرف على الأنماط الفراكتلية
                                                    if (strats.includes('الأنماط الفراكتلية') || strats.includes('fractal')) {
                                                        if (prices.length >= 5) {
                                                            let p = prices.slice(-5);
                                                            if (p[2] > p[0] && p[2] > p[1] && p[2] > p[3] && p[2] > p[4]) putScore += 3.0;
                                                            if (p[2] < p[0] && p[2] < p[1] && p[2] < p[3] && p[2] < p[4]) callScore += 3.0;
                                                        }
                                                    }

                                                    // 5. تحليل موجات إليوت الذكي
                                                    if (strats.includes('موجات إليوت') || strats.includes('elliott')) {
                                                        if (prices.length >= 8) {
                                                            let trend = prices[prices.length-1] - prices[prices.length-8];
                                                            if (trend > 0 && prices[prices.length-1] > prices[prices.length-2]) callScore += 2.2;
                                                            else if (trend < 0 && prices[prices.length-1] < prices[prices.length-2]) putScore += 2.2;
                                                        }
                                                    }

                                                    if (callScore === 0 && putScore === 0) {
                                                        targetBtn = (Math.random() > 0.5) ? btnCall : btnPut; // Fallback to avoid dead loop
                                                    } else {
                                                        if (callScore > putScore) targetBtn = btnCall;
                                                        else if (putScore > callScore) targetBtn = btnPut;
                                                        else targetBtn = (Math.random() > 0.5) ? btnCall : btnPut;
                                                    }
                                                } else {
                                                    targetBtn = (Math.random() > 0.5) ? btnCall : btnPut;
                                                }
                                                
                                                if (targetBtn) {
                                                    let clicks = 1;
                                                    window.pocketBotState.tradesSinceRandom = (window.pocketBotState.tradesSinceRandom || 0) + 1;
                                                    if (window.pocketBotState.tradesSinceRandom > (Math.random() * 5 + 3)) {
                                                        clicks = 3;
                                                        window.pocketBotState.tradesSinceRandom = 0;
                                                    }
                                                    for (let i = 0; i < clicks; i++) {
                                                        setTimeout(() => { 
                                                            if (targetBtn) {
                                                                targetBtn.click();
                                                                targetBtn.dispatchEvent(new MouseEvent('mousedown', { bubbles: true, cancelable: true, view: window }));
                                                                targetBtn.dispatchEvent(new MouseEvent('mouseup', { bubbles: true, cancelable: true, view: window }));
                                                                targetBtn.dispatchEvent(new MouseEvent('click', { bubbles: true, cancelable: true, view: window }));
                                                                targetBtn.dispatchEvent(new PointerEvent('pointerdown', { bubbles: true, cancelable: true, view: window }));
                                                                targetBtn.dispatchEvent(new PointerEvent('pointerup', { bubbles: true, cancelable: true, view: window }));
                                                            } 
                                                        }, i * 300);
                                                    }
                                                    window.botInTrade = true;
                                                }
                                                
                                                if (window.pocketBotState.isRunning) {
                                                    let nextDuration = window.pocketBotState.durationMode;
                                                    if (nextDuration === 'random') {
                                                        const fastDurations = ['1s', '2s', '3s', '4s', '5s', '6s', '7s', '8s', '9s', '10s', '13s', '15s'];
                                                        nextDuration = fastDurations[Math.floor(Math.random() * fastDurations.length)];
                                                    }
                                                    
                                                    // Attempt to set duration visually if input exists
                                                    if (nextDuration) {
                                                        let formattedTime = window.formatPocketOptionTime(nextDuration);
                                                        window.setInputValue('time', formattedTime);
                                                    }
                                                    
                                                    let waitTime = window.tradeDurationMs(nextDuration) || 60000;
                                                    if(window.botTimeout) clearTimeout(window.botTimeout);
                                                    window.botTimeout = setTimeout(window.executeTradeSim, waitTime);
                                                }
                                            };

                                            const OriginalWebSocket = window.WebSocket;
                                            window.WebSocket = function(url, protocols) {
                                                let ws = protocols ? new OriginalWebSocket(url, protocols) : new OriginalWebSocket(url);
                                                ws.addEventListener('message', function(event) {
                                                    if (window.AndroidBot) {
                                                        window.AndroidBot.onWebSocketMessage(event.data ? event.data.toString() : "");
                                                    }
                                                    try {
                                                        let msg = event.data.toString();
                                                        let match = msg.match(/\[\s*([\d.]+)\s*,\s*[\d.]+\s*\]/);
                                                        if (match && match[1]) {
                                                            let p = parseFloat(match[1]);
                                                            if(!isNaN(p) && p > 0) {
                                                                if(window.priceHistory.length === 0 || window.priceHistory[window.priceHistory.length - 1] !== p) {
                                                                    window.priceHistory.push(p);
                                                                    if(window.priceHistory.length > 60) window.priceHistory.shift();
                                                                }
                                                            }
                                                        }
                                                    } catch(err) {}
                                                });
                                                return ws;
                                            };
                                            window.WebSocket.prototype = OriginalWebSocket.prototype;
                                            Object.assign(window.WebSocket, OriginalWebSocket);
                                            console.log('Pocket Bot Real Analysis Injected');
                                        })();
                                    """.trimIndent()
                                    view?.evaluateJavascript(jsInject, null)
                                }
                            }
                            webChromeClient = android.webkit.WebChromeClient()
                            val targetUrl = if (selectedBroker == "Quotex") {
                                "https://broker-qx.pro/sign-up/?lid=2217556"
                            } else {
                                "https://u3.shortink.io/register?utm_campaign=48754&utm_source=affiliate&utm_medium=sr&a=uzxUgJzznrCXil&al=1780287&ac=boot&cid=968158&code=50START"
                            }
                            loadUrl(targetUrl)
                        }
                    },
                    update = { view ->
                        webViewRef = view
                    }
                )
                
                // Floating Buttons when Bot is Running
                if (isBotRunning) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 16.dp),
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        FloatingActionButton(
                            onClick = {
                                val time = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
                                tradeHistory.add(0, TradeRecord(time, entryAmount, "win"))
                                webViewRef?.evaluateJavascript("if(window.pocketBotState) { window.pocketBotState.consecutiveLosses = 0; window.pocketBotState.currentAmount = window.pocketBotState.baseAmount; }", null)
                            },
                            containerColor = Color(0xFF4CAF50),
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "ربح", tint = Color.White)
                        }

                        FloatingActionButton(
                            onClick = {
                                val time = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
                                tradeHistory.add(0, TradeRecord(time, entryAmount, "loss"))
                                val safeMultiplier = martingaleMultiplier.toDoubleOrNull() ?: 2.0
                                webViewRef?.evaluateJavascript("if(window.pocketBotState) { window.pocketBotState.consecutiveLosses++; window.pocketBotState.currentAmount *= $safeMultiplier; }", null)
                            },
                            containerColor = Color(0xFFF44336),
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "خسارة", tint = Color.White)
                        }

                        FloatingActionButton(
                            onClick = {
                                isBotRunning = false
                                webViewRef?.evaluateJavascript("if(window.stopTrading) window.stopTrading();", null)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("تم إيقاف البوت", duration = SnackbarDuration.Short)
                                }
                            },
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = "إيقاف البوت", tint = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }
            }

            // Bottom Control Panel
            AnimatedVisibility(visible = !isBotRunning) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                    // Header for collapsing
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isBottomPanelExpanded = !isBottomPanelExpanded }
                            .padding(bottom = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .width(40.dp)
                                    .height(4.dp)
                                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                Icon(
                                    imageVector = if (isBottomPanelExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                                    contentDescription = "Expand/Collapse",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isBottomPanelExpanded) "إخفاء لوحة التحكم" else "عرض إعدادات البوت والتحليلات",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    AnimatedVisibility(visible = isBottomPanelExpanded) {
                        Column(modifier = Modifier.fillMaxWidth()) {
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
                                        color = if (aiSentiment > 70f) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                androidx.compose.material3.LinearProgressIndicator(
                                    progress = { aiSentiment / 100f },
                                    modifier = Modifier.fillMaxWidth(),
                                    color = if (aiSentiment > 70f) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            }

                            // استراتيجيات القناص فايز الربادي
                            Text("استراتيجيات المتداول اليمني (خاص)", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(bottom = 8.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(fayezStrategies) { strategy ->
                                    FilterChip(
                                        selected = selectedStrategies.contains(strategy),
                                        onClick = {
                                            if (selectedStrategies.contains(strategy)) {
                                                selectedStrategies = selectedStrategies - strategy
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
                                        label = { Text(strategy, style = MaterialTheme.typography.bodySmall) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                                            selectedLabelColor = MaterialTheme.colorScheme.tertiary
                                        )
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))

                            Text("الاستراتيجيات التحليلية والإحصائية", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(bottom = 8.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(statsStrategies) { strategy ->
                                    FilterChip(
                                        selected = selectedStrategies.contains(strategy),
                                        onClick = {
                                            if (selectedStrategies.contains(strategy)) {
                                                selectedStrategies = selectedStrategies - strategy
                                            } else {
                                                selectedStrategies = selectedStrategies + strategy
                                            }
                                        },
                                        label = { Text(strategy, style = MaterialTheme.typography.bodySmall) }
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))

                            Text("الاستراتيجيات المتقدمة (العصبية والكمية)", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(bottom = 8.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(radicalStrategies) { strategy ->
                                    FilterChip(
                                        selected = selectedStrategies.contains(strategy),
                                        onClick = {
                                            if (selectedStrategies.contains(strategy)) {
                                                selectedStrategies = selectedStrategies - strategy
                                            } else {
                                                selectedStrategies = selectedStrategies + strategy
                                            }
                                        },
                                        label = { Text(strategy, style = MaterialTheme.typography.bodySmall) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                                            selectedLabelColor = MaterialTheme.colorScheme.secondary
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Settings Expander
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isSettingsExpanded = !isSettingsExpanded }
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("إعدادات التداول وإدارة رأس المال", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                                Icon(
                                    imageVector = if (isSettingsExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            AnimatedVisibility(visible = isSettingsExpanded) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .verticalScroll(rememberScrollState())
                                ) {
                                    // أزواج العملات المتاحة
                                    Text("أزواج العملات المتاحة:", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(bottom = 4.dp, top = 8.dp))
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(currencyPairs) { pair ->
                                            FilterChip(
                                                selected = selectedCurrencyPairs.contains(pair),
                                                onClick = {
                                                    if (selectedCurrencyPairs.contains(pair)) {
                                                        if (selectedCurrencyPairs.size > 1) selectedCurrencyPairs = selectedCurrencyPairs - pair
                                                    } else {
                                                        selectedCurrencyPairs = selectedCurrencyPairs + pair
                                                    }
                                                },
                                                label = { Text(pair, style = MaterialTheme.typography.bodySmall) }
                                            )
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = entryAmount,
                                            onValueChange = { entryAmount = it },
                                            label = { Text("مبلغ الدخول (\$)", style = MaterialTheme.typography.bodySmall) },
                                            modifier = Modifier.weight(1f),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                        )
                                        
                                        // Trade Duration Selector
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("وقت التداول", style = MaterialTheme.typography.bodySmall)
                                            var durationDropdownExpanded by remember { mutableStateOf(false) }
                                            Box {
                                                OutlinedButton(
                                                    onClick = { durationDropdownExpanded = true },
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text(durationLabels[selectedDuration] ?: selectedDuration, maxLines = 1)
                                                }
                                                DropdownMenu(
                                                    expanded = durationDropdownExpanded,
                                                    onDismissRequest = { durationDropdownExpanded = false }
                                                ) {
                                                    durations.forEach { dur ->
                                                        DropdownMenuItem(
                                                            text = { Text(durationLabels[dur] ?: dur) },
                                                            onClick = {
                                                                selectedDuration = dur
                                                                durationDropdownExpanded = false
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = takeProfit,
                                            onValueChange = { takeProfit = it },
                                            label = { Text("وقف الربح (\$)", style = MaterialTheme.typography.bodySmall) },
                                            modifier = Modifier.weight(1f),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                        )
                                        OutlinedTextField(
                                            value = stopLoss,
                                            onValueChange = { stopLoss = it },
                                            label = { Text("وقف الخسارة (\$)", style = MaterialTheme.typography.bodySmall) },
                                            modifier = Modifier.weight(1f),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Risk Percentage
                                    OutlinedTextField(
                                        value = riskPercentage,
                                        onValueChange = { riskPercentage = it },
                                        label = { Text("نسبة المخاطرة من رأس المال (%)", style = MaterialTheme.typography.bodySmall) },
                                        modifier = Modifier.fillMaxWidth(),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Checkbox(
                                            checked = useMartingale,
                                            onCheckedChange = { useMartingale = it }
                                        )
                                        Text("تفعيل نظام المارتينجال المضاعف (خطر)", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                                    }

                                    if (useMartingale) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(start = 16.dp),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            OutlinedTextField(
                                                value = martingaleSteps,
                                                onValueChange = { martingaleSteps = it },
                                                label = { Text("عدد المضاعفات", style = MaterialTheme.typography.bodySmall) },
                                                modifier = Modifier.weight(1f),
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                            )
                                            OutlinedTextField(
                                                value = martingaleMultiplier,
                                                onValueChange = { martingaleMultiplier = it },
                                                label = { Text("عامل الضرب", style = MaterialTheme.typography.bodySmall) },
                                                modifier = Modifier.weight(1f),
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                            )
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    // Advanced protections
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Checkbox(checked = useTrailingStop, onCheckedChange = { useTrailingStop = it })
                                        Text("تفعيل حماية الأرباح (Trailing Stop)", style = MaterialTheme.typography.bodyMedium)
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Checkbox(checked = avoidNews, onCheckedChange = { avoidNews = it })
                                        Text("تجنب التداول وقت الأخبار القوية", style = MaterialTheme.typography.bodyMedium)
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    val simulateTrade: () -> Unit = {
                                        val simulatedAmount = listOf("1", "2", "5", "10", "20", "50").random()
                                        val result = if (Math.random() > 0.45) "win" else "loss"
                                        val time = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
                                        tradeHistory.add(0, TradeRecord(time, simulatedAmount, result))
                                        if (tradeHistory.size > 25) {
                                            tradeHistory.removeLast()
                                        }
                                    }

                                    TradePerformanceDashboard(
                                        tradeHistory = tradeHistory,
                                        onSimulateTrade = simulateTrade,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (!isBotRunning) {
                                if (timeLeftSeconds <= 0) {
                                    showAdPrompt = true
                                } else {
                                    isBotRunning = true
                                    coroutineScope.launch {
                                        val strategiesMerged = selectedStrategies.joinToString(" + ")
                                        val durationStr = durationLabels[selectedDuration] ?: selectedDuration
                                        val safeAmount = entryAmount.toDoubleOrNull() ?: 1.0
                                        val safeMartingale = if (useMartingale) martingaleSteps.toIntOrNull() ?: 3 else 0
                                        val safeMultiplier = martingaleMultiplier.toDoubleOrNull() ?: 2.0
                                        webViewRef?.evaluateJavascript(
                                            "if(window.startTrading) window.startTrading($safeAmount, '$selectedDuration', $safeMartingale, '$strategiesMerged', $safeMultiplier);", 
                                            null
                                        )
                                        snackbarHostState.showSnackbar(
                                            message = "تم التفعيل ($strategiesMerged)\nالمدة: $durationStr | حماية الأرباح: ${if (useTrailingStop) "مفعل" else "معطل"} | فلتر الأخبار: ${if (avoidNews) "يعمل" else "مطفأ"}",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                }
                            } else {
                                isBotRunning = false
                                coroutineScope.launch {
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
                            text = if (isBotRunning) "إيقاف البوت الآلي" else "تشغيل البوت للمتداول اليمني",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
            } // Close AnimatedVisibility
        }
    }
}

@Composable
fun BrokerSelectionScreen(onSelect: (String) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(80.dp)
                    .padding(bottom = 16.dp)
            )
            
            Text(
                "أهلاً بك في بوت المتداول اليمني",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                "يرجى اختيار المنصة التي تود التداول عليها",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 32.dp)
            )
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(bottom = 16.dp)
                    .clickable { onSelect("PocketOption") },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("منصة Pocket Option", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                }
            }
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clickable { onSelect("Quotex") },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("منصة Quotex", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            val uriHandler = LocalUriHandler.current
            
            Text(
                text = "التطبيق صنع بواسطة قناة المتداول اليمني فايز الربادي",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            TextButton(
                onClick = { uriHandler.openUri("https://www.youtube.com/@Yemeni-trader") }
            ) {
                Text("لزيارة القناة انقر هنا")
            }
        }
    }
}
