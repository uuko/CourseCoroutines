package com.rengwuxian.coursecoroutines._2_structured_concurrency

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.EmptyCoroutineContext

@OptIn(ExperimentalCoroutinesApi::class)
fun main() = runBlocking<Unit> {
  //這裏有自帶job
  val scope = CoroutineScope(EmptyCoroutineContext)
  println("${scope.coroutineContext[Job]}")
  val initJob = scope.launch {
    launch {  }
    launch {  }
  }
  //true
  println("${initJob.parent===scope.coroutineContext[Job]}")

  //父攜程會等子攜程玩在全結束
  scope.launch {
    //會等初始化攜程結束後再結束
    initJob.join()
    // ???
  }
  var innerJob: Job? = null
  val job = scope.launch {
    launch(Job()) {
      delay(100)
    }
    val customJob = Job()
    innerJob = launch(customJob) {
      delay(100)
    }
  }
  val startTime = System.currentTimeMillis()
  job.join()
  val duration = System.currentTimeMillis() - startTime
  println("duration: $duration")

  //父子都是ｔｒｕｅ
//  var innerAJob: Job? = null
//  val jobA = scope.launch {
//    innerAJob = launch {
//      delay(100)
//    }
//  }
//  val children = jobA.children
//  println("children count: ${children.count()}")
//  println("innerJob === children.first(): ${innerAJob === children.first()}")
//  println("innerJob.parent === job: ${innerAJob?.parent === jobA}")

  var innerBJob: Job? = null
  val jobB = scope.launch {
    innerBJob = scope.launch {
      delay(100)
    }
  }
  val children = jobB.children
  println("innerJob.parent === job: ${innerBJob === jobB}")
  println("innerJob.parent === job: ${innerBJob?.parent === jobB.parent}")

//  println("children count: ${children.count()}")
//  println("innerJob === children.first(): ${innerBJob === children.first()}")
//  println("innerJob.parent === job: ${innerBJob?.parent === jobB}")
}