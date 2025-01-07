package com.rengwuxian.coursecoroutines._2_structured_concurrency

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.suspendCoroutine

fun main() = runBlocking<Unit> {
  val scope = CoroutineScope(EmptyCoroutineContext) // Default
  val job = launch(Dispatchers.Default) {
    suspendCoroutine<String> {

    }
    suspendCancellableCoroutine<String> {

    }
    /*var count = 0
    while (true) {
//      ensureActive()
      if (!isActive) {
        // clear
        //thread用return corutine用拋異常（這樣才會結束子攜程 不然只會結束這個）
        //delay會自己丟 就會結束了 所以不用try catch ，如果try catch反而會遇到問題 delay不管用 因為看做已經結束但又沒丟出去 除非這裡也丟出去
        throw CancellationException()
      }
      count++
      if (count % 100_000_000 == 0) {
        println(count)
      }
      if (count % 1_000_000_000 == 0) {
        break
      }
    }*/
    // InterruptedException
    var count = 0
    while (true) {
      /*if (!isActive) {
        // Clear
        return@launch
      }*/
      println("count: ${count++}")
      try {
        delay(500)
      } /*catch (e: CancellationException) {
        println("Cancelled")
        // Clear
        throw e
      }*/ finally {
        // Clear
      }
    }
  }
  delay(1000)
  job.cancel()
  // Thread.interrupt()


  //isDeamon（corutine） > 守護現成 （沒在管會直接關閉）所以runblocking 裡面不要解綁父子攜程不然可能會被關 在裡面開thread反而不會
// thread不會（用戶現成）
}