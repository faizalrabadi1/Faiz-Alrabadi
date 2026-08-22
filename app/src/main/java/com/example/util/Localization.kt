package com.example.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf

enum class AppLanguage(val code: String, val displayName: String, val isRtl: Boolean) {
    ARABIC("ar", "العربية", true),
    ENGLISH("en", "English", false);

    fun toggle(): AppLanguage = if (this == ARABIC) ENGLISH else ARABIC
}

val LocalAppLanguage = compositionLocalOf { AppLanguage.ARABIC }

object AppStrings {
    fun welcomeTitle(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "أهلاً بك في بوت المتداول اليمني"
        AppLanguage.ENGLISH -> "Welcome to Yemeni Trader Bot"
    }

    fun selectBrokerDesc(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "يرجى اختيار المنصة التي تود التداول عليها"
        AppLanguage.ENGLISH -> "Please select the trading platform you wish to use"
    }

    fun pocketOptionPlatform(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "منصة Pocket Option"
        AppLanguage.ENGLISH -> "Pocket Option Platform"
    }

    fun quotexPlatform(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "منصة Quotex"
        AppLanguage.ENGLISH -> "Quotex Platform"
    }

    fun createdBy(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "التطبيق صنع بواسطة قناة المتداول اليمني فايز الربادي"
        AppLanguage.ENGLISH -> "Created by Yemeni Trader Channel - Fayez Al-Rabadi"
    }

    fun visitChannel(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "لزيارة القناة انقر هنا"
        AppLanguage.ENGLISH -> "Click here to visit the channel"
    }

    fun dashboardTitle(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "لوحة تحكم البوت"
        AppLanguage.ENGLISH -> "Trading Bot Dashboard"
    }

    fun warningTitle(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "تنبيه هام"
        AppLanguage.ENGLISH -> "Important Notice"
    }

    fun warningMessage(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "التداول ينطوي على مخاطر عالية جداً.\n\nهذا بوت خاص بقناة المتداول اليمني فايز الربادي للتداول الآلي."
        AppLanguage.ENGLISH -> "Trading involves very high risk.\n\nThis bot is exclusively dedicated to the Yemeni Trader Fayez Al-Rabadi channel for automated trading."
    }

    fun ok(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "موافق"
        AppLanguage.ENGLISH -> "OK"
    }

    fun timeExpiredTitle(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "انتهى وقتك"
        AppLanguage.ENGLISH -> "Time Expired"
    }

    fun timeExpiredDesc(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "شاهد إعلانًا لتحصل على ٥ دقائق من التداول الآلي"
        AppLanguage.ENGLISH -> "Watch an ad to get 5 minutes of automated trading"
    }

    fun watchAd(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "مشاهدة الإعلان"
        AppLanguage.ENGLISH -> "Watch Ad"
    }

    fun cancel(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "إلغاء"
        AppLanguage.ENGLISH -> "Cancel"
    }

    fun adNotReady(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "الإعلان غير جاهز بعد. تأكد من إيقاف مانع الإعلانات، أو جرب تشغيل VPN إذا كانت دولتك تحظر الإعلانات."
        AppLanguage.ENGLISH -> "Ad is not ready yet. Make sure ad blockers are disabled, or try using a VPN if ads are restricted in your region."
    }

    fun botRunning(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "إيقاف البوت الآلي"
        AppLanguage.ENGLISH -> "Stop Automated Bot"
    }

    fun botStopped(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "تشغيل البوت للمتداول اليمني"
        AppLanguage.ENGLISH -> "Start Yemeni Trader Bot"
    }

    fun botActivated(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "تم التفعيل بنجاح"
        AppLanguage.ENGLISH -> "Bot activated successfully"
    }

    fun botDeactivated(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "تم إيقاف البوت"
        AppLanguage.ENGLISH -> "Bot stopped"
    }

    fun hideControlPanel(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "إخفاء لوحة التحكم"
        AppLanguage.ENGLISH -> "Hide Control Panel"
    }

    fun showControlPanel(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "عرض إعدادات البوت والتحليلات"
        AppLanguage.ENGLISH -> "Show Bot Settings & Analytics"
    }

