package com.rengwuxian.coursecoroutines._4_flow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.EmptyCoroutineContext

fun main() = runBlocking<Unit> {
  val scope = CoroutineScope(EmptyCoroutineContext)
  val flow1 = flowOf(1, 2, 3, 4, 5,3).onEach {
    println("throw #$it")
  }
  scope.launch {
//    flow1.drop(2).collect { println("1: $it") }
    flow1.dropWhile { it != 3 }.collect { println("2col: $it") }
//    flow1.take(2).collect { println("3: $it") }
    flow1.takeWhile { it != 3 }.collect { println("4col: $it") }
  }
  delay(10000)
}
//drop 丟棄資料 dropWhile 跳過序列中的元素，直到條件 不成立。 直到不等於條件前會跳過
//take 取資料 takewhile 保留元素，直到條件不成立為止。一旦條件不成立，後續的所有元素都會被忽略 直到不等於條件錢會拿取