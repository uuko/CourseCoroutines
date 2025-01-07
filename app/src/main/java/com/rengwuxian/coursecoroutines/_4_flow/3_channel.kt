package com.rengwuxian.coursecoroutines._4_flow

import com.rengwuxian.coursecoroutines.common.Contributor
import com.rengwuxian.coursecoroutines.common.gitHub
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.EmptyCoroutineContext

@OptIn(ExperimentalCoroutinesApi::class)
fun main() = runBlocking<Unit> {
  val scope = CoroutineScope(EmptyCoroutineContext)
  val channel = Channel<List<Contributor>>()
  scope.launch {
    channel.send(gitHub.contributors("square", "retrofit"))
  }
  scope.launch {
    while (isActive) {
      channel.receive()
    }
  }
  //兩個攜程中的隊列 跟管道 可以送可以拿 掛起是的隊列 blockingquer 先禁見出 會阻塞攜程應該說是掛起//
  //produce就是封裝後的做法讓他送 一個訂閱對應一個recevice 所以不適合做訂閱 可能會有人沒收到
  //事件流訂閱要用shardflow
  //caneel是單1v1
  scope.launch {
    while (isActive) {
      channel.receive()
    }
  }
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
}
//b=scope.poducer{ //this=a} 這裏B跟A是一樣的所其實channel負責收發都有