    fun aiMarketAnalysis(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "تحليل الذكاء الاصطناعي للسوق"
        AppLanguage.ENGLISH -> "AI Market Sentiment Analysis"
    }

    fun expectedSuccessRate(lang: AppLanguage, rate: Int) = when (lang) {
        AppLanguage.ARABIC -> "$rate% نسبة النجاح المتوقعة"
        AppLanguage.ENGLISH -> "$rate% Expected Win Rate"
    }

    fun fayezStrategiesHeader(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "استراتيجيات المتداول اليمني (خاص)"
        AppLanguage.ENGLISH -> "Yemeni Trader Strategies (Exclusive)"
    }

    fun statsStrategiesHeader(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "الاستراتيجيات التحليلية والإحصائية"
        AppLanguage.ENGLISH -> "Analytical & Statistical Strategies"
    }

    fun radicalStrategiesHeader(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "الاستراتيجيات المتقدمة (العصبية والكمية)"
        AppLanguage.ENGLISH -> "Advanced Strategies (Neural & Quantum)"
    }

    fun tradingSettingsHeader(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "إعدادات التداول وإدارة رأس المال"
        AppLanguage.ENGLISH -> "Trading & Risk Management Settings"
    }

    fun availablePairs(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "أزواج العملات المتاحة:"
        AppLanguage.ENGLISH -> "Available Currency Pairs:"
    }

    fun entryAmount(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "مبلغ الدخول ($)"
        AppLanguage.ENGLISH -> "Entry Amount ($)"
    }

    fun tradeDuration(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "وقت التداول"
        AppLanguage.ENGLISH -> "Trade Duration"
    }

    fun takeProfit(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "وقف الربح ($)"
        AppLanguage.ENGLISH -> "Take Profit ($)"
    }

    fun stopLoss(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "وقف الخسارة ($)"
        AppLanguage.ENGLISH -> "Stop Loss ($)"
    }

    fun riskPercentage(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "نسبة المخاطرة من رأس المال (%)"
        AppLanguage.ENGLISH -> "Capital Risk Percentage (%)"
    }

    fun enableMartingale(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "تفعيل نظام المارتينجال المضاعف (خطر)"
        AppLanguage.ENGLISH -> "Enable Martingale Multiplier (High Risk)"
    }

    fun martingaleSteps(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "عدد المضاعفات"
        AppLanguage.ENGLISH -> "Martingale Steps"
    }

    fun martingaleMultiplier(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "عامل الضرب"
        AppLanguage.ENGLISH -> "Multiplier Factor"
    }

    fun trailingStop(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "تفعيل حماية الأرباح (Trailing Stop)"
        AppLanguage.ENGLISH -> "Enable Trailing Stop (Profit Lock)"
    }

    fun avoidNews(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "تجنب التداول وقت الأخبار القوية"
        AppLanguage.ENGLISH -> "Avoid High-Impact News Events"
    }

    fun performanceAnalytics(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "تحليل الأداء وسجل الصفقات"
        AppLanguage.ENGLISH -> "Performance Analytics & Trade Log"
    }

    fun simulateTrade(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "محاكاة صفقة"
        AppLanguage.ENGLISH -> "Simulate Trade"
    }

    fun netProfit(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "صافي الأرباح"
        AppLanguage.ENGLISH -> "Net Profit"
    }

    fun winRate(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "نسبة النجاح"
        AppLanguage.ENGLISH -> "Win Rate"
    }

    fun totalTrades(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "الصفقات"
        AppLanguage.ENGLISH -> "Trades"
    }

    fun cumulativeCurve(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "منحنى الأرباح التراكمي:"
        AppLanguage.ENGLISH -> "Cumulative Profit Curve:"
    }

    fun noTradesYet(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "لا توجد صفقات مسجلة بعد"
        AppLanguage.ENGLISH -> "No trades recorded yet"
    }

    fun startBotToDrawChart(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "قم بتشغيل البوت لبدء رسم المنحنى التفاعلي"
        AppLanguage.ENGLISH -> "Start the bot to begin plotting the interactive curve"
    }

    fun recentTrades(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "سجل الصفقات الأخيرة:"
        AppLanguage.ENGLISH -> "Recent Trades Log:"
    }

