package com.rengwuxian.coursecoroutines._4_flow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.flow.timeout
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
fun main() = runBlocking<Unit> {
  val scope = CoroutineScope(EmptyCoroutineContext)
  val flow1 = flow {
    emit(0)
    delay(500)
    emit(1)
    delay(800)
    emit(2)
    delay(900)
    emit(3)
    delay(1000)
  }
//  scope.launch {
////    flow1.timeout(1.seconds).collect { println("1: $it") }
////    flow1.sample(1.seconds).collect { println("2: $it") }
//    flow1.debounce(1.seconds).collect { println("3: $it") }
//  }

  val flowt = flow<Int> {
    delay(100)
    emit(1)
    delay(200)
    emit(2)

    delay(1100)
    emit(3)

  }
  scope.launch {
    println(this.coroutineContext.job.parent)
    launch {
//      flowt.timeout(1.seconds).collect{
//        println("hihihi : $it")
//      }
      //sample是只會在掐表的時候發數據 像是1s 就是這1s內收集最後一條 那如果在掐表之前flow就做完了就沒數據了
      flowt.sample(1.seconds).collect{
        println("hihihi : $it")
        //hihihi : 2 因為發3之後就關了 沒到下個掐表
      }
    }

    //會扔timeoutCancelExc 是cancelExc的子類 所以扔的時候會往下傳給子累取消所有子累
    //只有正常的exc才會往上傳再往下傳
    //CancellationException 是一種「協程內部的通知機制」，而非真正的錯誤，所以它的傳播被限制在當前的子協程內。

    ////異常跟取消走的在底層是同流程 都是會改isactive = false iscancel=true
    ////只是取消只會向內（子） 異常取消會向外（父） 而父取消也會向內走 所以就會全取消
    ////去看jobsupport job都是繼承這個
    ////如果是ｃａｎｃｅｌ 就不叫cancelImpl（取消父自己） 其他會取消
    ////   public open fun childCancelled(cause: Throwable): Boolean {
    ////        if (cause is CancellationException) return true
    ////        return cancelImpl(cause) && handlesException
    ////    }
    launch {
      delay(1000)
      println("hihihi : end")

    }
  }
  scope.launch {

    delay(2000)
    println("hihihi : delay")


  }
  delay(10000)
}
//debouce是一直收集直到有段小間個內沒資料才丟出去 丟出去後又收集下一次 所以如果是用戶按兩次 就會要再等一小段時間才丟會感覺到延遲
//所以要用的點就是收集資料後要有一段時間冷卻再發出去 像是搜尋可能你打第一個字後 想要讓他等個一下確定字沒變化再丟
//在冷卻時間內，無論接收到多少次數據，只會發送最後一筆。 flowOf(1, 2, 3)
//    .onEach { delay(100) } // 模擬數據流的間隔
//    .debounce(200)        // 設置 200ms 的冷卻時間
//    .collect { println(it) } 這樣永遠不會收到資料因為沒到冷卻時間
//按鈕點集的去抖動是 用戶連續按兩次如果第一次跟第二次之間時間太近就丟氣第二次
fun <T> Flow<T>.throttle(timeWindow: Duration): Flow<T> = flow {
  var lastTime = 0L
  collect {
    if (System.currentTimeMillis() - lastTime > timeWindow.inWholeMilliseconds) {
      emit(it)
      lastTime = System.currentTimeMillis()
    }
  }
}