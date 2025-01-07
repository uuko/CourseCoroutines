package com.rengwuxian.coursecoroutines._4_flow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.random.Random

fun main() = runBlocking<Unit> {
  val scope = CoroutineScope(EmptyCoroutineContext)
//  val name = MutableStateFlow("rengwuxian")
//  val flow1 = flow {
//    emit(1)
//    delay(1000)
//    emit(2)
//    delay(1000)
//    emit(3)
//  }
//  name.asStateFlow()
//  val state = flow1.stateIn(scope)
//  scope.launch {
//    name.collect {
//      println("State: $it")
//    }
//  }
//  scope.launch {
//    delay(2000)
//    name.emit("扔物线")
//  }

  //如果改成shareflowˋ直接收集就不好了 會跟著一起停對吧
  //ChatGPT 說：
  //ChatGPT
  //沒錯！如果你直接在相同的協程範圍 (scope) 中啟動 SharedFlow 和收集它，當 scope.cancel() 被調用時，上游和下游都會一起停止。
  //✅ 正確方式 – 分離上下游協程範圍 或是用flow+shareflow
  //要讓 SharedFlow 的數據持續發送，即使下游停止收集，上游仍能保持數據生產，可以這樣做：
  //
  //🔹 關鍵點：
  //上游數據生產和下游收集分離，分別運行在不同的 CoroutineScope 中。
  //即使下游的 collect 停止，上游數據生產協程依然獨立存在。

  val stockPriceFlow = MutableSharedFlow<Float>(
    replay = 3,                    // 緩存最近 3 條數據
    extraBufferCapacity = 5
  )

// 上游：負責持續發送數據
  val producerScope = CoroutineScope(Dispatchers.Default)
  producerScope.launch {
    while (true) {
      val price = 100 + Random.nextFloat() * 10
      stockPriceFlow.emit(price)
      println("📤 發送新股價: $price")
      delay(1000)
    }
  }

// 下游：當用戶進入頁面時收集數據
  val consumerScope = CoroutineScope(Dispatchers.IO)
  consumerScope.launch {
    println("📱 開始收集數據...")
    stockPriceFlow.collect {
      println("📈 畫面顯示股價: $it")
    }
  }

// 模擬 5 秒後用戶離開頁面，停止收集
  consumerScope.launch {
    delay(5000)
    println("📴 離開頁面，停止收集...")
    consumerScope.cancel()  // 只取消下游，不影響上游
  }
  //========================
//  val stockPriceFlow = MutableSharedFlow<Float>(
//    replay = 3,
//    extraBufferCapacity = 5
//  )
//
//// 上游持續發送數據
//  val producerScope = CoroutineScope(Dispatchers.Default)  // 上游 scope
//  producerScope.launch {
//    while (true) {
//      val price = 100 + Random.nextFloat() * 10
//      stockPriceFlow.emit(price)
//      println("📤 產生新股價: $price")
//      delay(1000)  // 模擬每秒更新一次數據
//    }
//  }
//
//// 用戶進入頁面開始收集數據
//  val consumerScope = CoroutineScope(Dispatchers.IO)  // 下游 scope
//  consumerScope.launch {
//    delay(1000)
//    println("📱 開始收集數據...")
//    stockPriceFlow.collect {
//      println("📈 畫面顯示股價: $it")
//    }
//  }
//
//// 模擬 5 秒後用戶離開頁面，取消消費者收集
//  consumerScope.launch {
//    delay(8000)
//    println("📴 離開頁面，停止收集...")
//    consumerScope.cancel()  // 取消下游收集，不影響上游數據發送
//  }
  delay(10000)
}

//@OptIn(ExperimentalSubclassOptIn::class)
//@SubclassOptInRequired(ExperimentalForInheritanceCoroutinesApi::class)
//public interface StateFlow<out T> : SharedFlow<T> {
//    /**
//     * The current value of this state flow.
//     */
//    public val value: T
//}
//stateflow 是sharedflow最後一條最新數據 一個只能緩存最後一條數據的shareflow 並且能拿到最新緩存 ＝帶有最新事件的狀態
//replay 1 val stateflow = MutableSharedFlow<Int>(replay = 1, extraBufferCapacity = 0)
//只保留最新的數據狀態，過去的數據不會緩存或重播。
//新的 collect 只會收到當前的狀態，不會收到之前的數據。
//總是有初始值，因此與狀態管理非常契合。
//熱流（Hot Flow）：即使沒有消費者，數據也會持續更新。
//最新狀態持續保留： StateFlow 始終保存最新的一條數據，非常適合用於 UI 狀態管理。
//自動重發最新數據： 當消費者（UI）啟動時，自動接收當前最新狀態，無需處理歷史數據。
//無數據丟失風險： 即使消費者進程掛起或恢復，也能拿到最新的狀態，避免數據同步問題

