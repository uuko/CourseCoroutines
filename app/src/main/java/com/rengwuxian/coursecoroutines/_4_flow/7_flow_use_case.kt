package com.rengwuxian.coursecoroutines._4_flow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.coroutines.EmptyCoroutineContext

fun main() = runBlocking<Unit> {
  val flow = flow {
    emit("a")
  }
  //low started on DefaultDispatcher-worker-1
  //Flow started on DefaultDispatcher-worker-2
  //就能看出其實是collect食材呼叫的 （不同dispather）
  fun myFlow() = flow {
    println("Flow started on ${Thread.currentThread().name}") // 觀察執行緒
    emit(1)
    emit(2)
    emit(3)
  }
  val job1 = launch(Dispatchers.Default) { // 在 Dispatchers.Default 上收集
    myFlow().collect { value ->
      println("Collector 1 on ${Thread.currentThread().name}: $value")
    }
  }

  val job2 = launch(Dispatchers.IO) { // 在 Dispatchers.IO 上收集
    myFlow().collect { value ->
      println("Collector 2 on ${Thread.currentThread().name}: $value")
    }
  }

  joinAll(job1, job2)

  val scope2 = CoroutineScope(EmptyCoroutineContext)
  scope2.launch {
    //等於collect在叫的時候是把{
    //    emit("a")
    //  }這塊在這裡呼叫 他是lazy的
    flow.collect {
      println("first: $it")
    }
  }

  scope2.launch {
    flow.collect {
      println("sec: $it")
    }
  }



  val numsFlow = flow {
    emit(1)
    delay(100)
    emit(2)
  }
  val scope = CoroutineScope(EmptyCoroutineContext)
  scope.launch {
//    showWeather(weatherFlow)
    weatherFlow.collect {
      println("Weather: $it")
    }
    // log("done")
    /*numsFlow.collect {
      println("A: $it")
    }*/
  }
  scope.launch {
    delay(50)
    numsFlow.collect {
      println("B: $it")
    }
  }
  delay(10000)
}

val weatherFlow = flow {
  while (true) {
    emit(getWeather())
    delay(60000)
  }
}

suspend fun showWeather(flow: Flow<String>) {
  flow.collect {
    println("Weather: $it")
  }
}

suspend fun getWeather() = withContext(Dispatchers.IO) {
  "Sunny"
}

//collect是把flow{} 代碼塊的內容移過去用 他是lazy的
//然後flow是跨協程的
//由flow對象提供數據流的生產邏輯 然後在收集流程裏執行這套生產邏輯並處理每條生產數據
//flow就是這樣的一個數據生產工具

//hot?cold?
//channel是hot 是因為channel有自己獨立/統一化的生產線調用一次send就生產一次數據
// 跟是否調用recieve無關
//channel本質上是一個管道而已 是讓人放數據的（類似blockingqueue）當buffer=1時

//flow是一套生產規則（數據生產的邏輯）
// 真正的生產一定是在collect開始的 每次collect都有一套完整的生產流程 這就是cold
//flow創建不用在協程裏 因為他只是生產邏輯 collect要在協程裏因為他是真正調用的
  //public fun <T> flow(@BuilderInference block: suspend FlowCollector<T>.() -> Unit): Flow<T> = SafeFlow(block)
//flow的應用場景是要把生產跟處理數據分開才用 flow是收集不是訂閱 會把東西堵住

//channel =>
//1.類似blocking queue (buffer=1)
//2.是個管道主要讓跨協程通訊
//3.1v1的 除了broadcastchannel
//你對於 collect 和 receive 會阻塞協程的理解大致正確，但需要更精確地說明它們阻塞的 情境 和 方式。
//
//receive 的阻塞行為
//
//receive() 是 Channel 的一個操作，用於從 Channel 中接收訊息。它會 阻塞 協程，直到 Channel 中有訊息可用。
//flow=>
//1.是個生產規則
//2.在收集時才會開始生產（預設情況下，每個 collect 都會獨立執行 Flow 的生產規則）
//3.拆開生產跟收集用
//collcect 會阻塞協程 要放到單個lauch
//Flow 在多個收集器（collect）和多個協程（launch）同時操作共享狀態時可能產生的資料混亂風險。你的疑慮是正確的，如果處理不當，的確會造成不安全的問題

//Channel 就像一個送貨員： 他負責將貨物 (訊息) 從一個地方送到另一個地方。
//Flow 就像一份食譜： 它描述了如何製作一道菜 (資料流)。每次你想吃這道菜 (收集 Flow) 時，你都需要按照食譜 (生產規則) 重新烹飪一次。