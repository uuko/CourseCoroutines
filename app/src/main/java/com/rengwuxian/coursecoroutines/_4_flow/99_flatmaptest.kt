package com.rengwuxian.coursecoroutines._4_flow

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val start = System.currentTimeMillis()
    flowOf("顧客A", "顧客B", "顧客C")
        .map { customer ->
            println("$customer 開始煮咖啡")
            delay(3000)  // 每次煮咖啡需要 3 秒
            "$customer 的咖啡完成"
        }
        .collect { println(" $it in ${System.currentTimeMillis() - start}") }

    flowOf("顧客A", "顧客B", "顧客C")
        .flatMapMerge { customer ->
            flow {
                println("$customer 開始煮咖啡")
                delay(3000)  // 煮咖啡需要 3 秒
                emit("$customer 的咖啡完成")
            }
        }
        .collect { println(" $it in ${System.currentTimeMillis() - start}") }

//    val customer1 = flow {
//        emit("顧客A 開始點單")
//        delay(1000)
//        emit("顧客A 煮咖啡")
//        delay(2000)
//        emit("顧客A 咖啡完成")
//    }
//
//    val customer2 = flow {
//        emit("顧客B 開始點單")
//        delay(1000)
//        emit("顧客B 煮咖啡")
//        delay(2000)
//        emit("顧客B 咖啡完成")
//    }
//
//    val customer3 = flow {
//        emit("顧客C 開始點單")
//        delay(1000)
//        emit("顧客C 煮咖啡")
//        delay(2000)
//        emit("顧客C 咖啡完成")
//    }
//    flowOf(customer1, customer2, customer3)
//        .collect { println(it) }
//    flowOf("顧客A", "顧客B", "顧客C")
//        .flatMapConcat { name ->
//            flow {
//                emit("$name 開始點單")
//                delay(1000)
//                emit("$name 咖啡完成")
//            }
//        }
    //flowOf
    //flatMapConcat 的主要作用是減少樣板程式碼，讓程式邏輯更簡潔、易讀，並保持相同的行為方式。

    //1. map – 一台咖啡機，顧客依序排隊
    //排隊規則：顧客依序點餐和煮咖啡，不能併行，前面的人咖啡煮好後，下一個人才開始煮。
    //特點：線性順序處理，煮咖啡需要等待上一位顧客的咖啡煮完。 總耗時：3 秒 × 3 位顧客 = 9 秒
//flatMapMerge – 三台自助咖啡機，顧客同時煮咖啡
    //flatMapMerge – 三台自助咖啡機，顧客同時煮咖啡
    //規則：三台咖啡機可以同時煮咖啡，所有顧客不需要等待前一位煮完再開始。
    //特點：非阻塞併行處理，多台設備同時進行，顧客獨立排隊，彼此不干擾。
    //總耗時：3 秒 （併行處理，所有顧客同時完成）
    //map是逐個下載 flatmap是多個下載
    //flatMapMerge 的價值在於內部處理是「耗時操作」時，能併行處理，顯著縮短總處理時間。
}