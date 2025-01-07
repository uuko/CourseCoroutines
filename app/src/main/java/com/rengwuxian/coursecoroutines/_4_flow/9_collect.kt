package com.rengwuxian.coursecoroutines._4_flow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.coroutines.EmptyCoroutineContext

fun main() = runBlocking<Unit> {
  val flow = flow {
    /*launch(Dispatchers.IO) {
      delay(2000)
      emit(2)
    }*/
    delay(1000)
    emit(1)
  }
  val scope = CoroutineScope(EmptyCoroutineContext)
  
  flow.onEach {
    println("flow: $it")
  }.launchIn(scope)
  
  scope.launch(Dispatchers.Default) {
    flow.collect {
      println("flow: $it")
    }
    flow.collectIndexed { index, value ->  }
    flow.collectLatest {  }
  }

  val channelflow  = channelFlow {
    launch {
      // 模擬數據源 1
      for (i in 1..3) {
        delay(1000) // 模擬耗時操作
        send(i) // 發送數據到 Channel
        println("Sent from source 1: $i")
      }
    }
    launch {
      // 模擬數據源 2
      for (i in 4..6) {
        delay(1000) // 模擬耗時操作
        send(i) // 發送數據到 Channel
        println("Sent from source 2: $i")
      }
    }
  }
  launch {
    println("Collected: before")

    delay(200)
    var time = System.currentTimeMillis()
    channelflow.collect { value ->
      val tmpTime = System.currentTimeMillis()
      println("Collected: $value ${tmpTime-time}")
      time=tmpTime
    }
  }

  delay(10000)
}

//flow collect 主要是因為這樣開發者會分不清楚從哪個collect從哪個協程來
//如果是ｕｉ collect  但送又是裡面有在launch到時候會分不清楚 又要在collect裡面寫切協程
//所以kotlin乾脆直接禁止了 flow{切協程} 目的是為了讓collect的大括號 在呼叫collect所在的攜程內運行 為了更符合開發者直覺
//那為什麼channelflow可以是因為他上游是channel 下游是collect 基本上collect的代碼也是在他所在的攜程運行
//是直接把上下游分開
//ps FLOW只是生產工具 一定得在協城內運行collect 沒有自己開協程的能力 flow{}也不是協程
//在 flow 區塊中，任何執行的邏輯都會運行在調用 collect 的那個協程中。
//flow{ aaa }  launch{ flow.collect() === aaa在裡面執行}
//flow {} 是一個構造函數，創建了一個 Flow 實例。它的執行邏輯描述了如何發射數據（emit）。
//flow {} 本身不是協程，但其內部的邏輯（例如 emit）必須運行在協程中。
//Flow 不需要「一定在協程內啟動」，它的生產過程（flow {}）是惰性的，只有在調用 collect 並執行時才會與協程掛鉤，因此 Flow 的執行本質上依賴於 collect 發生的位置。
//
//Channel 需要「一定在協程內操作」，因為 send 和 receive 都是掛起函數，這意味著它們只能在協程中執行。

//Flow 為什麼 collect 必須在協程內？
//開發者直覺與一致性
//
//Flow 的設計遵循「冷流」（Lazy）的概念，執行是在 collect 時觸發，Kotlin 強制要求 collect 在協程內執行，是為了避免數據處理流程混亂，並保持上下文一致性。
//如果允許 Flow 在 flow {} 內切換上下文，開發者可能會搞不清楚數據處理的上下文到底是哪一個，特別是在需要與 UI 或其他協程交互時，這樣的行為可能引發難以追蹤的錯誤。
//collect 的運行上下文
//
//collect 的設計目的是讓其執行在與它所在的協程上下文中，這樣開發者能清楚掌握數據流在哪裡被處理，不會因為 flow {} 內的上下文切換而困惑。
//為什麼 channelFlow 可以？
//channelFlow 的上下游設計
//
//channelFlow 是基於 Channel 實現的數據流，send 和 receive 都是明確的協程掛起操作，channelFlow 內的代碼執行是在它自己的協程中，這使得上下游的邏輯更加清晰。
//雖然下游（collect）仍然需要在協程內執行，但上游（channelFlow）的邏輯已經被分開處理，不會直接影響下游的執行上下文。
//更靈活的控制
//
//由於 channelFlow 使用 Channel 作為內部機制，它可以在 flow 中添加上下文切換邏輯，並保證 collect 的代碼依然執行在 collect 所在的協程內。
//這種分離使得開發者可以在 channelFlow 中進行更複雜的異步操作，而不會打亂 collect 的上下文邏輯。

//channelflow 一樣式collect才被啟動 啟動後裡面可以launch 會發送到channel 再從channel給到flow 所以適合用在病發場景
//channelFlow 是 collect 時啟動執行。啟動後，它內部可以啟動多個協程（launch），這些協程將數據通過 send 發送到內部的 Channel。
//下游的 collect 方法會從 Channel 中取數據，實現數據的消費。
//因此，channelFlow 適合用於 並發數據生產 或 多數據源整合 的場景。
//
// 即時發送與消費：
//
//send 發送數據時，數據會立即進入 Channel。
//下游的 collect 會動態地從 Channel 中取出數據進行處理。
//數據不會「預先全部放入 Channel」。
//協程隔離：
//
//channelFlow 內部可以啟動多個協程，用於處理不同數據源。
//各數據源可以獨立運行且互不干擾。


//launch in  .launchIn(scope) === scope.launch{ flow.collect}