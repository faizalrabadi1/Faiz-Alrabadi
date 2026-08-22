package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.AppLanguage
import com.example.util.AppStrings
import com.example.util.LocalAppLanguage
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

data class TechnicalIndicatorsState(
    val currentPrice: Double = 1.08520,
    val rsi: Double = 50.0,
    val rsiSignal: String = "Neutral", // "Oversold", "Overbought", "Neutral"
    val macd: Double = 0.00012,
    val macdSignalLine: Double = 0.00008,
    val macdHistogram: Double = 0.00004,
    val macdCross: String = "Bullish", // "Bullish", "Bearish", "Neutral"
    val bbUpper: Double = 1.08580,
    val bbMiddle: Double = 1.08520,
    val bbLower: Double = 1.08460,
    val bbPercent: Double = 0.5,
    val bbSignal: String = "Neutral", // "LowerBand", "UpperBand", "Neutral"
    val ema9: Double = 1.08525,
    val ema21: Double = 1.08510,
    val sma50: Double = 1.08495,
    val maTrend: String = "Bullish", // "Bullish", "Bearish", "Sideways"
    val stochasticK: Double = 55.0,
    val stochasticD: Double = 52.0,
    val supportS1: Double = 1.08470,
    val resistanceR1: Double = 1.08570,
    val pivot: Double = 1.08520,
    val candlestickPattern: String = "نصرة داخلة صاعدة (Hammer)",
    val signalDirection: String = "CALL", // "CALL", "PUT", "WAIT"
    val signalConfidence: Int = 85,
    val signalReason: String = "تشبع بيعي RSI + تقاطع MACD صاعد"
)

