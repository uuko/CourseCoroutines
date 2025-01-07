package com.rengwuxian.coursecoroutines._4_flow

import com.rengwuxian.coursecoroutines.common.Contributor
import com.rengwuxian.coursecoroutines.common.gitHub
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.FileWriter
import kotlin.coroutines.EmptyCoroutineContext

fun main() = runBlocking<Unit> {
  val scope = CoroutineScope(EmptyCoroutineContext)
  val fileChannel = Channel<FileWriter>() { it.close() }
  fileChannel.send(FileWriter("test.txt"))
//  val channel = Channel<List<Contributor>>(8, BufferOverflow.DROP_OLDEST)
//  val channel = Channel<List<Contributor>>(1, BufferOverflow.DROP_OLDEST)
  val channel = Channel<List<Contributor>>(CONFLATED)
  scope.launch {
    channel.send(gitHub.contributors("square", "retrofit"))
    channel.close()
    channel.close(IllegalStateException("Data error!"))
    channel.receive()
    channel.receive()
    channel.send(gitHub.contributors("square", "retrofit"))
    channel.trySend(gitHub.contributors("square", "retrofit"))
    channel.tryReceive()
  }
  launch {
    //掛起是的 因為他自己也不知道有沒有下一個元素 所以會掛起等待 for寫法跟while相同 可以ctrl點in去看
    //send如果隊列滿了也是會掛起直到有人去消耗
    //channel主要事項blockingqueue那樣 讓兩個攜程間通信 但是是1v1 然後先進先出
    //送出滿了就掛起 收到沒收到也掛起等人送 默認情況下沒有緩衝區 所以兩個攜程必定先做的人要先等著
    //有緩衝區的情況下送出就是要緩衝區滿才會等著
    //策略：drop 丟棄 丟棄的話就是像是有個東西再一直處理ui要用的數據 但是有新數據 這樣舊的就沒用就可以直接丟掉
    //conflate = buffer1,drop 就是永遠丟棄舊的然後緩衝區是1
      //public interface Channel<E> : SendChannel<E>, ReceiveChannel<E> { } 實現了收根冠
    //close 是send的關閉 如果在close後在ｓｅｎｄ會丟異常 應該要往怎麼去處理這異常想 然後close是我們自己控制的所以是要想好業務邏輯
    //close後 如果是在這之前send還是會送 receive還是能接收 因為要關receive不是這樣關

//    public class ClosedSendChannelException(message: String?) : IllegalStateException(message)
//public class ClosedReceiveChannelException(message: String?) : NoSuchElementException(message)
    //然後思考方向是send後close 有可能他send的recieve還沒收到 所以才要切兩個方法
    //全都收到後recive也會自己關閉了 如果在呼叫也會丟ex
    //所以在close錢掛起的send會做完 recieve會等全收完才關
    //cancel是用接收端去看了 會把發送端跟接收端的boolean都改掉 cancel後去叫都會丟ex
    //因為都不需要接收了所以立刻把發根收都關了就正常了 （這個在suspend的發也會關）
    //cancelException
    //    onUndeliveredElement: ((E) -> Unit)? = null 已發送但被丟棄的數據要回收用這個
    //不然如果是file就有問題

    //trysend/tryrecevie不掛起 如果失敗就直接丟 然後有 resutl (ex ) iscloase 去判斷是不是因為關閉做的 因為有可能是緩衝區滿了
    //recerivecathing 是一個掛起但也會回resutl 就是catch ex 讓你判斷

    //produce是把channel創建跟發送簡化 合成一個
    for (data in channel) {
      println("Contributors: $data")
    }
    /*while (isActive) {
      val contributors = channel.receive()
      println("Contributors: $contributors")
    }*/
  }
  delay(1000)
  channel.cancel()
  delay(10000)
}