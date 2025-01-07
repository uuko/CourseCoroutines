package com.rengwuxian.coursecoroutines._4_flow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.coroutines.EmptyCoroutineContext

fun main() = runBlocking<Unit> {
  val list = buildList {
//    while (true) {
      add(getData())
//    }
  }
  for (num in list) {
    println("List item: $num")
  }
  val nums = sequence {
    while (true) {
      yield(1)
    }
  }.map { "number $it" }
  for (num in nums) {
    println(num)
  }

  val numsFlow = flow {
    while (true) {
      emit(getData())
    }
  }.map { "number $it" }
  val scope = CoroutineScope(EmptyCoroutineContext)
  scope.launch {
    numsFlow.collect {
      println(it)
    }
  }
  delay(10000)
}

suspend fun getData(): Int {
  return 1
}

//channel基本上沒再用因為flow底層
//flow關注的是同格攜程的數據
//sharedflow主要是關注跨攜程的

//sequence是lazy的 用到的時候才會生產
// 他其實只是記錄代碼塊（要用的時候才會去生產 用一條才生產一條）
//讓他生產過程變少減少時間 sequece可以看作生產規則 所以即使用while{yied也沒問題} 如果是list就會卡住了
//所以應用場景是那些需要一條就生產一條的 像是網路獲取數據 這樣生產時間就比較少拿到比較快（對一條來說
//但是到最後可能拿第十條的時間是一樣的 像是list一次生產時條 一次拿 seq是生產一拿一 到最後時間是一樣的 只是對於前幾條比較快
//seq 只能用自己的掛起函數（只能用yied） 因為是seqScope 不能用別人的掛起函數
//buildlist是build的時候就會add了

//flow是支持掛起函數的seqence（可以看成協程版的seq）一定得在協程內運行 flow遍利是用collect seq可以用for

//所以flow跟seqence 重點是提供一個邊生產邊消費的數據序列 （非數據結構而是生產規則）
//Sequence 提供一個同步的、惰性的、可遍歷的元素序列的生產和消費規則。
//Flow 提供一個非同步的、可連續發射多個值的數據流的生產和消費規則。

//seq
//惰性求值 (Lazy Evaluation)： Sequence 的一個重要特性是惰性求值，也就是說，只有在需要使用元素時才會進行計算或生成。這使得處理無限序列或非常大的集合成為可能，因為不需要一次性將所有元素都載入記憶體。
//Pull-based (拉取式)： 消費者 (程式碼) 主動向 sequence 要求下一個元素，sequence 才會提供。
//單次遍歷： 一般情況下，sequence 只能被遍歷一次。遍歷過後，若要再次遍歷，需要重新建立 sequence。

