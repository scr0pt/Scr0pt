package utils.localstorage

fun main() {
    val localStorage = LocalStorage("filename.txt")
    localStorage.setItem("key1","hello world")
    println(localStorage.getItem("key1"))
}