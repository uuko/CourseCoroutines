package com.rengwuxian.coursecoroutines._4_flow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.EmptyCoroutineContext

fun main() = runBlocking<Unit> {
  val scope = CoroutineScope(EmptyCoroutineContext)
  val flow1 = flowOf("rengwuxian", "RengWuXian", "rengwuxian.com")
  scope.launch {
    flow1.distinctUntilChanged().collect { println("1: $it") } // == equals()
    flow1.distinctUntilChanged { a, b -> a.uppercase() == b.uppercase() }.collect { println("2: $it") }
    flow1.distinctUntilChangedBy { it.uppercase() }.collect { println("3: $it") }
  }
  delay(10000)
}
//distinctUntilChanged跟equals一樣
//distinctUntilChanged 可以前面寫{}要判斷的條件
//distinctUntilChangedBy 一樣是改key{判斷}不會改最後結果