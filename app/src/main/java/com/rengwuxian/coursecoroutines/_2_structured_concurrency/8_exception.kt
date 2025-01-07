package com.rengwuxian.coursecoroutines._2_structured_concurrency

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.concurrent.thread
import kotlin.coroutines.EmptyCoroutineContext

fun main() = runBlocking<Unit> {
  val scope = CoroutineScope(EmptyCoroutineContext)
  var childJob: Job? = null
  val parentJob = scope.launch {
    childJob = launch {
      launch {
        println("Grand child started")
        delay(3000)
        println("Grand child done")
      }
      delay(1000)
      throw IllegalStateException("User invalid!")
    }
    println("Parent started")
    delay(3000)
    println("Parent done")
  }
  delay(500)
//  CoroutineExceptionHandler
//  parentJob.cancel()
//  println("isActive: parent - ${parentJob.isActive}, child - ${childJob?.isActive}")
//  println("isCancelled: parent - ${parentJob.isCancelled}, child - ${childJob?.isCancelled}")
//  delay(1000)
//  println("isActive: parent - ${parentJob.isActive}, child - ${childJob?.isActive}")
//  println("isCancelled: parent - ${parentJob.isCancelled}, child - ${childJob?.isCancelled}")
  delay(10000)
}

//異常跟取消走的在底層是同流程 都是會改isactive = false iscancel=true
//只是取消只會向內（子） 異常取消會向外（父） 而父取消也會向內走 所以就會全取消
//去看jobsupport job都是繼承這個
//如果是ｃａｎｃｅｌ 就不叫cancelImpl（取消父自己） 其他會取消
//   public open fun childCancelled(cause: Throwable): Boolean {
//        if (cause is CancellationException) return true
//        return cancelImpl(cause) && handlesException
//    }
// 如果不希望子拋異常 父被影響可用ｓｕｐｅｒｖｉｓｅｒｊｏｂ
//取消流程可以從內部丟 外部呼叫（cancel) 異常只能內部丟
//3.取消exception到不了線程世界，異常exception可以到（所以會閃退） 可以用handelr去攔截


//父子可以想像成大流程 父跟子是並行的
// 大流程取消子也沒意義 所以父取消子也應該取消 大流程應該等子流程全完成
// 而子取消不會影響父 但子壞掉父（大流程）也應該有問題了
//父子流程是並行的：
//
//父與子可以同時執行，互不干擾。
//父流程不一定要等子流程完成，才繼續自己的邏輯。
//父流程取消子流程：
//
//父流程一旦取消，子流程也應該隨之取消，因為大流程已經失去意義。
//父流程應該等子流程完成：
//
//父流程只有在子流程全部完成後，才能真正結束，這樣才能保證邏輯完整性。
//子流程取消對父流程的影響：
//
//子流程被取消不應影響父流程的執行。
//但如果子流程“壞掉”（出錯或異常），父流程應該停止，因為子流程的成功是父流程成功的基礎。


//場景 1: 電商訂單處理
//描述
//在電商系統中，客戶下單後會觸發一個處理流程（父流程）。這個流程包含幾個子流程：
//
//庫存檢查（子流程 1）：確認庫存是否充足。
//支付處理（子流程 2）：處理客戶的支付。
//物流生成（子流程 3）：生成物流訂單並通知倉庫。
//邏輯分析
//父子流程是並行的：
//
//父流程可以同時啟動庫存檢查和支付處理，兩者是互相獨立的，可以並行執行。
//當庫存檢查完成後，父流程可以開始物流生成，而支付處理的結果對物流生成也至關重要。
//父流程取消子流程：
//
//如果父流程被取消，例如客戶取消訂單，子流程應該立刻終止。此時，不再需要檢查庫存或進行支付處理。
//父流程應該等子流程完成：
//
//庫存檢查和支付處理都必須成功，父流程才能進一步生成物流信息。
//如果子流程之一失敗（例如支付處理失敗），父流程應該結束並通知客戶支付失敗。
//子流程取消對父流程的影響：
//
//如果庫存檢查發現商品已售罄（子流程失敗），父流程應該中斷，通知客戶訂單無法完成。
//但如果物流生成出現問題（例如倉庫系統延遲），可以允許父流程先完成主要部分，再處理後續問題。


//取消是不可阻止的
//取消狀態會影響所有掛起函數，掛起函數檢測到取消後會自動停止執行。
//為什麼取消不可阻止？
//底層設計：協程取消是狀態驅動的
//
//當協程取消時，內部狀態 isCancelled = true，並且所有掛起函數（如 delay 或 yield）會檢查這個狀態。
//這些掛起函數在檢測到取消時會拋出 CancellationException，從而停止協程的執行。
//協程的「取消語義」
//
//協程的取消語義設計為「一旦取消，整個協程範圍必須停止」。
//即使捕獲了 CancellationException，協程仍然處於取消狀態，無法恢復。
//強制執行的狀態變更
//
//Job 的狀態是由 cancelImpl 強制設置的，這個變更是不可撤銷的。