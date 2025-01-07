package com.rengwuxian.coursecoroutines._2_structured_concurrency

import kotlinx.coroutines.runBlocking
import kotlin.concurrent.thread

fun main() = runBlocking<Unit> {
  val thread = thread {
    println("Thread: I'm running!")
    Thread.sleep(200)
    println("Thread: I'm done!")
  }
  Thread.sleep(100)
  thread.stop()
  //強制被終結，這樣不能判斷哪裡有問題（可能某個變數memory不前不後只能重走流程才行）
}