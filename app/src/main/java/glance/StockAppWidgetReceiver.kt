package com.example.glanceexample.glance

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.work.*
import com.example.glanceexample.data.PriceDataRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class StockAppWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget = StockAppWidget()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        // Одноразовое обновление при добавлении виджета
        PriceDataRepo.update()
        // Запуск периодического обновления (каждые 20 секунд)
        val workRequest = PeriodicWorkRequestBuilder<PriceUpdateWorker>(
            20, TimeUnit.SECONDS
        ).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "price_update",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        // Останавливаем периодическое обновление, когда виджет удалён
        WorkManager.getInstance(context).cancelUniqueWork("price_update")
    }
}

// Worker для фонового обновления цены
class PriceUpdateWorker(context: Context, params: WorkerParameters) :
    androidx.work.CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        PriceDataRepo.update()
        // Обновить все экземпляры виджетов
        StockAppWidget().updateAll(applicationContext)
        return Result.success()
    }
}