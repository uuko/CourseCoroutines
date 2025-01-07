package com.rengwuxian.coursecoroutines._2_structured_concurrency

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.system.exitProcess

fun main() = runBlocking<Unit> {
  Thread.setDefaultUncaughtExceptionHandler { t, e ->
    println("Caught default: $e")
    exitProcess(1)
  }
  /*val thread = Thread {
    try {

    } catch (e: NullPointerException) {

    }
    throw RuntimeException("Thread error!")
  }*/
  /*thread.setUncaughtExceptionHandler { t, e ->
    println("Caught $e")
  }*/
//  thread.start()
  val scope = CoroutineScope(EmptyCoroutineContext)
  val handler = CoroutineExceptionHandler { _, exception ->
    println("Caught in Coroutine: $exception")
  }
  scope.launch(handler) {
    launch {
      throw RuntimeException("Error!")
    }
    launch {

    }
  }
  delay(10000)
}
//如果可預知的異常應該寫在try catch讓他重置狀態or retry
//所以到setDefaultUncaughtExceptionHandler都是不可預期的異常了
// ，所以只能自殺（android會閃退也是這樣）+ 紀錄log 服務端應用不自殺會繼續做 整個就會很怪
//或是單一線程能做完的事情也可用讓他retry

//corutine handler也是 是針對單一個的corutine 整個應用還是要用thread，
// 通常是在收到後重啟單一corutine(因為是tree)
//是為了 善後跟未知異常 所以通常是整個corutinetree
// ，已知的異常應該要在裡面try catch，未知的才是丟到handler

//所以handler基本等於這行 /*thread.setUncaughtExceptionHandler { t, e ->
//    println("Caught $e")
//  }*/
//所以corutine異常管理跟thread其實基本是一樣的，已知的都是用try catch