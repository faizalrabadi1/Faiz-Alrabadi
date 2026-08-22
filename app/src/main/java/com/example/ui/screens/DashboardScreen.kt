package com.example.ui.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.components.ActiveTradeTracker
import com.example.ui.components.AdminDashboardDialog
import com.example.ui.components.AdminLoginDialog
import com.example.ui.components.AuthDialog
import com.example.ui.components.LiveActiveTrade
import com.example.ui.components.LiveIndicatorsPanel
import com.example.ui.components.ProActivationDialog
import com.example.ui.components.TechnicalIndicatorsState
import com.example.ui.components.TradePerformanceDashboard
import com.example.ui.components.calculateLiveIndicators
import com.example.util.AppLanguage
import com.example.util.AppStrings
import com.example.util.LocalAppLanguage
import com.example.util.ProLicenseManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
    fun onWebSocketMessage(message: String) {}

    @JavascriptInterface
    fun onPriceUpdate(price: String) {
        onPriceUpdated(price)
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
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val licenseManager = remember { ProLicenseManager(context) }
    var isProActive by remember { mutableStateOf(licenseManager.isProActive()) }

    var showSplash by remember { mutableStateOf(true) }
    var showOnboarding by remember { mutableStateOf(false) }
    var showAuthDialog by remember { mutableStateOf(false) }
    var authInitialTab by remember { mutableIntStateOf(0) }

    var currentLanguage by remember { mutableStateOf(AppLanguage.ARABIC) }
    var selectedBottomTab by remember { mutableIntStateOf(1) } // Default to Robot Tab (matching video)

    var isDemoAccount by remember { mutableStateOf(true) }
    var accountBalance by remember { mutableDoubleStateOf(50000.0) }

    var showProDialog by remember { mutableStateOf(false) }
    var showAdminLoginDialog by remember { mutableStateOf(false) }
    var showAdminDashboardDialog by remember { mutableStateOf(false) }

    var isBotRunning by remember { mutableStateOf(false) }
    var showWarningDialog by remember { mutableStateOf(false) }

    var timeLeftSeconds by remember { mutableIntStateOf(0) }
    var showAdPrompt by remember { mutableStateOf(false) }
    var rewardedAd by remember { mutableStateOf<RewardedAd?>(null) }

    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    val pocketOptionUrl = "https://u3.shortink.io/register?utm_campaign=48754&utm_source=affiliate&utm_medium=sr&a=uzxUgJzznrCXil&al=1780287&ac=boot&cid=968158&code=50START"

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Live Indicators and Trades State
    val livePriceHistory = remember {
        mutableStateListOf(
            1.08500, 1.08506, 1.08512, 1.08518, 1.08510,
            1.08522, 1.08528, 1.08520, 1.08532, 1.08538,
            1.08525, 1.08515, 1.08520, 1.08526, 1.08530
        )
    }
    val activeTrades = remember { mutableStateListOf<LiveActiveTrade>() }
    var lastOutcomeNotification by remember { mutableStateOf<String?>(null) }
    val liveIndicators = remember(livePriceHistory.toList()) { calculateLiveIndicators(livePriceHistory) }

    val tradeHistory = remember {
        mutableStateListOf(
            TradeRecord("10:15:30", "10", "win"),
            TradeRecord("10:17:45", "10", "loss"),
            TradeRecord("10:20:12", "10", "win"),
            TradeRecord("10:24:00", "10", "win")
        )
    }

    // Ads Loading
    LaunchedEffect(isProActive) {
        if (!isProActive) {
            val adRequest = AdRequest.Builder().build()
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
    }

    // Real-Time Price Simulation & Auto Execution Loop
    LaunchedEffect(isBotRunning) {
        var basePrice = 567.80
        while (true) {
            delay(1000)
            val delta = ((-8..8).random() / 100.0)
            basePrice += delta
            livePriceHistory.add(basePrice)
            if (livePriceHistory.size > 60) livePriceHistory.removeAt(0)

            // Resolve active trades
            val iterator = activeTrades.iterator()
            while (iterator.hasNext()) {
                val trade = iterator.next()
                trade.currentPrice = basePrice
                trade.remainingSeconds -= 1
                if (trade.remainingSeconds <= 0) {
                    val isWin = if (trade.isCall) trade.currentPrice >= trade.entryPrice else trade.currentPrice <= trade.entryPrice
                    val result = if (isWin) "win" else "loss"
                    tradeHistory.add(0, TradeRecord(trade.startTime, trade.amount.toString(), result))
                    if (isWin) {
                        val profit = trade.amount * 0.92
                        accountBalance += profit
                        lastOutcomeNotification = AppStrings.tradeWonNotification(currentLanguage, String.format("%.2f", profit))
                    } else {
                        accountBalance -= trade.amount
                        lastOutcomeNotification = AppStrings.tradeLostNotification(currentLanguage, String.format("%.2f", trade.amount))
                    }
                    iterator.remove()
                }
            }

            // Auto-trigger trade during bot running
            if (isBotRunning && activeTrades.isEmpty() && (1..4).random() == 2) {
                val dir = if (Math.random() > 0.5) "CALL" else "PUT"
                val curTime = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
                val newTrade = LiveActiveTrade(
                    asset = "FACEBOOK INC OTC",
                    direction = dir,
                    entryPrice = basePrice,
                    currentPrice = basePrice,
                    amount = 10.0,
                    totalSeconds = 60,
                    remainingSeconds = 60,
                    startTime = curTime,
                    reason = "MACD Momentum Cross + 92% OTC Signal"
                )
                activeTrades.add(newTrade)
                webViewRef?.evaluateJavascript(
                    "if(window.executeSingleTrade) { window.executeSingleTrade('$dir', 10.0, '1m'); }",
                    null
                )
            }
        }
    }

    if (showProDialog) {
        ProActivationDialog(
            currentLanguage = currentLanguage,
            licenseManager = licenseManager,
            onDismiss = { showProDialog = false },
            onActivatedSuccess = {
                isProActive = licenseManager.isProActive()
            }
        )
    }

    if (showAdminLoginDialog) {
        AdminLoginDialog(
            currentLanguage = currentLanguage,
            licenseManager = licenseManager,
            onDismiss = { showAdminLoginDialog = false },
            onLoginSuccess = {
                showAdminLoginDialog = false
                showAdminDashboardDialog = true
            }
        )
    }

    if (showAdminDashboardDialog) {
        AdminDashboardDialog(
            currentLanguage = currentLanguage,
            licenseManager = licenseManager,
            onDismiss = {
                showAdminDashboardDialog = false
                isProActive = licenseManager.isProActive()
            }
        )
    }

    if (showAuthDialog) {
        AuthDialog(
            currentLanguage = currentLanguage,
            initialTab = authInitialTab,
            onDismiss = { showAuthDialog = false },
            onAuthSuccess = { _, isDemo ->
                isDemoAccount = isDemo
                showAuthDialog = false
                showOnboarding = false
            }
        )
    }

    CompositionLocalProvider(
        LocalAppLanguage provides currentLanguage,
        LocalLayoutDirection provides (if (currentLanguage.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr)
    ) {
        if (showSplash) {
            SplashScreen(
                currentLanguage = currentLanguage,
                onSplashFinished = {
                    showSplash = false
                    showOnboarding = false // Direct entry to bot matching video flow
                }
            )
        } else if (showOnboarding) {
            OnboardingScreen(
                currentLanguage = currentLanguage,
                onOpenRegister = {
                    authInitialTab = 0
                    showAuthDialog = true
                },
                onOpenLogin = {
                    authInitialTab = 1
                    showAuthDialog = true
                },
                onSkipToDemo = {
                    isDemoAccount = true
                    showOnboarding = false
                }
            )
        } else {
            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    Surface(
                        color = Color(0xFF101221),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Logo Title
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF6C5CE7),
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("1R", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Rocket Matix",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            // Quick Actions: Admin, VIP Pro Badge, Language
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (isProActive) {
                                    Surface(
                                        color = Color(0xFFFFB300),
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.clickable { showProDialog = true }
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Icon(Icons.Default.Diamond, contentDescription = null, tint = Color(0xFF101221), modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("VIP", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFF101221))
                                        }
                                    }
                                } else {
                                    Surface(
                                        color = Color(0xFF242944),
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.clickable { showProDialog = true }
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Icon(Icons.Default.Diamond, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("PRO", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    }
                                }

                                IconButton(
                                    onClick = { showAdminLoginDialog = true },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.AdminPanelSettings, contentDescription = "Admin", tint = Color(0xFF8E9AA8), modifier = Modifier.size(20.dp))
                                }

                                IconButton(
                                    onClick = { currentLanguage = currentLanguage.toggle() },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.Language, contentDescription = "Lang", tint = Color(0xFF8E9AA8), modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                },
                bottomBar = {
                    NavigationBar(
                        containerColor = Color(0xFF101221),
                        contentColor = Color(0xFF6C5CE7),
                        tonalElevation = 8.dp
                    ) {
                        // Tab 0: المنصة (Platform)
                        NavigationBarItem(
                            selected = selectedBottomTab == 0,
                            onClick = { selectedBottomTab = 0 },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.ShowChart,
                                    contentDescription = AppStrings.tabPlatform(currentLanguage)
                                )
                            },
                            label = {
                                Text(
                                    text = AppStrings.tabPlatform(currentLanguage),
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedBottomTab == 0) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = Color.White,
                                unselectedIconColor = Color(0xFF8E9AA8),
                                unselectedTextColor = Color(0xFF8E9AA8),
                                indicatorColor = Color(0xFF6C5CE7)
                            )
                        )

                        // Tab 1: روبوت (Robot)
                        NavigationBarItem(
                            selected = selectedBottomTab == 1,
                            onClick = { selectedBottomTab = 1 },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.SmartToy,
                                    contentDescription = AppStrings.tabRobot(currentLanguage)
                                )
                            },
                            label = {
                                Text(
                                    text = AppStrings.tabRobot(currentLanguage),
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedBottomTab == 1) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = Color.White,
                                unselectedIconColor = Color(0xFF8E9AA8),
                                unselectedTextColor = Color(0xFF8E9AA8),
                                indicatorColor = Color(0xFF6C5CE7)
                            )
                        )

                        // Tab 2: الدعم (Support)
                        NavigationBarItem(
                            selected = selectedBottomTab == 2,
                            onClick = { selectedBottomTab = 2 },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Forum,
                                    contentDescription = AppStrings.tabSupport(currentLanguage)
                                )
                            },
                            label = {
                                Text(
                                    text = AppStrings.tabSupport(currentLanguage),
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedBottomTab == 2) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = Color.White,
                                unselectedIconColor = Color(0xFF8E9AA8),
                                unselectedTextColor = Color(0xFF8E9AA8),
                                indicatorColor = Color(0xFF6C5CE7)
                            )
                        )

                        // Tab 3: الحساب (Account)
                        NavigationBarItem(
                            selected = selectedBottomTab == 3,
                            onClick = { selectedBottomTab = 3 },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = AppStrings.tabAccount(currentLanguage)
                                )
                            },
                            label = {
                                Text(
                                    text = AppStrings.tabAccount(currentLanguage),
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedBottomTab == 3) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = Color.White,
                                unselectedIconColor = Color(0xFF8E9AA8),
                                unselectedTextColor = Color(0xFF8E9AA8),
                                indicatorColor = Color(0xFF6C5CE7)
                            )
                        )
                    }
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // TAB 0: المنصة (Platform) - WebView + HUD
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .then(if (selectedBottomTab != 0) Modifier.size(0.dp) else Modifier)
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
                                    settings.cacheMode = WebSettings.LOAD_DEFAULT
                                    settings.userAgentString = "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"

                                    addJavascriptInterface(
                                        BotJavascriptInterface(
                                            onPriceUpdated = {},
                                            onTradeResult = { time, amount, result ->
                                                tradeHistory.add(0, TradeRecord(time, amount, result))
                                            }
                                        ),
                                        "AndroidBot"
                                    )

                                    webViewClient = object : WebViewClient() {
                                        override fun onPageFinished(view: WebView?, url: String?) {
                                            super.onPageFinished(view, url)
                                            // Fast auto trade injector script
                                            view?.evaluateJavascript(
                                                """
                                                window.executeSingleTrade = function(dir, amt, dur) {
                                                    try {
                                                        var isCall = (dir === 'CALL');
                                                        var btn = document.querySelector(isCall ? '.btn-call, .action-call, [data-action="call"]' : '.btn-put, .action-put, [data-action="put"]');
                                                        if(btn) btn.click();
                                                    } catch(e) {}
                                                };
                                                """.trimIndent(),
                                                null
                                            )
                                        }
                                    }
                                    loadUrl(pocketOptionUrl)
                                    webViewRef = this
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )

                        // Floating Live Indicator Signal HUD on top of chart
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF101221).copy(alpha = 0.92f),
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(12.dp)
                                .border(1.dp, Color(0xFF282D4A), RoundedCornerShape(16.dp))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(if (isBotRunning) Color(0xFF00E676) else Color(0xFFFF5252), CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isBotRunning) "البوت نشط" else "البوت متوقف",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }

                                Text("|", color = Color(0xFF384065))

                                Text(
                                    text = "RSI: ${liveIndicators.rsi.toInt()}",
                                    fontSize = 12.sp,
                                    color = Color(0xFF00D2FF),
                                    fontWeight = FontWeight.Bold
                                )

                                Text("|", color = Color(0xFF384065))

                                Text(
                                    text = "الصفقات النشطة: ${activeTrades.size}",
                                    fontSize = 12.sp,
                                    color = Color(0xFFFFB300),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // TAB 1: روبوت (Robot)
                    if (selectedBottomTab == 1) {
                        RobotTabScreen(
                            currentLanguage = currentLanguage,
                            isBotRunning = isBotRunning,
                            onStartBot = {
                                isBotRunning = true
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(AppStrings.botActivated(currentLanguage))
                                }
                            },
                            onStopBot = {
                                isBotRunning = false
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(AppStrings.botDeactivated(currentLanguage))
                                }
                            },
                            onShowChart = {
                                selectedBottomTab = 0
                            },
                            activeTradesCount = activeTrades.size,
                            accountBalance = accountBalance,
                            isDemoAccount = isDemoAccount,
                            onToggleAccountType = { isDemo ->
                                isDemoAccount = isDemo
                                accountBalance = if (isDemo) 50000.0 else 0.0
                            }
                        )
                    }

                    // TAB 2: الدعم (Support)
                    if (selectedBottomTab == 2) {
                        SupportScreen(
                            currentLanguage = currentLanguage,
                            onOpenTelegram = {
                                uriHandler.openUri("https://t.me/yemenitrader")
                            }
                        )
                    }

                    // TAB 3: الحساب (Account)
                    if (selectedBottomTab == 3) {
                        AccountScreen(
                            currentLanguage = currentLanguage,
                            accountBalance = accountBalance,
                            isDemo = isDemoAccount,
                            isPro = isProActive,
                            onOpenDeposit = {
                                selectedBottomTab = 0
                            },
                            onOpenWithdraw = {
                                selectedBottomTab = 0
                            },
                            onOpenAdmin = {
                                showAdminLoginDialog = true
                            },
                            onOpenProActivation = {
                                showProDialog = true
                            },
                            onToggleLanguage = {
                                currentLanguage = currentLanguage.toggle()
                            }
                        )
                    }
                }
            }
        }
    }
}
