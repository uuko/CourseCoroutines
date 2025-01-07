package com.rengwuxian.coursecoroutines._4_flow

import com.rengwuxian.coursecoroutines.common.unstableGitHub
import io.reactivex.rxjava3.internal.util.NotificationLite.getValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeoutException
import kotlin.coroutines.EmptyCoroutineContext

fun main() = runBlocking<Unit> {
  val scope = CoroutineScope(EmptyCoroutineContext)

//  val myFlow = flow {
//    emit(1) // 這裡調用的是 FlowCollector.emit
//    //如果 emit(getNum()) 中的 getNum() 發生異常，emit 不會執行，異常會直接傳播，並且整個 Flow 的執行會中斷。
////    emit(getNum())
////    throw Exception("aaa")
//  }.map {
//    //裡面try catch沒辦法拿上面的因為這只是轉換方法（transform） ＝  return@transform emit(transform(value))
//    throw Exception("ccc")
//  }
//  scope.launch {
//    try {
//      myFlow.collect(object :FlowCollector<Int>{
//        override suspend fun emit(value: Int) {
//          try {
//
//          }
//          catch (ex:Exception){
//            println("ex: $ex")
//          }
//        }
//      })
//    }catch (ex:Exception){
//      println("out ex: $ex")
//
//    }

    //這括號只能收emit collectSafely=collect 其實他裡面有一層呼叫block

    //private class SafeFlow<T>(private val block: suspend FlowCollector<T>.() -> Unit) : AbstractFlow<T>() {
    //    override suspend fun collectSafely(collector: FlowCollector<T>) {
    //        collector.block()
    //    }
    //}
//  }


  val flow1 = flow {
    for (i in 1..5) {
      // 数据库读数据
      // 网络请求
      if (i == 3) {
        throw RuntimeException("flow() error")
      } else {
        emit(i)
      }
    }
  }.catch {
    println("catch(): $it")
    emit(100)
    emit(200)
    emit(300)
//    throw RuntimeException("Exception from catch()")
  }/*.onEach { throw RuntimeException("Exception from onEach()") }
    .catch { println("catch() 2: $it") }*/
  scope.launch {
    try {
      flow1.collect {
        /*val contributors = unstableGitHub.contributors("square", "retrofit")
        println("Contributors: $contributors")*/
        println("Data: $it")
      }
    } catch (e: TimeoutException) {
      println("Network error: $e")
    }
  }
  delay(10000)
}

fun getNum(): Int {
throw Exception("bbb")
}
//catch就像是用try catch去包住上游的所有代碼塊 但是emit的異常會丟出去 讓下游自己處理
//不會補住emit操作符的異常會抓其他異常 =不會補貨下游異常的catch 也不會catch cancelexecption 因為這是要取消協程用的
//=不補下游異常 跟cancelexcpiton 只補上游異常的try catch
//如果在兩個catch中間就是補貨他上層的 // catch A catch 第二個就是拿到catch1 + A異常
// 因為emit = collect{//這塊其實是省略她interface}
//collect(object :FlowCollector<Int>{
//        override suspend fun emit(value: Int) {
//
//
//        }

//public fun <T> Flow<T>.catch(action: suspend FlowCollector<T>.(cause: Throwable) -> Unit): Flow<T> =
//    flow {
//        val exception = catchImpl(this)
//        if (exception != null) action(exception)
//    }

//flow是大管道 所以他會生產數據往下流 如果生產就壞了 就會下面管道全壞了（code不能向下執行） 最後丟到collect
//catch = 在上層try catch
//catch 能恢復上層生產嗎？ 沒辦法 他只是接管 然後重送 （因為catch也有receiver 可以emit）
//下游不會知道什麼回事 所以catch就是在上游死的時候去接管發送數據的操作符
//try catch是在flow裏 catch在flow外 trycatch還有機會拯救這個flow讓生產繼續 而catch只能接管道上面flow其實已經死了
//能選ｔｒｙ catch就選 因為還有機會拯救 會用catch是你用別人的flow 你可能看不懂他的原始碼 在他裡面catch很危險或是要很多人用
//然後通常在catch只能做收尾了不然上面幹嘛用的 通常上面也不能貼上
//要問emit 2 try 為啥3還是死了

