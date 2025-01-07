package com.rengwuxian.coursecoroutines._2_structured_concurrency

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.concurrent.thread
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.EmptyCoroutineContext

fun main() = runBlocking<Unit> {
  val scope = CoroutineScope(Dispatchers.IO)
  var innerJob: Job? = null
  var doubleJob: Job? = null
  var innerScope: CoroutineScope? = null
  val outerJob = scope.launch(Dispatchers.Default) {
    innerJob = coroutineContext[Job]
    innerScope = this
    doubleJob= launch {

    }
  }
  outerJob.cancel()

  scope.async {

  }
  println("outerJob: $outerJob")
  println("innerJob: $innerJob")
  println("doubleJob: $doubleJob")

  println("outerJob === innerJob: ${outerJob === innerJob}")
  println("outerJob === innerScope: ${outerJob === innerScope}")
  println("doubleJob === innerJob: ${doubleJob === innerJob}")

  //job 是控管流程，可以管是開始/結束/狀態 父子攜程
//  innerJob?.start()
//  innerJob?.cancel()
//
//  innerJob?.isActive
//  innerJob?.parent


//  CoroutineScope是最上層管理器每個都有coroutineContext（是大總管）
//  coroutineContext 是這個scope的上下文 可以拿到dispather
//  CoroutineDispatcher 跟一些名稱 （Job)等等的

//  scope.launch(Dispatchers.Default) {
////    this 這個this 跟 返回的job是同對象 只是job主要管流程，為了api乾淨，所以this跟job都可被看作攜程
//    //也可以看成一個大括號就是一個攜程
//    }

}