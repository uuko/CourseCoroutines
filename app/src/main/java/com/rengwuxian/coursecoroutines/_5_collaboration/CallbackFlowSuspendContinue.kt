package com.rengwuxian.coursecoroutines._5_collaboration

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class CallbackFlowSuspendContinue {
    interface ButtonClickListener {
        fun onClick(count: Int)
    }

    class ThirdPartyButton {
        private var listener: ButtonClickListener? = null

        fun setClickListener(listener: ButtonClickListener?) {
            this.listener = listener
        }

        // 模擬點擊觸發回調
        fun simulateClick() {
            listener?.onClick((1..100).random())  // 回傳隨機點擊次數
        }
    }

    fun buttonClickFlow(button: ThirdPartyButton): Flow<Int> = callbackFlow {
        val listener = object : ButtonClickListener {
            override fun onClick(count: Int) {
                trySend(count)  // 將回調結果發送到 Flow
            }
        }

        button.setClickListener(listener)

        // 當 Flow 被取消時，移除回調，防止內存洩漏
        awaitClose {
            button.setClickListener(null)
            println("Flow 取消，移除回調")
        }
    }

    suspend fun waitForClick(button: ThirdPartyButton): Int =
        suspendCancellableCoroutine { continuation ->
            val listener = object : ButtonClickListener {
                override fun onClick(count: Int) {
                    continuation.resume(count)  // 恢復協程並返回數據
                    button.setClickListener(null)  // 解除監聽，防止再次觸發
                }
            }
            button.setClickListener(listener)
            continuation.invokeOnCancellation {
                println("取消請求")
            }
        }
}

fun main() = runBlocking {
    val sample = CallbackFlowSuspendContinue()
    val button = CallbackFlowSuspendContinue.ThirdPartyButton()
//    launch {
//        val clickCount = sample.waitForClick(button)
//        println("按鈕點擊次數：$clickCount")
//    }

    launch {
//        sample.buttonClickFlow(button)
//            .collect { count ->
//                println("收到點擊次數: $count")
//            }
        val clickCount = sample.waitForClick(button)
        println("按鈕點擊次數：$clickCount")
    }

    // 模擬多次點擊
    repeat(5) {
        delay(1000)
        button.simulateClick()
    }

}

//功能	callbackFlow（因為是channel所以不會做完就取消）	suspendCoroutine
//性質	連續監聽回調，數據多次發送	只監聽一次回調，數據只發送一次
//場景	位置更新、WebSocket、股票推送	單次 API 請求、按鈕點擊等
//自動取消	awaitClose 取消監聽，釋放資源	需手動解除監聽
//異常處理	trySend 捕獲異常並繼續運行	resumeWithException 結束協程
//返回類型	Flow<T>	suspend fun 返回單次 T

//1️⃣ callbackFlow：
//上游：Channel
//下游：Flow
//取消機制：
//下游的 collect 停止後，會自動取消上游的 Channel，並觸發 awaitClose 進行清理。
//callbackFlow 是一個完整的閉環，Flow 被取消後可以真正停止數據的生產，釋放資源。
//callbackFlow 是「主動式關閉」數據流。
//2️⃣ SharedFlow：
//上游：Flow
//內部：Channel 轉發數據，並進行多播（廣播）
//下游：Flow（多個消費者同時收集）
//取消機制：
//即使下游的 collect 停止，SharedFlow 本身仍繼續運行，不影響上游數據流的發送。
//SharedFlow 不會自動取消或關閉，除非顯式取消協程作用域或 SharedFlow 本身的作用域。
//SharedFlow 是「被動式關閉」，僅停止轉發，不會停止上游生產。

//🚧 核心差異：
//callbackFlow 的取消是雙向的：下游取消收集時，會自動影響上游，並終止數據源。
//SharedFlow 的取消是單向的：下游取消收集，只會停止數據接收，但不影響上游的數據生產。

//fun stockPriceFlow(scope: CoroutineScope): Flow<String> {
//    val channel = Channel<String>(Channel.BUFFERED)  // 持續產生數據的 Channel
//
//    scope.launch {
//        while (true) {
//            channel.send("股價更新: ${System.currentTimeMillis()}")
//            delay(1000)
//        }
//    }
//
//    return flow {
//        for (price in channel) {
//            emit(price)  // 將 Channel 內容發送給 Flow
//        }
//    }
//}