    fun waitingFirstTrades(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "بانتظار تنفيذ الصفقات الأولى..."
        AppLanguage.ENGLISH -> "Waiting for first trades execution..."
    }

    fun amountLabel(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "المبلغ:"
        AppLanguage.ENGLISH -> "Amount:"
    }

    fun loss(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "خسارة"
        AppLanguage.ENGLISH -> "Loss"
    }

    fun win(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "ربح"
        AppLanguage.ENGLISH -> "Win"
    }

    fun stopBotFab(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "إيقاف البوت"
        AppLanguage.ENGLISH -> "Stop Bot"
    }

    fun getDurationLabel(key: String, lang: AppLanguage): String {
        return when (lang) {
            AppLanguage.ARABIC -> when (key) {
                "random" -> "عشوائي"
                "1s" -> "ثانية واحدة"
                "2s" -> "ثانيتين"
                "3s" -> "3 ثواني"
                "4s" -> "4 ثواني"
                "5s" -> "5 ثواني"
                "6s" -> "6 ثواني"
                "7s" -> "7 ثواني"
                "8s" -> "8 ثواني"
                "9s" -> "9 ثواني"
                "10s" -> "10 ثواني"
                "13s" -> "13 ثانية"
                "15s" -> "15 ثانية"
                "18s" -> "18 ثانية"
                "22s" -> "22 ثانية"
                "30s" -> "30 ثانية"
                "34s" -> "34 ثانية"
                "49s" -> "49 ثانية"
                "1m" -> "1 دقيقة"
                "144s" -> "144 ثانية"
                "2m" -> "2 دقيقة"
                "3m" -> "3 دقائق"
                "5m" -> "5 دقائق"
                else -> key
            }
            AppLanguage.ENGLISH -> when (key) {
                "random" -> "Random"
                "1s" -> "1 Second"
                "2s" -> "2 Seconds"
                "3s" -> "3 Seconds"
                "4s" -> "4 Seconds"
                "5s" -> "5 Seconds"
                "6s" -> "6 Seconds"
                "7s" -> "7 Seconds"
                "8s" -> "8 Seconds"
                "9s" -> "9 Seconds"
                "10s" -> "10 Seconds"
                "13s" -> "13 Seconds"
                "15s" -> "15 Seconds"
                "18s" -> "18 Seconds"
                "22s" -> "22 Seconds"
                "30s" -> "30 Seconds"
                "34s" -> "34 Seconds"
                "49s" -> "49 Seconds"
                "1m" -> "1 Minute"
                "144s" -> "144 Seconds"
                "2m" -> "2 Minutes"
                "3m" -> "3 Minutes"
                "5m" -> "5 Minutes"
                else -> key
            }
        }
    }

    fun getStrategyDisplayName(key: String, lang: AppLanguage): String {
        if (lang == AppLanguage.ARABIC) return key
        return when (key) {
            "استراتيجية القناص (فايز الخاص)" -> "Sniper Strategy (Fayez Custom)"
            "استراتيجية الشموع (فايز)" -> "Candlestick Strategy (Fayez)"
            "استراتيجية VIP" -> "VIP Strategy"
            "الاستراتيجية الثانية (تقاطع SMA)" -> "2nd Strategy (SMA Cross)"
            "التحليل الفني بعلم الرمل" -> "Geomancy Technical Analysis"
            "الانحدار الخطي (Linear Regression)" -> "Linear Regression"
            "الارتداد المعياري (Z-Score)" -> "Z-Score Mean Reversion"
            "سلاسل ماركوف (Markov Chains)" -> "Markov Chains"
            "الاحتمالية البايزية (Bayesian Probability)" -> "Bayesian Probability"
            "توزيع جاوس (Gaussian Distribution)" -> "Gaussian Distribution"
            "الشبكة العصبية العميقة" -> "Deep Neural Network"
            "الزخم الكمي" -> "Quantum Momentum"
            "خوارزمية الفوضى" -> "Chaos Algorithm"
            "الأنماط الفراكتلية" -> "Fractal Patterns"
            "موجات إليوت الذكية" -> "Smart Elliott Waves"
            else -> key
        }
    }

