package com.rengwuxian.coursecoroutines._4_flow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.EmptyCoroutineContext

@OptIn(ObsoleteCoroutinesApi::class)
fun main() = runBlocking<Unit> {
  val scope = CoroutineScope(EmptyCoroutineContext)
  val sender = scope.actor<Int> {
    for (num in this) {
      println("Number: $num")
    }
  }
  scope.launch {
    for (num in 1..100) {
      sender.send(num)
      delay(1000)
    }
  }
  delay(10000)
}
//produce是創一個攜程 然後把send暴露出來
//actor是創一個攜程把收暴露出來 都是簡化了創channel的過程在裡面