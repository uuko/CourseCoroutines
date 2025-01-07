package com.rengwuxian.coursecoroutines._4_flow

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.plus
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.EmptyCoroutineContext

fun main() = runBlocking<Unit> {
  val scope = CoroutineScope(EmptyCoroutineContext)
  val flow1 = flow {
    println("CoroutineContext in flow(): ${currentCoroutineContext()}")
    for (i in 1..5) {
      emit(i)
    }
  }.map {
    println("CoroutineContext in map() 1: ${currentCoroutineContext()}")
    it * 2
  }.flowOn(Dispatchers.IO).flowOn(Dispatchers.Default)
    .map {
      println("CoroutineContext in map() 2: ${currentCoroutineContext()}")
      it * 2
    }.flowOn(newFixedThreadPoolContext(2, "TestPool"))
  val flow2 = channelFlow {
    println("CoroutineContext in channelFlow(): ${currentCoroutineContext()}")
    for (i in 1..5) {
      send(i)
    }
  }.map { it }.flowOn(Dispatchers.IO)
  scope.launch {
    /*flow1.map {
      it + 1
    }.onEach {
      println("Data: $it - ${currentCoroutineContext()}")
    }.flowOn(Dispatchers.IO)
      .collect {}*/
    flow2.collect()
  }
  /*flow1.map {
    it + 1
  }.onEach {
    println("Data: $it - ${currentCoroutineContext()}")
  }.launchIn(scope + Dispatchers.IO)*/
  delay(10000)
}

//flowon跟catch一樣只管上游 他是制定corutincontext的
//collect也是算下游
//因為維護的人不會知道上游在幹嘛所以這麼設計
//假設會影響這樣前面上游用flowon 我在collect的時候要一直想前面在幹嘛 不合邏輯

//flow= flow(FlowCollector.()->Unit)
//如果用withcontext包著emit 會導致下游collect也是withcontext切過去的
//因為：collect其實是實作emit interface 所以collect的括號其實是執行上游emit執行點
//所以這裏emit就會影響collect的{}在哪執行
//變成dataprocess原本在collect的協程運行可以切去io再切回來
//然後發送1
//emit切去io 變成collect{}括號在io 開發者很難知道 這樣就有問題
//flow {
//dataprocess()//切去io
//    emit(1) // 預設上下文
//    withContext(Dispatchers.IO) {
//        emit(2) // 切換到 IO 上下文
//    }
//}.collect { value ->
//    println("Collected $value in thread: ${Thread.currentThread().name}")
//}
//collector.block() 是執行整個flow{}括號內容 所以try catch要包在collect外 裡面只是收到emit後在做什麼
//// flow2.collect(object :FlowCollector<Int>{
////        override suspend fun emit(value: Int) {
////
////            //1.在括號在try catch 能夠catch住
////            val contributors = gitHub.contributors("square", "retrofit")
////            println("Contributors: $contributors")
////
////        }
////
////      })
//    public final override suspend fun collect(collector: FlowCollector<T>) {
//        val safeCollector = SafeCollector(collector, coroutineContext)
//        try {
//            collectSafely(safeCollector)
//        } finally {
//            safeCollector.releaseIntercepted()
//        }
//    }
//private class SafeFlow<T>(private val block: suspend FlowCollector<T>.() -> Unit) : AbstractFlow<T>() {
//    override suspend fun collectSafely(collector: FlowCollector<T>) {
//        collector.block()
//    }
//}


//所以flow on 跟 withcontext 是各有各的用法但withcontext不能包emit會報錯
//通常如果是小塊的內容就用withcontext(在flow裡面）
//然後flow on可用範圍是在兩個flow on之間

/*flow1.map {
     it + 1
   }.onEach {
     println("Data: $it - ${currentCoroutineContext()}")
   }.flowOn(Dispatchers.IO).collect{}

   也可以這樣寫這樣可以控制 或是在collecy外切dispather
   也可以用launchIn
 */

//flowon是右往左＋ 所以是右邊的會被蓋掉
//channelflow = flowon