//緩衝的運作方式
//無緩衝時（extraBufferCapacity = 0）：
//
//如果下游還在處理數據，上游發送新數據時會直接掛起（suspend），直到下游消費完畢。
//或者，如果使用 tryEmit 發送，則會立即返回 false，表示數據無法進入緩衝區。
//有緩衝時（extraBufferCapacity > 0）：
//
//上游發送數據後，即使下游尚未處理完成，數據會先暫存到緩衝區中，上游不會被掛起。
//緩衝區滿了之後，新的發送會掛起，直到下游消費數據釋放空間。

//所以sharflow是可以生產跟消費分開 stateflow 不行？ 因為即使底層是shareflow但她由於 buffer=0 所以一定要 消費後才能再生產 但是跟flow還是有本質差別因為他不需要ｃｏｌｌｅｃｔ啟動


//SharedFlow：
//
//生產與消費可以分開，即使沒有消費者，數據依然可以持續發送（因為有緩衝區 extraBufferCapacity）。
//即使下游慢，上游可以繼續發送，緩衝區滿後才會掛起。
//事件流、廣播流的最佳選擇。
//StateFlow：其實還是分開的 因為生產者不需消費者啟動
//但本質上都不會自動結束或停止，除非手動取消或程式範圍（CoroutineScope）結束
//只是生產者在沒有消費者收集時，只能覆蓋當前狀態，不能積累或緩存數據。
//上游數據無法堆積，舊數據會直接丟棄，緩衝為 0。
//適合用於「最新狀態持續保留」的場景（如 UI 狀態管理）。


//1. Flow
//
//生產者和消費者同步運作，消費者 collect 啟動後，上游才開始發送數據。
//使用場景： 適合短暫的、一對一的數據流處理，無需跨協程或持續保留數據的場景。
//例子：
//下載文件進度監聽。
//請求一次 API 並處理結果。
//表單驗證，每次輸入觸發驗證流。
//2. SharedFlow
//
//生產者和消費者完全分離，生產者可以在無消費者的情況下持續發送數據。
//底層： 基於 Channel，支持跨協程。
//緩衝區： 可配置 extraBufferCapacity（防止數據丟失）。
//多播能力： 多個消費者可以同時收集，數據廣播。
//使用場景： 適合需要事件流、數據持續產生的場景，即使暫時無消費者也需持續發送數據。
//例子：
//股票即時行情推送。
//即時訊息推送（聊天室訊息）。
//日誌監聽系統。
//3. StateFlow
//
//StateFlow 本質上是 SharedFlow 的特例，緩衝固定為 0，重播（replay）為 1。
//生產者和消費者分離，但只保留最新狀態，過去的狀態會被覆蓋。
//適用於「狀態管理」，而非事件流。
//使用場景： 適合 UI 層的狀態監聽或單純的狀態持續監測。
//例子：
//ViewModel 中的 UI 狀態管理（Loading 狀態、按鈕啟用狀態）。
//網路連接狀態（online/offline 切換）。
//播放器播放狀態（Playing、Paused、Stopped）。

//那我們來討論使用場景
//分成flow
//
//sharedflow :  又往下分stateflow 跟shareflow
//
//
//首先 生產跟消費者可以分開的用sharfow 因為底層是用channel所以可以輕易跨攜程 flow只能用flowon去做到
//
//生產者跟消費者可以分開是因為 用channel去切割 所以不用一定要collect後才去做
//
//而是一開始創建就有可能訂閱上游
//
//使用場景是可分開的場景：ex:股票數據流 在未有消費者也要一直傳遞
//
//
//ｆｌｏｗ是要單純collect後才做的事情 （沒想到舉例
//
//
//再來是 shareflow跟stateflow
//
//stateflow = buffer0 replay1
//跟shareflow一樣可能會掉數據 但沒buffer永遠是最新數據 會重播最新數據 當每次collecy時
//
//跟shareflow一樣可以廣播（因為shareflow其實只是收集工具收集上層flow發給下游） 不像純flow是collecy才開始
//
//由於只會拿到最新數據所以適合去查看狀態 不適合查看數據


