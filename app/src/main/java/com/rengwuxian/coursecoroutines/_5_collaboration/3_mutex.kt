package com.rengwuxian.coursecoroutines._5_collaboration

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread
import kotlin.coroutines.EmptyCoroutineContext

//@Synchronized
fun main() = runBlocking {
  var number = 0
  val lock = Any()
  /*val thread1 = thread {
    repeat(100_0000) {
      synchronized(lock) {
        number++
      }
    }
  }
  val thread2 = thread {
    repeat(100_0000) {
      synchronized(lock) {
        number--
      }
    }
  }
  thread1.join()
  thread2.join()
  println("number: $number")*/
  val scope = CoroutineScope(EmptyCoroutineContext)
  val mTJob = scope.launch {
    val mJob1= launch {
      repeat(100_0000) {
        synchronized(lock) {
          number++
        }
      }
    }

    val mJob2=launch {
      repeat(100_0000) {
        synchronized(lock) {
          number--
        }
      }
    }
    mJob1.join()
    mJob2.join()
    println("number: $number")

  }
  mTJob.join()

//  val mutex = Mutex() // mutual exclusion
//  val semaphore = Semaphore(3)
//  AtomicInteger()
//  CopyOnWriteArrayList<String>()
//  val job1 = scope.launch {
//    semaphore.acquire()
//    semaphore.release()
//    repeat(100_0000) {
//      mutex.withLock {
//        number++
//      }
//    }
//  }
//  val job2 = scope.launch {
//    repeat(100_0000) {
//      mutex.withLock {
//        number--
//      }
//    }
//  }
//  job1.join()
//  job2.join()
//  println("number: $number")
  delay(10000)
}

@Volatile var v = 0
@Transient var t = 0

//找到競態條件 根據競態條件去解決
//條件如何產生？ 兩個thread共用 共享資源的變量訪問 （多線程訪問同共享資源代碼） 臨界區

//rentenlock是可重入鎖 但鎖幾次就要解幾次
//@Synchronized 是針對線程 非協程
//jvm上的協程還是基於線程所以還是有互斥問題 協程是幫忙管理線城池 所以最後還是會到線程上的
//所以用synchronize還是有用的 因為他還是把線程lock住了


//lock ==> 通常要try{lock}finally{unlock} 可重入
//sychronize 不可重入
//semaphone 多線程可持有的鎖 不能用於解決競爭條件問題（因為競爭條件是兩線程訪問就有問題了） 所以是用來做池子的 像是性能控制 控制對象
//攜程也有semaphone - 控制同時訪問資源的執行緒數量，通常用於資源池或限流。
//atimocinteger copyonwritelsit 都可以用 因為卡住線程 協程一定也被卡了 協程底層還是線程

//mutex 基於協程lock 是掛起的 會把當時協程給讓出（其實就是suspend掛起）
//非協程就用synchronize+lock 協程就用mutex 如果變量在協程裡外都用到還是用傳統的

//協程也是可以用synchronize吧 他底層也是用線程去實現的 線程能鎖 就可以了吧
//畢竟病發問題就是在線程病發 共享資源的變量訪問 才有問題的
//協程就是用來管理現成池的上層api 底層還是是開現成池的
//
//ChatGPT 說：
//ChatGPT
//你說得沒錯！協程的底層確實是基於線程實現的，所以 synchronized 在協程中是有效的，因為它鎖住的是線程，而不是協程本身。
//當你在協程中使用 synchronized，你確實能避免競態條件，但這樣會阻塞整個線程，從而影響該線程上的其他協程

//他這個非阻塞其實是狀態機吧 等到狀態完成後再回來
//ChatGPT 說：
//ChatGPT
//是的，你理解得非常到位！協程的非阻塞本質上確實是基於**狀態機（State Machine）**的設計。
//
//協程如何透過狀態機實現非阻塞？
//1. 狀態保存：
//當協程掛起時，它會保存當前執行的狀態（程式計數器、變數等），並返回控制權給呼叫方或線程。
//
//例子： 你在協程內部呼叫了一個 suspend 函數，這時協程進入掛起狀態。
//2. 等待結果（掛起）：
//協程會暫時「離開」當前線程，允許其他協程在這個線程上執行，而不阻塞線程。
//
//這裡線程是空閒的，其他協程可以利用它執行任務。
//3. 恢復狀態：
//當協程等待的結果（例如 I/O、網路請求）完成後，協程恢復執行，並從先前保存的狀態繼續執行，彷彿它從未離開過。
//
//狀態機的具體運作方式
//當協程掛起時，Kotlin 會將協程「切成多個步驟」，每一步對應一個狀態。這些步驟會被封裝成狀態機，然後逐步執行。
//
//示例（協程拆解成狀態機過程）：
//kotlin
//複製程式碼
//suspend fun fetchData(): String {
//    delay(1000)  // 模擬網路請求
//    return "Data from server"
//}
//實際底層會轉換成類似這樣的狀態機：
//
//kotlin
//複製程式碼
//class FetchDataStateMachine : Continuation<String> {
//    var label = 0
//    var result: String? = null
//
//    override fun resumeWith(result: Result<String>) {
//        when (label) {
//            0 -> {
//                println("Step 1: 發起網路請求")
//                label = 1
//                fetchDataAsync(this)
//            }
//            1 -> {
//                println("Step 2: 收到結果，繼續執行")
//                this.result = result.getOrNull()
//            }
//        }
//    }
//}
//label 表示狀態機當前的狀態位置，每次掛起和恢復都會根據 label 進行狀態切換。
//fetchDataAsync 表示模擬一個非同步網路請求，協程掛起後，狀態會保存到 label = 1。
//為什麼這樣設計？
//避免線程阻塞：
//狀態機可以讓線程釋放資源，允許其他協程執行。這是協程比傳統線程更高效的原因之一。
//線程復用：
//相同的線程可以運行多個協程，每個協程透過狀態機切換狀態，不會互相阻塞。
//Mutex 如何利用狀態機做到非阻塞？
//Mutex 本質上也是透過狀態機管理協程鎖的狀態：
//
//當協程獲取鎖失敗時，協程會保存狀態並掛起，釋放線程。
//當鎖釋放後，之前掛起的協程會恢復，繼續執行。
//Mutex 示例：
//kotlin
//複製程式碼
//val mutex = Mutex()
//
//suspend fun criticalSection() {
//    mutex.withLock {
//        println("進入臨界區")
//        delay(1000)  // 模擬臨界區操作
//        println("離開臨界區")
//    }
//}
//當鎖被佔用時，協程會掛起，而不是阻塞線程，這就是「非阻塞」的實質。