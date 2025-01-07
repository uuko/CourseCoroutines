package com.rengwuxian.coursecoroutines._3_scope_context

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.coroutines.EmptyCoroutineContext

fun main() = runBlocking<Unit> {
  val scope = CoroutineScope(EmptyCoroutineContext)
  scope.launch {
    withContext(coroutineContext) {

    }
    coroutineScope {

    }
    launch {

    }

    //withcontext等於 可傳參數的coroutineScope = 並行的lauch
    //並行 串行指的是括號內
  }
  delay(10000)
}