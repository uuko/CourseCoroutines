package com.rengwuxian.coursecoroutines._2_structured_concurrency

import kotlinx.coroutines.runBlocking
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread

fun main() = runBlocking<Unit> {
  val thread = object : Thread() {
    override fun run() {
      println("Thread: I'm running!")
      try {
        Thread.sleep(200)
      } catch (e: InterruptedException) {
        println("isInterrupted: $isInterrupted")
        println("Clearing ...")
        return
      }
      val lock = Object()
      try {
        lock.wait()
      } catch (e: InterruptedException) {
        //這裏會給false
        println("isInterrupted: $isInterrupted")
        println("Clearing ...")
        return
      }
      val newThread = thread {

      }
      newThread.join()
      val latch = CountDownLatch(3)
      latch.await()
      /*var count = 0
      while (true) {
        if (isInterrupted) {
          // clear
          return
        }
        count++
        if (count % 100_000_000 == 0) {
          println(count)
        }
        if (count % 1_000_000_000 == 0) {
          break
        }
      }*/
      println("Thread: I'm done!")
    }
  }.apply { start() }
  Thread.sleep(100)
  thread.interrupt()

  //交互式結束 外部調用 然後通知內部（isInterrupted）．在耗時工作前檢查
  //因為在結束線程就是為了節省資源（而且需要clear該清理的變數）
  //像是加濾鏡，濾鏡thread加一半被中斷應該要考慮濾鏡圖片是否要還原
  //interrupted()第一次會給ｔｒｕｅ 第二次給false
  //在thread內部結束就用return就好了（run方法就會結束了）
  //有等待性質的都是這樣
  //sleep配合比較高級，sleep是會丟exception(中斷，此時應該return+清理)，因為他只是等待所以可以被打斷，自己寫得那些是要自己檢查
  //如果緊急需要結束？？stop?? 不存在 因為結束thread是要節省資源 thread是自己寫的

}