package com.rengwuxian.coursecoroutines._4_flow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.EmptyCoroutineContext

fun main() = runBlocking<Unit> {
  Ticker.start()
  val scope = CoroutineScope(EmptyCoroutineContext)
//  val flow1 = flow {
//    emit(1)
//    delay(1000)
//    emit(2)
//    delay(1000)
//    emit(3)
//  }
  val flow2 = callbackFlow {
    Ticker.subscribe { trySend(it) }
    awaitClose()
  }
  scope.launch {
    delay(2500)
    flow2.collect {
      println("flow2 - 1: $it")
    }
  }
  scope.launch {
    delay(1500)
    flow2.collect {
      println("flow2 - 2: $it")
    }
  }
//  val sharedFlow = flow1.shareIn(scope, SharingStarted.Eagerly)
  /*scope.launch {
    delay(500)
    sharedFlow.collect {
      println("SharedFlow in Coroutine 1: $it")
    }
  }
  // Channel: hot
  // FLow: cold
  scope.launch {
    delay(1500)
    sharedFlow.collect {
      println("SharedFlow in Coroutine 2: $it")
    }
  }*/
  delay(10000)
}

object Ticker {
  private var time = 0
    set(value) { // Kotlin setter
      field = value
      subscribers.forEach { it(value) }
    }

  private val subscribers = mutableListOf<(Int) -> Unit>()

  fun subscribe(subscriber: (Int) -> Unit) {
    subscribers += subscriber
  }

  fun start() {
    GlobalScope.launch {
      while (true) {
        delay(1000)
        time++
      }
    }
  }
}

//sharedin 會決定何時collect上游 並轉發出去  (也會返回flow)
// 每次下游在呼叫這個sharedflow collect都會轉發
//發送跟讀取拆開了
//我可以理解為 sharedin(時間) 就是何時開始collect上游 而collect sharedflow的人 只是去接他的轉發而已嗎？
//有點像channel那樣？但channel是點對點(1v1)
//上游 Flow → SharedFlow（數據存儲站） → 多個下游消費者 collect
//啟動上游 Flow 的觸發點 是 shareIn 的 SharingStarted 策略，而不是下游的 collect。
//多次數據依賴是同一個數據源就可以用這個了 or 數據源提前啟動
//數據生產與數據收集 流程分拆 的需求 限制：可以露數據的場景 數據訂閱 flow:數據流
//上游 Flow 啟動後會持續產生數據，即使沒有消費者（協程未 collect），數據也會存儲在 SharedFlow 中。
//生產者（上游）和消費者（下游）可以在不同的時間點獨立運行。 支援多消費者同時收集數據（廣播式分發）
// 生產者與消費者是否解耦？
//問題： 上游數據流是否應該持續運行，而不依賴下游消費者是否存在？
//問題： 是否需要將相同的數據流同時發送給多個消費者？
//判斷點：
//如果需要廣播數據，多個協程/消費者需要同時收集相同數據。
//例如：
//UI 各個模組監聽同一個 ViewModel 狀態變化。
//多個後端模組監聽相同的系統事件或警報。
//. 是否需要數據重播（Replay）機制？
//問題： 消費者是否可能在數據生產後才加入，並需要獲取之前生產的數據？
//判斷點：
//vs launchIn？？
//數據生產與消費需要分離，上游數據獨立運行。
//多個消費者同時收集數據。
//如果數據生產與消費同步，沒有分離，Flow 直接用就好。
//如果數據生產獨立，消費者可能隨時加入或離開，那麼 SharedFlow 是更好的選擇。

//數據跟讀取分開 => 熱？

