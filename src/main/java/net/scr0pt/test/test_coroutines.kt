package net.scr0pt.test

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * Created by Long
 * Date: 10/15/2019
 * Time: 8:36 PM
 */

suspend fun main() {
    val job = GlobalScope.launch {
        callSum()
    }
    job.join()
}

suspend fun callSum(){
    println("Call sum")
    sum()
}

suspend fun sum(){
    println("Sum")
    mer()
}
suspend fun mer(){
    println("Mer")
}