    // Pro System & Admin Dashboard Strings
    fun proBadge(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "برو VIP"
        AppLanguage.ENGLISH -> "PRO VIP"
    }

    fun goProButton(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "تفعيل برو"
        AppLanguage.ENGLISH -> "Go PRO"
    }

    fun proActiveMessage(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "النسخة البرو مفعلة (بدون إعلانات وتداول غير محدود)"
        AppLanguage.ENGLISH -> "PRO version active (No ads & unlimited trading)"
    }

    fun adminButton(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "لوحة الأدمن"
        AppLanguage.ENGLISH -> "Admin Panel"
    }

    fun proActivationTitle(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "تفعيل اشتراك PRO بدون إعلانات"
        AppLanguage.ENGLISH -> "Activate PRO Ad-Free License"
    }

    fun proActivationDesc(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "أدخل السيريال نمبر الذي حصلت عليه للاستمتاع بالبوت بدون إعلانات وبوقت تشغيل مستمر."
        AppLanguage.ENGLISH -> "Enter your subscription serial number to enjoy unlimited ad-free automated trading."
    }

    fun serialInputLabel(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "أدخل السيريال نمبر (PRO-XXXX-XXXX-XXXX)"
        AppLanguage.ENGLISH -> "Enter Serial Number (PRO-XXXX-XXXX-XXXX)"
    }

    fun pasteFromClipboard(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "لصق"
        AppLanguage.ENGLISH -> "Paste"
    }

    fun activateLicense(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "تفعيل السيريال"
        AppLanguage.ENGLISH -> "Activate Serial"
    }

    fun contactToBuyPro(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "طلب سيريال اشتراك (تواصل مع المطور)"
        AppLanguage.ENGLISH -> "Request a PRO Serial (Contact Developer)"
    }

    fun activeSubscriptionDetails(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "تفاصيل الاشتراك الحالي:"
        AppLanguage.ENGLISH -> "Current Subscription Details:"
    }

    fun subscriptionType(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "نوع الباقة:"
        AppLanguage.ENGLISH -> "Plan Type:"
    }

    fun expiresOn(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "تاريخ الانتهاء:"
        AppLanguage.ENGLISH -> "Expires on:"
    }

    fun remainingTime(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "الوقت المتبقي:"
        AppLanguage.ENGLISH -> "Remaining Time:"
    }

    fun adminLoginTitle(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "دخول لوحة تحكم الأدمن"
        AppLanguage.ENGLISH -> "Admin Dashboard Access"
    }

    fun adminLoginDesc(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "يرجى إدخال السيريال الخاص بالأدمن للوصول إلى أدوات توليد التراخيص وإدارتها."
        AppLanguage.ENGLISH -> "Please enter the Admin Master Serial to access key generation and license management."
    }

    fun masterAdminSerialLabel(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "سيريال الأدمن الماستر"
        AppLanguage.ENGLISH -> "Master Admin Serial"
    }

    fun loginButton(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "دخول للأدمن"
        AppLanguage.ENGLISH -> "Enter Admin"
    }

    fun adminDashboardTitle(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "لوحة تحكم وتوليد السيريالات (Admin)"
        AppLanguage.ENGLISH -> "Admin License Generator Dashboard"
    }

    fun generateNewSerialHeader(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "توليد سيريال اشتراك جديد للعميل"
        AppLanguage.ENGLISH -> "Generate New Client Serial"
    }

    fun selectDuration(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "مدة الاشتراك:"
        AppLanguage.ENGLISH -> "Subscription Duration:"
    }

    fun clientNameOptional(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "اسم العميل / المستفيد (اختياري)"
        AppLanguage.ENGLISH -> "Client Name (Optional)"
    }

    fun clientNoteOptional(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "ملاحظات (اختياري)"
        AppLanguage.ENGLISH -> "Notes (Optional)"
    }

    fun generateSerialButton(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "توليد السيريال الآن"
        AppLanguage.ENGLISH -> "Generate Serial Now"
    }

    fun serialGeneratedSuccess(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "تم توليد السيريال بنجاح!"
        AppLanguage.ENGLISH -> "Serial Generated Successfully!"
    }

