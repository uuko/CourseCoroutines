package com.rengwuxian.coursecoroutines._4_flow




class FlowCollectorA<T> {
        fun emit(value: T) {
            println("Emitting value: $value")
        }
    }
    fun <T> customFlow(block: FlowCollectorA<T>.() -> Unit): FlowCollectorA<T> {
        val collector = FlowCollectorA<T>() // 創建一個 FlowCollector
        collector.block() // 執行傳入的 block
        return collector
    }

//    fun main() {
//        customFlow<Int> (block = {}) // 明確指定 `block` 執行邏輯
//
//        customFlow<Int> (block = blockImplementation()) // 明確指定 `block` 執行邏輯
//    }
    fun <T> blockImplementation(): FlowCollectorA<T>.() -> Unit = {
        emit(10 as T)
        emit(20 as T)
    }

    class Tag(val name: String) {
        private val children = mutableListOf<Tag>()
        fun add(tag: Tag) {
            children.add(tag)
        }
        fun render(): String {
            return "<$name>${children.joinToString("") { it.render() }}</$name>"
        }
    }

    // 擴展函數 + 帶接收者的 Lambda
    fun Tag.div(block: Tag.() -> Unit) {
        val div = Tag("div")
        div.block() // 在 div 的上下文中執行 block
        add(div) // 將 div 添加為當前 Tag 的子節點
    }

    fun Tag.span(block: Tag.() -> Unit) {
        val span = Tag("span")
        span.block()
        add(span)
    }
    fun Tag.test(block: () -> Unit) {
        block()  // 單純執行 block，不切換上下文
    }

    // 使用 DSL 構建 HTML
    fun main() {
        val html = Tag("html").apply {
            div {
               println( render())
//                span {
//                    add(Tag("b")) // 添加一個 <b> 節點
//                }
            }

//            this.div(fun Tag.() { // 明確指定接收者型別
//
//            })
        }

//        println(html.render())
    }


//public fun <T> flow(@BuilderInference block: suspend FlowCollector<T>.() -> Unit): Flow<T> = SafeFlow(block)
//private class SafeFlow<T>(private val block: suspend FlowCollector<T>.() -> Unit) : AbstractFlow<T>() {
//    override suspend fun collectSafely(collector: FlowCollector<T>) {
//        collector.block()
//    }
//}
//public abstract class AbstractFlow<T> : Flow<T>, CancellableFlow<T> {
//
//    public final override suspend fun collect(collector: FlowCollector<T>) {
//        val safeCollector = SafeCollector(collector, coroutineContext)
//        try {
//            collectSafely(safeCollector)
//        } finally {
//            safeCollector.releaseIntercepted()
//        }
//    }
//public fun interface FlowCollector<in T> {
//
//    /**
//     * Collects the value emitted by the upstream.
//     * This method is not thread-safe and should not be invoked concurrently.
//     */
//    public suspend fun emit(value: T)
//}
//flow{} =>這括號等於block然後是FlowCollector的擴展函數
//  safeflow 的上層flow 在collcet的時候會創FlowCollector的實例  所以collect的時候才會創FlowCollector 就會呼叫block 等於說就會呼叫{} ?
//val myFlow = flow {
//    emit(1)
//    emit(2)
//}
// === val myFlow = SafeFlow(block = { emit(1); emit(2) })
//flow 函數 的參數是一個 suspend FlowCollector<T>.() -> Unit，也就是接收者為 FlowCollector 的掛起函數。
//這裡的 { emit(1); emit(2) } 被作為 block 傳遞給 SafeFlow。
//本質上，這只是構造了一個 Flow 實例，並不會執行 block。
//public final override suspend fun collect(collector: FlowCollector<T>) {
//    val safeCollector = SafeCollector(collector, coroutineContext)
//    try {
//        collectSafely(safeCollector)
//    } finally {
//        safeCollector.releaseIntercepted()
//    }
//}
//collector 是用來收集數據的實例，通常由下游的 collect {} block 提供。
//SafeCollector 是一個包裝器，它保證 FlowCollector 的操作是安全的（例如處理上下文切換等）。
//collectSafely 是一個抽象方法，由子類（SafeFlow）實現。
//這裡的 collector.block() 是關鍵，它將之前 flow {} 傳入的 block 以 collector 作為接收者執行。
//也就是說，block 中的所有操作（如 emit）實際上是 collector.emit 的調用。

