package com.rengwuxian.coursecoroutines._5_collaboration

import kotlinx.coroutines.runBlocking
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

class ContinuationExample {

   inner class FetchDataStateMachine(override val context: CoroutineContext) : Continuation<String> {
        var label = 0  // 代表狀態機的狀態（0 表示開始執行，1 表示掛起後恢復）

        var result: String? = null

        override fun resumeWith(result: Result<String>) {
            when (label) {
                0 -> {  // 初始狀態
                    println("Step 1: 發起網路請求")
                    label = 1  // 變更狀態，表示進入掛起狀態
                    fetchDataAsync(this)  // 模擬非同步請求
                }
                1 -> {  // 從掛起狀態恢復
                    println("Step 2: 收到結果，繼續執行")
                    this.result = result.getOrNull()
                    println("結果: ${this.result}")
                }
            }
        }
    }

    // 模擬網路請求的非同步邏輯
    fun fetchDataAsync(continuation: Continuation<String>) {
        println("模擬掛起，請求進行中...")
        Thread {
            Thread.sleep(1000)  // 模擬網路請求延遲
            continuation.resumeWith(Result.success("Data from server"))
        }.start()
    }


    suspend fun run() {
        val stateMachine = FetchDataStateMachine(coroutineContext)
        stateMachine.resumeWith(Result.success(""))
    }
}

fun main() = runBlocking {
    ContinuationExample().run()
}