    fun copySerial(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "نسخ السيريال"
        AppLanguage.ENGLISH -> "Copy Serial"
    }

    fun shareSerial(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "مشاركة السيريال للعميل"
        AppLanguage.ENGLISH -> "Share Serial to Client"
    }

    fun generatedHistoryTitle(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "سجل السيريالات المولدة"
        AppLanguage.ENGLISH -> "Generated Serials Log"
    }

    fun noSerialsYet(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "لم تقم بتوليد أي سيريالات بعد."
        AppLanguage.ENGLISH -> "No serials generated yet."
    }

    fun totalGeneratedCount(lang: AppLanguage, count: Int) = when (lang) {
        AppLanguage.ARABIC -> "إجمالي السيريالات: $count"
        AppLanguage.ENGLISH -> "Total Serials: $count"
    }

    fun copiedToClipboard(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "تم نسخ السيريال إلى الحافظة"
        AppLanguage.ENGLISH -> "Serial copied to clipboard"
    }

    fun close(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "إغلاق"
        AppLanguage.ENGLISH -> "Close"
    }

    // Live Indicators & Practical Strategy Execution
    fun liveIndicatorsTitle(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "المؤشرات الفنية الحية وتنفيذ الاستراتيجية"
        AppLanguage.ENGLISH -> "Live Technical Indicators & Strategy Execution"
    }

    fun liveIndicatorsTab(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "المؤشرات الفنية"
        AppLanguage.ENGLISH -> "Technical Indicators"
    }

    fun activeTradesTab(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "الصفقات الحية"
        AppLanguage.ENGLISH -> "Active Trades"
    }

    fun strategySignalHeader(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "إشارة الاستراتيجية المركبة"
        AppLanguage.ENGLISH -> "Consensus Strategy Signal"
    }

    fun strongBuy(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "شراء قوي جداً (CALL)"
        AppLanguage.ENGLISH -> "STRONG BUY (CALL)"
    }

    fun buy(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "شراء (CALL)"
        AppLanguage.ENGLISH -> "BUY (CALL)"
    }

    fun neutral(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "انتظار / محايد (WAIT)"
        AppLanguage.ENGLISH -> "NEUTRAL (WAIT)"
    }

    fun sell(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "بيع (PUT)"
        AppLanguage.ENGLISH -> "SELL (PUT)"
    }

    fun strongSell(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "بيع قوي جداً (PUT)"
        AppLanguage.ENGLISH -> "STRONG SELL (PUT)"
    }

    fun signalStrength(lang: AppLanguage, pct: Int) = when (lang) {
        AppLanguage.ARABIC -> "قوة الإشارة: $pct%"
        AppLanguage.ENGLISH -> "Signal Strength: $pct%"
    }

    fun executeCallNow(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "تنفيذ صعود (CALL 🟢)"
        AppLanguage.ENGLISH -> "Execute CALL 🟢"
    }

    fun executePutNow(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "تنفيذ هبوط (PUT 🔴)"
        AppLanguage.ENGLISH -> "Execute PUT 🔴"
    }

    fun autoExecutionToggle(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "التنفيذ الآلي للصفقات عند إشارة قوية"
        AppLanguage.ENGLISH -> "Auto-execute trades on strong signal"
    }

    fun rsiIndicator(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "مؤشر القوة النسبية RSI(14)"
        AppLanguage.ENGLISH -> "RSI(14) Relative Strength"
    }

    fun macdIndicator(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "مؤشر الماكد MACD(12,26,9)"
        AppLanguage.ENGLISH -> "MACD(12,26,9) Trend Momentum"
    }

    fun bollingerBands(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "بولنجر باند Bollinger(20,2)"
        AppLanguage.ENGLISH -> "Bollinger Bands(20,2)"
    }

    fun movingAverages(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "المتوسطات المتحركة (EMA 9/21, SMA 50)"
        AppLanguage.ENGLISH -> "Moving Averages (EMA 9/21, SMA 50)"
    }

    fun supportResistance(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "مستويات الدعم والمقاومة والبيفوت"
        AppLanguage.ENGLISH -> "Support & Resistance Pivots"
    }

