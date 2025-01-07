package com.rengwuxian.coursecoroutines._4_flow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.lastOrNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.produceIn
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.toCollection
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.toSet
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.EmptyCoroutineContext

fun main() = runBlocking<Unit> {
  val scope = CoroutineScope(EmptyCoroutineContext)
  val flow1 = flowOf(1, 2, 3, 4, 5)
  val flow = (1..5).asFlow().onEach { delay(500) }  // 產生 1 到 5 的數據流，每500ms發送一次

  val channel2 = flow.produceIn(scope)  // 將Flow轉成Channel（1對1）

  launch {
    for (item in channel2) {
      println("協程 A 收到: $item")
    }
  }

  launch {
    for (item in channel2) {
      println("協程 B 收到: $item")
    }
  }
  //協程 A 收到: 1
  //協程 B 收到: 2
  //協程 A 收到: 3
  //協程 B 收到: 4
  //協程 A 收到: 5
  val channel = flow1.produceIn(scope) // Channel produce()
  scope.launch {
    println("First: ${flow1.first()}")
    println("First with condition: ${flow1.first { it > 2 }}")
    try {
      flowOf<Int>().first()
    } catch (e: NoSuchElementException) {
      println("No element")
    }
    println("firstOrNull(): ${flow1.firstOrNull { it > 5 }}")
    // terminal operator
    flow1.last()
    flow1.lastOrNull()
    try {
      flow1.single()
    } catch (e: Exception) {

    }
    flow1.singleOrNull()
    println("count(): ${flow1.count { it > 2 }}")
    val list = mutableListOf<Int>()
    println("List: ${flow1.toList(list)}")
    flow1.toSet() // Set
    flow1.toCollection(list)
  }
  delay(10000)
}
//terminal operator
//收集flow並轉換樣式（collect並返回） collect是掛起函數

//flow.produceIn =>轉channel 會創一個channel把資料丟進去 並在協成里收集數據
//val flow = (1..5).asFlow().onEach { delay(500) }
//produceIn 是將 Flow 轉換為 Channel，使 Flow 的數據能夠持續發送到 Channel，允許在多個協程中併行消費。
//    // 使用 produceIn 將 Flow 轉換成 Channel
//    val channel: ReceiveChannel<Int> = flow.produceIn(this)
//
//    // 啟動多個協程同時消費 Channel 的數據
//    launch {
//        for (item in channel) {
//            println("協程 A 收到: $item")
//        }
//    }
//
//    launch {
//        for (item in channel) {
//            println("協程 B 收到: $item")
//        }
//    }

//2️⃣ ChannelFlow 是什麼？
//ChannelFlow 是 用 Channel 作為數據來源，並將數據發送到 Flow 的機制。
//你可以在 Flow 內部構建 Channel，讓上游（數據源）以 Channel 方式推送數據，下游（Flow）收集數據。


//fun simpleFlow(): Flow<Int> = flow {
//    val channel = Channel<Int>(Channel.BUFFERED)  // 創建一個緩衝Channel
//
//    launch {
//        for (i in 1..5) {
//            println("發送數據: $i")
//            channel.send(i)  // 發送到Channel
//            delay(500)
//        }
//        channel.close()  // 數據發送結束，關閉Channel
//    }
//
//    for (item in channel) {
//        emit(item)  // 將數據發送到Flow
//    }
//}

//像這樣兩邊剛好反向
//produceIn（Flow -> Channel）：
//
//Flow 轉換為 Channel，使其變成熱流，即使沒有消費者，數據也持續發送到 Channel。
//適合多協程同時消費數據，且不需要重新啟動 Flow。
//ChannelFlow（Channel -> Flow）：
//
//Flow 內部透過 Channel 發送數據，本質仍然是冷流，只有 collect 時才啟動。
//適合需要細緻控制數據發送的場景，比如手動發送數據到 Channel，Flow 接收並轉發。
