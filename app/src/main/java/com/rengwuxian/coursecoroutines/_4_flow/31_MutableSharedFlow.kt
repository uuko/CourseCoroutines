package com.rengwuxian.coursecoroutines._4_flow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.EmptyCoroutineContext

fun main() = runBlocking<Unit> {
  val scope = CoroutineScope(EmptyCoroutineContext)
  val flow1 = flow {
    emit(1)
    delay(1000)
    emit(2)
    delay(1000)
    emit(3)
  }
  val clickFlow = MutableSharedFlow<String>()
  val readonlyClickFlow = clickFlow.asSharedFlow()
  val sharedFlow = flow1.shareIn(scope, SharingStarted.WhileSubscribed(), 2)
  scope.launch {
    clickFlow.emit("Hello")
    delay(1000)
    clickFlow.emit("Hi")
    delay(1000)
    clickFlow.emit("你好")
    val parent = this
    launch {
      delay(4000)
      parent.cancel()
    }
    delay(1500)
    sharedFlow.collect {
      println("SharedFlow in Coroutine 1: $it")
    }
  }
  scope.launch {
    delay(5000)
    sharedFlow.collect {
      println("SharedFlow in Coroutine 2: $it")
    }
  }
  delay(10000)
}

//事件流可以從任何地方發送數據  mutableshareflow
//SharedFlow 底層基於 Channel 實現，而 Channel 天生具有線程安全機制。
//無論多少個協程同時 emit，SharedFlow 都會正確處理，保證數據發送順序
//數據流都是設計好的不太會從任何地方發 sharedIn就是街上層數據流 底層也是mutableshareflow
//public fun <T> Flow<T>.shareIn(
//    scope: CoroutineScope,
//    started: SharingStarted,
//    replay: Int = 0
//): SharedFlow<T> {
//    val config = configureSharing(replay)
//    val shared = MutableSharedFlow<T>(
//        replay = replay,
//        extraBufferCapacity = config.extraBufferCapacity,
//        onBufferOverflow = config.onBufferOverflow
//    )
//    @Suppress("UNCHECKED_CAST")
//    val job = scope.launchSharing(config.context, config.upstream, shared, started, NO_VALUE as T)
//    return ReadonlySharedFlow(shared, job)
//}

//有現成flow 就去用sharedflow 有要從外面用生產的就用mutableshareflow
//也可以轉程 mutableshareflow.asshareflow 就是讓外面不能丟emit用的