//特性	Flow	SharedFlow	StateFlow
//生產者與消費者關係	生產者與消費者同步啟動，消費者啟動後生產數據	生產者獨立於消費者，可無消費者持續發送數據	生產者獨立於消費者，保留最新狀態
//緩衝區	無緩衝區，逐條發送	可設置 extraBufferCapacity	緩衝區固定為 0
//Replay	無重播，每次 collect 開始重新生產	可配置 replay（重播多條數據）	replay = 1，重播最新數據
//消費者加入後行為	只能收集新數據	收到緩衝區數據或 replay 數據	只收到最新狀態
//消費者數量	一對一（冷流）	一對多（熱流）	一對多（熱流）
//結束條件	上游完成或範圍結束	持續運行，直到 CoroutineScope 被取消	持續運行，直到 CoroutineScope 被取消
//適用場景	短暫數據流、一次性任務	事件流、多消費者訂閱數據	狀態管理

//生產和消費是否能「分離」 是決定使用 Flow 或 SharedFlow / StateFlow 的核心關鍵。
//是否需要「累積歷史數據」或「只關心最新狀態」 是決定使用 SharedFlow 或 StateFlow 的關鍵。

//1. 下載文件進度監聽
//特性： 下載開始後，消費者同步監聽進度變化。
//生產與消費同步：按下按鈕後才觸發下載，且消費端（進度條）必須隨著數據流動。
//使用：Flow
//為什麼？
//按下按鈕開始下載，collect 後上游才開始產生數據。
//不需要保留過去數據，直接同步處理當前進度即可。
//. 下載文件是否完成（只關心最新狀態）
//特性： 無論什麼時候加入消費，只關心文件是否已完成。
//生產與消費分離：下載進行中或完成狀態可以持續更新，即使消費者晚加入也只需知道「最新狀態」。
//使用：StateFlow
//為什麼？
//只關心「最新的下載狀態」，例如：下載中 -> 完成 -> 失敗，無需保留過去歷程。
//即使消費者晚加入，也能直接獲得當前最新狀態（StateFlow 只保留最新狀態）。
//. 下載文件進度（可保留歷史數據）
//特性： 進度可以保留一段時間，即使消費者晚加入也能獲得最近進度資訊。
//生產與消費分離：可以提前啟動下載，消費者隨時加入獲取當前或歷史進度數據。
//使用：SharedFlow
//為什麼？
//進度數據可以緩存，即使消費者在 50% 加入，仍能獲得之前的進度數據。
//可設定緩衝區和重播次數，允許消費者稍後收集數據。

//例子：聊天室即時訊息（SharedFlow）+ 私聊（Flow）
//即時訊息流： 廣播給所有用戶，使用 SharedFlow。
//個人私聊訊息： 只有用戶點開對話框後才建立數據流，使用 Flow。
//股票數據流設計概念：
//
//底層數據來源（後台）： 使用 SharedFlow 不斷接收股票數據，確保即使沒有 UI 消費者，數據流也持續運行。
//前台 UI 顯示（畫面）： 在用戶進入或需要顯示股票數據時，通過 Flow 收集和顯示當前的股票數據，避免浪費資源。
//切點： Flow 可以在用戶打開頁面時 collect SharedFlow，獲取最新數據。
//val stockPriceFlow = MutableSharedFlow<Float>(
//    replay = 3,                   // 緩存最近 3 條數據
//    extraBufferCapacity = 5       // 緩衝容量，允許突發數據
//)
//
//// 模擬股票數據來源
//launch {
//    while (true) {
//        stockPriceFlow.emit(100 + Random.nextFloat() * 10)  // 模擬股價變動
//        delay(1000)  // 每秒推送一次數據
//    }
//}
//fun getStockPriceFlow(): Flow<Float> = flow {
//    stockPriceFlow.collect { latestPrice ->
//        emit(latestPrice)  // 發送數據給 UI 層
//    }
//}
//
//// 模擬用戶進入股票詳情頁面
//launch {
//    println("📱 進入股票頁面，開始接收數據...")
//    getStockPriceFlow().collect { price ->
//        println("📈 畫面顯示股價: $price")
//    }
//}
//股票數據流（後台持續運行）	SharedFlow	持續產生數據，即使無消費者也不停止
//股票詳情頁面（即時但不持續收集）	Flow + first()	只關心當前最新數據，進入頁面時收集一次
//股票走勢圖（持續顯示最新數據）	Flow + collect	進入頁面後開始持續收集
//股票警報（只顯示最新狀態）	StateFlow	只需要關心最新警報，不記錄歷史數據
//離開後停止收集，不影響上游：
//透過 關閉 CoroutineScope 或取消 Job，停止下游收集。
//上游持續發送數據，不受下游是否存在影響。
//不需關閉上游 SharedFlow，上游依然在發送數據：
//下游離場（關閉頁面）後，不需要特意關閉 SharedFlow，持續數據生產，讓下一個進來的消費者繼續收集數據。
//SharedFlow 本身不會因為下游的消費者（collect）停止而中斷，因為它是熱流（Hot Flow），生產者會一直持續發送數據，與消費者的生命周期解耦。