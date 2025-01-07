package com.rengwuxian.coursecoroutines._4_flow

import kotlinx.coroutines.runBlocking

fun main() = runBlocking<Unit> {
  // StateFlow //狀態訂閱 持續性 有歷史記錄
//  SharedFlow // event flow 事件訂閱 一次性操作 不具備歷史紀錄
//  Flow // data flow 數據流
//  Channel 協程間協作工具 多條數據 async是單條 然後如果不需要跨線程不需要用
//  async {  }
}