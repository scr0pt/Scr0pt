package net.scr0pt

fun main() {
    println(System.getProperty("sun.arch.data.model"))
}

object OSUtils {
    val OSName = System.getProperty("os.name")
    val OSModel = System.getProperty("sun.arch.data.model")

    fun isWindows() = OSName.startsWith("Windows")
    fun isLinux() = OSName.startsWith("Linux")
    fun isMac() = OSName.startsWith("Mac")
}