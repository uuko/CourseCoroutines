package com.rengwuxian.coursecoroutines._2_structured_concurrency

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.measureTime

fun main() = runBlocking<Unit> {
  val scope = CoroutineScope(EmptyCoroutineContext)
  val parentJob = scope.launch {
    val childJob = launch {
      println("Child job started")

        delay(3000)


      println("Child job finished")
    }
  }
  delay(1000)
  parentJob.cancel()
  //join 要做完攜程才會結束
  measureTime { parentJob.join() }.also { println("Duration: $it") }
  delay(10000)
}
//步驟
//外部呼叫cancel 或自己丟cancelexception
//1. isactive變成false
//2. 調用所有子攜程cancel
//3. 檢查isactive(到檢查點） 然後丟異常cancelexception
//所以每個檢查點都不同 不能知道順序
//子攜程只能在檢查點去拒絕 這樣就會繼續運行 但有風險 即使這樣isactive仍然是變了因為步驟1先做
//而且這樣有風險在所有suspend func(delay)都要這樣呼叫 而且父攜程關不掉（因為是結構式）
//      try {
//        delay(3000)
//
//      }catch (ex:CancellationException){
//
//      }