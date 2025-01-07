package com.rengwuxian.coursecoroutines._5_collaboration

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class Shareflowmonitor {
    interface StockPriceCallback {
        fun onPriceUpdate(price: String)
    }
    object StockPriceSdk {
        private var callback: StockPriceCallback? = null

        fun registerCallback(cb: StockPriceCallback) {
            callback = cb
            // 模擬價格更新
            GlobalScope.launch {
                while (true) {
                    delay(1000)
                    callback?.onPriceUpdate("價格更新: ${System.currentTimeMillis()}")
                }
            }
        }

        fun unregisterCallback(cb: StockPriceCallback) {
            if (callback == cb) {
                callback = null
            }
        }
    }
    fun stockPriceFlowManual(scope: CoroutineScope): Flow<String> = flow {
        val channel = Channel<String>(Channel.BUFFERED)

        // 啟動協程監聽 callback 並將數據發送到 Channel
        val job = scope.launch {
            val callback = object : StockPriceCallback {
                override fun onPriceUpdate(price: String) {
                    channel.trySend(price)  // 將價格發送到 Channel
                }
            }
            StockPriceSdk.registerCallback(callback)

            try {
                for (price in channel) {
                    emit(price)  // 將 Channel 內容發送到 Flow
                }
            } finally {
                // 解除 callback 並關閉 channel
                StockPriceSdk.unregisterCallback(callback)
                channel.close()
            }
        }

        // 當 Flow 被取消時，關閉 channel 並取消監聽協程
//        awaitClose {
//            job.cancel()  // 取消協程
//            channel.close()
//        }
    }

    fun stockPriceFlow(scope: CoroutineScope): Flow<String> {
        val channel = Channel<String>(Channel.BUFFERED)  // 持續產生數據的 Channel

        scope.launch {
            while (true) {
                channel.send("股價更新: ${System.currentTimeMillis()}")
                println("Sending 股價更新: ${System.currentTimeMillis()} ~~~~~~")
                delay(1000)
            }
        }

        return flow {
            for (price in channel) {
                emit(price)  // 將 Channel 內容發送給 Flow
            }
        }
    }
}

fun main() = runBlocking{
    val monitor = Shareflowmonitor()
    val flow = monitor.stockPriceFlow(this)
//    .collect {
//        println(it)
//    }

}
//在 SharedFlow 的典型用法中，上游的數據生產通常在與 SharedFlow 同一個 CoroutineScope 啟動，避免因為下游收集結束而導致上游生產被取消。