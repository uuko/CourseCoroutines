package com.rengwuxian.coursecoroutines._4_flow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flattenConcat
import kotlinx.coroutines.flow.flattenMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.EmptyCoroutineContext

@OptIn(ExperimentalCoroutinesApi::class)
fun main() = runBlocking<Unit> {
  val scope = CoroutineScope(EmptyCoroutineContext)
//  flowOf(3, 5).map { from ->
//    (1 until from).asFlow().map {
//      "$from - $it"
//    }.collect {
//      println(it)
//    }
//  }
  //1. 收到 3 -> 開始收集 (1 until 3).asFlow() -> 完全收集後才發射下一個數據
  //2. 收到 5 -> 開始收集 (1 until 5).asFlow() -> 完全收集後結束
  val start = System.currentTimeMillis()
//  flowOf(3, 5)
//    .onEach { delay(1000) }  // 上游每次發射都延遲 1 秒
//    .flatMapMerge { from ->
//      (1 until from).asFlow()
//    }
//    .collect {
//      println("Received: $it at ${System.currentTimeMillis() -start}")
//    }
  //上游的每個元素轉換成 Flow 並依序展開，等待內部 Flow 完全處理完，再處理下一個元素。
//確保數據順序一致，不會交錯發射數據。
//缺點是內部 Flow 無法併行，處理速度較慢。
  //flatMapConcat 也只是內部串行而已跟上游無關
  flowOf(1, 2)
    .flatMapConcat { value ->
      flow {
        repeat(3) {
          delay(300)  // 模擬耗時操作
          emit("$value - $it")
        }
      }
    }
    .collect { println("Concat : $it  at ${System.currentTimeMillis() -start}") }

  flowOf(1, 2)
    .flatMapMerge { value ->
      flow {
        repeat(3) {
          delay(300)  // 模擬耗時操作
          emit("$value - $it")
        }
      }
    }
    .collect { println("merge : $it  at ${System.currentTimeMillis() -start}") }

  //

  flowOf(1, 2, 3)
    .onEach { delay(1000) }  // 上游發射間隔 1 秒
    .flatMapMerge { value ->
      flow {
        emit("$value - Start")
        delay(500)  // 每個內部 Flow 延遲 500ms
        emit("$value - End")
      }
    }
    .collect {println("merge top delay: $it  at ${System.currentTimeMillis() -start}") }
  //flatMapMerge 重點在於「內部 Flow 的併行展開」，並不會影響上游 Flow 的發射速度或節奏。
  //內部 Flow 會同時（併行）啟動，不會等待前一個 Flow 完成。
  //多個內部 Flow 同時進行，下游收集順序可能會交錯
//  val flow11 = flow {
//    emit(1)
//    delay(300)
//    emit(2)
//  }
//
//  val flow21 = flow {
//    emit("A")
//    delay(100)
//    emit("B")
//  }
//
//  flow11.combine(flow21) { a, b ->
//    "$a$b"
//  }.collect { value ->
//    println("Collected: $value at ${System.currentTimeMillis()}")
//  }
  val flow1 = flow {
    delay(500)
    emit(1)
    delay(500)
    emit(2)
    delay(500)
    emit(3)
  }
  val flow2 = flow {
    delay(250)
    emit(4)
    delay(500)
    emit(5)
    delay(500)
    emit(6)
  }
  val mergedFlow = merge(flow1, flow2)
  val flowList = listOf(flow1, flow2)
  val mergedFlowFromList = flowList.merge()
  val flowFlow = flowOf(flow1, flow2) // flatten
  val concattedFlowFlow = flowFlow.flattenConcat() // concatenate
  val mergedFlowFlow = flowFlow.flattenMerge()


  val concattedMappedFlow = flow1.flatMapConcat { from -> (1..from).asFlow().map { "$from - $it" } }
  val mergedMappedFlow = flow1.flatMapMerge { from -> (1..from).asFlow().map { "$from - $it" } }
  val latestMappedFlow = flow1.flatMapLatest { from -> (1..from).asFlow().map { "$from - $it" } }
  val combinedFlow = flow1.combine(flow2) { a, b -> "$a - $b" }
  val combinedFlow2 = combine(flow1, flow2, flow1) { a, b, c -> "$a - $b - $c" }
  flow1.combineTransform(flow2) { a, b -> emit("$a - $b") }
  val zippedFlow = flow1.zip(flow2) { a, b -> "$a - $b" }
  scope.launch {
    zippedFlow.collect { println(it) }
  }
  delay(10000)
}

//merge 是呼叫兩個 flow，收集它們並將結果轉發成一個 flow，所以它的發射是交織的。
//concat 是串行，按順序展開，會等第一個 flow 收集完再收集第二個。
//combine 當現在發的會去找對面上一個。 大致正確。combine 將兩個（或多個）Flow 的最新值結合起來，每當任何一個上游 Flow 發射新值時，會結合該值與其他 Flow 的最新值來產生一個新的結果。
//zip是拉鏈所以一定要1v1

//使用 merge 當你需要同時處理來自多個 Flow 的獨立數據。
//使用 concat 當你需要依序執行多個 Flow，確保順序性。
//使用 combine 當你需要根據多個 Flow 的最新值生成新的數據。
//使用 zip 當你需要一一對應兩個 Flow 的數據，類似於兩條數列的配對。

//flatMapConcat：串行處理，每個內部 Flow 完成後開始下一個。
//flatMapMerge：並行處理，多個內部 Flow 同時運行，結果交織發射。
//flatMapLatest：只處理最新的內部 Flow，取消未完成的處理。

//map + flowOn 的線性性
//線性處理：每個數據項按順序處理，無並行或交織。
//協程上下文切換：上游在指定的 Dispatcher，下游保持在原始上下文。
//不引入並發：整個數據流保持線性，不會同時處理多個數據項。
//一對一轉換，每個數據項按順序處理，並切換上游執行上下文。

//flatMap：將每個數據展開成一個新的 Flow，並將這些內部 Flow 合併到外部的 Flow 中。
//與 map 不同的是，flatMap 允許多個數據並行或交錯發射。

