package com.rengwuxian.coursecoroutines._3_scope_context

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
import kotlin.coroutines.EmptyCoroutineContext

fun main() = runBlocking<Unit> {
  val scope = CoroutineScope(EmptyCoroutineContext)
  scope.launch {
    val startTime1 = System.currentTimeMillis()
    //Duration trySuspend: 1005
    trySuspend(startTime1)
    //Duration trySuspend End: 1006
    println("Duration trySuspend End: ${System.currentTimeMillis() - startTime1}")
  //  掛起函數是串行的 是狀態機
    val startTime2 = System.currentTimeMillis()
    coroutineScope {
      launch {
        delay(2000)
        println("Duration coroutineScope launch 22222 : ${System.currentTimeMillis() - startTime2}")

      }
      delay(1000)
      println("Duration coroutineScope : ${System.currentTimeMillis() - startTime2}")

    }
    println("Duration coroutineScope End : ${System.currentTimeMillis() - startTime2}")
  //Duration coroutineScope : 1003
    //Duration coroutineScope End : 1004
    //Duration coroutineScope : 1001
    //Duration coroutineScope launch 22222 : 2006
    //Duration coroutineScope End : 2006
    //corutinescope 不能傳參 會自動創scope + 繼承context + 丟父job (變父子）
    //所以會等子運行完再說
    //但又是掛起函數所以是串行
    //可以看成無參的launch+join launch的job
    // 通常使用場景是“掛起函數內啟動攜程”
    //1.並行（看成掛起函數）
    //3.有返回值
    //4.封裝串行模塊錯誤不是傳到最上面而是自己處理
    val startTime4 = System.currentTimeMillis()

//    val cl =  coroutineScope {
//       println("Duration return1 : ${System.currentTimeMillis() - startTime4}")
//
//       val hi = async {
//         delay(1000)
//         "hihi"}
//       val s = async {
//         delay(2000)
//         "sssss"}
//       hi.await()+s.await()
//     }
//    println("Duration return44444 :  ${cl } === ${System.currentTimeMillis() - startTime4}")

//    launch {
//      try {
//        throw  Exception("aaa")
//      }catch (e:Exception){
//        println("Exec : $e")
//      }
//    }
    //任何錯誤都catch裡面很正常～可以想成thread

//    try {
//      launch {
//
//          throw  Exception("bbb")
//      }
//    }catch (e:Exception){
//      println("Exec : $e")
//
//    }
    //Exception in thread "DefaultDispatcher-worker-2" java.lang.Exception: bbb
    // 因為對於整個協程來說他是並行的，並行的不能預知錯誤會影響多大，所以要丟到最上層
    //舉例：準備麵團/切配料/燒烤爐預熱 但如果烤爐爆炸了（相當於全局的重大異常），那整個廚房可能就需要停下來，所有的任務都無法繼續。
    //並行邏輯的特點是：
    //任務之間是獨立的，異常發生時，不同任務間無法互相通知。
    //重大異常必須由最外層捕捉並進行全局處理。
//    try {
//      coroutineScope {
//        throw  Exception("bbb")
//      }
//    }catch (e:Exception){
//      println("Exec : $e")
//
//    }
    //Exec : java.lang.Exception: bbb
    // 因為對於整個協程來說coroutineScope他是串行的
    //需要先麻醉，然後開刀，再縫合 所以在麻醉這步驟如果錯誤就可以單一catch 不會影響其他人執行（因為上下有序）
//結論： 串行邏輯的特點是：
//每個步驟依賴前一步的結果，異常可以逐步處理。
//局部的異常不會擴散到整個系統，能保持任務的穩定性
    //舉例：麻醉失敗了：
    //
    //因為麻醉沒成功，手術流程會暫停，醫生可能會改用其他麻醉方案。
    //影響範圍： 異常被限制在「麻醉步驟」，後續步驟（開刀、縫合）不會執行。
    val handler = CoroutineExceptionHandler { _, exception ->
      println("Caught in CoroutineExceptionHandler: $exception")
    }

    try {
      supervisorScope {
        launch(handler) {
          println("Duration supervisorScope:2222")

          delay(1000)
          throw Exception("aaaa")

        }
        launch {
          delay(2000)
          println("Duration supervisorScope:11111")

        }
      }
      println("Duration supervisorScope:3333")


    }catch (e:Exception){
      //沒用
      println("Duration supervisorScope:e$e")

    }
    //SupervisorCoroutine
    //他也是綁自動繼承調用者的 CoroutineContext跟job 只是他是SupervisorCoroutine
    // 如果子類有錯不會傳遞到父 當子協程失敗時，異常不會向 supervisorScope 傳播，所以在外面try沒用
  //  可以給handler即使在 coroutineScope 中設置了 CoroutineExceptionHandler
    //  ，異常仍然會傳播到上層並導致整個 coroutineScope 被取消。這是因為 coroutineScope 的設計理念是 結構化並發
    //  ，子協程的失敗會影響到父協程和同一作用域內的其他子協程。
    //批量處理：如用戶數據同步、文件上傳，其中某個任務失敗，不應影響其他任務。
    //並行數據加載：加載多個數據源，某一數據源失敗，不應影響界面的整體展示。
    //界面組件渲染：部分 UI 模塊加載失敗，不影響其他模塊的渲染。

    val startTime = System.currentTimeMillis()
    launch {
      //Duration AAAA: 1013
      delay(1000)
      println("Duration AAAA: ${System.currentTimeMillis() - startTime}")

    }
    //Duration BBB: 7
    println("Duration BBB: ${System.currentTimeMillis() - startTime}")

    //協程是並行的不會等待
  }
  //Duration CCCC:
  //Duration trySuspend: 1010
  //Duration trySuspend End: 1011
  //Duration coroutineScope : 1013
  //Duration coroutineScope launch 22222 : 2005
  //Duration coroutineScope End : 2006
  //Duration BBB: 1
  //Duration AAAA: 1004
//  println("Duration CCCC: ")

//  scope.launch {
//    supervisorScope {
//
//    }
//    val name = try {
//      coroutineScope {
//        val deferred1 = async { "rengwuxian" }
//        val deferred2: Deferred<String> = async { throw RuntimeException("Error!") }
//        "${deferred1.await()} ${deferred2.await()}"
//      }
//    } catch (e: Exception) {
//      e.message
//    }
//    println("Full name: $name")
//    val startTime1 = System.currentTimeMillis()
//    coroutineScope {
//      launch {
//        delay(2000)
//      }
//      delay(1000)
//      println("Duration within coroutineScope: ${System.currentTimeMillis() - startTime1}")
//    }
//    println("Duration of coroutineScope: ${System.currentTimeMillis() - startTime1}")
//    val startTime = System.currentTimeMillis()
//    launch {
//      delay(1000)
//      println("Duration within launch: ${System.currentTimeMillis() - startTime}")
//    }
//    println("Duration of launch: ${System.currentTimeMillis() - startTime}")
//  }
  delay(10000)
}

