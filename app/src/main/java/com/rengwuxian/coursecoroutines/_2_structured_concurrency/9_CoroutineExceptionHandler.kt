package com.rengwuxian.coursecoroutines._2_structured_concurrency

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.EmptyCoroutineContext

fun main() = runBlocking<Unit> {
  val scope = CoroutineScope(EmptyCoroutineContext)
  val handler = CoroutineExceptionHandler { _, exception ->
    println("Caught $exception")
  }
  scope.launch(handler) {
    launch {
      throw RuntimeException("Error!")
    }
    launch {

    }
  }

  delay(10000)

  //handler 只有在最外層corutine才有用
//  這段程式碼不會捕捉到 RuntimeException，
//  因為 try-catch 區塊只會捕捉當前執行緒中的例外，
//  而 thread 創建了一個新的執行緒，例外發生在該執行緒中，因此無法被捕捉。
    //他只能監聽啟動過程 = 外層
//  try {
//      val thread = Thread{
//              throw RuntimeException("Error!")
//      }
//    thread.start()
//  }catch (  e:Exception){
//
//  }
//  try {
//    launch {
//
//      throw RuntimeException("Error!")
//
//
//    }
//  } catch (e: Exception) {
//
//  }
//=========================
//    launch {
//      try {
//        throw RuntimeException("Error!")
//
//      } catch (e: Exception) {
//
//      }
//  }

}