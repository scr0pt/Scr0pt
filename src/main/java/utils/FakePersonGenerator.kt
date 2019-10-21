package utils

import utils.curl.LongConnection

fun main() {
    FakePersonGenerator.generate()
}

object FakePersonGenerator {
    fun generate(){
        val response = LongConnection().get("https://www.fakepersongenerator.com/")
        val doc = response?.doc ?: return
        val image = doc.selectFirst(".basic-face .face img")?.attr("src")?.run  {
            "https://www.fakepersongenerator.com$this"
        }
        val name = doc.selectFirst(".basic-face .name")?.text()
    }
}