    fun candlestickPattern(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "نموذج الشموع وعلم الرمل"
        AppLanguage.ENGLISH -> "Candlestick & Geomancy Pattern"
    }

    fun oversold(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "تشبع بيعي (فرصة شراء 🟢)"
        AppLanguage.ENGLISH -> "Oversold (Buy Zone 🟢)"
    }

    fun overbought(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "تشبع شرائي (فرصة بيع 🔴)"
        AppLanguage.ENGLISH -> "Overbought (Sell Zone 🔴)"
    }

    fun neutralZone(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "منطقة معتدلة"
        AppLanguage.ENGLISH -> "Neutral Zone"
    }

    fun bullishCross(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "تقاطع صاعد (Golden Cross 🟢)"
        AppLanguage.ENGLISH -> "Golden Crossover 🟢"
    }

    fun bearishCross(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "تقاطع هابط (Death Cross 🔴)"
        AppLanguage.ENGLISH -> "Death Crossover 🔴"
    }

    fun bullishTrend(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "اتجاه صاعد قوي"
        AppLanguage.ENGLISH -> "Strong Bullish Trend"
    }

    fun bearishTrend(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "اتجاه هابط قوي"
        AppLanguage.ENGLISH -> "Strong Bearish Trend"
    }

    fun touchingLowerBand(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "ملامسة الحد السفلي (ارتداد صاعد 🟢)"
        AppLanguage.ENGLISH -> "Lower Band Bounce (CALL 🟢)"
    }

    fun touchingUpperBand(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "ملامسة الحد العلوي (ارتداد هابط 🔴)"
        AppLanguage.ENGLISH -> "Upper Band Rejection (PUT 🔴)"
    }

    fun middleBandZone(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "داخل القناة السعرية"
        AppLanguage.ENGLISH -> "Inside Bands Channel"
    }

    // Active Trade Tracking Strings
    fun activeTradesHeader(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "الصفقات المفتوحة والتتبع المباشر"
        AppLanguage.ENGLISH -> "Live Active Trades & Direct Outcome Tracking"
    }

    fun noActiveTrades(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "لا توجد صفقات جارية الآن. انقر على تنفيذ أو شغل البوت لبدء صفقة."
        AppLanguage.ENGLISH -> "No active trades in progress. Click Execute or start the bot to place a trade."
    }

    fun entryPriceLabel(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "سعر الدخول:"
        AppLanguage.ENGLISH -> "Entry Price:"
    }

    fun currentPriceLabel(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "السعر الحالي:"
        AppLanguage.ENGLISH -> "Current Price:"
    }

    fun pointsDiffLabel(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "الفارق السعري:"
        AppLanguage.ENGLISH -> "Price Diff:"
    }

    fun remainingTimer(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "الوقت المتبقي لإغلاق الصفقة:"
        AppLanguage.ENGLISH -> "Remaining Time to Close:"
    }

    fun currentlyWinning(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "🟢 صفقة رابحة حالياً (In The Money)"
        AppLanguage.ENGLISH -> "🟢 Currently Winning (In The Money)"
    }

    fun currentlyLosing(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "🔴 صفقة خاسرة حالياً (Out Of The Money)"
        AppLanguage.ENGLISH -> "🔴 Currently Losing (Out Of The Money)"
    }

    fun tradeWonNotification(lang: AppLanguage, profit: String) = when (lang) {
        AppLanguage.ARABIC -> "🎉 انتهت الصفقة بربح: +$profit$"
        AppLanguage.ENGLISH -> "🎉 Trade Won! Profit: +$$profit"
    }

    fun tradeLostNotification(lang: AppLanguage, loss: String) = when (lang) {
        AppLanguage.ARABIC -> "🔻 انتهت الصفقة بخسارة: -$loss$"
        AppLanguage.ENGLISH -> "🔻 Trade Lost: -$$loss"
    }

    fun tradeDirectionCall(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "صعود ↗️"
        AppLanguage.ENGLISH -> "CALL ↗️"
    }

    fun tradeDirectionPut(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "هبوط ↘️"
        AppLanguage.ENGLISH -> "PUT ↘️"
    }

