package com.rengwuxian.coursecoroutines._4_flow

    // DogOwner：像 Flow<T> 一樣的擴展函數接收者
    class DogOwner(val name: String) {
        fun teachCommand(dog: Dog, command: String) {
            println("$name teaches ${dog.name} to $command")
        }
    }

    // Dog：像 FlowCollector<T> 一樣作為參數
    class Dog(val name: String)

    // trainDog 是 DogOwner 的擴展函數
    fun DogOwner.trainDog(dog: Dog, commands: List<String>) {
        println("[trainDog] this is DogOwner: $name")
        for (command in commands) {
            teachCommand(dog, command) // 調用 DogOwner 的方法
        }
    }

    // simulateTraining 模擬 flow { ... }
    fun simulateTraining(block: Dog.() -> Unit) {
        val dog = Dog("Buddy") // 創建一個 Dog 對象
        println("[simulateTraining] this is Dog: ${dog.name}")
        dog.block() // 執行傳入的 block，在 Dog 的上下文中執行
    }

    fun main() {
        val dogOwner = DogOwner("Alice")

        // 模擬類似於 flow { ... }
        simulateTraining {
            println("[Dog block] this is Dog: $name")

            // 調用 DogOwner 的擴展函數 trainDog
            dogOwner.trainDog(this, listOf("sit", "stay", "roll over"))
        }
    }

