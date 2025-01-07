package com.rengwuxian.coursecoroutines._4_flow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.EmptyCoroutineContext

fun main() = runBlocking<Unit> {
  val scope = CoroutineScope(EmptyCoroutineContext)
  val flow1 = flowOf(1, 2, 3)
  scope.launch {
    flow1.customOperator().collect { println("1: $it") }
    flow1.double().collect { println("2: $it") }
  }
  delay(10000)
}

fun <T> Flow<T>.customOperator(): Flow<T> = flow {
  collect {
    emit(it)
    emit(it)
  }
}

fun Flow<Int>.double(): Flow<Int> = channelFlow {
  collect {
    send(it * 2)
  }
}

//自定義操作符
//1.要先擴展flow 所以要用flow.XXX
//2.通常是用channelflow or flow做擴展
//3.要能擴展所以是我們要收集上游的數據 this.collect{}
//4.要能轉成下游 所以在this.collect{ emit(???)} ???是你要怎轉的新值