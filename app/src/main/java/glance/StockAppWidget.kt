package com.example.glanceexample.glance

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.clickable
import androidx.glance.cornerRadius
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.DpSize
import com.example.glanceexample.R
import com.example.glanceexample.data.PriceDataRepo
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale

class StockAppWidget : GlanceAppWidget() {

    companion object {
        // размеры для responsive-режима
        private val smallMode = DpSize(100.dp, 80.dp)
        private val mediumMode = DpSize(120.dp, 120.dp)
    }

    override val sizeMode = SizeMode.Responsive(
        setOf(smallMode, mediumMode)
    )

    // метод принудительного обновления данных и виджета при клике
    private fun refreshPrice(context: Context, glanceId: GlanceId) {
        PriceDataRepo.update()
        // перерисовываем виджет после изменения данных
        kotlinx.coroutines.GlobalScope.launch {
            update(context, glanceId)
        }
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                val currentPrice = PriceDataRepo.currentPrice.collectAsState().value
                val size = LocalSize.current
                when (size) {
                    smallMode -> Small(currentPrice) { refreshPrice(context, id) }
                    mediumMode -> Medium(currentPrice) { refreshPrice(context, id) }
                    else -> Small(currentPrice) { refreshPrice(context, id) } // fallback
                }
            }
        }
    }

    // Общий компонент отображения цены и тикера
    @Composable
    private fun StockDisplay(price: Float, changePercent: Int) {
        val color = if (changePercent >= 0) {
            GlanceTheme.colors.primary
        } else {
            GlanceTheme.colors.error
        }
        val priceStyle = TextStyle(
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Column {
            Text(
                text = PriceDataRepo.ticker,
                style = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = String.format(Locale.getDefault(), "%.2f", price),
                style = priceStyle
            )
            Text(
                text = "$changePercent %",
                style = priceStyle
            )
        }
    }

    // Макет для маленького размера
    @Composable
    private fun Small(price: Float, onRefresh: () -> Unit) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .clickable { onRefresh() }
                .background(GlanceTheme.colors.background)
                .padding(8.dp)
        ) {
            StockDisplay(price, PriceDataRepo.change)
        }
    }

    // Макет для среднего размера (со стрелкой)
    @Composable
    private fun Medium(price: Float, onRefresh: () -> Unit) {
        Column(
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
            modifier = GlanceModifier
                .fillMaxSize()
                .clickable { onRefresh() }
                .cornerRadius(15.dp)
                .background(GlanceTheme.colors.background)
                .padding(8.dp)
        ) {
            StockDisplay(price, PriceDataRepo.change)
            Image(
                provider = ImageProvider(
                    if (PriceDataRepo.change >= 0) R.drawable.up_arrow
                    else R.drawable.down_arrow
                ),
                contentDescription = "Arrow",
                modifier = GlanceModifier
                    .fillMaxSize()
                    .padding(20.dp)
            )
        }
    }
}