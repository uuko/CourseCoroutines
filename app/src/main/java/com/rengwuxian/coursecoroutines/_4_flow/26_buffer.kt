package com.rengwuxian.coursecoroutines._4_flow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.concurrent.thread
import kotlin.coroutines.EmptyCoroutineContext

fun main() = runBlocking<Unit> {
  val scope = CoroutineScope(EmptyCoroutineContext)
  val start = System.currentTimeMillis()
  flow {
    emit(1)
    delay(100)  // 發送間隔
    emit(2)
    delay(100)
    emit(3)
    delay(100)
    emit(4)
  }.mapLatest {
    println("Mapping $it...")
    delay(400)  // 下游處理慢，模擬耗時
    "Mapped $it"
  }.buffer(0).collect {
    println("Collected: $it")
  }
//  val flow1 = flow {
//    for (i in 1..5) {
//      emit(i)
//      println("Emitted: $i - ${System.currentTimeMillis() - start}ms")
//    }
//  }
////    .buffer(1)
//    .flowOn(Dispatchers.IO)
//    .buffer(2)
////    .conflate()
//    .map { it + 1 }
//    .map { it * 2 }
//  scope.launch {
//    flow1.mapLatest { it }.buffer(0).collect {
//      delay(1000)
//      println("Data: $it")
//    }
//  }
  delay(10000)
}

//val flow1 = flow {
//    for (i in 1..5) {
//        emit(i)
//        println("Emitted: $i - ${System.currentTimeMillis() - start}ms")
//    }
//}
//    .flowOn(Dispatchers.IO)  // 影響上游的執行執緒 (IO 執行緒)
//    .map { it + 1 }          // 在主執行緒 (main) 中執行
//    .map { it * 2 }          // 仍然在主執行緒 (main) 中執行
//上游執行 IO 執行緒，與主執行緒並行執行。
//下游在主執行緒逐一處理，即使上游發送速度很快，資料還是會按照順序在主執行緒處理。
//如果上游很快完成，主執行緒處理下游時可能會稍微滯後，但不會跳過或丟失資料。

//flow {
//    repeat(5) {
//        emit(it)
//        println("Emitting: $it on ${Thread.currentThread().name}")
//    }
//}.flowOn(Dispatchers.IO)
//    .collect {
//        println("Collected: $it on ${Thread.currentThread().name}")
//    }

//flowOn = channelflow//buffer也是變channelflow
//解決問題：變成flow發出的部分在創一個flow = 上游emit在flowOn指定的thread 裡面用channel收到後再轉emit一次
//既能把emit分開 又可以讓下游回到原本的thread(collect thread)
//如果在emit直接切thread 會導致collect的collector也在那thread 下游非常難維護 而且上下游還是真正阻塞都在io 並沒有解耦
//flow {
//    withContext(Dispatchers.IO) {
//        emit(fetchData())  // 在 IO 執行緒發送數據
//    }
//}.collect {
//    println("Collected on ${Thread.currentThread().name}")
//}

//fun <T> Flow<T>.flowOn(dispatcher: CoroutineDispatcher): Flow<T> = channelFlow {
//    launch(dispatcher) {
//        collect { value ->
//            send(value)  // 發送數據到通道
//        }
//    }
//}
//internal class ChannelFlowOperatorImpl<T>(
//    flow: Flow<T>,
//    context: CoroutineContext = EmptyCoroutineContext,
//    capacity: Int = Channel.OPTIONAL_CHANNEL,
//    onBufferOverflow: BufferOverflow = BufferOverflow.SUSPEND
//) : ChannelFlowOperator<T, T>(flow, context, capacity, onBufferOverflow) {
//    override fun create(context: CoroutineContext, capacity: Int, onBufferOverflow: BufferOverflow): ChannelFlow<T> =
//        ChannelFlowOperatorImpl(flow, context, capacity, onBufferOverflow)
//
//    override fun dropChannelOperators(): Flow<T> = flow
//
//    override suspend fun flowCollect(collector: FlowCollector<T>) =
//        flow.collect(collector)
//}
//flowOn 內部會將上游的 Flow 轉換為 ChannelFlow。
//ChannelFlow 利用 Channel 在不同執行緒之間傳遞數據，實現跨執行緒處理。
//下游透過 collect，實際上是在內部不斷地呼叫 channel.receive()，直到數據處理完畢。
//flowOn 使用預設緩衝策略 (BUFFERED)

//fun customChannelFlow(): Flow<Int> = flow {
//    val channel = Channel<Int>(Channel.BUFFERED)  // 緩衝區
//
//    // 上游在 IO 執行緒發送數據
//    launch(Dispatchers.IO) {
//        for (i in 1..5) {
//            channel.send(i)
//            println("Sent: $i on ${Thread.currentThread().name}")
//        }
//        channel.close()
//    }
//
//    // 下游在 collect 中收集數據
//    for (item in channel) {
//        emit(item)
//        println("Collected: $item on ${Thread.currentThread().name}")
//    }
//}


//maplatest也是channelflow但他buffer=0 變成上游如果發很快下游處理很慢 buffer = 0 的作用是控制數據的發送節奏
//變成1如果沒collect玩 2就發來了 他還在等1所以直接把2丟了 這樣

//emit(1) 發送，進入 mapLatest 處理。
//emit(2) 發送，取消正在處理的 1，開始處理 2。
//emit(3) 發送，取消正在處理的 2，開始處理 3。
//emit(4) 發送，取消正在處理的 3，開始處理 4。
//emit 結束，沒有更多數據，4 可以完整處理並被收集。
//buffer = 0 不允許上游在下游處理未完成時繼續發送數據。
//上游 emit(1) → 下游 mapLatest { 處理 1 }  → 新數據 emit(2) 來了
//                          ↓
//                 mapLatest 取消 1，開始處理 2
//                          ↓
//                 emit(3) 來了，取消 2，開始處理 3