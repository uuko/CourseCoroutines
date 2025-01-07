package com.rengwuxian.coursecoroutines._3_scope_context

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.EmptyCoroutineContext

@OptIn(DelicateCoroutinesApi::class)
fun main() = runBlocking<Unit> {
//  val scope =  CoroutineScope(EmptyCoroutineContext) 還是有job的
  val scope2 =  CoroutineScope(Dispatchers.Default)

//  val scope3 =  GlobalScope.launch {
//    println("scope3 parent: ${this.coroutineContext[Job]?.parent}")
//
//  }
  GlobalScope.launch {

  }
//  GlobalScope.cancel()
  val job = GlobalScope.launch() {
    delay(1000)

    println("JJJJ parent: ${this.coroutineContext[Job]}")

    launch() {
      try {
        delay(2000)
      }catch (e:CancellationException){
        println("AAAAA parent: ${this.coroutineContext[Job]?.parent}")
        throw  e

      }
      println("BBB parent: ${this.coroutineContext[Job]?.parent}")


    }
  }
  delay(1000)
  job.cancel()
//  scope2.cancel()
  //因為GlobalScope沒job 所以他啟動的攜程也沒父job 所以他取消得時候子攜程也取消不了
  //當一個 Job 完成後，其 parent 屬性將變為 null
  println("job parent: ${job}")
  delay(5000)
  //scope作用啟動協程 + 提供協程上下文
  //gloablscope沒有job 他的corutinecontext是empty
// 其他像是lauch有job 在外面創的corutinescope也有job
//@Suppress("FunctionName")
//public fun CoroutineScope(context: CoroutineContext): CoroutineScope =
//    ContextScope(if (context[Job] != null) context else context + Job())
}