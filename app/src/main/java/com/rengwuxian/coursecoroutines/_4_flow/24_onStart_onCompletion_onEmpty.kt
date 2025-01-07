package com.rengwuxian.coursecoroutines._4_flow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEmpty
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.EmptyCoroutineContext

fun main() = runBlocking<Unit> {
  val scope = CoroutineScope(EmptyCoroutineContext)
  val flow1 = flow {
    try {
      for (i in 1..5) {
        emit(i)
      }
    } catch (e: Exception) {
      println("try / catch: $e")
    }
  }.onStart {
    println("onStart 1")
    throw RuntimeException("onStart error")
  }
    .onStart { println("onStart 2") }
    .onCompletion {
      println("onCompletion: $it")
    }.onEmpty { println("onEmpty") }
    .catch { println("catch: $it") }
  scope.launch {
    flow1.collect {
      println("Data: $it")
    }
  }
  delay(10000)
}

//onstart是在collect之前的（flow的開始） 所以collect外try catch 補不了 然後兩個onstart 後會比先執行
//oncompletion是flow結束 異常也能算結束 所以能print ex 但他並不catch住所以還是會往下丟
