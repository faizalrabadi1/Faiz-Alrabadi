package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.AppLanguage
import com.example.util.AppStrings
import kotlinx.coroutines.launch

data class TradingAsset(
    val name: String,
    val payoutPercent: Int = 92,
    val category: String = "OTC",
    val isOtc: Boolean = true
)

data class TechnicalIndicatorItem(
    val id: String,
    val titleAr: String,
    val titleEn: String,
    val descAr: String,
    val descEn: String,
    val icon: ImageVector,
    val isAi: Boolean = false
)

data class TradingStrategyItem(
    val id: String,
    val titleAr: String,
    val titleEn: String,
    val riskLevelAr: String,
    val riskLevelEn: String,
    val minBalance: Int,
    val riskColor: Color,
    val descAr: String,
    val descEn: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RobotTabScreen(
    currentLanguage: AppLanguage,
    isBotRunning: Boolean,
    onStartBot: () -> Unit,
    onStopBot: () -> Unit,
    onShowChart: () -> Unit,
    activeTradesCount: Int,
    accountBalance: Double,
    isDemoAccount: Boolean,
    onToggleAccountType: (Boolean) -> Unit
) {
    var selectedAmount by remember { mutableIntStateOf(10) }
    var selectedDuration by remember { mutableStateOf("1 دقيقة") }
    var selectedAsset by remember { mutableStateOf(TradingAsset("FACEBOOK INC OTC", 92)) }
    var autoSelectAsset by remember { mutableStateOf(false) }

    var selectedIndicator by remember {
        mutableStateOf(
            TechnicalIndicatorItem(
                id = "macd",
                titleAr = "MACD",
                titleEn = "MACD",
                descAr = "تداول الاتجاه مع المتوسط المتحرك",
                descEn = "Trend trading with moving averages",
                icon = Icons.Default.ShowChart
            )
        )
    }

    var selectedStrategy by remember {
        mutableStateOf(
            TradingStrategyItem(
                id = "optimal",
                titleAr = "الأمثل",
                titleEn = "Optimal",
                riskLevelAr = "مخاطرة متوسطة",
                riskLevelEn = "Medium Risk",
                minBalance = 200,
                riskColor = Color(0xFF6C5CE7),
                descAr = "توازن مثالي بين استمرارية الصفقات وإدارة رأس المال الذكية.",
                descEn = "Optimal balance between trade frequency and smart risk management."
            )
        )
    }

    var takeProfitEnabled by remember { mutableStateOf(true) }
    var takeProfitTarget by remember { mutableIntStateOf(20) }
    var stopLossEnabled by remember { mutableStateOf(false) }
    var stopLossTarget by remember { mutableIntStateOf(50) }

    // Bottom Sheets State
    var showAmountDialog by remember { mutableStateOf(false) }
    var showTimeSheet by remember { mutableStateOf(false) }
    var showAssetSheet by remember { mutableStateOf(false) }
    var showIndicatorSheet by remember { mutableStateOf(false) }
    var showStrategySheet by remember { mutableStateOf(false) }

    val allAssets = remember {
        listOf(
            TradingAsset("FACEBOOK INC OTC", 92),
            TradingAsset("Johnson & Johnson OTC", 92),
            TradingAsset("Pfizer INC OTC", 92),
            TradingAsset("AED/CNY OTC", 92),
            TradingAsset("AUD/CAD OTC", 92),
            TradingAsset("AUD/NZD OTC", 92),
            TradingAsset("Avalanche OTC", 92),
            TradingAsset("Bitcoin ETF OTC", 92),
            TradingAsset("BNB OTC", 92),
            TradingAsset("Bitcoin OTC", 92),
            TradingAsset("CAD/CHF OTC", 92),
            TradingAsset("EUR/USD OTC", 92),
            TradingAsset("GBP/USD OTC", 92),
            TradingAsset("USD/JPY OTC", 92)
        )
    }

    val allIndicators = remember {
        listOf(
            TechnicalIndicatorItem(
                id = "ai",
                titleAr = "مؤشر الذكاء الاصطناعي",
                titleEn = "AI Smart Indicator",
                descAr = "التحليل التلقائي لتداولاتك السابقة ومعايير السوق",
                descEn = "Auto-analysis of market variables and previous trades",
                icon = Icons.Default.Psychology,
                isAi = true
            ),
            TechnicalIndicatorItem(
                id = "macd",
                titleAr = "MACD",
                titleEn = "MACD",
                descAr = "تداول الاتجاه مع المتوسط المتحرك",
                descEn = "Trend trading with moving averages",
                icon = Icons.Default.ShowChart
            ),
            TechnicalIndicatorItem(
                id = "stochastic",
                titleAr = "ستوكاستيك (Stochastic)",
                titleEn = "Stochastic",
                descAr = "الفرق بين القمم والقيعان في الصفقة الحالية",
                descEn = "Oscillator detecting overbought and oversold extremes",
                icon = Icons.Default.AutoGraph
            ),
            TechnicalIndicatorItem(
                id = "bollinger",
                titleAr = "Bollinger Bands",
                titleEn = "Bollinger Bands",
                descAr = "يحسب بناءً على الانحراف المعياري لمتوسط متحرك",
                descEn = "Calculates volatility envelopes around a moving average",
                icon = Icons.Default.GraphicEq
            ),
            TechnicalIndicatorItem(
                id = "rsi",
                titleAr = "RSI",
                titleEn = "RSI Relative Strength",
                descAr = "مؤشر حركة السعر والسرعة",
                descEn = "Momentum oscillator measuring speed and price changes",
                icon = Icons.Default.Speed
            ),
            TechnicalIndicatorItem(
                id = "cci",
                titleAr = "CCI",
                titleEn = "CCI Channel Index",
                descAr = "يقيس انحراف السعر عن المتوسط",
                descEn = "Measures variation from statistical mean",
                icon = Icons.Default.TrendingUp
            ),
            TechnicalIndicatorItem(
                id = "close_price",
                titleAr = "سعر الإغلاق",
                titleEn = "Close Price Action",
                descAr = "مؤشر الشموع لتتبع الاتجاهات",
                descEn = "Japanese candlestick price action momentum",
                icon = Icons.Default.ShowChart
            )
        )
    }

    val allStrategies = remember {
        listOf(
            TradingStrategyItem(
                id = "fixed",
                titleAr = "مبلغ ثابت",
                titleEn = "Fixed Amount",
                riskLevelAr = "مخاطرة منخفضة",
                riskLevelEn = "Low Risk",
                minBalance = 50,
                riskColor = Color(0xFF00E676),
                descAr = "دخول بمبلغ ثابت لكل صفقة دون مضاعفات لحماية رأس المال.",
                descEn = "Fixed amount per trade without multipliers for capital protection."
            ),
            TradingStrategyItem(
                id = "conservative",
                titleAr = "محافظ",
                titleEn = "Conservative",
                riskLevelAr = "مخاطرة منخفضة",
                riskLevelEn = "Low Risk",
                minBalance = 100,
                riskColor = Color(0xFF00E676),
                descAr = "مضاعفات مرنة بنسب مدروسة لتغطية الصفقات الخاسرة بأقل مخاطرة.",
                descEn = "Gentle step recovery to cover losses with minimal drawdown."
            ),
            TradingStrategyItem(
                id = "optimal",
                titleAr = "الأمثل",
                titleEn = "Optimal",
                riskLevelAr = "مخاطرة متوسطة",
                riskLevelEn = "Medium Risk",
                minBalance = 200,
                riskColor = Color(0xFF6C5CE7),
                descAr = "الاستراتيجية الأكثر توازناً ونسبة نجاح، موصى بها من قبل المتداولين.",
                descEn = "The most balanced approach maximizing risk-to-reward ratio."
            ),
            TradingStrategyItem(
                id = "aggressive",
                titleAr = "عدواني",
                titleEn = "Aggressive",
                riskLevelAr = "مخاطرة مرتفعة",
                riskLevelEn = "High Risk",
                minBalance = 500,
                riskColor = Color(0xFFFF5252),
                descAr = "وفقاً لهذه الاستراتيجية، سيسحب الروبوت الصفقات بحيث تتلقى في المتوسط كل دقيقة عائد من أصول التداول الأولية.",
                descEn = "Rapid martingale execution every minute for aggressive compounding."
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF101221))
    ) {
        if (!isBotRunning) {
            // ==================== BOT SETUP VIEW ====================
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Account Selector Header
                item {
                    Column {
                        Text(
                            text = if (currentLanguage == AppLanguage.ARABIC) "اختيار حساب" else "Select Account",
                            style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(bottom = 10.dp)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF1B1E32))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Demo Account Option
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onToggleAccountType(true) },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isDemoAccount) Color(0xFF282D4A) else Color.Transparent
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = AppStrings.demoAccount(currentLanguage),
                                        fontSize = 13.sp,
                                        color = if (isDemoAccount) Color.White else Color(0xFF8E9AA8),
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "50,000.000 US$",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDemoAccount) Color(0xFF00D2FF) else Color(0xFF8E9AA8)
                                    )
                                }
                            }

                            // Real Account Option
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onToggleAccountType(false) },
                                shape = RoundedCornerShape(12.dp),
                                color = if (!isDemoAccount) Color(0xFF282D4A) else Color.Transparent
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = AppStrings.realAccount(currentLanguage),
                                        fontSize = 13.sp,
                                        color = if (!isDemoAccount) Color.White else Color(0xFF8E9AA8),
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "0.000 US$",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (!isDemoAccount) Color(0xFF00E676) else Color(0xFF8E9AA8)
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. Main Options Card (الخيارات الرئيسية)
                item {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Text(
                                text = AppStrings.mainOptions(currentLanguage),
                                style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.HelpOutline,
                                contentDescription = null,
                                tint = Color(0xFF8E9AA8),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1E32)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                // Row: Amount & Time
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Amount Selector
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { showAmountDialog = true },
                                        shape = RoundedCornerShape(14.dp),
                                        color = Color(0xFF242944)
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Text(
                                                text = AppStrings.amount(currentLanguage),
                                                fontSize = 12.sp,
                                                color = Color(0xFF8E9AA8)
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "$selectedAmount US$",
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }

                                    // Time Duration Selector
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { showTimeSheet = true },
                                        shape = RoundedCornerShape(14.dp),
                                        color = Color(0xFF242944)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(14.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = AppStrings.timeDuration(currentLanguage),
                                                    fontSize = 12.sp,
                                                    color = Color(0xFF8E9AA8)
                                                )
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(
                                                    text = selectedDuration,
                                                    fontSize = 17.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                            Icon(
                                                imageVector = Icons.Default.KeyboardArrowRight,
                                                contentDescription = null,
                                                tint = Color(0xFF8E9AA8)
                                            )
                                        }
                                    }
                                }

                                // Trading Asset Selector Row
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showAssetSheet = true },
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color(0xFF242944)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(38.dp)
                                                    .background(Color(0xFF1877F2), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("f", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(
                                                    text = AppStrings.tradingAssets(currentLanguage),
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF8E9AA8)
                                                )
                                                Text(
                                                    text = "${selectedAsset.payoutPercent}% ${selectedAsset.name}",
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF00D2FF)
                                                )
                                            }
                                        }
                                        Icon(
                                            imageVector = Icons.Default.KeyboardArrowRight,
                                            contentDescription = null,
                                            tint = Color(0xFF8E9AA8)
                                        )
                                    }
                                }

                                // Technical Indicator Selector Row
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showIndicatorSheet = true },
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color(0xFF242944)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(38.dp)
                                                    .background(Color(0xFF373E68), RoundedCornerShape(10.dp)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = selectedIndicator.icon,
                                                    contentDescription = null,
                                                    tint = Color(0xFF00E676),
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(
                                                    text = AppStrings.technicalIndicator(currentLanguage),
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF8E9AA8)
                                                )
                                                Text(
                                                    text = if (currentLanguage == AppLanguage.ARABIC) selectedIndicator.titleAr else selectedIndicator.titleEn,
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                                Text(
                                                    text = if (currentLanguage == AppLanguage.ARABIC) selectedIndicator.descAr else selectedIndicator.descEn,
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF8E9AA8),
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                        Icon(
                                            imageVector = Icons.Default.KeyboardArrowRight,
                                            contentDescription = null,
                                            tint = Color(0xFF8E9AA8)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. Risk Management Card (إدارة الأخطار)
                item {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Text(
                                text = AppStrings.riskManagement(currentLanguage),
                                style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.HelpOutline,
                                contentDescription = null,
                                tint = Color(0xFF8E9AA8),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1E32)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                // Strategy Row
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showStrategySheet = true },
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color(0xFF242944)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(38.dp)
                                                    .background(Color(0xFF343B63), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Security,
                                                    contentDescription = null,
                                                    tint = selectedStrategy.riskColor,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(
                                                    text = AppStrings.strategy(currentLanguage),
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF8E9AA8)
                                                )
                                                Text(
                                                    text = if (currentLanguage == AppLanguage.ARABIC) selectedStrategy.titleAr else selectedStrategy.titleEn,
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                                Text(
                                                    text = if (currentLanguage == AppLanguage.ARABIC) selectedStrategy.riskLevelAr else selectedStrategy.riskLevelEn,
                                                    fontSize = 11.sp,
                                                    color = selectedStrategy.riskColor
                                                )
                                            }
                                        }
                                        Icon(
                                            imageVector = Icons.Default.KeyboardArrowRight,
                                            contentDescription = null,
                                            tint = Color(0xFF8E9AA8)
                                        )
                                    }
                                }

                                // Take Profit Row
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Color(0xFF242944))
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = AppStrings.takeProfitLimit(currentLanguage),
                                            fontSize = 12.sp,
                                            color = Color(0xFF8E9AA8)
                                        )
                                        Text(
                                            text = "$takeProfitTarget US$",
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF00E676)
                                        )
                                    }
                                    Switch(
                                        checked = takeProfitEnabled,
                                        onCheckedChange = { takeProfitEnabled = it },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = Color(0xFF6C5CE7)
                                        )
                                    )
                                }

                                if (takeProfitEnabled) {
                                    val targetBalance = (if (isDemoAccount) 50000 else 0) + takeProfitTarget
                                    Text(
                                        text = AppStrings.botWillStopAt(currentLanguage, targetBalance.toString()),
                                        fontSize = 12.sp,
                                        color = Color(0xFF8E9AA8),
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. Big Start Button ("ابدأ التداول")
                item {
                    Button(
                        onClick = onStartBot,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6C5CE7)
                        )
                    ) {
                        Icon(Icons.Default.Bolt, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = AppStrings.startTrading(currentLanguage),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        } else {
            // ==================== BOT RUNNING VIEW ====================
            val infiniteTransition = rememberInfiniteTransition(label = "pulse_radar")
            val pulseScale by infiniteTransition.animateFloat(
                initialValue = 0.95f,
                targetValue = 1.08f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "scale"
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Balance ("الاستثمار الحالي")
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1E32)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = AppStrings.currentInvestment(currentLanguage),
                                fontSize = 14.sp,
                                color = Color(0xFF8E9AA8)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "49,990.000 US$",
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "الربح: 0.000 US$",
                                fontSize = 13.sp,
                                color = Color(0xFF00E676),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Live Pulse Radar Card ("البحث عن إشارات...")
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1E32)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = AppStrings.searchingSignals(currentLanguage, selectedAsset.name),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = AppStrings.activeIndicatorLabel(currentLanguage, selectedIndicator.titleAr),
                                    fontSize = 13.sp,
                                    color = Color(0xFF00D2FF)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .scale(pulseScale)
                                    .background(Color(0xFF6C5CE7).copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GraphicEq,
                                    contentDescription = "Radar pulse",
                                    tint = Color(0xFF6C5CE7),
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                }

                // AI Upsell Promo Card ("جرب مؤشر الذكاء الاصطناعي")
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF222744)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF6C5CE7).copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .background(
                                        brush = Brush.linearGradient(
                                            listOf(Color(0xFF00D2FF), Color(0xFF6C5CE7))
                                        ),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Psychology,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = AppStrings.tryAiIndicatorTitle(currentLanguage),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = AppStrings.tryAiIndicatorDesc(currentLanguage),
                                    fontSize = 12.sp,
                                    color = Color(0xFFC0C7D6),
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }

                // Bot Config Summary Grid (المبلغ / الاستراتيجية / حد الربح)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF1B1E32)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(AppStrings.amount(currentLanguage), fontSize = 11.sp, color = Color(0xFF8E9AA8))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("$selectedAmount US$", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF1B1E32)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(AppStrings.strategy(currentLanguage), fontSize = 11.sp, color = Color(0xFF8E9AA8))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(selectedStrategy.titleAr, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6C5CE7))
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF1B1E32)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(AppStrings.takeProfitLimit(currentLanguage), fontSize = 11.sp, color = Color(0xFF8E9AA8))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("$takeProfitTarget US$", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00E676))
                            }
                        }
                    }
                }

                // Action Buttons: "أوقف الروبوت" & "إظهار الرسم البياني"
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = onStopBot,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2C1E2A)
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFF5252))
                        ) {
                            Text(
                                text = AppStrings.stopRobot(currentLanguage),
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF5252)
                            )
                        }

                        Button(
                            onClick = onShowChart,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF6C5CE7)
                            )
                        ) {
                            Icon(Icons.Default.ShowChart, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = AppStrings.showChart(currentLanguage),
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                // Support Footer Email
                item {
                    Text(
                        text = AppStrings.supportFooterNotice(currentLanguage),
                        fontSize = 12.sp,
                        color = Color(0xFF8E9AA8),
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    )
                }
            }
        }
    }

    // ==================== MODAL BOTTOM SHEETS ====================

    // Sheet 1: Duration Selector Sheet
    if (showTimeSheet) {
        val durations = listOf("5 ثوانٍ", "15 ثانية", "30 ثانية", "1 دقيقة", "2 دقيقة", "3 دقائق", "5 دقائق", "15 دقيقة")
        ModalBottomSheet(
            onDismissRequest = { showTimeSheet = false },
            containerColor = Color(0xFF191C30)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = if (currentLanguage == AppLanguage.ARABIC) "تحديد وقت الصفقة" else "Select Trade Duration",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(16.dp))
                durations.forEach { dur ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedDuration = dur
                                showTimeSheet = false
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = dur, fontSize = 16.sp, color = Color.White)
                        if (selectedDuration == dur) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF6C5CE7))
                        }
                    }
                    HorizontalDivider(color = Color(0xFF282D4A))
                }
            }
        }
    }

    // Sheet 2: Trading Assets Selector Sheet
    if (showAssetSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAssetSheet = false },
            containerColor = Color(0xFF191C30)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = AppStrings.tradingAssets(currentLanguage),
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(14.dp))

                // Auto Select Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF242944))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(AppStrings.autoSelectAsset(currentLanguage), fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                        Text(AppStrings.autoSelectAssetDesc(currentLanguage), color = Color(0xFF8E9AA8), fontSize = 11.sp)
                    }
                    Switch(
                        checked = autoSelectAsset,
                        onCheckedChange = { autoSelectAsset = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF6C5CE7)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.height(380.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(allAssets) { asset ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedAsset = asset
                                    showAssetSheet = false
                                },
                            shape = RoundedCornerShape(12.dp),
                            color = if (selectedAsset.name == asset.name) Color(0xFF2D3356) else Color(0xFF222744)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFF6C5CE7).copy(alpha = 0.2f),
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text("${asset.payoutPercent}%", color = Color(0xFF00D2FF), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(asset.name, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                                }
                                if (selectedAsset.name == asset.name) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF00E676))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Sheet 3: Technical Indicators Selector Sheet
    if (showIndicatorSheet) {
        ModalBottomSheet(
            onDismissRequest = { showIndicatorSheet = false },
            containerColor = Color(0xFF191C30)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = if (currentLanguage == AppLanguage.ARABIC) "اختيار المؤشر" else "Select Indicator",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.height(440.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(allIndicators) { ind ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedIndicator = ind
                                    showIndicatorSheet = false
                                },
                            shape = RoundedCornerShape(14.dp),
                            color = if (ind.isAi) Color(0xFF262C4E) else if (selectedIndicator.id == ind.id) Color(0xFF2D3356) else Color(0xFF222744),
                            border = if (ind.isAi) androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF6C5CE7)) else null
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = ind.icon,
                                        contentDescription = null,
                                        tint = if (ind.isAi) Color(0xFF00D2FF) else Color(0xFF00E676),
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = if (currentLanguage == AppLanguage.ARABIC) ind.titleAr else ind.titleEn,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 15.sp
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = if (currentLanguage == AppLanguage.ARABIC) ind.descAr else ind.descEn,
                                            color = Color(0xFF8E9AA8),
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                                if (selectedIndicator.id == ind.id) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF00E676))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Sheet 4: Strategy Selector Sheet
    if (showStrategySheet) {
        ModalBottomSheet(
            onDismissRequest = { showStrategySheet = false },
            containerColor = Color(0xFF191C30)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = if (currentLanguage == AppLanguage.ARABIC) "تحديد الاستراتيجية" else "Select Strategy",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.height(440.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(allStrategies) { strat ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedStrategy = strat
                                    showStrategySheet = false
                                },
                            shape = RoundedCornerShape(16.dp),
                            color = if (selectedStrategy.id == strat.id) Color(0xFF2E355B) else Color(0xFF222744),
                            border = if (selectedStrategy.id == strat.id) androidx.compose.foundation.BorderStroke(1.5.dp, strat.riskColor) else null
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Security,
                                            contentDescription = null,
                                            tint = strat.riskColor,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (currentLanguage == AppLanguage.ARABIC) strat.titleAr else strat.titleEn,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 17.sp
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = strat.riskColor.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = if (currentLanguage == AppLanguage.ARABIC) strat.riskLevelAr else strat.riskLevelEn,
                                            color = strat.riskColor,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = if (currentLanguage == AppLanguage.ARABIC) "الرصيد الموصى به: لا يقل عن ${strat.minBalance} US$" else "Recommended balance: min ${strat.minBalance} US$",
                                    color = Color(0xFF00D2FF),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = if (currentLanguage == AppLanguage.ARABIC) strat.descAr else strat.descEn,
                                    color = Color(0xFFC0C7D6),
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog: Quick Amount Selector
    if (showAmountDialog) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showAmountDialog = false }
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF191C30),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (currentLanguage == AppLanguage.ARABIC) "تحديد مبلغ الصفقة" else "Select Trade Amount",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 18.sp
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    val quickAmounts = listOf(1, 5, 10, 20, 50, 100)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        quickAmounts.take(3).forEach { amt ->
                            Button(
                                onClick = { selectedAmount = amt; showAmountDialog = false },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedAmount == amt) Color(0xFF6C5CE7) else Color(0xFF242944)
                                )
                            ) {
                                Text("$$amt")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        quickAmounts.drop(3).forEach { amt ->
                            Button(
                                onClick = { selectedAmount = amt; showAmountDialog = false },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedAmount == amt) Color(0xFF6C5CE7) else Color(0xFF242944)
                                )
                            ) {
                                Text("$$amt")
                            }
                        }
                    }
                }
            }
        }
    }
}
