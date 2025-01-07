package com.rengwuxian.coursecoroutines._4_flow

import com.rengwuxian.coursecoroutines.common.gitHub
import com.rengwuxian.coursecoroutines.common.unstableGitHub
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeoutException
import kotlin.coroutines.EmptyCoroutineContext

fun main() = runBlocking<Unit> {
  val scope = CoroutineScope(EmptyCoroutineContext)


  val flow3=  flow {

    try {
      emit(1)
      emit(2)
      emit(3)
    }catch (ex:Exception){
      println("emit value: $ex")

    }


  }.map { value ->
    println("Mapping value: $value")
    if (value == 2) throw Exception("Error in map for value: $value")
    value * 2
  }
  //public inline fun <T, R> Flow<T>.map(crossinline transform: suspend (value: T) -> R): Flow<R> = transform { value ->
  //    return@transform emit(transform(value))
  //}
    //emit 並沒有機會執行，因為異常已經打斷了 map 操作的執行流程。 所以要在更上層才行
  //所以下游的異常collect.{//這是下游其實是實現emit interface}會往上拋不應該catch任何emit（因為會攔截） 要的話也是要原地丟出來 不然上游很難維護
  //map的{}沒有emit transform才有


  scope.launch {

    flow3.collect { value ->
      try {
        println("Collected value: $value")
      }catch (Ex:Exception){
        println("Collected value: $Ex")

      }
    }

  }


  val flow2 = flow {
    try {
      for (i in 1..5) {
        // 数据库读数据
        // 网络请求
        //
        //2.等於是往下呼叫emit 所以在emit的內的錯誤 這裏可以catch到
        emit(i)
      }
    } catch (e: Exception) {
      println("Error in flow(): $e")
      throw e
    }
  }.map {
    it+1
  }
  scope.launch {
    //Q:如果調用map？
    //public inline fun <T, R> Flow<T>.map(crossinline transform: suspend (value: T) -> R): Flow<R> = transform { value ->
    //    return@transform emit(transform(value))
    //}

    //1.順序 => collect(他是啟動最下面的) -> map內的emit(但是不是在括號) 所以map{}收集不到異常 但transform如果自己寫可以收集到(因為emit自己呼叫)
    // -> 上層的emit(ex:flow emit) <到頂了> ->上層emit往下到map emit 再到flowcollector的{}
    //但是map emit是被包起來的所以收不到
    //異常是會照調用順序一步步網外拋的 像是fun1(func2) func2(func3) 會從3->2->1
    //所以是先collect 啟動flow 再從flow emit到collect的括號 但是這樣有個問題 exception transparency 就是上游emit的try catch 其實只要包住生產而已
    //(emit之前可能去撈數據庫之類的） 但是他現在變成下游的異常也會丟去上游（因為其實是呼叫emit在下由（{}) 就變成寫生產的人可能不知道有啥問題很難維護
    //3.在collect try catch 是因為內部沒有被catch所以會往上丟
    //所以變成try catch 不應該包住emit 或是包住emit exception應該也要往外丟
    try {
      flow2.collect(object :FlowCollector<Int>{
        override suspend fun emit(value: Int) {

            //1.在括號在try catch 能夠catch住
            val contributors = gitHub.contributors("square", "retrofit")
            println("Contributors: $contributors")

        }

      })
    } catch (e: TimeoutException) {
      println("Network error: $e")
    } catch (e: NullPointerException) {
      println("Null data: $e")
    }
  }
//  val flow1 = flow {
//    try {
//      for (i in 1..5) {
//        // 数据库读数据
//        // 网络请求
//        emit(i)
//      }
//    } catch (e: Exception) {
//      println("Error in flow(): $e")
//      throw e
//    }
//  }.map { throw NullPointerException() }
//    .onEach { throw NullPointerException() }
//    .transform<Int, Int> {
//      val data = it * 2
//      emit(data)
//      emit(data)
//    }
//  // Exception Transparency
//  scope.launch {
//    try {
//      flow1.collect {
//        val contributors = unstableGitHub.contributors("square", "retrofit")
//        println("Contributors: $contributors")
//      }
//    } catch (e: TimeoutException) {
//      println("Network error: $e")
//    } catch (e: NullPointerException) {
//      println("Null data: $e")
//    }
//  }
  delay(10000)
}
//collect

private fun fun1() {
  fun2()
}

private fun fun2() {
  fun3()
}

private fun fun3() {
  throw NullPointerException("User null")
}