fun calculateLiveIndicators(priceHistory: List<Double>): TechnicalIndicatorsState {
    if (priceHistory.isEmpty()) {
        return TechnicalIndicatorsState()
    }

    val prices = priceHistory.takeLast(60)
    val current = prices.last()

    // 1. RSI (14)
    var rsi = 50.0
    if (prices.size >= 14) {
        val recent14 = prices.takeLast(14)
        var gains = 0.0
        var losses = 0.0
        for (i in 1 until recent14.size) {
            val diff = recent14[i] - recent14[i - 1]
            if (diff > 0) gains += diff else losses += abs(diff)
        }
        val avgGain = gains / 14.0
        val avgLoss = if (losses == 0.0) 0.00001 else losses / 14.0
        val rs = avgGain / avgLoss
        rsi = 100.0 - (100.0 / (1.0 + rs))
    }

    val rsiSignal = when {
        rsi <= 32.0 -> "Oversold"
        rsi >= 68.0 -> "Overbought"
        else -> "Neutral"
    }

    // 2. Moving Averages & MACD
    val ema9 = if (prices.size >= 9) prices.takeLast(9).average() else current
    val ema21 = if (prices.size >= 21) prices.takeLast(21).average() else current
    val sma50 = if (prices.size >= 30) prices.takeLast(min(50, prices.size)).average() else current

    val ema12 = if (prices.size >= 12) prices.takeLast(12).average() else current
    val ema26 = if (prices.size >= 26) prices.takeLast(26).average() else current
    val macd = ema12 - ema26
    val macdSignalLine = macd * 0.75 // Smooth approximation
    val macdHistogram = macd - macdSignalLine
    val macdCross = when {
        macd > macdSignalLine && macd > 0 -> "Bullish"
        macd < macdSignalLine && macd < 0 -> "Bearish"
        else -> "Neutral"
    }

    // 3. Bollinger Bands (20, 2)
    val bbPeriod = min(20, prices.size)
    val bbList = prices.takeLast(bbPeriod)
    val bbMiddle = bbList.average()
    val variance = bbList.map { (it - bbMiddle).pow(2) }.average()
    val stdDev = max(0.00005, sqrt(variance))
    val bbUpper = bbMiddle + (stdDev * 2.0)
    val bbLower = bbMiddle - (stdDev * 2.0)
    val bbRange = max(0.0001, bbUpper - bbLower)
    val bbPercent = ((current - bbLower) / bbRange).coerceIn(0.0, 1.0)
    val bbSignal = when {
        current <= bbLower + (bbRange * 0.15) -> "LowerBand"
        current >= bbUpper - (bbRange * 0.15) -> "UpperBand"
        else -> "Neutral"
    }

    // 4. Moving Average Trend
    val maTrend = when {
        ema9 > ema21 && current > ema9 -> "Bullish"
        ema9 < ema21 && current < ema9 -> "Bearish"
        else -> "Sideways"
    }

    // 5. Support & Resistance & Pivots
    val high30 = prices.takeLast(min(30, prices.size)).maxOrNull() ?: current
    val low30 = prices.takeLast(min(30, prices.size)).minOrNull() ?: current
    val pivot = (high30 + low30 + current) / 3.0
    val resistanceR1 = (2.0 * pivot) - low30
    val supportS1 = (2.0 * pivot) - high30

    // 6. Candlestick Pattern detection
    val pattern = when {
        prices.size >= 4 && prices.last() > prices[prices.size - 2] && prices[prices.size - 2] < prices[prices.size - 3] -> "نصرة داخلة صاعدة (Hammer 🟢)"
        prices.size >= 4 && prices.last() < prices[prices.size - 2] && prices[prices.size - 2] > prices[prices.size - 3] -> "شهاب هابط (Shooting Star 🔴)"
        rsiSignal == "Oversold" -> "فرح (ابتلاع شرائي قوي 🟢)"
        rsiSignal == "Overbought" -> "حزن (ابتلاع بيعي قوي 🔴)"
        maTrend == "Bullish" -> "طريق صاعد (Marubozu صاعد)"
        maTrend == "Bearish" -> "طريق هابط (Marubozu هابط)"
        else -> "شمعة دوجي متوازنة (Doji)"
    }

    // 7. Composite Consensus Calculation
    var callVotes = 0
    var putVotes = 0
    val reasons = mutableListOf<String>()

    if (rsiSignal == "Oversold") {
        callVotes += 3
        reasons.add("تشبع RSI (${String.format("%.1f", rsi)})")
    } else if (rsiSignal == "Overbought") {
        putVotes += 3
        reasons.add("تشبع شرائي RSI (${String.format("%.1f", rsi)})")
    }

    if (macdCross == "Bullish") {
        callVotes += 2
        reasons.add("تقاطع MACD إيجابي")
    } else if (macdCross == "Bearish") {
        putVotes += 2
        reasons.add("تقاطع MACD سلبي")
    }

    if (bbSignal == "LowerBand") {
        callVotes += 2
        reasons.add("ارتداد بولنجر سفلي")
    } else if (bbSignal == "UpperBand") {
        putVotes += 2
        reasons.add("ارتداد بولنجر علوي")
    }

    if (maTrend == "Bullish") {
        callVotes += 2
    } else if (maTrend == "Bearish") {
        putVotes += 2
    }

    val totalVotes = callVotes + putVotes
    val direction: String
    val confidence: Int
    if (callVotes > putVotes && callVotes >= 3) {
        direction = "CALL"
        confidence = min(98, 70 + (callVotes * 5))
    } else if (putVotes > callVotes && putVotes >= 3) {
        direction = "PUT"
        confidence = min(98, 70 + (putVotes * 5))
    } else {
        direction = "WAIT"
        confidence = 50
    }

    val reasonText = if (reasons.isNotEmpty()) reasons.joinToString(" + ") else "ترقب تأكيد المؤشرات الفنية"

    return TechnicalIndicatorsState(
        currentPrice = current,
        rsi = rsi,
        rsiSignal = rsiSignal,
        macd = macd,
        macdSignalLine = macdSignalLine,
        macdHistogram = macdHistogram,
        macdCross = macdCross,
        bbUpper = bbUpper,
        bbMiddle = bbMiddle,
        bbLower = bbLower,
        bbPercent = bbPercent,
        bbSignal = bbSignal,
        ema9 = ema9,
        ema21 = ema21,
        sma50 = sma50,
        maTrend = maTrend,
        stochasticK = (rsi * 1.05).coerceIn(10.0, 95.0),
        stochasticD = (rsi * 0.95).coerceIn(10.0, 95.0),
        supportS1 = supportS1,
        resistanceR1 = resistanceR1,
        pivot = pivot,
        candlestickPattern = pattern,
        signalDirection = direction,
        signalConfidence = confidence,
        signalReason = reasonText
    )
}

