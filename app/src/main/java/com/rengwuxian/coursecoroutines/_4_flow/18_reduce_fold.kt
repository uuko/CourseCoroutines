package com.rengwuxian.coursecoroutines._4_flow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.flow.reduce
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.runningReduce
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.EmptyCoroutineContext

fun main() = runBlocking<Unit> {
  val scope = CoroutineScope(EmptyCoroutineContext)
  val flow1 = flowOf(1, 2, 3, 4, 5)
  val list = listOf(1, 2, 3, 4, 5)
  list.reduce { acc, i -> acc + i }.let { println("List reduced to $it") }
  list.runningReduce { acc, i -> acc + i }.let { println("New List: $it") }
  list.fold(10) { acc, i -> acc + i }.let { println("List folded to $it") }
  list.fold("ha") { acc, i -> "$acc - $i" }.let { println("List folded to string: $it") }
  list.runningFold("ha") { acc, i -> "$acc - $i" }.let { println("New String List: $it") }
  scope.launch {
    val sum = flow1.reduce { accumulator, value -> accumulator + value }
    println("Sum is $sum")
    flow1.runningReduce { accumulator, value -> accumulator + value }
      .collect { println("runningReduce: $it") }
    flow1.fold("ha") { acc, i -> "$acc - $i" }.let { println("Flow folded to string: $it") }
    flow1.runningFold("ha") { acc, i -> "$acc - $i" }
      .collect { println("Flow.runningFold(): $it") }
    flow1.scan("ha") { acc, i -> "$acc - $i" }
      .collect { println("Flow.scan(): $it") }
  }
  delay(10000)
}
//reduce就是 acc,i acc是上次計算後的結果 如果是一開始的話acc=第一個元素 i=第二個
//flow reduce會啟動collect把所有的直收集（會啟動收集過程） 他返回不是flow對象而是計算後結果
//因為啟動flow收集過程所以一定是個掛起函數要在協城裏
//啟動flow並對每個flow數據進行運算 並在flow數據計算後返回最終結果 他是個terminal operator

//runningReduce 每個直都是上一輪計算出來的 1, 2, 3 =>1,3,6 running返回的是flow
//flow 的版本的回傳也是flow 但可以定義初始值 然後初始值的類型跟最後返回的類型一樣