package utils.curl

import com.google.gson.annotations.Expose
import utils.curl.adapter.toJsoupConnection
import utils.curl.adapter.toLongResponse


class LongConnection {
    @Expose
    var cookies = arrayListOf<Data>()
        private set
    @Expose
    var headers = arrayListOf<Data>()
        private set
    @Expose
    var data = arrayListOf<Data>()
        private set
    var storeCookies = true//is save cookie from response to next header request
    @Expose
    var url: String? = null
        set(value) {
            value?.let {
                field = value
                jsoup.url(value)
            }
        }

    val jsoup: org.jsoup.Connection =
        org.jsoup.Jsoup.connect("http://127.0.0.1").ignoreContentType(true).ignoreHttpErrors(true).followRedirects(true)

    fun headers(headers: HashMap<String, String>) {
        headers.forEach { (key, value) ->
            this.header(key, value)
        }
    }

    fun header(name: String, value: String, isHard: Boolean = false, method: LongConnection.REQUEST_METHOD? = null) {
        headers.removeIf { !it.isHard && it.name == name }
        if (headers.none { !it.isHard && it.name == name }) {
            headers.add(Data(name, value, isHard, method))
        }
    }

    fun data(name: String, value: String, isHard: Boolean = false, method: LongConnection.REQUEST_METHOD? = null) {
        data.removeIf { !it.isHard && it.name == name }
        if (data.none { !it.isHard && it.name == name }) {
            data.add(Data(name, value, isHard, method))
        }
    }

    fun referrer(value: String, isHard: Boolean = false) {
        header("Referrer", value, isHard, null)
    }

    fun userAgent(value: String, isHard: Boolean = false) {
        header("User-Agent", value, isHard, null)
    }

    fun cookie(name: String, value: String, isHard: Boolean = false): LongConnection {
        cookies.removeIf { !it.isHard && it.name == name }
        if (cookies.none { !it.isHard && it.name == name }) {
            cookies.add(Data(name, value, isHard, null))
        }
        return this
    }

    fun cookies(cookies: HashMap<String, String>) {
        cookies.forEach { (key, value) ->
            this.cookie(key, value)
        }
    }

    fun execute(
        url: String? = null,
        method: REQUEST_METHOD = REQUEST_METHOD.GET,
        data: ArrayList<Data>? = null,
        headers: ArrayList<Data>? = null
    ): LongResponse? {
        return try {
            url?.let { this.url = url }

            val conn = this.toJsoupConnection(method, data = data, headers = headers).method(method.toJsoup())

//            println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~")
//            conn.request().data()?.forEach {
//                println(it.key() +": "+ it.value())
//            }
//            println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~")

            val longResponse = conn.execute().toLongResponse()
            changeResponseBodyFunc?.let {
                longResponse.body = it(longResponse.body)
            }
            if (storeCookies) {
                cookies.addAll(longResponse.mCookies)
            }
            longResponse
        } catch (e: Exception) {
            null
        }
    }

    fun get(url: String? = null, headers: HashMap<String, String>? = null): LongResponse? = execute(url, REQUEST_METHOD.GET, headers = headers?.toLongConnectionData())
    fun post(
        url: String? = null,
        data: HashMap<String, String>? = null,
        headers: HashMap<String, String>? = null
    ): LongResponse? =
        execute(
            url,
            REQUEST_METHOD.POST,
            data = data?.toLongConnectionData(),
            headers = headers?.toLongConnectionData()
        )

    fun put(url: String? = null, data: HashMap<String, String>? = null): LongResponse? =
        execute(url, REQUEST_METHOD.PUT, data = data?.toLongConnectionData())

    fun delete(url: String? = null): LongResponse? = execute(url, REQUEST_METHOD.DELETE)

    enum class REQUEST_METHOD {
        POST, GET, PUT, DELETE
    }

    fun REQUEST_METHOD.toJsoup(): org.jsoup.Connection.Method = when (this) {
        REQUEST_METHOD.POST -> org.jsoup.Connection.Method.POST
        REQUEST_METHOD.GET -> org.jsoup.Connection.Method.GET
        REQUEST_METHOD.PUT -> org.jsoup.Connection.Method.PUT
        REQUEST_METHOD.DELETE -> org.jsoup.Connection.Method.DELETE
    }

    var changeResponseBodyFunc: ((String?) -> String?)? = null

    data class Data(
        var name: String,
        var value: String,
        var isHard: Boolean = false,
        var method: LongConnection.REQUEST_METHOD? = null,
        var once: Boolean = false
    )

    fun Data.equals(other: Any?): Boolean = when (other) {
        is Data -> this.name == other.name
        else -> false
    }


    fun ArrayList<Data>.addAll(list: ArrayList<Data>?) {
        list ?: return
        this.removeIf { a1 -> list.none { it == a1 } }
        this.addAll(list)
    }

    fun HashMap<String, String>.toLongConnectionData(): ArrayList<Data> {
        val listData = arrayListOf<Data>()
        this.forEach { (key, value) ->
            listData.add(Data(key, value))
        }
        return listData
    }
}