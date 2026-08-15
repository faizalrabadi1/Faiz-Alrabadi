package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.TradeRecord
import kotlin.math.max
import kotlin.math.min

@Composable
fun TradePerformanceDashboard(
    tradeHistory: List<TradeRecord>,
    onSimulateTrade: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 1. Calculate Analytics
    val totalTrades = tradeHistory.size
    val wins = tradeHistory.count { it.result.lowercase() == "win" }
    val losses = totalTrades - wins
    val winRate = if (totalTrades > 0) (wins.toFloat() / totalTrades * 100).toInt() else 0

    var netProfit = 0f
    val cumulativeProfits = ArrayList<Float>()
    cumulativeProfits.add(0f) // Start point

    // Go from oldest to newest to compute trend
    tradeHistory.reversed().forEach { trade ->
        val amt = trade.amount.toFloatOrNull() ?: 1.0f
        if (trade.result.lowercase() == "win") {
            netProfit += amt * 0.82f // Standard payout ~82%
        } else {
            netProfit -= amt
        }
        cumulativeProfits.add(netProfit)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "تحليل الأداء وسجل الصفقات",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Simulate trade button for immediate testing
                TextButton(
                    onClick = onSimulateTrade,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.secondary),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("محاكاة صفقة", style = MaterialTheme.typography.labelMedium)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stats Cards Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Stat 1: Net Profit
                StatsBox(
                    label = "صافي الأرباح",
                    value = String.format("%.2f", netProfit) + "$",
                    color = if (netProfit >= 0) Color(0xFF4CAF50) else Color(0xFFF44336),
                    icon = if (netProfit >= 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                    modifier = Modifier.weight(1f)
                )

                // Stat 2: Win Rate
                StatsBox(
                    label = "نسبة النجاح",
                    value = "$winRate%",
                    color = when {
                        winRate >= 70 -> Color(0xFF4CAF50)
                        winRate >= 50 -> Color(0xFFFF9800)
                        else -> Color(0xFFF44336)
                    },
                    icon = Icons.Default.BarChart,
                    modifier = Modifier.weight(1f)
                )

                // Stat 3: Total trades count
                StatsBox(
                    label = "الصفقات",
                    value = "$totalTrades",
                    color = MaterialTheme.colorScheme.primary,
                    icon = Icons.Default.ArrowUpward,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Chart area
            Text(
                text = "منحنى الأرباح التراكمي:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (cumulativeProfits.size <= 1) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            RoundedCornerShape(8.dp)
                        )
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "لا توجد صفقات مسجلة بعد",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "قم بتشغيل البوت لبدء رسم المنحنى التفاعلي",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            } else {
                // Performance Line Chart using Canvas
                InteractivePerformanceLineChart(points = cumulativeProfits)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Transactions History List
            Text(
                text = "سجل الصفقات الأخيرة:",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(8.dp)),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                if (tradeHistory.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("بانتظار تنفيذ الصفقات الأولى...", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(tradeHistory) { trade ->
                            val isWin = trade.result.lowercase() == "win"
                            val amount = trade.amount.toFloatOrNull() ?: 1.0f
                            val profitResult = if (isWin) "+${String.format("%.2f", amount * 0.82f)}$" else "-${String.format("%.2f", amount)}$"
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = if (isWin) Color(0xFF4CAF50).copy(alpha = 0.08f) else Color(0xFFF44336).copy(alpha = 0.08f),
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .padding(vertical = 8.dp, horizontal = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isWin) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                        contentDescription = null,
                                        tint = if (isWin) Color(0xFF4CAF50) else Color(0xFFF44336),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = trade.time,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "المبلغ: ${trade.amount}$",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(end = 12.dp)
                                    )
                                    Text(
                                        text = profitResult,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isWin) Color(0xFF4CAF50) else Color(0xFFF44336)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatsBox(
    label: String,
    value: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
        color = color.copy(alpha = 0.05f)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                fontSize = 10.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun InteractivePerformanceLineChart(
    points: List<Float>,
    modifier: Modifier = Modifier
) {
    val lineColor = if (points.last() >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    
    val density = LocalDensity.current
    val labelTextSize = with(density) { 8.sp.toPx() }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
            .background(Color.Black.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
            .padding(top = 16.dp, bottom = 8.dp, start = 8.dp, end = 12.dp)
    ) {
        val width = size.width
        val height = size.height

        val maxVal = points.maxOrNull() ?: 0f
        val minVal = points.minOrNull() ?: 0f
        val diff = max(0.1f, maxVal - minVal)

        // Draw horizontal grid lines
        val gridLinesCount = 4
        for (i in 0..gridLinesCount) {
            val y = height * i / gridLinesCount
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1f
            )
            
            // Draw standard horizontal labels
            val value = maxVal - (diff * i / gridLinesCount)
            drawContext.canvas.nativeCanvas.drawText(
                String.format("%.2f", value) + "$",
                width - 45f,
                y - 4f,
                android.graphics.Paint().apply {
                    color = android.graphics.Color.GRAY
                    textSize = labelTextSize
                    textAlign = android.graphics.Paint.Align.RIGHT
                }
            )
        }

        // Compute point coordinates
        val pointsSize = points.size
        val xStep = width / max(1, pointsSize - 1)
        val path = Path()
        val fillPath = Path()

        val renderedCoordinates = points.mapIndexed { index, valAtPoint ->
            val x = index * xStep
            // Inverse height because Canvas origin (0,0) is top-left
            val y = height - ((valAtPoint - minVal) / diff * height)
            Offset(x, y)
        }

        // Draw Line Path
        if (renderedCoordinates.isNotEmpty()) {
            path.moveTo(renderedCoordinates[0].x, renderedCoordinates[0].y)
            fillPath.moveTo(renderedCoordinates[0].x, height)
            fillPath.lineTo(renderedCoordinates[0].x, renderedCoordinates[0].y)

            for (i in 1 until renderedCoordinates.size) {
                // Smooth curves logic (Bezier approximation)
                val prev = renderedCoordinates[i - 1]
                val curr = renderedCoordinates[i]
                val conX1 = prev.x + (curr.x - prev.x) / 2f
                val conY1 = prev.y
                val conX2 = prev.x + (curr.x - prev.x) / 2f
                val conY2 = curr.y

                path.cubicTo(conX1, conY1, conX2, conY2, curr.x, curr.y)
                fillPath.cubicTo(conX1, conY1, conX2, conY2, curr.x, curr.y)
            }

            fillPath.lineTo(renderedCoordinates.last().x, height)
            fillPath.close()

            // Draw beautiful gradient fill below the path
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        lineColor.copy(alpha = 0.35f),
                        lineColor.copy(alpha = 0.01f)
                    ),
                    startY = 0f,
                    endY = height
                )
            )

            // Draw line curve
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = 3.dp.toPx())
            )

            // Draw circles on individual node points (if list size is reasonable, say < 25)
            if (pointsSize < 25) {
                renderedCoordinates.forEachIndexed { idx, offset ->
                    val isLast = idx == renderedCoordinates.size - 1
                    drawCircle(
                        color = if (isLast) lineColor else Color.White,
                        radius = if (isLast) 6.dp.toPx() else 3.dp.toPx(),
                        center = offset
                    )
                    if (isLast) {
                        drawCircle(
                            color = lineColor.copy(alpha = 0.3f),
                            radius = 12.dp.toPx(),
                            center = offset
                        )
                    }
                }
            }
        }
    }
}
