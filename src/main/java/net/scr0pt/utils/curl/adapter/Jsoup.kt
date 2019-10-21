package net.scr0pt.utils.curl.adapter

import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import net.scr0pt.utils.curl.LongConnection
import net.scr0pt.utils.curl.LongResponse

fun LongConnection.toJsoupConnection(
        method: LongConnection.REQUEST_METHOD? = null,
        data: ArrayList<LongConnection.Data>?,
        headers: ArrayList<LongConnection.Data>?
    ): org.jsoup.Connection {

    val conn = Jsoup.connect(url).method(method?.toJsoup() ?: Connection.Method.GET).ignoreContentType(true)
        .ignoreHttpErrors(true).followRedirects(true)

    val removeHeaders = arrayListOf<LongConnection.Data>()
    this.headers
        .filter { it.method == null || method == it.method }
        .forEach {
            conn.header(it.name, it.value)
            if (it.once) {
                removeHeaders.add(it.copy())
            }
        }
    if (removeHeaders.isNotEmpty()) {
        this.headers.removeIf { removeHeaders.contains(it) }
    }

    cookies.forEach { (name, value) ->
        conn.cookie(name, value)
    }

    this.data
        .filter { it.method == null || method == it.method }
        .forEach { (name, value) ->
            if (name != null && name.isNotEmpty()) {
                conn.data(name, value)
            }
        }

    if (data != null && data.isNotEmpty()) {
        data.forEach { (name, value) ->
            if (name != null && name.isNotEmpty()) {
                conn.data(name, value)
            }
        }
    }

    if (headers != null && headers.isNotEmpty()) {
        headers.forEach { (name, value) ->
            if (name != null && name.isNotEmpty()) {
                conn.header(name, value)
            }
        }
    }

    return conn
}

fun org.jsoup.Connection.Response.toLongResponse(): LongResponse {
    val longResponse = LongResponse()
    longResponse.url = this.url().toString()
    this.headers().forEach { name, value ->
        longResponse.mHeaders.add(LongConnection.Data(name, value))
    }
    this.cookies().forEach { name, value ->
        longResponse.mCookies.add(LongConnection.Data(name, value))
    }
    longResponse.body = this.body()
    longResponse.doc = this.parse()
    longResponse.statusCode = this.statusCode()
    longResponse.statusMessage = this.statusMessage()
    return longResponse
}

fun Element.selectLast(selector: String): Element? {
    return this.select(selector)?.lastOrNull()
}