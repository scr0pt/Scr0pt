/*
package utils.curl

class LongCurl {
    val connection = LongConnection()
    val response = LongResponse()

    fun execute(): Boolean {
        try {
            val response = connection.jsoup.execute()
            parseResponse(response)
        } catch (e: Exception) {
            return false
        }
        return true
    }

    private fun parseResponse(response: org.jsoup.Connection.Response) {
        this.response.url = response.url().toString()
        response.headers().forEach { name, value ->
            this.response.mHeaders.put(name, value)
        }
        response.cookies().forEach { name, value ->
            this.response.mCookies.put(name, value)
        }
    }
}*/
