package com.rengwuxian.coursecoroutines._4_flow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.coroutines.EmptyCoroutineContext

fun main() = runBlocking<Unit> {
  val scope = CoroutineScope(EmptyCoroutineContext)
  val flow1 = flowOf(1, 2, 3, 4, 5)
  val flow2 = flow {
    delay(100)
    emit(1)
    delay(100)
    emit(2)
    delay(100)
    emit(3)
  }.map {  }
//  scope.launch {
//    flow1.map { if (it == 3) null else it + 1 }.collect { println("1: $it") }
//    flow1.mapNotNull { if (it == 3) null else it + 1 }.collect { println("2: $it") }
//    flow1.map { if (it == 3) null else it + 1 }.filterNotNull()
//    flow1.filter { it != 3 }.map { it + 1 }
//    flow2.mapLatest { delay(120); it + 1}.collect { println("3: $it") }
//
//    val searchQueryFlow = flow {
//      val queries = listOf("kotlin", "coroutines", "flow", "mapLatest")
//      for (query in queries) {
//        emit(query) // 發出新輸入
//        delay(200)  // 模擬用戶輸入間隔
//      }
//    }
//    searchQueryFlow
//      .map { query ->
//        searchApi(query) // 調用 suspend 函數並切換上下文
//      }
//      .collect { result ->
//        println("Collected: $result on ${Thread.currentThread().name}")
//      }
//
//  }

  scope.launch {
    // 原始數據流
    val originalFlow = flow {
      val ccc = withContext(Dispatchers.Default){
        delay(100)
        1
      }
      emit(ccc)
      delay(100)
      emit(2)
      emit(3)
    }

    // 模擬 `map` 的邏輯
    val transformedFlow = flow {
      originalFlow.collect { value ->
        println("Upstream value received: $value on ${Thread.currentThread().name}")
        val transformedValue = withContext(Dispatchers.IO) { // 模擬轉換邏輯
          println("Transforming $value on ${Thread.currentThread().name}")
          value * 2
        }
        emit(transformedValue) // 手動發射轉換後的數據
      }
    }

    // 下游收集數據
    transformedFlow.collect { result ->
      println("Downstream collected: $result on ${Thread.currentThread().name}")
    }
  }

  delay(10000)
}
suspend fun searchApi(query: String): String = withContext(Dispatchers.IO) {
  println("Searching for: $query on ${Thread.currentThread().name}")
  delay(500) // 模擬網絡延遲
  "Results for \"$query\""
}
//mapNotNull 先map在filtet Null
//maplatest在一條數據如果在{}中還沒處理完成 下一條數據就到了就丟掉上一條數據去做最新的那條
//是用場景：數據生產比較慢而且新數據如果生產出來就數據轉換就沒有用的場景
//可用在搜索： 搜尋扔的時候用戶又加了扔掉這兩個字 這時候扔沒意義了可以不做
//操作是異步的：需要處理耗時任務（如 API 調用）
//flow不能切攜程 因為他預設就是在collect所在的攜程 但是flow 操作符可以這樣做 因為操作符其實就是collect上游 + 轉新的flow出去？
//Flow 操作符（如 map 或 mapLatest）可以使用 withContext 或 flowOn，這是因為它們執行的是數據的轉換邏輯，而不是控制數據的發射。
//數據的處理和轉換其實是執行在操作符內的協程中，而這些操作符允許使用 withContext 進行上下文切換

//生產數據可以切 發不能切 因為發等於是把那段丟給collect 如果你發在io 但collect寫在main 那這時候你要怎麼反應可能就要collect又去切 那如果很多發都不同根本不能處理
//emit 是負責將數據從當前的操作符發射到下游的。
//下游的操作符（包括最終的 collect）會立即接收到這個數據。
//如果 emit 切換到另一個上下文，而下游的操作（如 collect）在不同的上下文，可能會導致上下文衝突。
//emit 一定是串行執行的，這是 Kotlin Flow 的核心設計之一
//所以操作符一定是suspend函數 然後下游操作符要等上游 不然如果並行你collect收到順序跟emit順序不同你也根本無法處理

//Flow 的數據處理是單線程、逐步執行的，即每一條數據在被處理完成之前，下一條數據不會被處理。
//這種串行執行的特性保證了數據的處理順序和一致性。
//串行執行的原因
//數據的傳遞性
//
//上游發射的每條數據，必須經過所有操作符處理後，才能進入下游。
//如果 emit 是並行的，Flow 將無法保證數據的順序性，會導致數據處理混亂。
//數據處理的穩定性
//
//串行化執行避免了多協程之間的競爭，從而降低了數據處理的復雜性。
//如果允許 emit 並行執行，Flow 的數據處理順序可能被打亂。例如：
//
//上游同時發射數據 1 和 2。
//下游的操作符可能先處理 2 再處理 1。
//單協程執行
//Flow 的數據處理默認是在同一協程中執行的。
//即使上游和下游在不同的上下文（如通過 flowOn），每個數據的完整處理過程始終是串行的。
//2. 數據處理是掛起的
//當操作符（如 map, filter）內部執行掛起操作時，Flow 會等待該操作完成，再處理下一條數據。
//這樣可以確保數據處理的順序性。
//3. 操作符設計
//每個操作符（如 map, filter）都是串行處理數據，並將結果發射給下游。
//下游操作符的執行會等待上游操作符完成數據處理


//public inline fun <T, R> Flow<T>.map(crossinline transform: suspend (value: T) -> R): Flow<R> =
//    transform { value -> // transform 是內部通用的邏輯
//這裏才是下面unsafeFlow emit的地方
//        return@transform emit(transform(value)) // 將轉換後的數據發射給下游
//    }
//internal inline fun <T, R> Flow<T>.unsafeTransform(
//    @BuilderInference crossinline transform: suspend FlowCollector<R>.(value: T) -> Unit
//): Flow<R> = unsafeFlow { // unsafeFlow 是內部使用的高效實現
//這裏是collector所以其實是 用Flow<T>.collect  不是裡面的unsafeFlow
//    collect { value -> // 上游 collect 發射的數據傳遞到這裡
//        return@collect transform(value) // 轉換後的數據通過 transform 傳遞到下游
//    }
//}

//map