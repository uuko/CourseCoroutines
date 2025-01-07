package com.rengwuxian.coursecoroutines._4_flow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.chunked
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.EmptyCoroutineContext

@OptIn(ExperimentalCoroutinesApi::class)
fun main() = runBlocking<Unit> {
  val scope = CoroutineScope(EmptyCoroutineContext)
  val flow1 = flowOf(1, 2, 3, 4, 5)
  scope.launch {
    flow1.chunked(2).collect { println("chunked: $it") }
  }
  delay(10000)
}
//分塊用 也是flow操作符
//
// @ExperimentalCoroutinesApi
//public fun <T> Flow<T>.chunked(size: Int): Flow<List<T>> {
//    require(size >= 1) { "Expected positive chunk size, but got $size" }
//    return flow {
//        var result: ArrayList<T>? = null // Do not preallocate anything
//        collect { value ->
//            // Allocate if needed
//            val acc = result ?: ArrayList<T>(size).also { result = it }
//            acc.add(value)
//            if (acc.size == size) {
//                emit(acc)
//                // Cleanup, but don't allocate -- it might've been the case this is the last element
//                result = null
//            }
//        }
//        result?.let { emit(it) }
//    }
//}