    fun executeReason(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "سبب الدخول الفني:"
        AppLanguage.ENGLISH -> "Technical Entry Trigger:"
    }

    fun indicatorsPanelHud(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "لوحة المؤشرات الفنية المباشرة"
        AppLanguage.ENGLISH -> "Live Technical Indicators HUD"
    }

    fun performanceTab(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "سجل الأداء"
        AppLanguage.ENGLISH -> "Performance Log"
    }

    fun botSettingsTab(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "إعدادات البوت"
        AppLanguage.ENGLISH -> "Bot Settings"
    }

    fun standardIndicatorsHeader(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "المؤشرات الفنية الكلاسيكية"
        AppLanguage.ENGLISH -> "Standard Technical Indicators"
    }

    fun currencyPairs(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "أزواج العملات المتاحة"
        AppLanguage.ENGLISH -> "Currency Pairs"
    }

    // Rocket Matix Onboarding & Auth
    fun searchUpdates(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "جاري البحث عن تحديثات..."
        AppLanguage.ENGLISH -> "Checking for updates..."
    }

    fun connectingServer(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "جارِ الاتصال بالخادم..."
        AppLanguage.ENGLISH -> "Connecting to server..."
    }

    fun onboardingTitle1(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "أتمتة التداول الخاص بك على Pocket Option"
        AppLanguage.ENGLISH -> "Automate your trading on Pocket Option"
    }

    fun onboardingTitle2(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "قم بإعداد وتشغيل أول روبوت في أقل من 5 دقائق"
        AppLanguage.ENGLISH -> "Set up and run your first robot in under 5 minutes"
    }

    fun onboardingTitle3(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "لديك بالفعل حساب Pocket Option؟\nالدردشة عبر الإنترنت مع دعم فني 24/7"
        AppLanguage.ENGLISH -> "Already have a Pocket Option account?\nOnline chat with 24/7 technical support"
    }

    fun next(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "التالي"
        AppLanguage.ENGLISH -> "Next"
    }

    fun createNewAccount(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "إنشاء حساب جديد"
        AppLanguage.ENGLISH -> "Create New Account"
    }

    fun login(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "تسجيل الدخول"
        AppLanguage.ENGLISH -> "Login"
    }

    fun register(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "التسجيل"
        AppLanguage.ENGLISH -> "Register"
    }

    fun createPocketAccount(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "إنشاء حساب Pocket Option"
        AppLanguage.ENGLISH -> "Create Pocket Option Account"
    }

    fun email(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "البريد الإلكتروني"
        AppLanguage.ENGLISH -> "Email"
    }

    fun password(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "كلمة المرور"
        AppLanguage.ENGLISH -> "Password"
    }

    fun termsAgreement(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "By using this app, you agree to Rocket Matix الشروط والأحكام & سياسة الخصوصية"
        AppLanguage.ENGLISH -> "By using this app, you agree to Rocket Matix Terms & Privacy Policy"
    }

    fun openAccount(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "افتح حساب"
        AppLanguage.ENGLISH -> "Open Account"
    }

    fun skipToDemoBot(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "الدخول كحساب تجريبي (50,000$)"
        AppLanguage.ENGLISH -> "Enter as Demo Account ($50,000)"
    }

    // Bottom Navigation Tabs
    fun tabPlatform(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "المنصة"
        AppLanguage.ENGLISH -> "Platform"
    }

    fun tabRobot(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "روبوت"
        AppLanguage.ENGLISH -> "Robot"
    }

    fun tabSupport(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "الدعم"
        AppLanguage.ENGLISH -> "Support"
    }

    fun tabAccount(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "الحساب"
        AppLanguage.ENGLISH -> "Account"
    }

    // Robot Tab Options & Risk
    fun demoAccount(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "حساب تجريبي"
        AppLanguage.ENGLISH -> "Demo Account"
    }

    fun realAccount(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "حساب حقيقي"
        AppLanguage.ENGLISH -> "Real Account"
    }

    fun mainOptions(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "الخيارات الرئيسية"
        AppLanguage.ENGLISH -> "Main Options"
    }

    fun amount(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "المبلغ"
        AppLanguage.ENGLISH -> "Amount"
    }

