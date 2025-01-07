package com.rengwuxian.coursecoroutines._4_flow

import kotlinx.coroutines.runBlocking

    // FlowACollector 負責收集數據
    interface FlowACollector<T> {
        suspend fun emit(value: T)
    }

    // FlowA 是數據流的接口
    interface FlowA<T> {
        suspend fun collect(collector: FlowACollector<T>)
    }

    // flowA 函數，用於創建一個新的 FlowA
    fun <T> flowA(block: suspend FlowACollector<T>.() -> Unit): FlowA<T> {
        return object : FlowA<T> {
            override suspend fun collect(collector: FlowACollector<T>) {
                // 在下游的 FlowACollector 上執行 block
                block(collector)
            }
        }
    }

    // catchA 操作符，用於捕獲並處理 FlowA 中的異常
    fun <T> FlowA<T>.catchA(action: suspend FlowACollector<T>.(Throwable) -> Unit): FlowA<T> {
        return flowA {
            try {
                // 收集上游數據
                this@catchA.collect(this)
            } catch (e: Throwable) {
                // 捕獲異常並執行 action
                action(e)
            }
        }
    }





fun main() = runBlocking{
        // 上游 FlowA：發射數據並拋出異常
        val upstreamA = flowA<Int> {
            emit(1)
            emit(2)
            throw RuntimeException("Upstream Error!")
        }

        // 使用 catchA 包裝上游 FlowA
        val downstreamA = upstreamA.catchA { e ->
            println("Caught exception: $e")
        }

        // 最終收集數據
        downstreamA.collect(object : FlowACollector<Int> {
            override suspend fun emit(value: Int) {
                println("Collected: $value")
            }
        })
    }