//擴展函數不能覆蓋原方法
// 擴展函數只能訪問 interface 的公開成員
//當無法修改 interface 的原始代碼時，可以通過擴展函數增加功能。例如：


//是的，Kotlin 確實會按照作用域逐層查找符號（包括 this 的對象）。


//public fun <T> Flow<T>.catch(action: suspend FlowCollector<T>.(cause: Throwable) -> Unit): Flow<T> =
//    flow {
//這裏面試collector所以根本不能調用catchImpl
          //catchImpltchImpl 是 Flow<T> 的擴展函數，因此它的 this 永遠指向被擴展的 Flow<T>
         //flow {} 創建了一個新的 Flow，並且它的執行上下文是 FlowCollector。
//當在 flow {} 中調用 catchImpl(this) 時，這裡的 this 是指向內層的 FlowCollector，作為參數傳遞。
//        val exception = catchImpl(this)
//        if (exception != null) action(exception)
//    }
//internal suspend fun <T> Flow<T>.catchImpl(
//    collector: FlowCollector<T>
//): Throwable? {
//    var fromDownstream: Throwable? = null
//    try {
        //所以這裏就先collect上層的了
//        collect {
//            try {
//這collector是下游的 這是把上下游串接
//                collector.emit(it)
//            } catch (e: Throwable) {
//                fromDownstream = e // 記錄下游的異常
//                throw e // 重新拋出，告知上游有異常
//            }
//        }
//    } catch (e: Throwable) {
//        // Otherwise, smartcast is impossible
//        val fromDownstream = fromDownstream
//        /*
//         * First check ensures that we catch an original exception, not one rethrown by an operator.
//         * Seconds check ignores cancellation causes, they cannot be caught.
//         */
//      // 如果是下游異常或取消，直接重新拋出
//        if (e.isSameExceptionAs(fromDownstream) || e.isCancellationCause(coroutineContext)) {
//            throw e // Rethrow exceptions from downstream and cancellation causes
//        } else {
//            /*
//             * The exception came from the upstream [semi-] independently.
//             * For pure failures, when the downstream functions normally, we handle the exception as intended.
//             * But if the downstream has failed prior to or concurrently
//             * with the upstream, we forcefully rethrow it, preserving the contextual information and ensuring  that it's not lost.
//             */

//我看懂了只有上游異常才會return 如果是取消的話上下游都會有 這時候判斷是不是取消就throw跟只有下游也是throw
//            if (fromDownstream == null) {
//                return e // 如果只有上游異常，返回異常給上層處理
//            }
//            /*
//             * We consider the upstream exception as the superseding one when both upstream and downstream
//             * fail, suppressing the downstream exception, and operating similarly to `finally` block with
//             * the useful addition of adding the original downstream exception to suppressed ones.
//             *
//             * That's important for the following scenarios:
//             * ```
//             * flow {
//             *     val resource = ...
//             *     try {
//             *         ... emit as well ...
//             *     } finally {
//             *          resource.close() // Throws in the shutdown sequence when 'collect' already has thrown an exception
//             *     }
//             * }.catch { } // or retry
//             * .collect { ... }
//             * ```
//             * when *the downstream* throws.
//             */
//            if (e is CancellationException) {
//                fromDownstream.addSuppressed(e)// 下游異常優先，附加上游異常
//                throw fromDownstream //是上游取消也直接丟
//            } else {
//                e.addSuppressed(fromDownstream)// 上游異常優先，附加下游異常
//                throw e //這裏是下游就直接丟
//            }
//        }
//    }
//    return null
//}

// flow2.collect(object :FlowCollector<Int>{
//        override suspend fun emit(value: Int) {
//
//            //1.在括號在try catch 能夠catch住
//            val contributors = gitHub.contributors("square", "retrofit")
//            println("Contributors: $contributors")
//
//        }
//
//      })