    fun timeDuration(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "الوقت"
        AppLanguage.ENGLISH -> "Time"
    }

    fun tradingAssets(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "أصول التداول"
        AppLanguage.ENGLISH -> "Trading Assets"
    }

    fun technicalIndicator(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "المؤشر الفني"
        AppLanguage.ENGLISH -> "Technical Indicator"
    }

    fun riskManagement(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "إدارة الأخطار"
        AppLanguage.ENGLISH -> "Risk Management"
    }

    fun strategy(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "الاستراتيجية"
        AppLanguage.ENGLISH -> "Strategy"
    }

    fun takeProfitLimit(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "حد الربح"
        AppLanguage.ENGLISH -> "Take Profit Limit"
    }

    fun stopLossLimit(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "حد الخسارة"
        AppLanguage.ENGLISH -> "Stop Loss Limit"
    }

    fun botWillStopAt(lang: AppLanguage, targetBalance: String) = when (lang) {
        AppLanguage.ARABIC -> "سيتوقف الروبوت عند رصيد $targetBalance US$"
        AppLanguage.ENGLISH -> "The robot will stop at $targetBalance US$ balance"
    }

    fun startTrading(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "ابدأ التداول"
        AppLanguage.ENGLISH -> "Start Trading"
    }

    fun stopRobot(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "أوقف الروبوت"
        AppLanguage.ENGLISH -> "Stop Robot"
    }

    fun showChart(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "إظهار الرسم البياني"
        AppLanguage.ENGLISH -> "Show Chart"
    }

    fun currentInvestment(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "الاستثمار الحالي"
        AppLanguage.ENGLISH -> "Current Investment"
    }

    fun searchingSignals(lang: AppLanguage, asset: String) = when (lang) {
        AppLanguage.ARABIC -> "البحث عن إشارات $asset"
        AppLanguage.ENGLISH -> "Searching for signals $asset"
    }

    fun activeIndicatorLabel(lang: AppLanguage, indicator: String) = when (lang) {
        AppLanguage.ARABIC -> "المؤشر: $indicator"
        AppLanguage.ENGLISH -> "Indicator: $indicator"
    }

    fun tryAiIndicatorTitle(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "جرب مؤشر الذكاء الاصطناعي"
        AppLanguage.ENGLISH -> "Try AI Indicator"
    }

    fun tryAiIndicatorDesc(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "قم بزيادة دقة التداول الآلي الخاصة بك بنسبة تصل إلى 63%"
        AppLanguage.ENGLISH -> "Boost your auto-trading accuracy by up to 63%"
    }

    fun supportFooterNotice(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "لطرح أسئلة حول تحسين البرنامج، يمكن التواصل عبر البريد الإلكتروني:\nsupport@rocketmatix.com"
        AppLanguage.ENGLISH -> "For questions about optimizing the bot, contact us via email:\nsupport@rocketmatix.com"
    }

    // Asset selection
    fun autoSelectAsset(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "التحديد التلقائي"
        AppLanguage.ENGLISH -> "Auto-Select"
    }

    fun autoSelectAssetDesc(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "اختيار الأصل ذو أعلى نسبة ربح"
        AppLanguage.ENGLISH -> "Select asset with highest payout"
    }

    // Account Tab
    fun chargeAccount(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "اشحن حسابك"
        AppLanguage.ENGLISH -> "Top-up your Account"
    }

    fun accountActivation(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "تفعيل الحساب"
        AppLanguage.ENGLISH -> "Account Activation"
    }

    fun balance(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "الرصيد"
        AppLanguage.ENGLISH -> "Balance"
    }

    fun deposit(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "الإيداع"
        AppLanguage.ENGLISH -> "Deposit"
    }

    fun withdraw(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "سحب"
        AppLanguage.ENGLISH -> "Withdraw"
    }

    fun profile(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "الملف الشخصي"
        AppLanguage.ENGLISH -> "Profile"
    }

    fun settings(lang: AppLanguage) = when (lang) {
        AppLanguage.ARABIC -> "الإعدادات"
        AppLanguage.ENGLISH -> "Settings"
    }
}

