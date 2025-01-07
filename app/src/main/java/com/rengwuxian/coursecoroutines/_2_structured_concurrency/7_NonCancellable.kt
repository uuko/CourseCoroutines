package com.rengwuxian.coursecoroutines._2_structured_concurrency

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.coroutines.EmptyCoroutineContext

fun main() = runBlocking<Unit> {
  val scope = CoroutineScope(EmptyCoroutineContext)
  var childJob: Job? = null
  var childJob2: Job? = null
  val newParent = Job()
  val parentJob = scope.launch {
    childJob = launch(NonCancellable) {
      println("Child started")
      delay(1000)
      println("Child stopped")
    }
    println("childJob parent: ${childJob?.parent}")
    childJob2 = launch(newParent) {
      println("Child started")
      writeInfo()
      launch(NonCancellable) {
        // Log
      }
      if (!isActive) {

        withContext(NonCancellable) {
          // Write to database (Room)
          delay(1000)
        }
        throw CancellationException()
      }
      try {
        delay(3000)
      } catch (e: CancellationException) {

        throw e
      }
      println("Child 2 started")
      delay(3000)
      println("Child 2 stopped")
    }
    println("Parent started")
    delay(3000)
    println("Parent stopped")
  }
  delay(1500)
  newParent.cancel()
  delay(10000)
}
//corutine 父子是並行的
//NonCancellable 是job但他把children都是空的 所以他其實只是阻斷父子關係 但他也沒辦法取消（因為他沒綁子攜程）
//通常是在
// 1.清理工作做 不然清理工作如果有用到delay會丟exception不能往下
//2.不能被取消的事情（像是寫文件） 讀room/delay=>這裏會自動檢查 自己寫的除非去檢查才會
//3. log
//suspend fun performTask() {
//    try {
//        writeInfo() // 此处确保不会因取消中断
//        // writeInfo 完成后，继续处理后续逻辑
//        if (!isActive) {
//            // 如果父协程取消，此处可检测并优雅终止
//            return
//        }
//        processData() // 其他处理逻辑
//    } catch (e: Exception) {
//        // 处理可能的异常（比如文件写入错误）
//    }
//}
suspend fun writeInfo() = withContext(Dispatchers.IO + NonCancellable) {
  // write to file
  // read from database (Room)
  // write data to file
}

suspend fun uselessSuspendFun() {
  Thread.sleep(1000)
}