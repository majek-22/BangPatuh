package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.GameSessionRecord
import com.example.data.local.UserStats
import com.example.ui.theme.CardViolationStart
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.GoldSecondary
import com.example.ui.theme.MintSuccess
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

data class CategoryChartSlice(
    val name: String,
    val count: Int,
    val color: Color
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryDistributionDonutChart(
    userStats: UserStats,
    modifier: Modifier = Modifier
) {
    val bribery = userStats.briberySliced
    val fraud = userStats.fraudSliced
    val aml = userStats.moneyLaunderingSliced
    val breach = userStats.dataBreachSliced
    val systemic = userStats.systemicCorruptionSliced
    val other = userStats.otherViolationsSliced

    val total = bribery + fraud + aml + breach + systemic + other

    val slices = remember(userStats) {
        listOf(
            CategoryChartSlice("Bribery", bribery, Color(0xFFF27D6B)),
            CategoryChartSlice("Fraud", fraud, Color(0xFFF6BD60)),
            CategoryChartSlice("AML", aml, Color(0xFF81B29A)),
            CategoryChartSlice("Data Breach", breach, Color(0xFF64B5F6)),
            CategoryChartSlice("Systemic", systemic, Color(0xFFE56B6F)),
            CategoryChartSlice("Other", other, Color(0xFFB39DDB))
        ).filter { it.count > 0 }
    }

    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(userStats) {
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, animationSpec = tween(700))
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(18.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0x99162235)),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.stats_chart_category_breakdown),
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (total == 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.stats_legend_no_data),
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Donut Canvas
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(110.dp)) {
                            val strokeWidth = 24.dp.toPx()
                            val radius = (size.minDimension - strokeWidth) / 2f
                            val centerOffset = Offset(size.width / 2f, size.height / 2f)
                            val arcSize = Size(radius * 2f, radius * 2f)
                            val topLeft = Offset(centerOffset.x - radius, centerOffset.y - radius)

                            var currentAngle = -90f
                            for (slice in slices) {
                                val sweep = (slice.count.toFloat() / total) * 360f * animProgress.value
                                drawArc(
                                    color = slice.color,
                                    startAngle = currentAngle,
                                    sweepAngle = sweep,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = arcSize,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                                )
                                currentAngle += sweep
                            }
                        }

                        // Center total text
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$total",
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "Neutralized",
                                color = TextSecondary,
                                fontSize = 9.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Legend column
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        slices.forEach { slice ->
                            val pct = ((slice.count.toFloat() / total) * 100).toInt()
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(slice.color)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = slice.name,
                                        color = TextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Text(
                                    text = "${slice.count} ($pct%)",
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScoreProgressionLineChart(
    sessions: List<GameSessionRecord>,
    modifier: Modifier = Modifier
) {
    val recentSessions = remember(sessions) { sessions.take(8).reversed() }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(18.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0x99162235)),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.stats_chart_score_progression),
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (recentSessions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.stats_no_history),
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
            } else {
                val maxScore = (recentSessions.maxOfOrNull { it.score } ?: 100).coerceAtLeast(100)

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                ) {
                    val w = size.width
                    val h = size.height
                    val bottomPadding = 24.dp.toPx()
                    val topPadding = 12.dp.toPx()
                    val chartHeight = h - bottomPadding - topPadding

                    val stepX = if (recentSessions.size > 1) {
                        w / (recentSessions.size - 1)
                    } else {
                        w / 2f
                    }

                    val points = recentSessions.mapIndexed { index, session ->
                        val x = if (recentSessions.size > 1) index * stepX else w / 2f
                        val ratio = session.score.toFloat() / maxScore
                        val y = topPadding + chartHeight * (1f - ratio)
                        Offset(x, y)
                    }

                    // Background area gradient fill
                    val fillPath = Path().apply {
                        if (points.isNotEmpty()) {
                            moveTo(points.first().x, topPadding + chartHeight)
                            for (p in points) {
                                lineTo(p.x, p.y)
                            }
                            lineTo(points.last().x, topPadding + chartHeight)
                            close()
                        }
                    }

                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                CoralPrimary.copy(alpha = 0.35f),
                                Color.Transparent
                            )
                        )
                    )

                    // Line stroke
                    for (i in 0 until points.size - 1) {
                        drawLine(
                            color = CoralPrimary,
                            start = points[i],
                            end = points[i + 1],
                            strokeWidth = 3.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }

                    // Point circles
                    for ((idx, p) in points.withIndex()) {
                        drawCircle(
                            color = Color(0xFF0F172A),
                            radius = 6.dp.toPx(),
                            center = p
                        )
                        drawCircle(
                            color = GoldSecondary,
                            radius = 4.dp.toPx(),
                            center = p
                        )
                    }
                }

                // Bottom session indicators
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    recentSessions.forEachIndexed { idx, s ->
                        Text(
                            text = "${s.score}",
                            color = GoldSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