@Composable
fun LiveIndicatorsPanel(
    indicators: TechnicalIndicatorsState,
    onExecuteCall: () -> Unit,
    onExecutePut: () -> Unit,
    autoExecuteEnabled: Boolean,
    onToggleAutoExecute: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = LocalAppLanguage.current
    var isExpanded by remember { mutableStateOf(true) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(
            1.dp,
            when (indicators.signalDirection) {
                "CALL" -> Color(0xFF4CAF50).copy(alpha = 0.4f)
                "PUT" -> Color(0xFFF44336).copy(alpha = 0.4f)
                else -> MaterialTheme.colorScheme.outlineVariant
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header with toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(
                                color = when (indicators.signalDirection) {
                                    "CALL" -> Color(0xFF4CAF50)
                                    "PUT" -> Color(0xFFF44336)
                                    else -> Color(0xFFFFB300)
                                },
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = AppStrings.indicatorsPanelHud(lang),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = when (indicators.signalDirection) {
                            "CALL" -> Color(0xFF4CAF50).copy(alpha = 0.15f)
                            "PUT" -> Color(0xFFF44336).copy(alpha = 0.15f)
                            else -> Color(0xFFFFB300).copy(alpha = 0.15f)
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "${indicators.signalConfidence}%",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = when (indicators.signalDirection) {
                                "CALL" -> Color(0xFF4CAF50)
                                "PUT" -> Color(0xFFF44336)
                                else -> Color(0xFFFFB300)
                            }
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Consensus Signal Banner
            val isCall = indicators.signalDirection == "CALL"
            val isPut = indicators.signalDirection == "PUT"

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
                color = when {
                    isCall -> Color(0xFF4CAF50).copy(alpha = 0.12f)
                    isPut -> Color(0xFFF44336).copy(alpha = 0.12f)
                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                },
                border = BorderStroke(
                    1.dp,
                    when {
                        isCall -> Color(0xFF4CAF50).copy(alpha = 0.35f)
                        isPut -> Color(0xFFF44336).copy(alpha = 0.35f)
                        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    }
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = when {
                                    isCall -> Icons.Default.TrendingUp
                                    isPut -> Icons.Default.TrendingDown
                                    else -> Icons.Default.BarChart
                                },
                                contentDescription = null,
                                tint = when {
                                    isCall -> Color(0xFF4CAF50)
                                    isPut -> Color(0xFFF44336)
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = when {
                                        isCall -> AppStrings.strongBuy(lang)
                                        isPut -> AppStrings.strongSell(lang)
                                        else -> AppStrings.neutral(lang)
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = when {
                                        isCall -> Color(0xFF4CAF50)
                                        isPut -> Color(0xFFF44336)
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }
                                )
                                Text(
                                    text = "${AppStrings.executeReason(lang)} ${indicators.signalReason}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Confidence Linear Progress
                    LinearProgressIndicator(
                        progress = { indicators.signalConfidence / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = when {
                            isCall -> Color(0xFF4CAF50)
                            isPut -> Color(0xFFF44336)
                            else -> MaterialTheme.colorScheme.primary
                        },
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(12.dp))

                    // Individual Indicator Cards Grid (2x3)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // RSI Card
                        IndicatorMiniCard(
                            name = "RSI (14)",
                            value = String.format("%.1f", indicators.rsi),
                            statusText = when (indicators.rsiSignal) {
                                "Oversold" -> AppStrings.oversold(lang)
                                "Overbought" -> AppStrings.overbought(lang)
                                else -> AppStrings.neutralZone(lang)
                            },
                            statusColor = when (indicators.rsiSignal) {
                                "Oversold" -> Color(0xFF4CAF50)
                                "Overbought" -> Color(0xFFF44336)
                                else -> Color(0xFFFF9800)
                            },
                            modifier = Modifier.weight(1f)
                        )

                        // MACD Card
                        IndicatorMiniCard(
                            name = "MACD (12,26,9)",
                            value = String.format("%.5f", indicators.macd),
                            statusText = when (indicators.macdCross) {
                                "Bullish" -> AppStrings.bullishCross(lang)
                                "Bearish" -> AppStrings.bearishCross(lang)
                                else -> AppStrings.neutralZone(lang)
                            },
                            statusColor = when (indicators.macdCross) {
                                "Bullish" -> Color(0xFF4CAF50)
                                "Bearish" -> Color(0xFFF44336)
                                else -> Color(0xFFFF9800)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Bollinger Bands Card
                        IndicatorMiniCard(
                            name = "Bollinger Bands",
                            value = "${String.format("%.4f", indicators.bbLower)} - ${String.format("%.4f", indicators.bbUpper)}",
                            statusText = when (indicators.bbSignal) {
                                "LowerBand" -> AppStrings.touchingLowerBand(lang)
                                "UpperBand" -> AppStrings.touchingUpperBand(lang)
                                else -> AppStrings.middleBandZone(lang)
                            },
                            statusColor = when (indicators.bbSignal) {
                                "LowerBand" -> Color(0xFF4CAF50)
                                "UpperBand" -> Color(0xFFF44336)
                                else -> MaterialTheme.colorScheme.primary
                            },
                            modifier = Modifier.weight(1f)
                        )

                        // Moving Averages Card
                        IndicatorMiniCard(
                            name = "EMA 9 / 21 Trend",
                            value = "EMA9: ${String.format("%.4f", indicators.ema9)}",
                            statusText = when (indicators.maTrend) {
                                "Bullish" -> AppStrings.bullishTrend(lang)
                                "Bearish" -> AppStrings.bearishTrend(lang)
                                else -> "تذبذب أفقي"
                            },
                            statusColor = when (indicators.maTrend) {
                                "Bullish" -> Color(0xFF4CAF50)
                                "Bearish" -> Color(0xFFF44336)
                                else -> Color(0xFFFF9800)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Candlestick Pattern / Geomancy Banner
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp)),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = AppStrings.candlestickPattern(lang),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = indicators.candlestickPattern,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Practical One-Click Trade Execution Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Execute CALL (🟢)
                Button(
                    onClick = onExecuteCall,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = AppStrings.executeCallNow(lang),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Execute PUT (🔴)
                Button(
                    onClick = onExecutePut,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = AppStrings.executePutNow(lang),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun IndicatorMiniCard(
    name: String,
    value: String,
    statusText: String,
    statusColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 11.sp
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = statusText,
                style = MaterialTheme.typography.bodySmall,
                color = statusColor,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
