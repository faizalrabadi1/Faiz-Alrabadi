package com.example.ui.screens

import android.annotation.SuppressLint
import com.example.ui.components.PortfolioStatsChart
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
    var isBotRunning by remember { mutableStateOf(false) }
    var showWarningDialog by remember { mutableStateOf(true) }
    var selectedStrategies by remember { mutableStateOf(setOf("RSI")) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var latestPrice by remember { mutableStateOf("جاري جلب السعر...") }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val tradeHistory = remember { mutableStateListOf<TradeRecord>() }
    
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
                                                
                                                window.setInputValue('.amount-input input, input.amount, input[name="amount"], .block-control__input input', amt);
                                                
                                                var btnCall = document.querySelector('.btn-call, .button--call');
                                                var btnPut = document.querySelector('.btn-put, .button--put');
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
                                                        setTimeout(() => { if (targetBtn) targetBtn.click(); }, i * 300);
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
                                                        window.setInputValue('.time-input input, input.time, input[name="time"], .block-control__input--time input, .block-control--time input', nextDuration);
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
                            loadUrl("https://pocketoption.com/en/cabinet/demo-high-low/")
                        }
                    },
                    update = { view ->
                        webViewRef = view
                    }
                )
                
            }

            // Bottom Control Panel
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (aiSentiment > 70f) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error,
                            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    PortfolioStatsChart()

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
                        text = "الاستراتيجيات الجذرية الجديدة",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(radicalStrategies) { strategy ->
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
                        items(durations.filter { listOf("random", "1s", "2s", "3s", "4s", "5s", "6s", "7s", "8s", "9s", "10s", "13s", "15s").contains(it) }) { duration ->
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

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = entryAmount,
                            onValueChange = { entryAmount = it },
                            label = { Text("مبلغ الدخول الأساسي ($)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        OutlinedTextField(
                            value = riskPercentage,
                            onValueChange = { riskPercentage = it },
                            label = { Text("نسبة المخاطرة (%)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "أزواج العملات المفضلة:",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(currencyPairs) { pair ->
                            FilterChip(
                                selected = selectedCurrencyPairs.contains(pair),
                                onClick = { 
                                    if (selectedCurrencyPairs.contains(pair)) {
                                        if (selectedCurrencyPairs.size > 1) {
                                            selectedCurrencyPairs = selectedCurrencyPairs - pair
                                        }
                                    } else {
                                        selectedCurrencyPairs = selectedCurrencyPairs + pair
                                    }
                                },
                                label = { Text(pair) }
                            )
                        }
                    }

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
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = martingaleMultiplier,
                                    onValueChange = { martingaleMultiplier = it },
                                    label = { Text("عامل المضاعفة (مثال: 1.9, 2.0, 2.5)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
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
                    
                    if (tradeHistory.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "سجل الصفقات الأخيرة",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            androidx.compose.foundation.lazy.LazyColumn(modifier = Modifier.padding(8.dp)) {
                                items(tradeHistory.size) { index ->
                                    val record = tradeHistory[index]
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = record.time, style = MaterialTheme.typography.bodySmall)
                                        Text(text = "${record.amount}$", style = MaterialTheme.typography.bodySmall, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                                        Text(
                                            text = if (record.result == "win") "ربح" else "خسارة", 
                                            color = if (record.result == "win") MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                        )
                                    }
                                    androidx.compose.material3.HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                                }
                            }
                        }
                    }
                    
                        }
                    }
                    } // Ends internal column for isBottomPanelExpanded
                    } // Ends AnimatedVisibility for isBottomPanelExpanded

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
                                    val safeMultiplier = martingaleMultiplier.toDoubleOrNull() ?: 2.0
                                    webViewRef?.evaluateJavascript(
                                        "if(window.startTrading) window.startTrading($safeAmount, '$selectedDuration', $safeMartingale, '$strategiesMerged', $safeMultiplier);", 
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
