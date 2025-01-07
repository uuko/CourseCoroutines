package com.rengwuxian.coursecoroutines._4_flow

import com.rengwuxian.coursecoroutines.common.gitHub
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.EmptyCoroutineContext

@OptIn(ExperimentalCoroutinesApi::class)
fun main() = runBlocking<Unit> {
  val scope = CoroutineScope(EmptyCoroutineContext)
  val receiver = scope.produce {
    while (isActive) {
      val data = gitHub.contributors("square", "retrofit")
      send(data)
    }
  }
  launch {
    delay(5000)
    while (isActive) {
      println("Contributors: ${receiver.receive()}")
    }
  }
  delay(10000)

  //跟async有點像 但async是一次性（通過最後一行做的）兩者都是跨攜程
  //因為defferd只能放一樣東西嗎 如果我在async while迴圈也沒用  因為只有最後一行會傳出去
  //produce是多次的  ReceiveChannel 通過send receive
  //@ExperimentalCoroutinesApi
  //public fun <E> CoroutineScope.produce(
  //    context: CoroutineContext = EmptyCoroutineContext,
  //    capacity: Int = 0,
  //    @BuilderInference block: suspend ProducerScope<E>.() -> Unit
  //): ReceiveChannel<E> =
  //    produce(context, capacity, BufferOverflow.SUSPEND, CoroutineStart.DEFAULT, onCompletion = null, block = block)
}