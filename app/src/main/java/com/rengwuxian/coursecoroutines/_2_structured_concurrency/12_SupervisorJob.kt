package com.rengwuxian.coursecoroutines._2_structured_concurrency

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.coroutines.EmptyCoroutineContext

fun main() = runBlocking<Unit> {
  val scope = CoroutineScope(SupervisorJob())
  val supervisorJob = SupervisorJob()
  val job = Job()
  scope.launch {
    val handler = CoroutineExceptionHandler { _, exception ->
      println("Caught in handler: $exception")
    }
    launch(SupervisorJob(coroutineContext.job) + handler) {
      launch {
        throw RuntimeException("Error!")
      }
    }
  }
  delay(1000)
  println("Parent Job cancelled: ${job.isCancelled}")
  delay(10000)
}
//    override fun childCancelled(cause: Throwable): Boolean = false
//重點是這行可以看到他不會因為子job丟的是exception就取消，所以他對子corutine丟異常沒用父不會被取消
//然後他會被當被丟到線程世界的job 所以handelr要加在那層
//然後async一樣是沒用的

//這是正常job
//   public open fun childCancelled(cause: Throwable): Boolean {
//        if (cause is CancellationException) return true
//        return cancelImpl(cause) && handlesException
//    }