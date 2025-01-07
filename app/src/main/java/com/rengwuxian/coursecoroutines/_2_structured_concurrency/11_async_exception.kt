package com.rengwuxian.coursecoroutines._2_structured_concurrency

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.EmptyCoroutineContext

fun main() = runBlocking<Unit> {
  val scope = CoroutineScope(EmptyCoroutineContext)
  val handler = CoroutineExceptionHandler { _, exception ->
    println("Caught in Coroutine: $exception")
  }

//  scope.launch(handler) {
//        async {
//            throw RuntimeException("Error!")
//        }
//        launch {
//          try {
//              delay(100)
//          } catch (exception: Exception) {
//              println("Caught in delay: $exception")
//
//          }
//        }
//    }
  //Caught in delay: kotlinx.coroutines.JobCancellationException: Parent job is Cancelling; job=StandaloneCoroutine{Cancelling}@14155c2b
  //Caught in Coroutine: java.lang.RuntimeException: Error!
  //流程：
  //1.async 丟exception 如果scope沒handler 他會也有紅字(因為沒人捕捉）但也會取消子協程
  //2.丟到上層exception後 會去取消子協程 所以子catch到的是cancel
  //3.如果不是最上層handler將會無用，只會丟紅字
  //Caught in delay: kotlinx.coroutines.JobCancellationException: Parent job is Cancelling; job=StandaloneCoroutine{Cancelling}@6acc7e1d
  //Exception in thread "DefaultDispatcher-worker-1" java.lang.RuntimeException: Error!
  //	at


//  scope.launch(handler) {
//    val deffer = async {
//      delay(100)
//      throw RuntimeException("A Error!")
//    }
//    launch {
//      try {
//        println("Caught await:")
//        deffer.await()
//      } catch (exception: Exception) {
//        println("Caught in await: $exception")
//
//      }
//      try {
//        delay(100)
//      } catch (e: Exception) {
//        println("Caught in delay: $e")
//
//      }
//      }
//    }
  //因為如果沒print await他可能會先執行沒錯誤？
  // 然後cancel比await丟的還要久所以他會先收到await錯誤，但如果在後面有delay就會發現他其實還是會收到的只是要等所以其實流程還是會因為async錯誤到上層再到下層的


  scope.async/*(handler)*/ {
    val deferred = async {
      delay(1000)
      throw RuntimeException("Error!")
    }
    launch(Job()) {
      try {
        deferred.await()
      } catch (e: Exception) {
        println("Caught in await: $e")
      }
      try {
        delay(1000)
      } catch (e: Exception) {
        println("Caught in delay: $e")
      }
    }
    delay(100)
    cancel()
  }
  //async 在await在等的地方也會丟異常的，但他不會丟給線程世界也不會給handler(最外層是async時）
  //因為為了給await(因為await才是終點)
  delay(10000)
}