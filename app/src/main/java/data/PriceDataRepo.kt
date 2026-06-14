
package com.example.glanceexample.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.random.Random

object PriceDataRepo {
    const val ticker = "GOOGL"
    private var previousPrice = 0f
    var change = 0
        private set

    private val _currentPrice = MutableStateFlow(0f)
    val currentPrice: StateFlow<Float> = _currentPrice

    fun update() {
        previousPrice = _currentPrice.value
        // случайная цена от 20 до 35 с дробной частью
        val newPrice = (Random.nextInt(20, 35) + Random.nextFloat()).toFloat()
        _currentPrice.value = newPrice
        change = if (previousPrice == 0f) {
            0
        } else {
            ((newPrice - previousPrice) / previousPrice * 100).toInt()
        }
    }
}