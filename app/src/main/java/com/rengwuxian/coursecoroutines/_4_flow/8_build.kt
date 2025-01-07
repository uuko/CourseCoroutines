package com.rengwuxian.coursecoroutines._4_flow

import com.rengwuxian.coursecoroutines.common.Contributor
import com.rengwuxian.coursecoroutines.common.gitHub
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.cancellation.CancellationException

fun main() = runBlocking<Unit> {
  val flow1 = flowOf(1, 2, 3)
  val flow2 = listOf(1, 2, 3).asFlow()
  val flow3 = setOf(1, 2, 3).asFlow()
  val flow4 = sequenceOf(1, 2, 3).asFlow()
  val channel = Channel<Int>()
  val flow5 = channel.consumeAsFlow()//只能呼叫一次不然會丟ex 因為channel本來是只能消費一次
  val flow6 = channel.receiveAsFlow()
  //跟上面不同他是監聽後才創channel 所以每個collect都不同channel
  val flow7 = channelFlow {
    launch {
      delay(2000)
      send(2)
    }
    delay(1000)
    send(1)
  }
  val flow8 = flow {
    launch {
      delay(2000)
      emit(2)
    }
    delay(1000)
    emit(1)
  }
  val flow9 = callbackFlow {
    gitHub.contributorsCall("square", "retrofit")
      .enqueue(object : Callback<List<Contributor>> {
        override fun onResponse(call: Call<List<Contributor>>, response: Response<List<Contributor>>) {
          trySend(response.body()!!)
          close()
        }

        override fun onFailure(call: Call<List<Contributor>>, error: Throwable) {
          cancel(CancellationException(error))
        }
      })
    awaitClose()
  }
//  suspendCancellableCoroutine<> {  }
  val scope = CoroutineScope(EmptyCoroutineContext)
  scope.launch {
    flow9.collect {
      println("channelFlow with callback: $it")
    }
    /*flow8.collect {
      println("channelFlow: $it")
    }*/
    /*flow5.collect {
      println("Flow6 - 1: $it")
    }*/
  }
  scope.launch {
    /*flow5.collect {
      println("Flow6 - 2: $it")
    }*/
  }
  /*channel.send(1)
  channel.send(2)
  channel.send(3)
  channel.send(4)*/

//  scope.launch {
//    callbackFlowCounter().collect { println("Collected: $it") }
//
//  }
//  scope.launch {
//    callbackFlowCounter().collect { println("Collected22: $it") }
//
//  }

  delay(10000)
}
//channelasFlow 上游還是channel所以資料會被瓜分 send(1) 2,3,4 collect是會分別拿到 1,2 /3,4 不會拿到1234因為上游是channel
//channelflow 就是上層是channel但是每次都會產生channel 所以跟flow一樣
//channelflow 裡面可以開子協程 flow不可以

//channelflow裡面因為是協程所以不會等callback除非你叫awaitclose 那callbackflow = channelflow ＋ awaitclose版
//callbackFlow 是一個 Flow 構建器，它的目標是將基於回呼的 事件流 轉換成 Flow。它內部使用一個 Channel 來緩衝回呼產生的值，並建立一個 Flow
// ，該 Flow 會從 Channel 中收集這些值並發射出去。callbackFlow 旨在處理 多次 或 連續 的回呼


// 如果一次的話用suspendcancelcrorutine就可以了 是一個 掛起函數，它將一個 單次 的回呼 API 轉換為協程。它的本質是：
//它會暫停目前的協程，直到 單次 回呼發生並恢復協程的執行。一旦回呼發生並使用 resume 或 resumeWithException 恢復協程後，suspendCancellableCoroutine 就完成了它的使命， 不會再被使用

//flow { ... } 構建器設計用於 同步 地發射值。它不適合直接處理 非同步 的回呼，原因如下：
//
//沒有內建的機制來處理回呼的生命週期： Flow 本身沒有提供任何機制來追蹤或取消註冊回呼。因此，如果在 flow { ... } 區塊內直接使用回呼，很容易導致資源洩漏。
//難以處理錯誤和取消： 處理回呼中的錯誤和取消通常比較複雜，需要在回呼函數內部進行特殊的處理。Flow 的 flow { ... } 構建器沒有提供方便的機制來處理這些情況。
//程式碼可讀性差： 在 flow { ... } 區塊內混合同步和非同步程式碼會導致程式碼難以理解和維護。


//理解 suspendCancellableCoroutine 和 callbackFlow 的本質區別。簡單來說，前者用於「一次性取得結果」，後者用於「持續接收事件」。


//suspendCancellableCoroutine 是只有一次 而flow是可以一直發送的
//callbackflow要用send到channel
//callbackFlow 是＋上awaitclose 他是除非flow區塊整個執行完 或是collect的協程被取消 就會觸發 這時候通常是清理listener
//所以順序應該是 callbackflow{ listener(senddata()) awaitclose(clearListener)}
// launch{ collect() }  lisener.sendXXX()
//順序是collect後打開flow 然後flow被awaitclose卡住所以會一直間聽 lisener send後會給flow 到collect
//直到監聽者取消

//channelflow主要是可以開子協程


//flow { ... } 區塊的設計是 協同程式安全 (coroutine-safe) 的
// ，但它本身 並不提供任何協程的上下文 (coroutine context)。
// 這意味著你 不能 在 flow { ... } 區塊內部直接使用像 launch、async
// 或 withContext 這樣的協程建構器。

//callbackFlow { ... } 提供了 協程的上下文，因此你
// 可以 在其內部使用 launch、async 等協程建構器。這使得
// callbackFlow 能夠處理需要 長時間運行 或需要 並行執行 的非同步操作，例如：
//
//監聽網路請求
//處理 UI 事件
//監聽感測器數據
//在 callbackFlow 中啟動的協程，其生命週期會與 callbackFlow 的生命週期綁定。當 callbackFlow 結束 (通常是因為收集器取消) 時，這些在內部啟動的協程也會被取消，確保資源的釋放。
fun callbackFlowCounter(): Flow<Int> = callbackFlow {
  println("callbackFlowCounter started") // 只會印出一次
  var counter = 0
  val job = launch {
    while(true){
      delay(500)
      trySendBlocking(++counter)
    }
  }

  awaitClose {
    println("callbackFlowCounter closed") // 只有在 collect 結束時才會印出
    job.cancel()
  }
}
