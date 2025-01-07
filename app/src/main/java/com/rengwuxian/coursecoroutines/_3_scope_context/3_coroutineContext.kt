package com.rengwuxian.coursecoroutines._3_scope_context

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.coroutineContext

fun main() = runBlocking<Unit> {
  val scope = CoroutineScope(EmptyCoroutineContext)
  scope.launch {
    showDispatcher()
  }
  delay(10000)
}

private fun flowFun() {
  flow<String> {
    coroutineContext
  }
  GlobalScope.launch {
    flow<String> {
      //這是為了拿scope得
      currentCoroutineContext()
    }
  }
}

private suspend fun showDispatcher() {
  delay(1000)
  println("Dispatcher: ${coroutineContext[ContinuationInterceptor]}")
}

//每個掛起函數一定會有context 因為他一定是外面有啟動corutine的