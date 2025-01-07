package com.rengwuxian.coursecoroutines._4_flow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.EmptyCoroutineContext

fun main() = runBlocking<Unit> {
  val scope = CoroutineScope(EmptyCoroutineContext)
  val flow1 = flow {
    emit(1)
    delay(1000)
    emit(2)
    delay(1000)
    emit(3)
    delay(1000)
    emit(4)
    delay(1200)
    emit(5)

  }
  val sharedFlow = flow1.shareIn(scope, SharingStarted.WhileSubscribed(), 2)
  scope.launch {
    val parent = this
    launch {
      delay(4000)
      parent.cancel()
    }
    delay(1500)
    sharedFlow.collect {
      println("SharedFlow in Coroutine 1: $it")
    }
  }
  scope.launch {
    delay(5000)
    sharedFlow.collect {
      println("SharedFlow in Coroutine 2: $it")
    }
  }
  delay(10000)
}
//緩沖＋緩存 = sharedin buffer=緩衝而已 用過就無了
//eargly 立刻啟動上游 持續數據生產，無需等消費者加入。
//例如：股票行情推送、即時日誌系統。
//lazily 當有第一個collect後啟動 僅在有消費者時才需要數據生產，節省資源。
//適合不需要持續數據生產的場景，例如按需數據拉取。
//whilesubscribe 當第一個collect結束
// 他會自己再開一個 就不會拿上一個緩存 希望消費者在存在時啟動，離開後自動釋放資源。
//只有在有消費者 collect 時啟動數據生產，消費者離開後，Flow 自動停止。
//如果消費者重新訂閱，Flow 會重新啟動並重新生產數據。
//每次新訂閱者都不會從舊數據繼續，而是重新啟動上游 Flow。
//適合即時數據拉取或狀態監聽。
//sharedflow結束：永遠不結束 因為他這個collect返回事nothing 他不會當上面flow代碼執行完就全結束
//一班的flow是會的
//為什麼 SharedFlow 不會結束？
//SharedFlow 本質上是一個緩存數據的熱流容器，即使上游 Flow 已完成，SharedFlow 仍然持有數據並等待新的消費者。
//SharedFlow 返回的是 Nothing，即表示只要協程作用域還活著，它會持續等待新的 collect。

//取消後
//你使用 shareIn 搭配 SharingStarted.WhileSubscribed()，當最後一個消費者取消訂閱後，上游 Flow 會停止運行。
//如果新的消費者再次訂閱，上游 Flow 會重新啟動，重新生產數據。
//沒訂閱（取消後上游也會被停止） whilesubscribe有個參數是collect結束後要隔多久才判斷這是真結束 replayExpirationMillis是collect結束後緩存隔多久要被丟
// public fun WhileSubscribed(
//            stopTimeoutMillis: Long = 0,
//            replayExpirationMillis: Long = Long.MAX_VALUE
//        ): SharingStarted =
//            StartedWhileSubscribed(stopTimeoutMillis, replayExpirationMillis)
//但這個策略不會影響 Replay（緩存）數據的行為。即使 Flow 停止運行，緩存的數據仍然保留，直到新的消費者來收集。
//緩存數據 (Replay)：
//
//是 SharedFlow 內部維護的緩存區，即使上游重啟或停止，緩存的數據依然存在。
//只有明確將 replay 設置為 0，或者手動清理，緩存數據才會消失。
//SharedFlow in Coroutine 1: 1
//SharedFlow in Coroutine 1: 2
//SharedFlow in Coroutine 1: 3
//SharedFlow in Coroutine 2: 2
//SharedFlow in Coroutine 2: 3
//SharedFlow in Coroutine 2: 1
//SharedFlow in Coroutine 2: 2
//SharedFlow in Coroutine 2: 3
//SharedFlow in Coroutine 2: 4
//SharedFlow in Coroutine 2: 5

//緩衝（Buffer）：
//
//緩衝區可以暫存數據，防止下游處理速度慢導致阻塞，但數據消費後即消失。
//適合臨時處理流量高峰的場景。
//緩存（Replay）：
//
//緩存數據，即使下游暫時取消收集，數據依然存在，可供後續消費者重新消費。
//適合需要「重播數據」的場景，消費者即使晚加入也能收到最近數據。