suspend fun trySuspend(startTime1: Long) {

  delay(1000)
  println("Duration trySuspend: ${System.currentTimeMillis() - startTime1}")

}

//coroutineScope
//1.創一個CoroutineScope 裡面會繼承上面協程的上下文 ＋ job是上面協成的子協程（跟launch很像）
//2.不能填任何參數（launch可以訂製job+dispather)
//3.協程是並行的 掛起函數是串形的 所以corutinescope會等所有幹完才結束
private suspend fun someFun() = coroutineScope {
  launch {

  }
}

//，「揉麵團」「切配料」「預熱烤箱」是同時進行的
//假如「切配料」失敗，但「揉麵團」和「預熱烤箱」已經完成，流程進入到「放配料到麵團上」這一步，卻發現配料不可用，整個流程被中斷。
//，「揉麵團」「切配料」「預熱烤箱」是串行的
//因為串行流程是「一步接一步」的，失敗的配料直接影響到下一步「放配料到麵團上」。
//但由於還沒進行到後續的「烘烤披薩」，可以在當前步驟及時修正，比如：
//思維卡住的核心原因
//你可能下意識地把「並行」當作一個獨立的任務系統，覺得每個任務可以各自為政。也就是說，你認為「切配料失敗」可以單獨處理，不影響其他並行的任務。
//
//但實際上，並行流程中每個任務之間是有依賴關係的，只是它們同時進行，這種依賴會因為異常擴散到全局，導致整個系統無法繼續。
//為什麼「並行的切配料」不能簡單地重切？
//1. 時間同步的問題
//在並行流程中，所有任務的時間進度是同步的。
//如果「切配料」需要重切，這個任務需要更多時間，但其他任務（比如烤箱已預熱、麵團已完成）卻早已完成，這就導致了資源閒置和浪費。
//例子：烤箱預熱好，但還要等配料重切，導致能耗浪費。
//2. 依賴衝突的問題
//並行流程中，後續的步驟依賴所有前置任務的最終結果。
//如果切配料失敗，就無法完成「放配料到麵團上」，導致整個流程無法匯總結果。
//例子：即使麵團和烤箱準備好了，沒有配料，披薩還是做不出來。
//3. 重切配料的修復成本
//「重切配料」是一個補救措施，但這需要一個全局協調機制來告訴其他任務暫停或等待。
//並行流程的設計初衷是「各自完成任務，匯總結果」，而不是「不停補救」。
//例子：讓揉麵團和預熱烤箱都等配料完成，會讓並行變成串行，違背並行設計的初衷。
//問自己這些問題
//如果讓其他並行任務都等待「切配料重切」完成，這還算並行嗎？
//
//答案：這會變成串行，並行的優勢消失了。
//其他任務是否可以不依賴切配料的結果完成？
//
//答案：不行。即使揉好麵團和預熱好烤箱，沒有配料，披薩仍然無法完成。
//如果切配料重切多次失敗，是否會浪費所有其他任務的資源？
//
//答案：是的，每次重切都導致烤箱能耗、麵團時間成本增加
//如何誘導你理解重試的問題
//問自己幾個問題
//重試需要多長時間？其他任務能等嗎？
//
//假設一個任務重試需要10分鐘，其他任務是否能等待這10分鐘？
//如果不能，結果可能已經失效，重試也沒有意義。
//失敗後，是否影響其他任務的結果？
//
//如果切配料失敗，烤箱預熱時間結束，重試切配料有什麼用？披薩已經無法按時完成。
//重試的成本是什麼？
//
//重試需要額外的資源（時間、機器等），這些資源是否會影響其他流程的進行？

